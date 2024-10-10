package me.paulrobinson.data.calculator

import api.APIHandler
import me.paulrobinson.data.historical.hsdataobjects.ATR
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import kotlin.math.abs
import kotlin.math.max

class ATRCalculator {

    fun calculate(
        ticker: String,
        beginSessionDate: LocalDate,
        endSessionDate: LocalDate,
        sessions: Int
    ): List<ATR> {

        val ATRArrayList = ArrayList<ATR>()
        val response = APIHandler().getATRData(
            ticker, beginSessionDate,
            endSessionDate
        )
        for (i in 0 until sessions) {
            val atrStockPrices = ArrayList<ATRStockPrice>()
            for (a in 11 downTo 1) {
                atrStockPrices.add(
                    ATRStockPrice(
                        response.results[a + i].high!!,
                        response.results[a + i].low!!,
                        response.results[a + i].close!!
                    )
                )
            }
            val localDate = Instant.ofEpochMilli(response.results[i + 11].timestampMillis!!)
                .atZone(ZoneId.of("America/New_York")).toLocalDate()
            ATRArrayList.add(ATR(calculateATR(atrStockPrices), localDate))
        }
        return ATRArrayList
    }

    private fun calculateATR(prices: MutableList<ATRStockPrice>): Double {
        require(prices.size >= 10) { "Not enough data to calculate ATR" }

        prices.reverse()

        val tr = DoubleArray(prices.size)
        // Calculate the true range for each day
        tr[0] = prices[0].high - prices[0].low
        for (i in 1 until prices.size) {
            val high = prices[i].high
            val low = prices[i].low
            val closePrev = prices[i - 1].close
            tr[i] = max(high - low, max(abs(high - closePrev), abs(low - closePrev)))
        }

        // Calculate the initial ATR (average of first 10 true ranges)
        var atr = 0.0
        for (i in 0..9) {
            atr += tr[i]
        }
        atr /= 10.0

        // Use an Exponential Moving Average for the rest of the ATR values
        for (i in 10 until tr.size) {
            atr = (atr * (10 - 1) + tr[i]) / 10
        }

        return atr
    }


    class ATRStockPrice(val high: Double, val low: Double, val close: Double)
}