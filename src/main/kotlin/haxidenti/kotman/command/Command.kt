package haxidenti.kotman.command

import haxidenti.kotman.Configuration
import haxidenti.kotman.command.ConfigCommand.dispatchConfig
import haxidenti.kotman.dto.ProjectDetails
import haxidenti.kotman.util.Project
import java.io.File

internal object Command {
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

                "ver" -> {
                    val (newVersion) = arguments.req(1)
                    Project.changeVersion(File("."), newVersion)
                    println("OK")
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
        kotman ver [version]              - Change project gradle version
        
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
}