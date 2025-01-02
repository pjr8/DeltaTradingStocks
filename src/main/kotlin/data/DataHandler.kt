package me.paulrobinson.data

import api.APIHandler
import io.polygon.kotlin.sdk.rest.PolygonRestClient
import me.paulrobinson.data.calculator.ATRCalculator
import me.paulrobinson.data.calculator.AvgMaxAVSpreadCalculator
import me.paulrobinson.data.calculator.SessionCalculator
import me.paulrobinson.data.calculator.StDevCalculator
import me.paulrobinson.data.historical.HsData
import java.time.LocalDate
import java.time.ZoneId

class DataHandler() : Runnable {

    //private val INTERNAL_STOCKS = listOf<String>("AAPL", "GOOGL", "AMZN", "MSFT", "TSLA")

    private val INTERNAL_STOCKS = HashSet<String>()// listOf<String>("AAPL")
    private val SESSIONS = 10
    private val LATEST_HISTORICAL_DATE = LocalDate.now().minusDays(1);
    private val ZONE_ID = ZoneId.of("America/New_York")
    val hsDataStocksLoaded = HashSet<HsData>()

    override fun run() {
        println("Data Initializing...")
        INTERNAL_STOCKS.add("AAPL")
        for (stock in INTERNAL_STOCKS) {
            hsDataStocksLoaded.add(createHistoricalData(stock))
        }
        println("Data Initialized")
    }

    fun createHistoricalData(stock: String) : HsData {
        val beginDates = APIHandler().getSessionStartDates(
            stock, LATEST_HISTORICAL_DATE,
            SESSIONS
        )

        val sessionEndDate = LATEST_HISTORICAL_DATE
        val sessionBeginDate = beginDates[0]
        val sessionBeginDateATR = beginDates[1]
        val historicalAtrList = ATRCalculator().calculate(stock, sessionBeginDateATR, sessionEndDate, SESSIONS)
        val historicalSessionList = SessionCalculator().calculate(
            stock, sessionBeginDate, sessionEndDate, historicalAtrList)
        val averageMaxPositiveAVSpread = AvgMaxAVSpreadCalculator().calculateHigh(historicalSessionList)
        val averageMaxNegativeAVSpread = AvgMaxAVSpreadCalculator().calculateLow(historicalSessionList)
        val standardDeviationPositive = StDevCalculator().calculateHigh(historicalSessionList)
        val standardDeviationNegative = StDevCalculator().calculateLow(historicalSessionList)

        val hsData = HsData(stock, SESSIONS, sessionEndDate)

        hsData.sessionBeginDate = sessionBeginDate
        hsData.sessionBeginDateATR = sessionBeginDateATR
        hsData.averageMaxPositiveAVSpread = averageMaxPositiveAVSpread
        hsData.averageMaxNegativeAVSpread = averageMaxNegativeAVSpread
        hsData.standardDeviationPositive = standardDeviationPositive
        hsData.standardDeviationNegative = standardDeviationNegative
        hsData.historicalAtrList = historicalAtrList
        hsData.historicalSessionList = historicalSessionList

/*        println("Historical Data for $stock created")
        println("Session Begin Date: $sessionBeginDate")
        println("Session Begin Date ATR: $sessionBeginDateATR")
        println("Average Max Positive AV Spread: $averageMaxPositiveAVSpread")
        println("Average Max Negative AV Spread: $averageMaxNegativeAVSpread")
        println("Standard Deviation Positive: $standardDeviationPositive")
        println("Standard Deviation Negative: $standardDeviationNegative")
        println("Historical ATR List: $historicalAtrList")
        println("Historical Session List: $historicalSessionList")*/

/*        historicalSessionList.forEach {
            println("Session Date: ${it.sessionDate}")
            println("Session ATR: ${it.historicalATR.atrValue}")
            //println("Session Candles: ${it.sessionCandles}")
            println("Session Candles Size: ${it.sessionCandles.size}")
            println("Session Highest AV: ${it.highestAV}")
            println("Session Lowest AV: ${it.lowestAV}")
        }*/
        return hsData
    }

}