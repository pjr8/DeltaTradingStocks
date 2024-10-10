package me.paulrobinson.data.calculator

import me.paulrobinson.data.historical.hsdataobjects.Session

class AvgMaxAVSpreadCalculator {

    fun calculateHigh(sessionsList: List<Session>): Double {
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
                cumulativeAV += dailyHighestAV.avSpread
                sessionsAVUsed++
            }
        }
        return cumulativeAV / sessionsAVUsed
    }

    fun calculateLow(sessionsList: List<Session>): Double {
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
                cumulativeAV += dailyLowestAV.avSpread
                sessionsAVUsed++
            }
        }
        return cumulativeAV / sessionsAVUsed
    }
}
