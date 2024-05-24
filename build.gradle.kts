import java.net.URI

val MAIN_CLASS = "haxidenti.kotman.MainKt"

plugins {
    kotlin("jvm") version "1.9.20"
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("application")
    
}

group = "HaxiDenti"
version = "1.0.0"

repositories {
    mavenCentral()
    mavenLocal()
    maven { url = URI("https://jitpack.io") }
    
}

dependencies {
    
    implementation(kotlin("stdlib-jdk8"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

application {
    mainClass.set(MAIN_CLASS)
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
        val rootScript = File(rootJar.nameWithoutExtension)
        rootScript.delete()
        rootScript.writeText(
            "SCRIPTPATH=\"\$( cd -- \"\$(dirname \"\$0\")\" >/dev/null 2>&1 ; pwd -P )\"\n"
            + "java -cp \"\$SCRIPTPATH/${rootJar.name}\" $MAIN_CLASS \"\$@\""
        )
        rootJar.delete()
        jar.copyTo(rootJar)
        File("build").deleteRecursively()
    }
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "HaxiDenti"
            artifactId = "kotman"
            version = "1.0.0"
            
            from(components["java"])
        }
    }
}