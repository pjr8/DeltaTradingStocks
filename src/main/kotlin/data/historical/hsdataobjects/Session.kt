package me.paulrobinson.data.historical.hsdataobjects

import java.io.Serializable
import java.time.LocalDate

class Session () : Serializable {
    var sessionDate: LocalDate = LocalDate.MIN
    var historicalATR: ATR = ATR(-1.0, LocalDate.MIN)
    var sessionCandles: ArrayList<Candle> = ArrayList()
    var highestAV: Double = 0.0
    var lowestAV: Double = 0.0
}
