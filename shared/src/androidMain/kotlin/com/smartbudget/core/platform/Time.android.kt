package com.smartbudget.core.platform

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

actual fun currentHourOfDay(): Int = try {
    LocalDateTime.now().hour
} catch (_: Exception) { 12 }

actual fun todayYearMonth(): String = try {
    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
} catch (_: Exception) { "2026-08" }
