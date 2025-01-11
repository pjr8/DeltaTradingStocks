package me.paulrobinson.server.realtimedata

import api.APIHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.paulrobinson.Main
import me.paulrobinson.data.historical.HsData
import me.paulrobinson.data.historical.hsdataobjects.Session
import java.text.DecimalFormat
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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
                            item.timeToUpdate = System.currentTimeMillis() + (2 * 1000L)
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
        //${Main.websocket.stockPrices.get(stock.ticker)}
        val toSend = "001:${stock.ticker},${stock.session!!.sessionCandles.last().close},${format.format(stock.av)},${stock.avTimeBucketData}"
        Main.server.sendDataToAllClients(toSend)
        //println("Time: ${LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)} | $toSend")
    }

    fun addRealTimeStock(hsData: HsData) {
        val realTimeStock = RealTimeStock(hsData.ticker, hsData.historicalAtrList.last().atrValue, hsData.timeBucketList!!)
        createInitialSession(realTimeStock)
        queuedUpdate.add(stockTimerClass(realTimeStock, System.currentTimeMillis() + (2 * 1000L)))
        realTimeDataList.add(realTimeStock)
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
        stock.av = stock.session!!.sessionCandles.last().avSpread
        //println("Updated Session")
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
}


