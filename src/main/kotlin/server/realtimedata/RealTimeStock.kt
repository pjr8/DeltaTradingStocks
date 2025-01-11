package me.paulrobinson.server.realtimedata

import data.timebucket.objects.AVTimeBucketData
import me.paulrobinson.data.historical.hsdataobjects.Session

class RealTimeStock(val ticker: String, val lastDayAtr: Double, val avTimeBucketDataList: List<AVTimeBucketData>) {
    var price: Double = 0.0
    var av: Double = 0.0
    var avTimeBucketData: String = "AWAITING UPDATE"
    var session: Session? = null
}