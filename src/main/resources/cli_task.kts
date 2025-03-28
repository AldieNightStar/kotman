tasks.register("cli") {
    group = "cli"
    dependsOn("shadowJar")
    doFirst {
        val task = tasks.getByName("shadowJar")
        task.actions[0].execute(this)

        val libs = File("./build/libs")
        val jar = libs.walk()
            .firstOrNull { it.nameWithoutExtension.endsWith("-all") } ?: return@doFirst
        val rootJar = File("./${project.name}.jar")

        // Generate bash file
        val rootScriptSh = File(rootJar.nameWithoutExtension)
        rootScriptSh.delete()
        rootScriptSh.writeText("""
            #!/bin/bash
            SCRIPTPATH="$( cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"
            java -cp "${"$"}SCRIPTPATH/${rootJar.name}" $MAIN_CLASS "$@"
        """.trimIndent())

        // Generate bat file
        val rootScriptBat = File(rootJar.nameWithoutExtension + ".bat")
        rootScriptBat.delete()
        rootScriptBat.writeText("""
            @echo off
            setlocal

            set SCRIPTPATH=%~dp0
            java -cp "%SCRIPTPATH%${rootJar.name}" $MAIN_CLASS %*

            endlocal
        """.trimIndent())

        rootJar.delete()
        jar.copyTo(rootJar)
    }
}