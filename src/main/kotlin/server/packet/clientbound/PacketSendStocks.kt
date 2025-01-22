package me.paulrobinson.server.packet.clientbound

import com.esotericsoftware.kryonet.Connection
import me.paulrobinson.server.packet.Packet
import me.paulrobinson.server.packet.clientbound.model.ShortenedRealTimeStock

class PacketSendStocks() : Packet {
    var stocks: List<ShortenedRealTimeStock> = emptyList()
    constructor(stocks: List<ShortenedRealTimeStock>) : this() {
        this.stocks = stocks
    }
}
