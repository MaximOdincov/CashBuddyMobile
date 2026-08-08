package com.smartbudget

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
