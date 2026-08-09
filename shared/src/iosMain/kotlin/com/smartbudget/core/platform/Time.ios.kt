package com.smartbudget.core.platform

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterFullStyle

actual fun currentHourOfDay(): Int = try {
    val formatter = NSDateFormatter()
    formatter.setDateFormat("HH")
    formatter.stringFromDate(NSDate()).toInt()
} catch (_: Exception) { 12 }
