package haxidenti.kotman

import java.io.File

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("""
            Usage:
              kotman lib [name]            - Create new kotlin library
              kotman app [name]            - Create new kotlin application
              kotman import [file.zip]     - Install zip contents into .m2/repository
              kotman export [package_name] - Pack libs from .m2/repository into zip file
        """.trimIndent())
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
        "import" -> {

        }
        "export" -> {

        }
        else -> {
            println("[!] Unknown command: $cmd")
        }
    }
}