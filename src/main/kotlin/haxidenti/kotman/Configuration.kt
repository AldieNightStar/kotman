package haxidenti.kotman

import com.google.gson.Gson
import haxidenti.kotman.dto.UserConfiguration
import java.io.Closeable
import java.io.File
import java.io.FileReader
import java.io.FileWriter

private val gson: Gson = Gson()

class Configuration : Closeable {
    private val configFile: File
    lateinit var userConfig: UserConfiguration

    val home by lazy {
        if (Sys.isWindows()) System.getenv("USERPROFILE") else System.getenv("HOME")
    }

    constructor() {
        configFile = File(home, "kotman.json")

        if (configFile.isFile) {
            userConfig = gson.fromJson(FileReader(configFile), UserConfiguration::class.java)
        } else {
            cleanConfig()
        }
    }

    override fun close() {
        val out = FileWriter(configFile)
        gson.toJson(userConfig, out)
        out.close()
    }

    fun reset() {
        cleanConfig()
        configFile.delete()
    }

    private fun cleanConfig() {
        userConfig = UserConfiguration()
    }
}


