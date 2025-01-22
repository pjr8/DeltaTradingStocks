package me.paulrobinson.server.client

import me.paulrobinson.Main
import me.paulrobinson.Main.Companion.server
import me.paulrobinson.data.historical.hsdataobjects.Sentiment
import java.net.Socket
import java.util.Random
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class Client(private val socket: Socket) : Thread() {
    var running = true

    override fun run() {


        while (running) {
            try {
                val message = receive()
                println("Received: $message")
                send("Received: $message")
            } catch (e: Exception) {
                println("Client ${socket.inetAddress.hostAddress}:${socket.port} disconnected")
                //Main.server.removeClient(this)
                running = false
            }

        }
    }

    fun receive(): String {
        return socket.getInputStream().bufferedReader().readLine()
    }

    fun send(message: String) {
        val toSend = message + "\n"
        socket.getOutputStream().write(toSend.toByteArray())
    }

    fun close() {
        socket.close()
    }
}