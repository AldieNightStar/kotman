package haxidenti.kotman.dto

import haxidenti.kotman.Constants

data class ProjectDetails(
    val projectName: String,
    val packageName: String,
    val author: String = Constants.AUTHOR,
    val version: String = Constants.VERSION,
    val kotlinVer: String = Constants.KOTLIN_VER,
    val additionalDependencies: List<String> = listOf(),
)