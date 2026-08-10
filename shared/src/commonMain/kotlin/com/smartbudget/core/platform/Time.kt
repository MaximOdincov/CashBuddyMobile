package com.smartbudget.core.platform

/**
 * Текущий час (0..23) в системной таймзоне. Платформенная реализация.
 * Android — java.time, iOS — Foundation NSDate.
 */
expect fun currentHourOfDay(): Int

/** Сегодняшняя дата в формате "YYYY-MM" (платформенная реализация). */
expect fun todayYearMonth(): String
