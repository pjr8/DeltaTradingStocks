package me.paulrobinson

import kotlinx.coroutines.*
import me.paulrobinson.data.DataHandler
import me.paulrobinson.server.Server
import me.paulrobinson.server.ServerInput
import kotlin.time.Duration

class Main {
    companion object {
        var server = Server()
        var dataHandler = DataHandler()
        var serverInput = ServerInput()
        var running = true
    }

    fun start() {
        while (running) {
        }
        println("Server Initializing...")

        serverInput.run()
        dataHandler.run()
        server.run()

        println("Server Initialized")
    }
}

fun main() {
    Main().start()
}