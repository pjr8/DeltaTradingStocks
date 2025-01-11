package data.timebucket

import data.timebucket.objects.AVTimeBucketData
import me.paulrobinson.data.historical.HsData
import me.paulrobinson.data.historical.hsdataobjects.Candle
import java.time.Duration
import java.time.LocalTime
import kotlin.math.pow
import kotlin.math.sqrt

class AVTBCalculator {

    private val timeBuckets = listOf(5, 10, 15, 20, 25, 30, 60, 90, 120, 180, 240, 300, 390)
    fun calculate(historicalData: HsData): List<AVTimeBucketData> {
        val open = LocalTime.of(9, 29, 59)
        val close = LocalTime.of(16, 0, 0)
        val toReturn = ArrayList<AVTimeBucketData>()
        for (timeBucket in timeBuckets) {
            var cumulativeHighMaxAV = 0.0
            var cumulativeLowMaxAV = 0.0
            var total = 0.0
            val highStDevCandles = ArrayList<Candle>()
            val lowStDevCandles = ArrayList<Candle>()
            for (historicalSession in historicalData.historicalSessionList!!) {
                val openCandles = ArrayList<Candle>()
                for (historicalCandle in historicalSession.sessionCandles) {
                    if (historicalCandle.datedCandle.toLocalTime()
                            .isAfter(open) && historicalCandle.datedCandle.toLocalTime().isBefore(close)
                    ) {
                        openCandles.add(historicalCandle)
                    }
                }
                var lowestMaxAVSpread = openCandles[0]
                var highestMaxAVSpread = openCandles[0]
                for (openCandle in openCandles) {
                    if (openCandle.datedCandle.toLocalTime().isAfter(open)
                        && openCandle.datedCandle.toLocalTime()
                            .isBefore(open.plus(Duration.ofMinutes(timeBucket.toLong())))
                    ) {
                        if (openCandle.avSpread < lowestMaxAVSpread.avSpread) {
                            lowestMaxAVSpread = openCandle
                        } else if (openCandle.avSpread > highestMaxAVSpread.avSpread) {
                            highestMaxAVSpread = openCandle
                        }
                    }
                }
                cumulativeHighMaxAV += highestMaxAVSpread.avSpread
                cumulativeLowMaxAV += lowestMaxAVSpread.avSpread
                highStDevCandles.add(highestMaxAVSpread)
                lowStDevCandles.add(lowestMaxAVSpread)
                total++
            }
            val averageHighMaxAV = cumulativeHighMaxAV / total
            val averageLowMaxAV = cumulativeLowMaxAV / total
            val stDevHigh = calculateStandardDeviation(highStDevCandles, averageHighMaxAV)
            val stDevLow = -calculateStandardDeviation(lowStDevCandles, averageLowMaxAV)
            toReturn.add(
                AVTimeBucketData(
                    averageHighMaxAV + stDevHigh,
                    averageLowMaxAV + stDevLow, timeBucket
                )
            )
        }
        return toReturn
    }

    private fun calculateStandardDeviation(avSpreads: ArrayList<Candle>, averageMaxAVSpread: Double): Double {
        var sum = 0.0
        for (historicalCandle in avSpreads) {
            sum += (historicalCandle.avSpread - averageMaxAVSpread).pow(2.0)
        }
        return sqrt(sum / (avSpreads.size - 1))
    }
}
