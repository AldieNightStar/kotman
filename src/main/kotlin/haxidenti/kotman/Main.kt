package haxidenti.kotman

import haxidenti.kotman.Sys.isWindows
import haxidenti.kotman.Sys.runShell
import haxidenti.kotman.util.MavenUtil
import java.io.File

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println(
            """
            Usage:
              kotman lib [name] - Create new kotlin library
              kotman app [name] - Create new kotlin application
              kotman loc        - Open maven local folder
        """.trimIndent()
        )
        return
    }
    runCmd(args[0], args.drop(1))
}

fun runCmd(cmd: String, args: List<String>) {
    val workDir = File("./")
    val arg = args.getOrNull(0)
    when (cmd) {
        "lib" -> {
            if (arg == null) {
                println("[!] Need name of the project to create")
                return
            }
            Project.createProject(workDir, args[0], false)
        }

        "app" -> {
            if (arg == null) {
                println("[!] Need name of the project to create")
                return
            }
            Project.createProject(workDir, args[0], true)
        }

        "loc" -> {
            val dir = MavenUtil.getMavenRepositoryDir()!!
            if (isWindows()) {
                runShell(dir, listOf("explorer", dir.absolutePath))
            } else {
                runShell(dir, listOf("xdg-open", dir.absolutePath))
            }
        }

        else -> {
            println("[!] Unknown command: $cmd")
        }
    }
}