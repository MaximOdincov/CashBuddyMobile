package com.smartbudget.core.platform

import java.time.LocalDateTime

actual fun currentHourOfDay(): Int = try {
    LocalDateTime.now().hour
} catch (_: Exception) { 12 }
