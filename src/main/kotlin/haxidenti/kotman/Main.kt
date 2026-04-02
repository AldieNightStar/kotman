package haxidenti.kotman

import java.io.File

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("""
            Usage:
              kotman new [name] - Create new kotlin project
              
            Inside project:
              kotman mod [name] - Creates new module inside gradle project
              kotman dist       - Distribute files into APP directory
        """.trimIndent())
        return
    }
    runCmd(args[0], args.drop(1))
}

fun runCmd(cmd: String, args: List<String>) {
    val workDir = File("./")
    when (cmd) {
        "new" -> {
            if (args.isEmpty()) {
                println("[!] Need name of the project to create")
                return
            }
            Project.createProject(workDir, args[0])
        }
        "mod" -> {
            if (args.isEmpty()) {
                println("[!] Need name of the module to create")
                return
            }
            val projectName = File(workDir.canonicalPath).name
            if (!Project.addGradleModule(workDir, args[0], projectName)) {
                println("[!] Can't add gradle module. Are you inside of the gradle root project?")
            }
        }
        "dist" -> {
            if (!Project.hasGradle(workDir)) {
                println("[!] Not a gradle directory. Make sure gradlew is present")
                return
            }
            val projectName = File(workDir.canonicalPath).name
            Project.makeDist(workDir.canonicalFile, projectName)
        }
        else -> {
            println("[!] Unknown command: $cmd")
        }
    }
}