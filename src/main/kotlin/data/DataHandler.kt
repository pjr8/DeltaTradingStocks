package me.paulrobinson.data

import api.APIHandler
import data.timebucket.AVTBCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import me.paulrobinson.Main
import me.paulrobinson.data.calculator.ATRCalculator
import me.paulrobinson.data.calculator.AvgMaxAVSpreadCalculator
import me.paulrobinson.data.calculator.SessionCalculator
import me.paulrobinson.data.calculator.StDevCalculator
import me.paulrobinson.data.historical.HsData
import java.time.LocalDate
import java.time.ZoneId

class DataHandler() : Runnable {

    private val SESSIONS = 10
    private val LATEST_HISTORICAL_DATE = LocalDate.now().minusDays(1);
    private val ZONE_ID = ZoneId.of("America/New_York")
    val hsDataStocksLoaded = HashSet<HsData>()

    override fun run() {
        println("Data Initializing...")

        val results = Main.startupTickers.map { ticker ->
            CoroutineScope(Dispatchers.Default).async {
                try {
                    createData(ticker)
                } catch (e: Exception) {
                    println("Error processing $ticker: ${e.message}")
                    e.printStackTrace()
                }
            }
        }

        // Await all coroutines to complete
        runBlocking {
            val finished = results.awaitAll() // Awaits all Deferreds in the list
            println("Data Initialized")
        }
    }

    fun addNewData(stock: String) {
        CoroutineScope(Dispatchers.Default).async {
            try {
                createData(stock)
            } catch (e: Exception) {
                println("Error processing $stock: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun createData(stock: String) {
        hsDataStocksLoaded.add(createHistoricalData(stock))
    }


    private fun createHistoricalData(stock: String) : HsData {
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
        hsData.timeBucketList = AVTBCalculator().calculate(hsData)


        Main.websocket.addStock(stock.uppercase())
        Main.realTimeData.addRealTimeStock(hsData)


        return hsData
    }

}