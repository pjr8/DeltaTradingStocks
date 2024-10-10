package me.paulrobinson.data.historical

import java.time.LocalDateTime

data class Candle(val high: Double, val low: Double, val open: Double, val close: Double, val volume: Int, val vwapValue: Double, val avSpread: Double, val datedCandle: LocalDateTime)