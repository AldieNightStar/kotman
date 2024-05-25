package haxidenti.kotman

import com.google.gson.Gson
import haxidenti.kotman.dto.UserConfiguration
import java.io.Closeable
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.nio.file.FileSystem
import java.nio.file.Files
import javax.swing.filechooser.FileSystemView

private val gson: Gson = Gson()

class Configuration : Closeable {
    private val configFile: File
    val userConfig: UserConfiguration

    constructor() {
        val home = if (Sys.isWindows()) System.getenv("USERPROFILE") else System.getenv("HOME")
        configFile = File(home, "kotman.json")

        if (configFile.isFile) {
            userConfig = gson.fromJson(FileReader(configFile), UserConfiguration::class.java)
        } else {
            userConfig = UserConfiguration()
        }
    }

    override fun close() {
        val out = FileWriter(configFile)
        gson.toJson(userConfig, out)
        out.close()
    }
}


