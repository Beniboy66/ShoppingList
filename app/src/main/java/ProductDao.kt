package com.example.shoppinglist

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE isCompleted = 0 ORDER BY id DESC")
    fun getPendingProducts(): LiveData<List<Product>>

    @Query("SELECT * FROM products WHERE isCompleted = 1 ORDER BY id DESC")
    fun getCompletedProducts(): LiveData<List<Product>>

    @Query("SELECT COUNT(*) FROM products WHERE isCompleted = 0")
    fun getPendingCount(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM products WHERE isCompleted = 1")
    fun getCompletedCount(): LiveData<Int>

    @Insert
    suspend fun insert(product: Product)

    @Update
    suspend fun update(product: Product)

    @Delete
    suspend fun delete(product: Product)

    @Query("DELETE FROM products WHERE isCompleted = 1")
    suspend fun deleteAllCompleted()
}