package me.paulrobinson.server

import me.paulrobinson.Main

class ServerInput : Runnable {
    override fun run() {
        println("Server Input Started")
        while (Main.running) {
            val input = readln()
            println("Received input: $input")
        }
    }
}