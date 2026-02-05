package haxidenti.kotman.util

import haxidenti.kotman.Constants
import haxidenti.kotman.Constants.GENERATOR_EXTENSIONS
import haxidenti.kotman.Constants.MODULE_COMMON
import haxidenti.kotman.dto.ProjectDetails
import java.io.File
import java.nio.file.Files
import kotlin.io.path.createParentDirectories

internal object ProjectUtil {
    fun File.addFile(name: String, content: String) {
        val path = File(this, name).toPath().createParentDirectories()
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

    fun generateSubproject(dir: File, moduleName: String, details: ProjectDetails) {
        dir.mkdirs()

        val packagePath = details.packageName.replace(".", "/")

        dir.mkdirMust("src/main/java/$packagePath")
        dir.mkdirMust("src/main/kotlin/$packagePath")
        dir.mkdirMust("src/main/resources/$packagePath")
        dir.mkdirMust("src/test/java/$packagePath")
        dir.mkdirMust("src/test/kotlin/$packagePath")
        dir.mkdirMust("src/test/resources/$packagePath")

        if (moduleName != MODULE_COMMON) {
            dir.addFile(
                "src/main/kotlin/$packagePath/Main.kt", """
                package ${details.packageName}
    
                fun main() {
                    commonHello()
                }
            """.trimIndent()
            )
        } else {
            dir.addFile("src/main/kotlin/$packagePath/Common.kt", """
                package ${details.packageName}
                
                fun commonHello() = println("Hello, Kotlin");
            """.trimIndent())
        }

        dir.addFile("build.gradle.kts", GradleUtil.generateSubprojectBuildGradle(moduleName))
    }

    fun generate(dir: File, details: ProjectDetails) {
        dir.mkdirs()

        dir.addFile("build.gradle.kts", GradleUtil.generateBuildGradle(details))
        dir.addFile("settings.gradle.kts", GradleUtil.generateGradleSettings(details.projectName))
        dir.addFile("gradle/wrapper/gradle-wrapper.properties", GradleUtil.generateGradleWrapperConfig())

        for (moduleName in Constants.MODULES) {
            generateSubproject(
                dir.mkdirMust(moduleName),
                moduleName,
                details
            )
        }

        // Adding simple README
        dir.addFile("README.md", "# ${details.projectName}\n")

        // Add .gitignore for ignoring unnecessary files
        dir.addFile(".gitignore", GitUtil.gitIgnore(details.projectName))

        // Add .gitkeep files for empty directories
        GitUtil.addGitKeepFiles(dir)

        // Init Git here
        GitUtil.init(dir)
    }

    fun runGenerator(dir: File) {
        if (!dir.isDirectory) throw IllegalStateException("Can't find src folder")
        val megabyte = 1024 * 1024
        for (file in dir.walkTopDown()) {
            if (!file.isFile) continue
            if (file.length() > megabyte) continue
            if (file.extension !in GENERATOR_EXTENSIONS) continue
            val commands = file.readLines()
                .map { it.trim() }
                .filter { it.startsWith("// generate:") }
                .map { it.substring(12).trim() }
                .filter { it.isNotBlank() }
            val parent = file.parentFile
            for (command in commands) {
                val ok = Sys.runCommand(parent, command)
                if (!ok) {
                    throw IllegalStateException("Can't run command \"$command\" :: Something is wrong. File ${file.name}")
                }
            }
        }
    }
}