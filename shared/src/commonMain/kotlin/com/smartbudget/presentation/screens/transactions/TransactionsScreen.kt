package com.smartbudget.presentation.screens.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartbudget.domain.model.TransactionDto
import org.koin.compose.koinInject

@Composable
fun TransactionsScreen(viewModel: TransactionsViewModel = koinInject()) {
    val state by viewModel.state.collectAsState()
    Scaffold(topBar = { TopAppBar(title = { Text("Транзакции") }) }) { padding ->
        when (val s = state) {
            is TransactionsState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is TransactionsState.Error -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(s.message, color = MaterialTheme.colorScheme.error)
            }
            is TransactionsState.Success -> LazyColumn(Modifier.padding(padding).fillMaxSize()) {
                items(s.items) { tx -> TxRow(tx) }
            }
        }
    }
}

@Composable
private fun TxRow(tx: TransactionDto) {
    Row(Modifier.fillMaxWidth().padding(16.dp, 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(tx.merchant, fontWeight = FontWeight.Medium)
            tx.categoryName?.let {
                Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            tx.timestamp?.let {
                Text(it.take(10), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text("${tx.amount} ₽", fontWeight = FontWeight.SemiBold)
    }
    HorizontalDivider()
}
