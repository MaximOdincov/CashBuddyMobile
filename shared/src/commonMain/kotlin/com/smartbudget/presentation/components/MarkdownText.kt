package com.smartbudget.presentation.components

import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp

/**
 * Простой Markdown-рендерер для сообщений AI.
 * Поддерживает: **жирный**, *курсив*, `код`, # заголовки, - списки, переносы строк.
 * Без внешних библиотек.
 */
@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified
) {
    val baseColor = color.takeIf { it != Color.Unspecified } ?: MaterialTheme.colorScheme.onSurface
    val lines = text.lines()
    val annotated = buildAnnotatedString {
        for ((index, line) in lines.withIndex()) {
            val trimmed = line.trimStart()
            // Заголовки
            when {
                trimmed.startsWith("### ") -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)) {
                        append(trimmed.removePrefix("### "))
                    }
                }
                trimmed.startsWith("## ") -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 17.sp)) {
                        append(trimmed.removePrefix("## "))
                    }
                }
                trimmed.startsWith("# ") -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp)) {
                        append(trimmed.removePrefix("# "))
                    }
                }
                // элементы списка: "- " или "* " или "1. "
                trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                    append("•  ")
                    appendInlineMarkdown(trimmed.drop(2))
                }
                Regex("^\\d+\\.\\s").containsMatchIn(trimmed) -> {
                    val num = trimmed.takeWhile { it.isDigit() || it == '.' }
                    append("$num ")
                    appendInlineMarkdown(trimmed.drop(num.length).trimStart())
                }
                else -> appendInlineMarkdown(line)
            }
            if (index < lines.lastIndex) append("\n")
        }
    }
    BasicText(
        text = annotated,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium.copy(color = baseColor)
    )
}

/** Обрабатывает inline-разметку: **bold**, *italic*, `code`. */
private fun androidx.compose.ui.text.AnnotatedString.Builder.appendInlineMarkdown(text: String) {
    var i = 0
    while (i < text.length) {
        when {
            // **bold**
            text.startsWith("**", i) -> {
                val end = text.indexOf("**", i + 2)
                if (end != -1) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(text.substring(i + 2, end)) }
                    i = end + 2
                } else { append(text[i]); i++ }
            }
            // *italic*
            text.startsWith("*", i) && !text.startsWith("**", i) -> {
                val end = text.indexOf("*", i + 1)
                if (end != -1) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Medium)) { append(text.substring(i + 1, end)) }
                    i = end + 1
                } else { append(text[i]); i++ }
            }
            // `code`
            text.startsWith("`", i) -> {
                val end = text.indexOf("`", i + 1)
                if (end != -1) {
                    withStyle(SpanStyle(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)) {
                        append(text.substring(i + 1, end))
                    }
                    i = end + 1
                } else { append(text[i]); i++ }
            }
            else -> { append(text[i]); i++ }
        }
    }
}
