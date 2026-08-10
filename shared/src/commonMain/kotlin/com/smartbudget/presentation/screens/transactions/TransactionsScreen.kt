package com.smartbudget.presentation.screens.transactions

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
import androidx.compose.ui.unit.sp
import com.smartbudget.core.util.formatMoney
import com.smartbudget.domain.model.TransactionDto
import com.smartbudget.presentation.components.BackArrow
import com.smartbudget.presentation.components.CashBuddyTopBar
import org.koin.compose.koinInject

@Composable
fun TransactionsScreen(
    asTab: Boolean = false,
    key: Int = 0,
    onAddTransaction: () -> Unit = {},
    onNotifications: () -> Unit = {},
    onSettings: () -> Unit = {},
    onBack: () -> Unit = {},
    viewModel: TransactionsViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(key) { viewModel.load() }

    Scaffold(
        topBar = {
            if (asTab) CashBuddyTopBar("Траты", onNotifications, onSettings)
            else TopAppBar(title = { Text("Транзакции") }, navigationIcon = { BackArrow(onBack) })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddTransaction,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Text("+", fontSize = 22.sp, fontWeight = FontWeight.Bold) },
                text = { Text("Добавить") }
            )
        }
    ) { padding ->
        when (val s = state) {
            is TransactionsState.Loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is TransactionsState.Error -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text(s.message, color = MaterialTheme.colorScheme.error) }
            is TransactionsState.Success -> {
                if (s.items.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        Text("Транзакций пока нет", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        Modifier.padding(padding).fillMaxSize(),
                        contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(s.items) { tx -> TxRow(tx) }
                    }
                }
            }
        }
    }
}

@Composable
private fun TxRow(tx: TransactionDto) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), shadowElevation = 1.dp) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(tx.merchant, fontWeight = FontWeight.Medium)
                tx.categoryName?.let {
                    Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                tx.timestamp?.let {
                    Text(it.take(10), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(formatMoney(tx.amount), fontWeight = FontWeight.SemiBold)
        }
    }
}
