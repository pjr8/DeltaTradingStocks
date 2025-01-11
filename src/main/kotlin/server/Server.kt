package me.paulrobinson.server

import me.paulrobinson.server.client.Client
import java.net.ServerSocket

class Server : Runnable {
    private val serverSocket : ServerSocket = ServerSocket(25566)
    private val clientList = HashSet<Client>()
    private var running = true

    override fun run() {
        while (running) {
            try {
                val socket = serverSocket.accept()
                val client = Client(socket)
                clientList.add(client)
                println("Client ${socket.inetAddress.hostAddress}:${socket.port} connected")
                client.start()
            } catch (e: Exception) {
                println("Error while running server: $e")
            }
        }
    }

    fun removeClient(client: Client) {
        clientList.remove(client)
    }

    fun sendDataToAllClients(data: String) {
        clientList.forEach {
            it.send(data)
        }
    }

}

/*


Assuming authentication

0000:data






 */