package me.paulrobinson.data.historical

import me.paulrobinson.data.historical.hsdataobjects.ATR
import me.paulrobinson.data.historical.hsdataobjects.Session
import java.time.LocalDate

class HsData(val ticker: String, val sessions: Int, val sessionEndDate : LocalDate) {
    var sessionBeginDate: LocalDate = LocalDate.MIN
    var sessionBeginDateATR: LocalDate = LocalDate.MIN
    var averageMaxPositiveAVSpread = 0.0
    var averageMaxNegativeAVSpread = 0.0
    var standardDeviationPositive = 0.0
    var standardDeviationNegative = 0.0
    var historicalAtrList: List<ATR> = ArrayList(0)
    var historicalSessionList: List<Session>? = ArrayList(0)
    
    override fun toString(): String {
        return "HsData(ticker='$ticker', sessions=$sessions, sessionEndDate=$sessionEndDate, " +
               "sessionBeginDate=$sessionBeginDate, sessionBeginDateATR=$sessionBeginDateATR, " +
               "averageMaxPositiveAVSpread=$averageMaxPositiveAVSpread, averageMaxNegativeAVSpread=$averageMaxNegativeAVSpread, " +
               "standardDeviationPositive=$standardDeviationPositive, standardDeviationNegative=$standardDeviationNegative, " +
               "historicalAtrList=$historicalAtrList, historicalSessionList=$historicalSessionList)"
    }
}