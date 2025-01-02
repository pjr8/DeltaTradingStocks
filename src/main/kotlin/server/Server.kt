package me.paulrobinson.server

import me.paulrobinson.server.client.Client
import java.net.ServerSocket

class Server : Runnable {
    private val serverSocket : ServerSocket = ServerSocket(8080)
    private val clientList = HashSet<Client>()
    private var running = true

    override fun run() {
        while (running) {
            val socket = serverSocket.accept()
            val client = Client(socket)
            clientList.add(client)
            println("Client ${socket.inetAddress.hostAddress}:${socket.port} connected")
            client.start()
        }
    }

    fun sendDataToAllClients(data: String) {
        println("Sending data to all clients: $data")
    }

}

/*


Assuming authentication

0000:data






 */