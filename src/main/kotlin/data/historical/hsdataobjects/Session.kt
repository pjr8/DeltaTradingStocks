package me.paulrobinson.data.historical

import java.io.Serializable

data class Session (var sessionDate: String, var historicalATR: ATR, var sessionCandles: ArrayList<Candle>, var highestAV: Double, var lowestAV: Double) : Serializable