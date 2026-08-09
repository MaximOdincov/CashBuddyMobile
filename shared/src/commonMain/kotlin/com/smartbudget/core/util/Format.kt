package com.smartbudget.core.util

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Форматирование денег: 18450.0 → "18 450 ₽".
 * Группировка по разрядам, без дробной части для целых сумм.
 */
fun formatMoney(value: Double): String {
    val rounded = kotlin.math.round(value).toLong()
    val sb = StringBuilder()
    val str = rounded.toString()
    val negative = rounded < 0
    val digits = if (negative) str.drop(1) else str
    for ((i, c) in digits.reversed().withIndex()) {
        if (i > 0 && i % 3 == 0) sb.append(' ')
        sb.append(c)
    }
    val grouped = sb.reverse().toString()
    return (if (negative) "-$grouped" else grouped) + " ₽"
}

/** Короткий формат денег: 120000 → "120 тыс", 1500000 → "1,5 млн". */
fun formatMoneyShort(value: Double): String {
    val abs = kotlin.math.abs(value)
    return when {
        abs >= 1_000_000 -> {
            val m = value / 1_000_000.0
            val s = if (m % 1.0 == 0.0) m.toInt().toString()
            else "${(m * 10).toInt() / 10.0}"
            "$s млн ₽"
        }
        abs >= 1000 -> "${((value / 1000).toInt())} тыс ₽"
        else -> formatMoney(value)
    }
}

/** Процент: 84.5 → "85%", 0.0 → "0%". */
fun formatPercent(value: Double): String = "${kotlin.math.round(value)}%"

/** Приветствие по времени суток: «Доброе утро/день/вечер/ночи». */
fun greeting(): String {
    val hour = currentHourOfDay()
    return when (hour) {
        in 5..11 -> "Доброе утро"
        in 12..17 -> "Добрый день"
        in 18..22 -> "Добрый вечер"
        else -> "Доброй ночи"
    }
}

/** Текущий час (0..23) в системной таймзоне. */
fun currentHourOfDay(): Int = try {
    Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .hour
} catch (_: Exception) { 12 }

/** Название месяца по строке периода "YYYY-MM" → "Август". */
fun monthName(period: String): String {
    val month = period.substringAfter('-').toIntOrNull() ?: return period
    return listOf(
        "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
        "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
    ).getOrElse(month - 1) { period }
}
