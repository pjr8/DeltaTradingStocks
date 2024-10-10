package me.paulrobinson.data.calculator

import me.paulrobinson.data.historical.hsdataobjects.Candle
import me.paulrobinson.data.historical.hsdataobjects.Session
import kotlin.math.pow
import kotlin.math.sqrt

class StDevCalculator {
    fun calculateHigh(sessionsList: List<Session>): Double {
        val standardDeviationCandles = ArrayList<Candle>()
        var cumulativeAV = 0.0
        var sessionsAVUsed = 0
        for (historicalSession in sessionsList) {
            var dailyHighestAV = historicalSession.sessionCandles[0]
            for (historicalCandle in historicalSession.sessionCandles) {
                if (dailyHighestAV.avSpread < historicalCandle.avSpread) {
                    dailyHighestAV = historicalCandle
                }
            }
            if (!(dailyHighestAV.avSpread > 1.5)) {
                standardDeviationCandles.add(dailyHighestAV)
                cumulativeAV += dailyHighestAV.avSpread
                sessionsAVUsed++
            }
        }
        return calculateStandardDeviation(
            standardDeviationCandles,
            cumulativeAV / sessionsAVUsed
        )
    }

    fun calculateLow(sessionsList: List<Session>): Double {
        val standardDeviationCandles = ArrayList<Candle>()
        var cumulativeAV = 0.0
        var sessionsAVUsed = 0
        for (historicalSession in sessionsList) {
            var dailyLowestAV = historicalSession.sessionCandles[0]
            for (historicalCandle in historicalSession.sessionCandles) {
                if (dailyLowestAV.avSpread > historicalCandle.avSpread) {
                    dailyLowestAV = historicalCandle
                }
            }
            if (!(dailyLowestAV.avSpread < -1.5)) {
                standardDeviationCandles.add(dailyLowestAV)
                cumulativeAV += dailyLowestAV.avSpread
                sessionsAVUsed++
            }
        }
        return -calculateStandardDeviation(
            standardDeviationCandles,
            cumulativeAV / sessionsAVUsed
        )
    }

    private fun calculateStandardDeviation(avSpreads: ArrayList<Candle>, averageMaxAVSpread: Double): Double {
        var sum = 0.0
        for (historicalCandle in avSpreads) {
            sum += (historicalCandle.avSpread - averageMaxAVSpread).pow(2.0)
        }
        return sqrt(sum / (avSpreads.size - 1))
    }
}