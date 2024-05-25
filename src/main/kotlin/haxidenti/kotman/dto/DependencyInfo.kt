package haxidenti.kotman.dto

data class DependencyInfo(
    val dependency: String,
    val testing: Boolean
) {
    override fun toString(): String {
        return if (testing) "TEST: $dependency" else "IMPL: $dependency"
    }
}