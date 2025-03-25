package haxidenti.kotman.command

import haxidenti.kotman.Configuration
import haxidenti.kotman.dto.UserConfiguration

internal object ConfigCommand {
    fun dispatchConfig(c: Configuration, arguments: List<String>) {
        if (arguments.size == 1) {
            singleArgumentProcess(arguments.first(), c)
        } else if (arguments.size > 1) {
            twoArgumentsProcess(arguments[0], arguments[1], c)
        } else {
            showStatus(c.userConfig)
        }
    }

    private fun showStatus(c: UserConfiguration) {
        println("author: ${c.author}")
        println("version: ${c.projectVersion}")
        println("kotlinver: ${c.kotlinVer}")
        println("corover: ${c.coroutineVer}")
        println(
            """
                
                Usage:
                    kotman config                - Show values
                    kotman config [name] [value] - Set new value
                    kotman config reset          - Reset the configuration
                
            """.trimIndent()
        )
    }

    private fun singleArgumentProcess(name: String, c: Configuration) {
        when (name) {
            "reset" -> {
                c.reset()
                println("Configuration dropped!")
            }

            else -> {
                println("Unknown command: $name")
            }
        }
    }

    private fun twoArgumentsProcess(name: String, value: String, c: Configuration) {
        when (name) {
            "author" -> {
                c.userConfig.author = value
            }

            "version" -> {
                c.userConfig.projectVersion = value
            }

            "kotlinver" -> {
                c.userConfig.kotlinVer = value
            }

            "corover" -> {
                c.userConfig.coroutineVer = value
            }

            "reset" -> {
                c.reset()
                println("Configuration is dropped")
            }

            else -> {
                println("Wrong config name")
            }
        }
    }
}