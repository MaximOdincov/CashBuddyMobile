package com.smartbudget.presentation.screens.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject

@Composable
fun AddTransactionScreen(
    onDone: () -> Unit,
    viewModel: TransactionsViewModel = koinInject()
) {
    var merchant by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }

    Scaffold(topBar = { TopAppBar(title = { Text("Новая транзакция") }) }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            OutlinedTextField(amount, { amount = it }, label = { Text("Сумма, ₽") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(merchant, { merchant = it }, label = { Text("Где потрачено (магазин)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(desc, { desc = it }, label = { Text("Описание (необязательно)") }, modifier = Modifier.fillMaxWidth(), maxLines = 2)
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    saving = true
                    viewModel.add(amount, merchant, null, desc.ifBlank { null })
                    onDone()
                },
                enabled = !saving && amount.isNotBlank() && merchant.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (saving) "Сохранение..." else "Добавить") }
        }
    }
}
