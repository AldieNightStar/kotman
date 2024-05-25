package haxidenti.kotman.dto

data class ProjectDetails(
    val projectName: String,
    val author: String,
    val version: String,
    val packageName: String,
    val kotlinVer: String,
    val additionalDependencies: List<String>
) {
    companion object {
        fun fromConfig(projectName: String, packageName: String, conf: UserConfiguration) = ProjectDetails(
            projectName = projectName,
            packageName = packageName,
            additionalDependencies = listOf(),
            author = conf.author,
            version = conf.projectVersion,
            kotlinVer = conf.kotlinVer
        )
    }
}
