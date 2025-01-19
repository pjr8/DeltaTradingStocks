package me.paulrobinson.server

import me.paulrobinson.Main

class ServerInput : Runnable {
    override fun run() {
        println("Server Input Started")
        while (Main.running) {
            try {
                val input = readln()
                println("Adding new stock: $input")
                Main.dataHandler.addNewData(input)
            } catch (e: Exception) {
                println("Error while receiving input: $e")
            }
        }


    }
}