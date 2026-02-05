package haxidenti.kotman

import haxidenti.kotman.dto.ProjectDetails
import haxidenti.kotman.util.ProjectUtil
import java.io.File

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println(usage())
        return
    }
    val argumentIterator = args.iterator()
    executeCommand(argumentIterator.next(), argumentIterator.asSequence().toList())
}

private fun executeCommand(name: String, arguments: List<String>) {
    when (name) {
        "new" -> {
            val (projectName, packageName) = arguments.req(2)
            val details = ProjectDetails(projectName, packageName)
            ProjectUtil.generate(File("./$projectName"), details)
            println("OK")
        }

        "gen" -> {
            ProjectUtil.runGenerator(File("."))
            println("OK")
        }

        else -> {
            println("WRONG COMMAND\n")
            println(usage())
            return
        }
    }
}

fun usage() = """
        kotman [command] [args]
        
        kotman new [name] [package]       - Create new project
        kotman gen                        - Run project code generator. It will scan for "// generate:" comments
        
        HaxiDenti
    """.trimIndent()

private fun List<String>.req(n: Int): List<String> {
    if (size < n) {
        throw IllegalArgumentException("Argument size is less than $n")
    }
    return this
}

