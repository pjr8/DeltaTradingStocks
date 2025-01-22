package me.paulrobinson.server.packet.serverbound

import me.paulrobinson.server.packet.Packet

class PacketEditStock : Packet {
    var ticker: String = ""
    constructor(ticker: String) {
        this.ticker = ticker
    }
    constructor()
}