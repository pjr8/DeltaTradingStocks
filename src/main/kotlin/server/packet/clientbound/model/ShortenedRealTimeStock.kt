package me.paulrobinson.server.packet.clientbound.model

import me.paulrobinson.data.historical.hsdataobjects.Sentiment

data class ShortenedRealTimeStock(private val ticker: String, private val price: Double,
                                  private val avSpread: Double, private val minimumEntry: String,
                                  private val sentiment: Sentiment)