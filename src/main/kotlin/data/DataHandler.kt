package me.paulrobinson.data

import api.APIHandler
import me.paulrobinson.data.calculator.ATRCalculator
import me.paulrobinson.data.calculator.AvgMaxAVSpreadCalculator
import me.paulrobinson.data.calculator.SessionCalculator
import me.paulrobinson.data.calculator.StDevCalculator
import me.paulrobinson.data.historical.HsData
import java.time.LocalDate
import java.time.ZoneId

class DataHandler {

    //private val INTERNAL_STOCKS = listOf<String>("AAPL", "GOOGL", "AMZN", "MSFT", "TSLA")
    private val INTERNAL_STOCKS = listOf<String>("AAPL")
    private val SESSIONS = 3
    private val LATEST_HISTORICAL_DATE = LocalDate.now().minusDays(1);
    private val ZONE_ID = ZoneId.of("America/New_York")
    val hsDataStocksLoaded = ArrayList<HsData>()

    fun initialize() {
        println("Data Initializing...")
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

        return hsData
    }
//val ticker: String, val sessions: Int, val sessionEndDate: String,
// val sessionBeginDate: String, val sessionBeginDateATR: String, val averageMaxPositiveAVSpread: Double,
// val averageMaxNegativeAVSpread: Double, val standardDeviationPositive:
// Double, val standardDeviationNegative: Double, val historicalAtrList: List<ATR>, val historicalSessionList: List<Session>





}