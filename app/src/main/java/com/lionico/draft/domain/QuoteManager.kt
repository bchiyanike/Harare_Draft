// File: app/src/main/java/com/lionico/draft/domain/QuoteManager.kt
package com.lionico.draft.domain

import android.content.Context
import com.lionico.draft.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

enum class QuoteType { GENERAL, WIN, LOSE, TAUNT }

@Singleton
class QuoteManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private data class QuoteEntry(val quote: String, val author: String)

    private val fallbackQuote: String = context.getString(R.string.quote_fallback)

    private val allQuotes: Map<QuoteType, List<QuoteEntry>> = loadQuotes()

    private val _currentQuote = MutableStateFlow(fallbackQuote)
    val currentQuote: StateFlow<String> = _currentQuote.asStateFlow()

    private var currentType: QuoteType = QuoteType.GENERAL
    private var timerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        startTimer()
        pickRandomQuote()
    }

    fun setType(type: QuoteType) {
        if (currentType != type) {
            currentType = type
            cancelTimer()
            pickRandomQuote()
            startTimer()
        }
    }

    private fun startTimer() {
        cancelTimer()
        timerJob = scope.launch {
            while (isActive) {
                delay(60_000L)
                pickRandomQuote()
            }
        }
    }

    private fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun pickRandomQuote() {
        val list = allQuotes[currentType] ?: allQuotes[QuoteType.GENERAL] ?: return
        if (list.isEmpty()) {
            _currentQuote.value = fallbackQuote
            return
        }
        val entry = list[Random.nextInt(list.size)]
        _currentQuote.value = "\"${entry.quote}\" — ${entry.author}"
    }

    private fun loadQuotes(): Map<QuoteType, List<QuoteEntry>> {
        return try {
            val jsonStr = context.assets.open("quotes.json")
                .bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonStr)
            mapOf(
                QuoteType.GENERAL to parseCategory(jsonObject, "general"),
                QuoteType.WIN     to parseCategory(jsonObject, "win"),
                QuoteType.LOSE    to parseCategory(jsonObject, "lose"),
                QuoteType.TAUNT   to parseCategory(jsonObject, "taunt")
            )
        } catch (e: Exception) {
            mapOf(
                QuoteType.GENERAL to listOf(parseFallbackEntry())
            )
        }
    }

    private fun parseCategory(jsonObject: JSONObject, key: String): List<QuoteEntry> {
        val array = jsonObject.getJSONArray(key)
        val list = mutableListOf<QuoteEntry>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(QuoteEntry(obj.getString("quote"), obj.getString("author")))
        }
        return list
    }

    private fun parseFallbackEntry(): QuoteEntry {
        val quoteStart = fallbackQuote.indexOf('"') + 1
        val quoteEnd = fallbackQuote.lastIndexOf('"')
        val quote = if (quoteStart in 1..quoteEnd) {
            fallbackQuote.substring(quoteStart, quoteEnd)
        } else {
            fallbackQuote
        }
        val author = fallbackQuote.substringAfterLast("—").trim()
        return QuoteEntry(quote, author.ifEmpty { "Unknown" })
    }
}