package haxidenti.kotman.util

import haxidenti.kotman.dto.DependencyInfo

object DependencyUtil {

    fun readDependencyString(line: String): DependencyInfo? {
        if (!line.contains("(") || !line.contains(")")) return null
        val depString = line.substringAfter("\"").substringBefore("\"")
        return DependencyInfo(depString, line.substringBefore("\"").lowercase().contains("test"))
    }

    private fun readStartingWith(line: String, prefix: String): String? =
        if (line.startsWith(prefix)) line.substring(prefix.length) else null
}

