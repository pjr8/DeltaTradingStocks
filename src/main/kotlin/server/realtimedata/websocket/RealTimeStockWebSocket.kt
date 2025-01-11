package me.paulrobinson.server.realtimedata.websocket

import io.polygon.kotlin.sdk.websocket.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.paulrobinson.Main.Companion.websocket
import java.util.concurrent.ConcurrentHashMap

class RealTimeStockWebSocket {
    private lateinit var websocketClient: PolygonWebSocketClient
    val stockPrices = ConcurrentHashMap<String, Double>()

    @OptIn(DelicateCoroutinesApi::class)
    fun realTimeStocksJava(polygonKey: String) {
        GlobalScope.launch {
            realTimeStocks(polygonKey)
        }
    }
    private
    suspend fun realTimeStocks(polygonKey: String) {
        websocketClient = PolygonWebSocketClient(
            polygonKey,
            Feed.RealTime,
            Market.Stocks,
            object : PolygonWebSocketListener {
                override fun onAuthenticated(client: PolygonWebSocketClient) {
                    println("[Polygon] Authenticated!")
                }

                override fun onReceive(
                    client: PolygonWebSocketClient,
                    message: PolygonWebSocketMessage
                ) {
                    when (message) {
                        is PolygonWebSocketMessage.RawMessage -> println(String(message.data))
                        is PolygonWebSocketMessage.StatusMessage -> println("Status: ${message.message}")
                        is PolygonWebSocketMessage.StocksMessage.Aggregate -> {
                            //stockPrices[message.ticker!!] = message.closePrice!!
                            //println("${message.ticker}: ${message.closePrice}")
                        }
                        is PolygonWebSocketMessage.StocksMessage.Trade -> {
                            stockPrices[message.ticker!!] = message.price!!
                        }
                        else -> println("Received Message: $message")
                    }
                }

                override fun onDisconnect(client: PolygonWebSocketClient) {
                    println("[Polygon] Web Socket Disconnected!")
                }

                override fun onError(client: PolygonWebSocketClient, error: Throwable) {
                    println("Error: ")
                    error.printStackTrace()
                }

            })

        //val subscriptions: MutableList<PolygonWebSocketSubscription> = mutableListOf()
        websocketClient.connect()
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun addStock(ticker: String) {
        GlobalScope.launch {
            websocketClient.subscribe(listOf(PolygonWebSocketSubscription(PolygonWebSocketChannel.Stocks.Trades, ticker)))
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun removeStock(ticker: String) {
        GlobalScope.launch {
            websocketClient.unsubscribe(listOf(PolygonWebSocketSubscription(PolygonWebSocketChannel.Stocks.Trades, ticker)))
        }
    }

    data object StockPrice {
        var ticker: String = ""
        var price: Double = 0.0
    }
}