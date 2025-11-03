package com.example.shoppinglist

import androidx.lifecycle.LiveData

class ProductRepository(private val productDao: ProductDao) {

    val pendingProducts: LiveData<List<Product>> = productDao.getPendingProducts()
    val completedProducts: LiveData<List<Product>> = productDao.getCompletedProducts()
    val pendingCount: LiveData<Int> = productDao.getPendingCount()
    val completedCount: LiveData<Int> = productDao.getCompletedCount()

    suspend fun insert(product: Product) {
        productDao.insert(product)
    }

    suspend fun update(product: Product) {
        productDao.update(product)
    }

    suspend fun delete(product: Product) {
        productDao.delete(product)
    }

    suspend fun deleteAllCompleted() {
        productDao.deleteAllCompleted()
    }
}