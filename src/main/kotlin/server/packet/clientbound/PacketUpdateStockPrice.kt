package me.paulrobinson.server.packet.clientbound

import me.paulrobinson.server.packet.Packet

class PacketUpdateStockPrice() : Packet {
    var ticker: String = ""
    var price: Double = 0.0

    constructor(ticker: String, price: Double) : this() {
        this.ticker = ticker
        this.price = price
    }
}