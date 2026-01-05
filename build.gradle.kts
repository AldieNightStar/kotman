val MAIN_CLASS = "haxidenti.kotman.MainKt"
val PROJECT_NAME = "kotman"
val AUTHOR = "HaxiDenti"
val VERSION = "1.0.0"

plugins {
    kotlin("jvm") version "2.2.20"
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = AUTHOR
version = VERSION

repositories {
    mavenCentral()
    mavenLocal()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.google.code.gson:gson:2.11.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

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

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = AUTHOR
            artifactId = PROJECT_NAME
            version = VERSION
            
            from(components["java"])
        }
    }
}