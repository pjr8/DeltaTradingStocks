package data.timebucket.objects

import java.io.Serializable

class AVTimeBucketData(var minHighEntry: Double, var minLowEntry: Double, var timeBucketMinutes: Int) : Serializable