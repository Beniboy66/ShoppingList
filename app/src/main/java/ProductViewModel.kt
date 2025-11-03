package com.example.shoppinglist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ProductViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProductRepository
    val pendingProducts: LiveData<List<Product>>
    val completedProducts: LiveData<List<Product>>
    val pendingCount: LiveData<Int>
    val completedCount: LiveData<Int>

    init {
        val productDao = AppDatabase.getDatabase(application).productDao()
        repository = ProductRepository(productDao)
        pendingProducts = repository.pendingProducts
        completedProducts = repository.completedProducts
        pendingCount = repository.pendingCount
        completedCount = repository.completedCount
    }

    fun insert(product: Product) = viewModelScope.launch {
        repository.insert(product)
    }

    fun update(product: Product) = viewModelScope.launch {
        repository.update(product)
    }

    fun delete(product: Product) = viewModelScope.launch {
        repository.delete(product)
    }

    fun deleteAllCompleted() = viewModelScope.launch {
        repository.deleteAllCompleted()
    }
}