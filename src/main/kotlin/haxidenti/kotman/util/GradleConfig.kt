package haxidenti.kotman.util

import haxidenti.kotman.dto.DependencyInfo
import haxidenti.kotman.util.DependencyUtil.readDependencyString
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

    // This method WILL NOT add something new but will change the settings (No values will not be added)
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

    fun readDependencies(): List<DependencyInfo> {
        val src = gradleFile.readText()
        var foundDependencies = false
        val list = mutableListOf<DependencyInfo>()
        for (line in src.lines()) {
            val trimmedLine = line.trim()
            if (foundDependencies) {

                // Stop reading dependencies if seeing this line
                if (trimmedLine == "}") break

                val dep = readDependencyString(trimmedLine)
                if (dep != null) {
                    list.add(dep)
                }

            } else {
                if (trimmedLine.startsWith("dependencies {")) {
                    foundDependencies = true
                }
            }
        }

        return list
    }

    fun addDependencies(list: List<String>) {
        val src = gradleFile.readText()
        var foundDependencies = false
        var newLines = mutableListOf<String>()

        for (line in src.lines()) {
            val trimmedLine = line.trim()
            if (foundDependencies) {

                // When this is end of dependencies
                // Here we are going to add new dependencies
                if (trimmedLine == "}") {

                    // Add all dependencies
                    list
                        .map { "    implementation(\"$it\")" }
                        .forEach { newLines.add(it) }

                    // Add ending line
                    newLines.add(line)

                    // Set dependencies as not found to continue to add all lines
                    foundDependencies = false
                    continue
                }

                // When dependencies is not ended yet
                newLines.add(line)

            } else {
                // When found dependencies section
                if (trimmedLine == "dependencies {") {
                    foundDependencies = true
                }
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