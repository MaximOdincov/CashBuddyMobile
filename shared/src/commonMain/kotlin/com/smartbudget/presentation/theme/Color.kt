package com.smartbudget.presentation.theme

import androidx.compose.ui.graphics.Color

// === Brand palette: «спокойный premium fintech» ===
// Акцент главного действия — фирменный жёлтый Т-Банка (НЕ янтарный).
// Янтарный (#FF9800) убран — заменён на мягкий персиковый «внимание».

// Primary — жёлтый Т-Банка
val TbankYellow = Color(0xFFFFDD2D)
val TbankYellowPressed = Color(0xFFF5CC00)
val TbankYellowContainer = Color(0xFFFFF6CC)
val TbankYellowOnContainer = Color(0xFF4A3F00)

// Накопления / успех — зелёный
val SavingsGreen = Color(0xFF00C808)
val SavingsGreenDark = Color(0xFF1FA22B)
val SavingsGreenContainer = Color(0xFFD1FAE5)

// Внимание — мягкий персиковый (НЕ кислотный янтарь)
val WarnPeach = Color(0xFFF4A340)
val WarnPeachContainer = Color(0xFFFFE8C7)

// Превышение — красный (только при реальной проблеме)
val DangerRed = Color(0xFFFF3B30)
val DangerRedContainer = Color(0xFFFFDAD6)

// Нейтрально / свободные деньги — синий
val NeutralBlue = Color(0xFF0A84FF)
val NeutralBlueContainer = Color(0xFFD1E9FF)

// Background
val LightBg = Color(0xFFF7F8FA)
val DarkBg = Color(0xFF0E0E10)
val LightSurface = Color(0xFFFFFFFF)
val DarkSurface = Color(0xFF1A1A1E)
val LightSurfaceVariant = Color(0xFFEFF1F4)
val DarkSurfaceVariant = Color(0xFF242428)

// Text
val TextPrimaryLight = Color(0xFF1A1A1E)
val TextPrimaryDark = Color(0xFFF1F3F5)
val TextSecondaryLight = Color(0xFF6B7280)
val TextSecondaryDark = Color(0xFF9CA3AF)

// Категории — мягкие закреплённые цвета
val CatFood = Color(0xFF34C759)        // продукты — зелёный
val CatBills = Color(0xFF5AC8FA)       // коммуналка — голубой (был оранжевый)
val CatFun = Color(0xFFFF2D55)         // развлечения — розово-красный
val CatTransport = Color(0xFF0A84FF)   // транспорт — синий
val CatHealth = Color(0xFFAF52DE)      // здоровье — фиолетовый
val CatSavings = Color(0xFF00C7BE)     // накопления — бирюзовый
val CatOther = Color(0xFF8E8E93)       // прочее — серый

/**
 * Цвет категории по её HEX (приходит с сервера) или по имени.
 * Сервер отдаёт цвета seed'а, но они оранжевые — здесь мы их переопределяем
 * на согласованную мягкую палитру.
 */
fun categoryColor(name: String): Color = when {
    name.contains("продукт", ignoreCase = true) -> CatFood
    name.contains("коммунал", ignoreCase = true) -> CatBills
    name.contains("развлеч", ignoreCase = true) -> CatFun
    name.contains("транспорт", ignoreCase = true) -> CatTransport
    name.contains("здоров", ignoreCase = true) -> CatHealth
    name.contains("накоп", ignoreCase = true) -> CatSavings
    else -> CatOther
}

/** Безопасный парсинг HEX-цвета из строки вида "#RRGGBB" (fallback на серый). */
fun parseHexColor(hex: String?): Color {
    if (hex.isNullOrBlank()) return CatOther
    return try {
        val clean = hex.removePrefix("#")
        val r = clean.substring(0, 2).toInt(16)
        val g = clean.substring(2, 4).toInt(16)
        val b = clean.substring(4, 6).toInt(16)
        Color(r, g, b)
    } catch (_: Exception) {
        CatOther
    }
}
