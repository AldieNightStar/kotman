package haxidenti.kotman.util

import java.io.File

private const val MAVEN_FOLDER_REPO_PATH = ".m2/repository"

internal object MavenUtil {

    fun getMavenRepositoryDir(): File? {
        val home = System.getProperty("user.home") ?: return null
        return File(home).resolve(MAVEN_FOLDER_REPO_PATH).also {
            if (!it.exists()) {
                it.mkdirs()
            }
        }
    }
}