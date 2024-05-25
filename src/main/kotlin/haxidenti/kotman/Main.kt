package haxidenti.kotman

import haxidenti.kotman.command.Command

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println(Command.usage())
        return
    }
    val argumentIterator = args.iterator()
    val command = argumentIterator.next()
    Command.dispatch(command, argumentIterator.asSequence().toList())
}

