package me.paulrobinson.server.client

import java.net.Socket

class Client(private val socket: Socket) : Thread() {
    override fun run() {
        while (true) {
            try {
                val message = receive()
                println("Received: $message")
                send("Received: $message")
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    fun receive(): String {
        return socket.getInputStream().bufferedReader().readLine()
    }

    fun send(message: String) {
        socket.getOutputStream().write(message.toByteArray())
    }

    fun close() {
        socket.close()
    }
}