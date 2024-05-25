package haxidenti.kotman.util

import java.io.File
import java.nio.file.Files

internal object ProjectUtil {
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

    fun File.gradleConfig(): GradleConfig {
        val gradle = File(this, "build.gradle.kts")
        if (!gradle.isFile) throw IllegalStateException("$this has no build.gradle.kts file")
        return GradleConfig(gradle)
    }

    fun gitIgnore(projectName: String) = """
        /.idea
        /.gradle
        /.vscode
        /build
        /$projectName
        /$projectName.jar
    """.trimIndent()

    fun packageFolder(projectFolder: File, packageName: String): File {
        val packagePath = packageName.replace(".", "/")
        val folder = projectFolder.resolve("src/main/kotlin/$packagePath").canonicalFile
        if (!folder.isDirectory) throw IllegalStateException("Package $packageName has no folder in the kotlin project")
        return folder
    }
}