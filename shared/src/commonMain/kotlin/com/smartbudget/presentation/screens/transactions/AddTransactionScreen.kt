package com.smartbudget.presentation.screens.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartbudget.presentation.components.BackArrow
import org.koin.compose.koinInject

@Composable
fun AddTransactionScreen(
    onDone: () -> Unit,
    viewModel: TransactionsViewModel = koinInject()
) {
    var merchant by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Новая трата") }, navigationIcon = { BackArrow(onDone) }) }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            OutlinedTextField(
                amount, { amount = it },
                label = { Text("Сумма, ₽") }, singleLine = true,
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                merchant, { merchant = it },
                label = { Text("Где потрачено (магазин)") }, singleLine = true,
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                desc, { desc = it },
                label = { Text("Описание (необязательно)") },
                modifier = Modifier.fillMaxWidth(), maxLines = 2,
                shape = RoundedCornerShape(14.dp)
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    val a = amount.toDoubleOrNull() ?: 0.0
                    viewModel.add(a, merchant, null, desc.ifBlank { null })
                    onDone()
                },
                enabled = amount.isNotBlank() && merchant.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) { Text("Добавить", fontWeight = FontWeight.Medium) }
        }
    }
}
