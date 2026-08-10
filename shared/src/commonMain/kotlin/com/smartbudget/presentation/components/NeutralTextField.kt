package com.smartbudget.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable

/**
 * Нейтральные цвета для TextField — БЕЗ жёлтого.
 * Курсор и focused-border используют onSurface (тёмный/белый), не primary (жёлтый).
 */
@Composable
fun neutralTextFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
    cursorColor = MaterialTheme.colorScheme.onSurface,
    focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
)
