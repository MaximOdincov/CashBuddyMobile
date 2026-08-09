package com.smartbudget.core.platform

import platform.Foundation.NSCalendar
import platform.Foundation.NSDate
import platform.Foundation.NSHourCalendarUnit
import platform.Foundation.currentCalendar

actual fun currentHourOfDay(): Int = try {
    val components = NSCalendar.currentCalendar()
        .components(NSHourCalendarUnit, fromDate = NSDate())
    components.hour.toInt()
} catch (_: Exception) { 12 }
