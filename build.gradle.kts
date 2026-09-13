val MAIN_CLASS = "haxidenti.kotman.MainKt"
val PROJECT_NAME = "kotman"
val AUTHOR = "HaxiDenti"
val VERSION = "1.0.0"

plugins {
    kotlin("jvm") version "2.4.20"
    `maven-publish`
    application
}

group = AUTHOR
version = VERSION

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("com.google.code.gson:gson:2.11.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
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

application {
    mainClass = "haxidenti.kotman.MainKt"
}