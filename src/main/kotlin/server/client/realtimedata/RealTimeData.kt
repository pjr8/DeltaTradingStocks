package me.paulrobinson.server.client.realtimedata

import api.APIHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.paulrobinson.Main
import me.paulrobinson.data.historical.HsData
import me.paulrobinson.data.historical.hsdataobjects.Sentiment
import me.paulrobinson.data.historical.hsdataobjects.Session
import java.text.DecimalFormat
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.ConcurrentHashMap

class RealTimeData: Runnable {

    companion object {
        val realTimeDataList = ConcurrentHashMap.newKeySet<RealTimeStock>()
    }

    val format = DecimalFormat("0.000")
    data class stockTimerClass(val stock: RealTimeStock, var timeToUpdate: Long)
    val queuedUpdate = ConcurrentHashMap.newKeySet<stockTimerClass>()

    override fun run() {
        if (LocalDate.now().dayOfWeek == DayOfWeek.SATURDAY ||
            LocalDate.now().dayOfWeek == DayOfWeek.SUNDAY) {
            return
        }
        val scope = CoroutineScope(Dispatchers.Default)

        scope.launch {
            while (Main.running) {
                queuedUpdate.forEach { item ->
                    if (item.timeToUpdate < System.currentTimeMillis()) {
                        launch {
                            updateRealTimeStock(item.stock)
                            item.timeToUpdate = System.currentTimeMillis() + 1250L
                        }
                    }
                }
                delay(100) // Suspend for a short period to avoid busy-waiting
            }
        }
    }

    private fun updateRealTimeStock(stock: RealTimeStock) {
        updateSession(stock)
        setAVTimeBucketDataString(stock)
        setSentiment(stock)
        val toSend: String
        if (stock.session!!.sessionCandles.isNotEmpty()) {
            toSend = "001:${stock.ticker},${stock.session!!.sessionCandles.last().close},${format.format(stock.av)},${stock.avTimeBucketData},${stock.currentSentiment}"
        } else {
            toSend = "001:${stock.ticker},999.99,${format.format(stock.av)},${stock.avTimeBucketData},${stock.currentSentiment}"
        }
        //Main.server.sendDataToAllClients(toSend)
    }

    fun addRealTimeStock(hsData: HsData) {
        val realTimeStock = RealTimeStock(hsData.ticker, hsData.historicalAtrList.last().atrValue, hsData.timeBucketList!!)
        createInitialSession(realTimeStock)
        queuedUpdate.add(stockTimerClass(realTimeStock, System.currentTimeMillis() + (2 * 1000L)))
        realTimeDataList.add(realTimeStock)
        setSentiment(realTimeStock)
        hsData.historicalSessionList = null
        Main.dataHandler.hsDataStocksLoaded.remove(hsData)
    }

    private fun createInitialSession(stock: RealTimeStock) {
        stock.session = APIHandler().getRealTimeSessionFull(stock.ticker)
        recalculateCandleData(stock.session!!, stock.lastDayAtr)
        if (stock.session!!.sessionCandles.isNotEmpty()) {
            stock.av = stock.session!!.sessionCandles.last().avSpread
        } else {
            stock.av = 500.0
        }
    }

    private fun updateSession(stock: RealTimeStock) {
        val candles = APIHandler().getRealTimeSessionPartial(stock.ticker)
        candles.asReversed()
        for (candle in candles) {
            if (stock.session!!.sessionCandles.last().datedCandle.isBefore(candle.datedCandle)) {
                stock.session!!.sessionCandles.add(candle)
            }
        }
        recalculateCandleData(stock.session!!, stock.lastDayAtr)
        if (stock.session!!.sessionCandles.isNotEmpty()) {
            stock.av = stock.session!!.sessionCandles.last().avSpread
        } else {
            stock.av = 999.99
        }
    }

    private fun recalculateCandleData(historicalSession: Session, atr: Double) {
        var cumulativeValue = 0.0
        var cumulativeVolume = 0
        for (historicalCandle in historicalSession.sessionCandles) {
            cumulativeValue += ((historicalCandle.low + historicalCandle.high) / 2) * historicalCandle.volume
            cumulativeVolume += historicalCandle.volume
            historicalCandle.vwapValue = cumulativeValue / cumulativeVolume
            historicalCandle.avSpread = calculateAVSpread(
                atr,
                historicalCandle.vwapValue,
                historicalCandle.close
            )
        }
    }

    private fun setAVTimeBucketDataString(stock : RealTimeStock) {
        val rightNow = LocalTime.now(ZoneId.of("America/New_York"))
        val marketOpen = LocalTime.of(9, 30)
        val format = DecimalFormat("0.0#")
        for (timeBucket in stock.avTimeBucketDataList) {
            if (rightNow.isAfter(marketOpen) &&
                rightNow.isBefore(marketOpen.plus(Duration.ofMinutes(timeBucket.timeBucketMinutes.toLong())))) {
                stock.avTimeBucketData = "${format.format(timeBucket.minHighEntry)} | ${format.format(timeBucket.minLowEntry)}"
                return
            }
        }
        stock.avTimeBucketData = "Pre/After Market"
    }

    private fun calculateAVSpread(atr: Double, vwap: Double, close: Double): Double {
        return (close - vwap) / atr
    }

    fun setSentiment(stock: RealTimeStock) {
        try {
            val results = APIHandler().getLast20News(stock.ticker)
            var positiveCount = 0
            var negativeCount = 0
            var neutralCount = 0

            // Count sentiment occurrences
            results.forEach {
                when (it.sentiment) {
                    Sentiment.POSITIVE -> positiveCount++
                    Sentiment.NEGATIVE -> negativeCount++
                    Sentiment.NEUTRAL -> neutralCount++
                    else -> println("Unknown sentiment: ${it.sentiment}")
                }
            }

            // Ensure counts add up to 20
            if (positiveCount + negativeCount + neutralCount != 20) {
                stock.currentSentiment = Sentiment.NO_DATA
                return
            }
            // Calculate sentiment ratios
            val positiveRatio = positiveCount / 20.0
            val negativeRatio = negativeCount / 20.0
            val neutralRatio = neutralCount / 20.0

            // Determine sentiment based on thresholds
            stock.currentSentiment = when {
                positiveRatio > 0.8 -> Sentiment.POSITIVE
                positiveRatio > 0.6 -> Sentiment.LOW_POSITIVE
                negativeRatio > 0.8 -> Sentiment.NEGATIVE
                negativeRatio > 0.6 -> Sentiment.LOW_NEGATIVE
                neutralRatio >= 0.4 -> Sentiment.NEUTRAL
                else -> {
                    // Handle edge cases or ties by choosing the dominant sentiment
                    when {
                        positiveRatio > negativeRatio -> Sentiment.POSITIVE
                        negativeRatio > positiveRatio -> Sentiment.NEGATIVE
                        else -> Sentiment.NEUTRAL // Default fallback
                    }
                }
            }
        } catch (e: Exception) {
            println("Error getting sentiment: ${e.message}")
            // Fallback to neutral sentiment in case of an error
            stock.currentSentiment = Sentiment.NEUTRAL
        }
    }


    //OLD SENTIMENT CALC
/*    fun setSentiment(stock: RealTimeStock) {
        try {
            val results = APIHandler().getLast20News(stock.ticker)
            var positiveCount = 0
            var negativeCount = 0
            var neutralCount = 0

            results.forEach {
                when (it.sentiment) {
                    Sentiment.POSITIVE -> positiveCount++
                    Sentiment.NEGATIVE -> negativeCount++
                    Sentiment.NEUTRAL  -> neutralCount++
                    // Or if there are other sentiment types, handle them as needed
                    Sentiment.LOW_POSITIVE -> println("Not possible!")
                    Sentiment.LOW_NEGATIVE -> ("Not possible!")
                }
            }

            require(positiveCount + negativeCount + neutralCount == 20) {
                "Counts must add up to 20."
            }

            val p = positiveCount / 20.0
            val n = negativeCount / 20.0
            val neu = neutralCount / 20.0

            stock.currentSentiment = when {
                p > n && p > neu -> {
                    if (p >= 0.5) Sentiment.POSITIVE else Sentiment.LOW_POSITIVE
                }
                n > p && n > neu -> {
                    if (n >= 0.5) Sentiment.NEGATIVE else Sentiment.LOW_NEGATIVE
                }
                else -> {
                    // Neutral dominates or there's a tie
                    Sentiment.NEUTRAL
                }
            }
        } catch (e: Exception) {
            println("Error getting sentiment: ${e.message}")
            // If there was an error, set it to NEUTRAL (or whatever your fallback is)
            stock.currentSentiment = Sentiment.NEUTRAL
        }
    }*/
}


