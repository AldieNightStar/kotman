package haxidenti.kotman

import java.io.File

object Command {
    fun dispatch(name: String, arguments: List<String>) {
        Configuration().use { c ->


            when (name) {
                "help" -> {
                    println(usage())
                    return
                }

                "config" -> {
                    dispatchConfig(c.userConfig, arguments)
                }

                "new" -> {
                    val (projectName, packageName) = arguments.req(2)
                    val details = ProjectDetails.fromConfig(projectName, packageName, c.userConfig)
                    Project.generate(details)
                    println("OK")
                }

                "cli" -> {
                    Project.generateCli(File("."))
                }

                else -> {
                    println("WRONG COMMAND\n")
                    println(usage())
                    return
                }
            }


        }
    }

    fun usage() = """
        kotman [command] [args]
        
        kotman new [name] [package]       - Create new project
        kotman cli                        - Create CLI for your project
        
        -- CONFIGURATION --
        kotman config                     - Read already set configuration
        kotman config author [name]       - Set new author for future projects
        kotman config version [version]   - Set new version for future projects
        kotman config kotlinver [version] - Set new kotlin version for future projects
    """.trimIndent()

    private fun List<String>.req(n: Int): List<String> {
        if (size < n) {
            throw IllegalArgumentException("Argument size if less than $n")
        }
        return this
    }

    private fun dispatchConfig(c: UserConfiguration, arguments: List<String>) {
        if (arguments.size < 2) {
            println("author: ${c.author}")
            println("version: ${c.projectVersion}")
            println("kotlinver: ${c.kotlinVer}")
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

            else -> {
                println("Wrong config name")
            }
        }
    }
}