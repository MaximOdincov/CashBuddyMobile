package com.smartbudget.core.platform

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter

actual fun currentHourOfDay(): Int = try {
    val formatter = NSDateFormatter()
    formatter.setDateFormat("HH")
    formatter.stringFromDate(NSDate()).toInt()
} catch (_: Exception) { 12 }

actual fun todayYearMonth(): String = try {
    val formatter = NSDateFormatter()
    formatter.setDateFormat("yyyy-MM")
    formatter.stringFromDate(NSDate())
} catch (_: Exception) { "2026-08" }
