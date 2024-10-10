package me.paulrobinson.client

import java.net.Socket

class Client(private val socket: Socket) {


    fun initialize() {

    }

    fun receive(): String {
        val buffer = ByteArray(1024)
        socket.getInputStream().read(buffer)
        return String(buffer)
    }

    fun send(message: String) {
        socket.getOutputStream().write(message.toByteArray())
    }

    fun close() {
        socket.close()
    }
}