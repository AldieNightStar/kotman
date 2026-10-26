package haxidenti.kotman.util

import haxidenti.kotman.APP_MOD_NAME
import haxidenti.kotman.LIB_MOD_NAME
import java.io.File

object GitUtil {
    fun addGitIgnore(gitIgnoreFile: File, newLines: Array<String>) {
        if (gitIgnoreFile.exists()) {
            gitIgnoreFile.appendText("\n" + newLines.joinToString("\n"))
        } else {
            gitIgnoreFile.writeText(newLines.joinToString("\n"))
        }
    }

    fun setupGitIgnore(workdir: File, isApp: Boolean) {
        val modName = if (isApp) APP_MOD_NAME else LIB_MOD_NAME
        addGitIgnore(workdir.resolve(".gitignore"), arrayOf(
            ".idea/",
            "$modName/build/"
        ))
    }
}