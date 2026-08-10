package com.smartbudget.presentation.screens.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartbudget.data.repository.BudgetRepository
import com.smartbudget.domain.model.CategoryView
import com.smartbudget.presentation.components.BackArrow
import com.smartbudget.presentation.components.neutralTextFieldColors
import com.smartbudget.presentation.theme.categoryColor
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun AddTransactionScreen(
    onDone: () -> Unit,
    viewModel: TransactionsViewModel = koinInject(),
    budgetRepository: BudgetRepository = koinInject()
) {
    var merchant by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<CategoryView?>(null) }
    var categories by remember { mutableStateOf<List<CategoryView>>(emptyList()) }
    val scope = rememberCoroutineScope()

    // Загружаем доступные категории один раз
    LaunchedEffect(Unit) {
        scope.launch {
            try { categories = budgetRepository.categories() } catch (_: Exception) {}
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Новая трата") }, navigationIcon = { BackArrow(onDone) }) }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            OutlinedTextField(
                amount, { amount = it },
                label = { Text("Сумма, ₽") }, singleLine = true,
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = neutralTextFieldColors()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                merchant, { merchant = it },
                label = { Text("Где потрачено (магазин)") }, singleLine = true,
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = neutralTextFieldColors()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                desc, { desc = it },
                label = { Text("Описание (необязательно)") },
                modifier = Modifier.fillMaxWidth(), maxLines = 2,
                shape = RoundedCornerShape(14.dp), colors = neutralTextFieldColors()
            )

            Spacer(Modifier.height(16.dp))
            Text("Категория", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(
                if (selectedCategory == null) "Авто-классификация по названию магазина"
                else "Выбрана: ${selectedCategory?.name}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            // Горизонтальный скролл категорий
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories.filter { !it.isSavings }) { cat ->
                    val selected = selectedCategory?.id == cat.id
                    val color = categoryColor(cat.name)
                    Surface(
                        onClick = { selectedCategory = if (selected) null else cat },
                        shape = RoundedCornerShape(20.dp),
                        color = if (selected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                        border = if (selected) androidx.compose.foundation.BorderStroke(2.dp, color) else null
                    ) {
                        Row(
                            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier.size(24.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) { Text(cat.icon, fontSize = 13.sp) }
                            Spacer(Modifier.width(6.dp))
                            Text(cat.name, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))
            Button(
                onClick = {
                    val a = amount.toDoubleOrNull() ?: 0.0
                    viewModel.add(a, merchant, selectedCategory?.id?.toString(), desc.ifBlank { null })
                    onDone()
                },
                enabled = amount.isNotBlank() && merchant.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) { Text("Добавить", fontWeight = FontWeight.Medium) }
        }
    }
}
