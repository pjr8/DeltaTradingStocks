package me.paulrobinson

import me.paulrobinson.data.DataHandler
import me.paulrobinson.server.Server
import me.paulrobinson.server.ServerInput
import me.paulrobinson.server.realtimedata.RealTimeData
import me.paulrobinson.server.realtimedata.websocket.RealTimeStockWebSocket

class Main {
    companion object {
//        val startupTickers = listOf<String>("AAPL")
        val startupTickers = listOf<String>("NVDA", "TSLA", "MSFT",
            "COIN", "AMZN", "TSM", "NIO", "CVNA", "LLY", "QQQ", "META",
            "NFLX", "NIO", "PLTR", "RIOT", "RIVN", "AMD", "AMZN", "AVGO",
            "COIN", "CVNA", "RGTI", "IONQ", "SPY", "QQQ")
        val server = Server()
        val dataHandler = DataHandler()
        val serverInput = ServerInput()
        val websocket = RealTimeStockWebSocket()
        val realTimeData = RealTimeData()
        var running = true
    }

    fun start() {

        websocket.realTimeStocksJava("6XNAOfJMkLup9fyWCxW4SWPYg34jjJkZ")
        Thread(dataHandler).start()
        Thread(server).start()
        Thread(serverInput).start()
        Thread(realTimeData).start()
        while (running) { //val toSend = "001:${stock.ticker},${stock.session!!.sessionCandles.last().close},${format.format(stock.av)},${stock.avTimeBucketData},${stock.currentSentiment}"
        }
    }
}

fun main() {
    Main().start()
}