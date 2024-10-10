package api

import io.polygon.kotlin.sdk.DefaultOkHttpClientProvider
import io.polygon.kotlin.sdk.rest.AggregatesDTO
import io.polygon.kotlin.sdk.rest.AggregatesParameters
import io.polygon.kotlin.sdk.rest.PolygonRestClient
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

class APIHandler {
    private val API_KEY = "c0pIklBuYjU6OQ5FScAONs7PAhQwbn9p"
    private val httpsProvider: DefaultOkHttpClientProvider = DefaultOkHttpClientProvider()
    private val polygonClient: PolygonRestClient = PolygonRestClient(API_KEY, httpsProvider)

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

    private fun calculateWeekend(date : LocalDate) : LocalDate {
        if (date.dayOfWeek == DayOfWeek.SUNDAY) {
            return date.minusDays(2)
        } else if (date.dayOfWeek == DayOfWeek.SATURDAY) {
            return date.minusDays(1)
        }
        return date
    }
}