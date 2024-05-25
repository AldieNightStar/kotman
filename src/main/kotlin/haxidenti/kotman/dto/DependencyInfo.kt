package haxidenti.kotman.dto

data class DependencyInfo(
    val prefix: String,
    val dependency: String,
    val dependencySuffix: String
) {
    override fun toString(): String {
        return if (testing) "TEST: $dependency" else "IMPL: $dependency"
    }

    val testing: Boolean get() = prefix.lowercase().contains("test")
}