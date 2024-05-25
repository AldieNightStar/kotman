package haxidenti.kotman.util

import haxidenti.kotman.dto.DependencyInfo

object DependencyUtil {
    private val LIST = listOf(
        "implementation(kotlin(",
        "testCompileClasspath(",
        "testImplementation(",
        "compileClasspath(",
        "testRuntimeOnly(",
        "testCompileOnly(",
        "implementation(",
        "compile(",
    )

    fun readDependencyString(line: String): DependencyInfo? {
        for (prefix in LIST) {
            val result = readStartingWith(line, prefix)
            if (result != null) {
                val depString = result.substringAfter("\"").substringBefore("\"")
                val brackets = prefix.count { it == '(' }
                val suffix = ")".repeat(brackets)
                return DependencyInfo(prefix, depString, suffix)
            }
        }
        return null
    }

    private fun readStartingWith(line: String, prefix: String): String? =
        if (line.startsWith(prefix)) line.substring(prefix.length) else null
}

