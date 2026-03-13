package haxidenti.kotman

import java.io.File

object Err {
    fun fail(t: String): Nothing = throw IllegalStateException(t)
    fun notExist(name: String): Nothing = throw IllegalStateException("$name is not exist")
    fun notExist(f: File): Nothing = notExist(f.toString())
}