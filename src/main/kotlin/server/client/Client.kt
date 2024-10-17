package me.paulrobinson.server.client

import me.paulrobinson.database.Database
import me.paulrobinson.database.client.ClientData
import java.net.Socket

class Client(private val socket: Socket) : Thread() {


    private val authenticated = false

    override fun run() {
        println("Happens 1")
        send("Hi")

        while (true) {
            println("Happens 2")

            //MANAGE AUTHENTICATION HERE
            if (!authenticated) {
                println("Happens 3")
                val message = receive()
                println("Happens 4 $message")

                val clientData = Database.instance.loadClientData(message)
                if (clientData != null) { //Account exists
                    send("Account Exists")
                    val password = receive()
                    if (password == clientData.password) { //Password matches
                        send("Authenticated")
                        authenticated == true
                    } else { //Password does not match
                        send("Username/Password does not match")
                    }
                } //Account does not exist
                else { //RESPOND THAT USERNAME OR PASSWORD DOES NOT MATCH
                    send("Username/Password does not match")
                }
            }


            val message = receive()
            println("Received: $message")
            send("Received: $message")
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