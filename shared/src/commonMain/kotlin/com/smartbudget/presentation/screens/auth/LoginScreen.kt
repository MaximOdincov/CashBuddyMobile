package com.smartbudget.presentation.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    viewModel: LoginViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    var username by remember { mutableStateOf("demo") }
    var password by remember { mutableStateOf("demo1234") }
    var shareCode by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf(LoginMode.LOGIN) }

    LaunchedEffect(state) {
        if (state is LoginState.Success) onLoggedIn()
    }

    Column(
        Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("💰 CashBuddy", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(8.dp))
        Text("Умный бюджет", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(32.dp))

        // Переключатель режима
        Row {
            FilterChip(mode == LoginMode.LOGIN, { mode = LoginMode.LOGIN }, { Text("Вход") })
            Spacer(Modifier.width(8.dp))
            FilterChip(mode == LoginMode.REGISTER, { mode = LoginMode.REGISTER }, { Text("Регистрация") })
            Spacer(Modifier.width(8.dp))
            FilterChip(mode == LoginMode.CODE, { mode = LoginMode.CODE }, { Text("По коду") })
        }

        Spacer(Modifier.height(16.dp))

        if (mode != LoginMode.CODE) {
            OutlinedTextField(
                value = username, onValueChange = { username = it },
                label = { Text("Логин") }, singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("Пароль") }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            OutlinedTextField(
                value = shareCode, onValueChange = { shareCode = it },
                label = { Text("Share-код (XXXX-XXXX)") }, singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                when (mode) {
                    LoginMode.LOGIN -> viewModel.login(username, password)
                    LoginMode.REGISTER -> viewModel.register(username, password)
                    LoginMode.CODE -> viewModel.loginByCode(shareCode)
                }
            },
            enabled = state !is LoginState.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (state is LoginState.Loading) "Подождите..." else when (mode) {
                LoginMode.LOGIN -> "Войти"
                LoginMode.REGISTER -> "Создать аккаунт"
                LoginMode.CODE -> "Войти по коду"
            })
        }

        (state as? LoginState.Error)?.let {
            Spacer(Modifier.height(12.dp))
            Text(it.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

enum class LoginMode { LOGIN, REGISTER, CODE }
