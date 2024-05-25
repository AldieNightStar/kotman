package haxidenti.kotman.util

import haxidenti.kotman.util.GradleUtil.readVals
import java.io.File
import java.nio.file.Files

internal object ProjectUtil {
    fun readProjectValues(projectFolder: File): Map<String, String> {
        if (!Gradle.currentFolderHasGradle(projectFolder)) {
            throw IllegalStateException("This is not a Gradle project or gradle wrapper is not initialized yet")
        }
        return readVals(projectFolder.gradleFileSrc())
    }

    fun generateScript(jarName: String, mainClassName: String) = """ 
            #!/bin/bash
            SCRIPTPATH="${'$'}( cd -- "${'$'}(dirname "${'$'}0")" >/dev/null 2>&1 ; pwd -P )"
            java -cp "${'$'}SCRIPTPATH/$jarName" $mainClassName "${'$'}@" 
        """.trimIndent()

    fun File.addFile(name: String, content: String) {
        val path = File(this, name).toPath()
        val bytes = content.toByteArray()
        try {
            Files.write(path, bytes)
        } catch (e: Exception) {
            throw IllegalStateException("Can't create file $name", e)
        }
    }

    fun File.mkdirMust(path: String): File {
        val dir = File(this, path)
        if (!dir.mkdirs()) {
            throw IllegalStateException("Can't create folder $path")
        }
        return dir.canonicalFile
    }

    fun File.gradleFileSrc(): String {
        val gradle = File(this, "build.gradle.kts")
        if (!gradle.isFile) throw IllegalStateException("$this has no build.gradle.kts file")
        return gradle.readText()
    }

    fun gitIgnore() = """
        /.idea/
        /.gradle/
    """.trimIndent()
}