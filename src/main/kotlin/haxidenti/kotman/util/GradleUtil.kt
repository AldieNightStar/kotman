package haxidenti.kotman.util

import haxidenti.kotman.Constants
import haxidenti.kotman.Constants.GRADLE_VERSION
import haxidenti.kotman.Constants.JUPITER_ENGINE_VER
import haxidenti.kotman.dto.ProjectDetails

object GradleUtil {
    private val dependencies = $$"""
        implementation(kotlin("stdlib-jdk8"))
        testImplementation("org.junit.jupiter:junit-jupiter-api:$$JUPITER_ENGINE_VER")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$$JUPITER_ENGINE_VER")
    """.trimIndent()

    private val cliTask by lazy {
        Constants::class.java.classLoader.getResource("cli_task.txt")!!.readText(Charsets.UTF_8)
    }

    fun generateSubprojectBuildGradle(module: String): String {
        val dependenciesStr = buildString {
            if (module != Constants.MODULE_COMMON) {
                appendLine("implementation(project(\":${Constants.MODULE_COMMON}\"))")
            }
            append(dependencies)
        }
        return """
            repositories {
                mavenLocal()
                mavenCentral()
                maven { url = uri("https://jitpack.io") }
            }

            plugins {
                java
                kotlin("jvm")
                id("com.github.johnrengelman.shadow") version "8.1.1"
                `maven-publish`
            }

            dependencies {
            %DEPS%
            }
        """.trimIndent()
            .replace("%DEPS%", dependenciesStr.prependIndent("    "))
    }

    fun generateBuildGradle(details: ProjectDetails): String {
        return $$"""
            val MAIN_CLASS = "$${details.packageName}.MainKt"

            val AUTHOR = "$${details.author}"
            val PROJECT_NAME = "$${details.projectName}"
            val VERSION = "$${details.version}"
            
            plugins {
                java
                kotlin("jvm") version "$${details.kotlinVer}"
                id("com.github.johnrengelman.shadow") version "8.1.1"
                `maven-publish`
            }
            
            group = AUTHOR
            version = VERSION
            
            allprojects {
                repositories {
                    mavenLocal()
                    mavenCentral()
                    maven { url = uri("https://jitpack.io") }
                }
            }
            
            dependencies {
            %DEPS%
            }
            
            %CLI_TASK%
            
            tasks.test {
                useJUnitPlatform()
            }
            
            java {
                withSourcesJar()
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
        """.trimIndent()
            .replace("%DEPS%", dependencies.prependIndent("    "))
            .replace("%CLI_TASK%", cliTask)
    }

    fun generateGradleSettings(projectName: String): String {
        val names = Constants.MODULES.map { "\":$it\"" }.joinToString(", ")
        return """
            rootProject.name = "$projectName"
            include($names)
        """.trimIndent()
    }

    fun generateGradleWrapperConfig() = """
        distributionBase=GRADLE_USER_HOME
        distributionPath=wrapper/dists
        distributionUrl=https\://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip
        networkTimeout=10000
        validateDistributionUrl=true
        zipStoreBase=GRADLE_USER_HOME
        zipStorePath=wrapper/dists
    """.trimIndent()
}