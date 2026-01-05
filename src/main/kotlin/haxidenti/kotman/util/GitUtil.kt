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
                it.addFile(".gitkeep", "<3")
                println("KEEP in ${it.canonicalFile}")
            }
    }
}