package com.example.shoppinglist

data class Group(
    val groupId: String = "",
    val name: String = "",
    val members: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)