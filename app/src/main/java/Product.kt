package com.example.shoppinglist

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,  // CAMBIAR val por var

    @get:Exclude
    var documentId: String = "",  // CAMBIAR val por var

    @get:PropertyName("name")
    @set:PropertyName("name")
    var name: String = "",

    @get:PropertyName("quantity")
    @set:PropertyName("quantity")
    var quantity: String = "",

    @get:PropertyName("category")
    @set:PropertyName("category")
    var category: String = "",

    @get:PropertyName("completed")
    @set:PropertyName("completed")
    var isCompleted: Boolean = false,

    @get:PropertyName("timestamp")
    @set:PropertyName("timestamp")
    var timestamp: Long = 0L,

    @get:PropertyName("createdBy")
    @set:PropertyName("createdBy")
    var createdBy: String = "",

    @get:PropertyName("createdByEmail")
    @set:PropertyName("createdByEmail")
    var createdByEmail: String = "",

    @get:PropertyName("completedBy")
    @set:PropertyName("completedBy")
    var completedBy: String? = null,

    @get:PropertyName("completedAt")
    @set:PropertyName("completedAt")
    var completedAt: Long? = null
) {
    constructor() : this(0, "", "", "", "", false, 0L, "", "", null, null)
}