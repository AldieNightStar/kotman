package haxidenti.kotman.util

import haxidenti.kotman.util.ProjectUtil.addFile
import java.io.File

object GitUtil {
    fun addGitKeepFiles(dir: File) {
        if (!dir.isDirectory) return
        dir.walkTopDown()
            .filter { it.isDirectory }
            .filter { it.listFiles()?.isEmpty() ?: false }
            .forEach {
                it.addFile(".gitkeep", "Keep me if there no other files <3")
            }
    }

    fun gitIgnore(projectName: String) = """
        /.idea
        /.gradle
        /.vscode
        /build
        /$projectName
        /$projectName.jar
    """.trimIndent()

    fun init(dir: File) {
        Sys.runShell(dir, listOf("git", "init", "--quiet"))
        Sys.runShell(dir, listOf("git", "add", "."))
        Sys.runShell(dir, listOf("git", "commit", "-m", "Init"))
    }
}