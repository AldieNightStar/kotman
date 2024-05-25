package haxidenti.kotman.util

import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

fun createZip(currentFolder: File, files: List<File>, zipFile: File) {
    ZipOutputStream(zipFile.outputStream()).use { zos ->
        files.flatMap {
            if (it.isDirectory) {
                it.walkTopDown().filter { it.isFile }.toList()
            } else {
                listOf(it)
            }
        }.forEach { file ->
            val relative = file.relativeTo(currentFolder).path
            zos.putNextEntry(ZipEntry(relative))
            file.inputStream().copyTo(zos)
            zos.closeEntry()
        }
    }
}
