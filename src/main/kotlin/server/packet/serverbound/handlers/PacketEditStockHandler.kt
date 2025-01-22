package me.paulrobinson.server.packet.serverbound.handlers

import com.esotericsoftware.kryonet.Connection
import me.paulrobinson.Main
import me.paulrobinson.server.packet.Packet
import me.paulrobinson.server.packet.PacketHandler
import me.paulrobinson.server.packet.clientbound.PacketStatusMessage
import me.paulrobinson.server.packet.serverbound.PacketEditStock

class PacketEditStockHandler : PacketHandler<PacketEditStock> {

    override fun handle(packet: Packet, connection: Connection) {
        var packet = packet as PacketEditStock
        if (true) {
            if (Main.realTimeData.queuedUpdate.stream().noneMatch { it.stock.ticker == packet.ticker.uppercase() }) {
                Main.dataHandler.addNewData(packet.ticker)
                connection.sendTCP(PacketStatusMessage("Stock ${packet.ticker} added."))
            } else { //Already exists!
                connection.sendTCP(PacketStatusMessage("Stock ${packet.ticker} already exists."))
            }
        } else {
            if (Main.realTimeData.queuedUpdate.stream().anyMatch { it.stock.ticker == packet.ticker.uppercase() }) {
                Main.realTimeData.queuedUpdate.removeIf { it.stock.ticker == packet.ticker.uppercase() }
                connection.sendTCP(PacketStatusMessage("Stock ${packet.ticker} removed."))
            } else { //Doesn't exist!
                connection.sendTCP(PacketStatusMessage("Stock ${packet.ticker} does not exist."))
            }
        }
    }
}