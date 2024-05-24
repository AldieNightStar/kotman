package haxidenti.kotman

import com.google.gson.Gson
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
        val home = FileSystemView.getFileSystemView().homeDirectory
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

data class UserConfiguration(
    var author: String = "HaxiDenti",
    var kotlinVer: String = "1.9.20",
    var projectVersion: String = "1.0.0"
)
