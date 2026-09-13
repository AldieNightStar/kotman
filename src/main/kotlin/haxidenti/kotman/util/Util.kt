package haxidenti.kotman.util

internal val String.tab get(): String {
    return "    "+this.replace("\n", "\n    ")
}