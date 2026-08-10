package com.smartbudget.presentation.screens.category

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartbudget.core.util.formatMoney
import com.smartbudget.domain.model.TransactionDto
import com.smartbudget.presentation.components.BackArrow
import com.smartbudget.presentation.screens.transactions.TransactionsState
import com.smartbudget.presentation.screens.transactions.TransactionsViewModel
import org.koin.compose.koinInject

@Composable
fun CategoryDetailScreen(
    categoryId: Long,
    categoryName: String = "",
    onBack: () -> Unit = {},
    viewModel: TransactionsViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(categoryId) { viewModel.load(categoryId = categoryId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(categoryName.ifBlank { "Категория" }) },
                navigationIcon = { BackArrow(onBack) }
            )
        }
    ) { padding ->
        when (val s = state) {
            is TransactionsState.Loading ->
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is TransactionsState.Error ->
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(s.message, color = MaterialTheme.colorScheme.error)
                }
            is TransactionsState.Success -> {
                LazyColumn(Modifier.padding(padding).fillMaxSize()) {
                    if (s.items.isEmpty()) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("Нет транзакций", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    items(s.items) { tx -> TxRow(tx) }
                }
            }
        }
    }
}

@Composable
private fun TxRow(tx: TransactionDto) {
    Surface(Modifier.fillMaxWidth().padding(16.dp, 4.dp), shape = RoundedCornerShape(12.dp), shadowElevation = 1.dp) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(tx.merchant, fontWeight = FontWeight.Medium)
                tx.timestamp?.let {
                    Text(it.take(10), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(formatMoney(tx.amount), fontWeight = FontWeight.SemiBold)
        }
    }
}
