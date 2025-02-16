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
        val rootScript = File(rootJar.nameWithoutExtension)
        rootScript.delete()
        rootScript.writeText(
            "#!/bin/bash\n"
            + "SCRIPTPATH=\"\$( cd -- \"\$(dirname \"\$0\")\" >/dev/null 2>&1 ; pwd -P )\"\n"
            + "java -cp \"\$SCRIPTPATH/${rootJar.name}\" $MAIN_CLASS \"\$@\""
        )
        rootJar.delete()
        jar.copyTo(rootJar)
        File("build").deleteRecursively()
    }
}