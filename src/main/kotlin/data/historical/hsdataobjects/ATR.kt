package me.paulrobinson.data.historical.hsdataobjects

import java.io.Serializable
import java.time.LocalDate

data class ATR(var atrValue: Double, var day: LocalDate) : Serializable