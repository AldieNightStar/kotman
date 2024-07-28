package haxidenti.kotman.command

import haxidenti.kotman.dto.UserConfiguration

internal object ConfigCommand {
    fun dispatchConfig(c: UserConfiguration, arguments: List<String>) {
        if (arguments.size < 2) {
            println("author: ${c.author}")
            println("version: ${c.projectVersion}")
            println("kotlinver: ${c.kotlinVer}")
            println("corover: ${c.coroutineVer}")
            println("""
                
                Usage:
                    kotman config                - Show values
                    kotman config [name] [value] - Set new value
                
            """.trimIndent())
            return
        }
        val (name, value) = arguments
        when (name) {
            "author" -> {
                c.author = value
            }

            "version" -> {
                c.projectVersion = value
            }

            "kotlinver" -> {
                c.kotlinVer = value
            }

            "corover" -> {
                c.coroutineVer = value
            }

            else -> {
                println("Wrong config name")
            }
        }
    }
}