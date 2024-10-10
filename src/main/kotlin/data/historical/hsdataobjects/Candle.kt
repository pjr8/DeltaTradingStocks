package me.paulrobinson.data.historical.hsdataobjects

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class Candle() {
    var high: Double = 0.0
    var low: Double = 0.0
    var open: Double = 0.0
    var close: Double = 0.0
    var volume: Int = 0
    var vwapValue: Double = 0.0
    var avSpread: Double = 0.0
    var datedCandle: LocalDateTime = LocalDateTime.of(LocalDate.of(2000, 1, 1), LocalTime.MIN)
}