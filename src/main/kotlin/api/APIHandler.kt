package api

import io.polygon.kotlin.sdk.DefaultOkHttpClientProvider
import io.polygon.kotlin.sdk.rest.AggregatesDTO
import io.polygon.kotlin.sdk.rest.AggregatesParameters
import io.polygon.kotlin.sdk.rest.PolygonRestClient
import me.paulrobinson.data.historical.hsdataobjects.Candle
import me.paulrobinson.data.historical.hsdataobjects.Session
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

class APIHandler() {
    companion object {
        private const val API_KEY = "6XNAOfJMkLup9fyWCxW4SWPYg34jjJkZ"
        private val httpsProvider: DefaultOkHttpClientProvider = DefaultOkHttpClientProvider()
        val polygonClient: PolygonRestClient = PolygonRestClient(API_KEY, httpsProvider)
    }



    fun getSessionStartDates(ticker: String, sessionEndDate: LocalDate, sessions: Int) : List<LocalDate> {
        val toReturn : MutableList<LocalDate> = ArrayList()
        val sessionsList : MutableList<Int> = listOf(sessions, (sessions + 11)) as MutableList<Int>
        for (days in sessionsList) {
            var sessionBeginDate = sessionEndDate
            var queryCount = 0
            var response: AggregatesDTO

            if (days != 1) {
                while (queryCount < days) {
                    sessionBeginDate = sessionBeginDate.minusDays((days - queryCount).toLong())
                    sessionBeginDate = calculateWeekend(sessionBeginDate)
                    response = getATRData(ticker, sessionBeginDate, sessionEndDate)
                    queryCount = response.queryCount?.toInt()!!
                }
            } else {
                sessionBeginDate = calculateWeekend(sessionBeginDate)
            }
            if (queryCount != days) {
                println("WTF ?")
                throw Exception("Query count does not match days: $queryCount != $days")
            }
            toReturn.add(sessionBeginDate)
        }
        return toReturn
    }

    fun getATRData(ticker: String, start: LocalDate, end: LocalDate): AggregatesDTO {
        val params = AggregatesParameters(
            ticker,
            1, "day",
            start.format(DateTimeFormatter.ISO_DATE),
            end.format(DateTimeFormatter.ISO_DATE),
            false,
            50000,
            "asc"
        )
        return polygonClient.getAggregatesBlocking(params)
    }

    fun getRealTimeSessionFull(ticker: String) : Session {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        val params = AggregatesParameters(
            ticker = ticker,
            timespan = "second",
            fromDate = today,
            toDate = today,
            sort = "asc",
            limit = 50_000
        )
        val results = polygonClient.getAggregatesBlocking(params)
        val historicalSession = Session()
        val candleList = ArrayList<Candle>()
        results.results.forEach { aggregate ->
            val historicalCandle = Candle()
            historicalCandle.low = aggregate.low!!
            historicalCandle.high = aggregate.high!!
            historicalCandle.open = aggregate.open!!
            historicalCandle.close = aggregate.close!!
            historicalCandle.volume = aggregate.volume!!.toInt()
            historicalCandle.datedCandle = LocalDateTime.ofInstant(Instant.ofEpochMilli(aggregate.timestampMillis!!),
                ZoneId.of("America/New_York"))
            candleList.add(historicalCandle)
        }
        historicalSession.sessionCandles = candleList
        return historicalSession
    }


    fun getRealTimeSessionPartial(ticker: String) : List<Candle> {
        val candleList = ArrayList<Candle>()
        try {
            val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
            val params = AggregatesParameters(
                ticker = ticker,
                timespan = "second",
                fromDate = today,
                toDate = today,
                sort = "desc",
                limit = 15
            )
            val results = polygonClient.getAggregatesBlocking(params)
            results.results.forEach { aggregate ->
                val historicalCandle = Candle()
                historicalCandle.low = aggregate.low!!
                historicalCandle.high = aggregate.high!!
                historicalCandle.open = aggregate.open!!
                historicalCandle.close = aggregate.close!!
                historicalCandle.volume = aggregate.volume!!.toInt()
                historicalCandle.datedCandle = LocalDateTime.ofInstant(Instant.ofEpochMilli(aggregate.timestampMillis!!),
                    ZoneId.of("America/New_York"))
                candleList.add(historicalCandle)
            }
        } catch (e: Exception) {
            println("Error getting real time session partial: $e")
        }
        return candleList
    }

    private fun calculateWeekend(date : LocalDate) : LocalDate {
        if (date.dayOfWeek == DayOfWeek.SUNDAY) {
            return date.minusDays(2)
        } else if (date.dayOfWeek == DayOfWeek.SATURDAY) {
            return date.minusDays(1)
        }
        return date
    }
}