package me.paulrobinson.server.packet.clientbound

import me.paulrobinson.server.packet.Packet

class PacketStatusMessage : Packet {
    var message: String = ""
    constructor(message: String) {
        this.message = message
    }
}