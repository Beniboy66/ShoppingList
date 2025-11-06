package com.example.shoppinglist

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val firebaseRepository = FirebaseRepository()
    private lateinit var pendingAdapter: ProductAdapter
    private lateinit var completedAdapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Verificar autenticación
        if (firebaseRepository.getCurrentUser() == null) {
            goToLogin()
            return
        }

        setContentView(R.layout.activity_main)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        setupRecyclerViews()
        observeProducts()

        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            showAddProductDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        // Verificar si el usuario sigue autenticado
        if (firebaseRepository.getCurrentUser() == null) {
            goToLogin()
        }
    }

    private fun setupRecyclerViews() {
        pendingAdapter = ProductAdapter(
            onProductClick = { product -> updateProduct(product) },
            onDeleteClick = { product -> deleteProduct(product) }
        )

        completedAdapter = ProductAdapter(
            onProductClick = { product -> updateProduct(product) },
            onDeleteClick = { product -> deleteProduct(product) }
        )

        findViewById<RecyclerView>(R.id.pendingRecyclerView).apply {
            adapter = pendingAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        findViewById<RecyclerView>(R.id.completedRecyclerView).apply {
            adapter = completedAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    private fun observeProducts() {
        lifecycleScope.launch {
            firebaseRepository.getProducts(false).collect { products ->
                pendingAdapter.submitList(products)
            }
        }

        lifecycleScope.launch {
            firebaseRepository.getProducts(true).collect { products ->
                completedAdapter.submitList(products)
            }
        }
    }

    private fun showAddProductDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_product, null)
        val nameInput = dialogView.findViewById<TextInputEditText>(R.id.productNameInput)
        val quantityInput = dialogView.findViewById<TextInputEditText>(R.id.quantityInput)
        val categoryInput = dialogView.findViewById<TextInputEditText>(R.id.categoryInput)

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Agregar") { _, _ ->
                val name = nameInput.text.toString()
                val quantity = quantityInput.text.toString()
                val category = categoryInput.text.toString()

                if (name.isNotEmpty()) {
                    val product = Product(
                        name = name,
                        quantity = quantity.ifEmpty { "1 unidad" },
                        category = category.ifEmpty { "General" },
                        isCompleted = false
                    )
                    insertProduct(product)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun insertProduct(product: Product) {
        lifecycleScope.launch {
            val result = firebaseRepository.insertProduct(product)
            result.onFailure {
                Toast.makeText(
                    this@MainActivity,
                    "Error al agregar producto: ${it.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateProduct(product: Product) {
        lifecycleScope.launch {
            val result = firebaseRepository.updateProduct(product)
            result.onFailure {
                Toast.makeText(
                    this@MainActivity,
                    "Error al actualizar producto: ${it.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun deleteProduct(product: Product) {
        lifecycleScope.launch {
            val result = firebaseRepository.deleteProduct(product)
            result.onFailure {
                Toast.makeText(
                    this@MainActivity,
                    "Error al eliminar producto: ${it.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}