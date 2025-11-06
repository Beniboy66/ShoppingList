package com.example.shoppinglist

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.PropertyName

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String = "",
    val quantity: String = "",
    val category: String = "",
    @get:PropertyName("completed")
    @set:PropertyName("completed")
    var isCompleted: Boolean = false
)