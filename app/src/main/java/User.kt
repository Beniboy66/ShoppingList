package com.example.shoppinglist

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val productsAdded: Int = 0,
    val productsCompleted: Int = 0
)