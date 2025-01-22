package me.paulrobinson.server.packet.clientbound

import me.paulrobinson.data.historical.hsdataobjects.Sentiment
import me.paulrobinson.server.packet.Packet

class PacketUpdateStock() : Packet {
    var ticker: String = ""
    var price: Double = 0.0
    var avSpread: Double = 0.0
    var minimumEntry: String = ""
    var sentiment: Sentiment = Sentiment.NO_DATA

    constructor(ticker: String, price: Double, avSpread: Double, minimumEntry: String, sentiment: Sentiment) : this() {
        this.ticker = ticker
        this.price = price
        this.avSpread = avSpread
        this.minimumEntry = minimumEntry
        this.sentiment = sentiment
    }
}