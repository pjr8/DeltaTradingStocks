package me.paulrobinson.data.calculator

import io.polygon.kotlin.sdk.rest.AggregatesParameters
import io.polygon.kotlin.sdk.rest.PolygonRestClient
import me.paulrobinson.data.historical.hsdataobjects.ATR
import me.paulrobinson.data.historical.hsdataobjects.Candle
import me.paulrobinson.data.historical.hsdataobjects.Session
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class SessionCalculator {

    val polygonClient = PolygonRestClient("")

    fun calculate(
        ticker: String,
        beginSession: LocalDate,
        endSession: LocalDate,
        historicalAtrList: List<ATR>
    ): List<Session> {
        val toReturn: ArrayList<Session> = ArrayList()
        val dates : MutableList<LocalDate> = ArrayList()
        dates.addAll(beginSession.datesUntil(endSession).toList())
        dates.add(endSession)
        for (date in dates) {
            val session = Session()
            session.sessionDate = date
            val params = AggregatesParameters(
                ticker = ticker,
                fromDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                toDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                limit = 50_000,
                multiplier = 1,
                timespan = "second",
                unadjusted = false,
                sort = "asc"
            )
            val results = polygonClient.getAggregatesBlocking(params);
            if (results.queryCount!! > 0) {
                for (result in results.results) {
                    val historicalCandle = Candle()
                    historicalCandle.open = result.open!!
                    historicalCandle.close = result.close!!
                    historicalCandle.high = result.high!!
                    historicalCandle.low = result.low!!
                    historicalCandle.volume = result.volume!!.toInt()
                    historicalCandle.datedCandle = LocalDateTime.ofInstant(Instant.ofEpochMilli(result.timestampMillis!!),
                        ZoneId.of("America/New_York"))
                    session.sessionCandles.add(historicalCandle)
                }
                toReturn.add(session)
            }
        }

        for ((i, session) in toReturn.withIndex()) {
            session.historicalATR = historicalAtrList[i]

            var cumulativeValue = 0.0
            var cumulativeVolume = 0
            var highestAV = 0.0
            var lowestAV = 0.0;
            for (historicalCandle in session.sessionCandles) {
                cumulativeValue += ((historicalCandle.low + historicalCandle.high) / 2) * historicalCandle.volume
                cumulativeVolume += historicalCandle.volume
                historicalCandle.vwapValue = (cumulativeValue / cumulativeVolume)
                historicalCandle.avSpread = (
                        calculateAVSpread(
                            session.historicalATR,
                            historicalCandle.vwapValue,
                            historicalCandle.close
                        )
                        )
                if (highestAV < historicalCandle.avSpread) {
                    highestAV = historicalCandle.avSpread
                }
                if (lowestAV > historicalCandle.avSpread) {
                    lowestAV = historicalCandle.avSpread
                }
            }
            session.highestAV = highestAV
            session.lowestAV = lowestAV
        }
        return toReturn
    }

    private fun calculateAVSpread(ATR: ATR, vwap: Double, close: Double): Double {
        return (close - vwap) / ATR.atrValue
    }
}