fun readCrashLog(context: Context): String {
    val file = java.io.File(context.filesDir, "crash_log.txt")
    return if (file.exists()) file.readText() else "No crash log found."
}

@Composable
fun CrashLogScreen(context: Context) {
    val log = remember { readCrashLog(context) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = log,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp
        )
    }
}