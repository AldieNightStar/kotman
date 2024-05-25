package haxidenti.kotman

import java.io.File

object Sys {
    fun runShell(workingFolder: File, arguments: List<String>): Boolean {
        val command = if (isWindows())
            listOf("cmd", "/c") + arguments
        else
            listOf("bash", "-c") + arguments

        try {
            val process = ProcessBuilder(command)
                .directory(workingFolder)
                .inheritIO()
                .start()
            val result = process.waitFor()
            return result == 0
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }


    fun isWindows() = System
        .getProperty("os.name")
        .lowercase()
        .contains("windows")

    fun runCommand(workingFolder: File, line: String): Boolean {
        val regex = Regex("(\"[^\"]*\"|[^\\s]+)")
        val args = regex.findAll(line)
            .map { it.value.trim() }
            .toList()
        return Sys.runShell(workingFolder, args)
    }
}