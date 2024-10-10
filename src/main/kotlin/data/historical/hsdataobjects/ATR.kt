package me.paulrobinson.data.historical

import java.io.Serializable
import java.time.LocalDate

data class ATR(var atrValue: Double, var day: LocalDate) : Serializable