package me.paulrobinson.server

import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.FrameworkMessage
import com.esotericsoftware.kryonet.Listener
import com.esotericsoftware.kryonet.Server
import com.esotericsoftware.minlog.Log
import me.paulrobinson.data.historical.hsdataobjects.Sentiment
import me.paulrobinson.server.packet.Packet
import me.paulrobinson.server.packet.PacketHandler
import me.paulrobinson.server.packet.clientbound.PacketSendStocks
import me.paulrobinson.server.packet.clientbound.PacketStatusMessage
import me.paulrobinson.server.packet.clientbound.PacketUpdateStock
import me.paulrobinson.server.packet.clientbound.PacketUpdateStockPrice
import me.paulrobinson.server.packet.clientbound.model.ShortenedRealTimeStock
import me.paulrobinson.server.packet.serverbound.PacketEditStock
import me.paulrobinson.server.packet.serverbound.handlers.PacketEditStockHandler
import java.lang.Thread.sleep

class Server {
    var server = Server()
    private val handlers = mutableMapOf<Class<*>, PacketHandler<*>>()

    fun start() {
        registerClasses()
        registerHandlers()
        Thread(server).start()
        server.bind(25567, 25568)
        Log.set(Log.LEVEL_TRACE)
        server.addListener(object : Listener {
            override fun connected(connection: Connection) {
                println("Client connected: ${connection.remoteAddressTCP}")
                connection.endPoint.kryo.register(PacketEditStock::class.java)
                connection.sendTCP(PacketEditStock("AAPL"))

            }

            override fun received(connection: Connection, packet: Any) {
                try {
                    println("Received packet: $packet")
                    if (packet !is Packet || packet is FrameworkMessage.KeepAlive) return
                    val handler = handlers[packet::class.java]
                    if (handler != null) {
                        (handler).handle(packet, connection)
                    } else {
                        println("No handler found for packet type: ${packet::class.java}")
                    }
                } catch (e: Exception) {
                    println("Error handling packet: $packet")
                    e.printStackTrace()
                }

            }

            override fun disconnected(connection: Connection) {
                println("Client disconnected: ${connection.isIdle}")
                println("Client disconnected: ${connection.tcpWriteBufferSize}")
                println("Client disconnected: ${connection.returnTripTime}")


            }
        })
    }

    fun registerClasses() {
        server.kryo.register(Packet::class.java)
        server.kryo.register(PacketHandler::class.java)
        server.kryo.register(String::class.java)
        server.kryo.register(Collection::class.java)
        server.kryo.register(PacketSendStocks::class.java)
        server.kryo.register(PacketUpdateStock::class.java)
        server.kryo.register(PacketUpdateStockPrice::class.java)
        server.kryo.register(PacketStatusMessage::class.java)
        server.kryo.register(ShortenedRealTimeStock::class.java)
        server.kryo.register(ArrayList::class.java)
        server.kryo.register(Sentiment::class.java)
        server.kryo.register(PacketEditStock::class.java)
    }

    fun registerHandlers() {
        registerHandler(PacketEditStock::class.java, PacketEditStockHandler())
    }

    fun registerHandler(packetClass: Class<*>, handler: PacketHandler<*>) {
        handlers[packetClass] = handler
    }

    fun stop() {
        server.stop()
    }
}
