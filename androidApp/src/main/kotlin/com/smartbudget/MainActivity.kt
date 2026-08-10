package com.smartbudget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.smartbudget.di.initKoin
import com.smartbudget.presentation.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Edge-to-edge: статус-бар прозрачный, стиль значков выбирается автоматически.
        enableEdgeToEdge()
        initKoin()
        setContent { App() }
    }
}
