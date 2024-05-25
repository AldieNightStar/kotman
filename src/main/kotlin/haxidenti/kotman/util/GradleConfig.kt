package haxidenti.kotman.util

import java.io.File

@JvmInline
value class GradleConfig(val gradleFile: File) {
    fun readValues(): MutableMap<String, String> {
        val map = mutableMapOf<String, String>()
        val src = gradleFile.readText()
        for (line in src.lines()) {
            val trimmedLine = line.trim()
            if (!trimmedLine.startsWith("val ") && !trimmedLine.startsWith("var ")) continue
            val name = trimmedLine.substring(4).substringBefore(" ")
            val value = trimmedLine.substringAfter("\"").substringBefore("\"")
            map[name] = value
        }
        return map
    }

    fun writeValues(values: Map<String, String>) {
        val src = gradleFile.readText()
        val newLines = mutableListOf<String>()
        for (line in src.lines()) {
            val trimmedLine = line.trim()

            // Skip lines that is not starting with val/var
            val valVarName = getValVarPrefix(trimmedLine)
            if (valVarName == null) {
                newLines.add(line)
                continue
            }

            // Parsing name and change the value
            val name = trimmedLine.substring(4).substringBefore(" ")

            // If name is present in map then we change that line
            if (values.containsKey(name)) {
                val value = values[name]
                newLines.add("$valVarName $name = \"$value\"")
                continue
            } else {
                // If name is not present then this line is just added to others
                newLines.add(line)
            }
        }

        gradleFile.writeText(newLines.joinToString("\n"))
    }

    private fun getValVarPrefix(line: String): String? {
        return if (line.startsWith("val "))
            "val"
        else if (line.startsWith("var "))
            "var"
        else
            null
    }
}