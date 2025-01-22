package me.paulrobinson.server.packet

import com.esotericsoftware.kryonet.Connection

interface PacketHandler<T : Packet> {
    fun handle(packet: Packet, connection: Connection)
}