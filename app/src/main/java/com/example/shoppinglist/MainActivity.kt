package com.example.shoppinglist

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: ProductViewModel
    private lateinit var pendingAdapter: ProductAdapter
    private lateinit var completedAdapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        viewModel = ViewModelProvider(this)[ProductViewModel::class.java]

        setupRecyclerViews()
        observeProducts()

        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            showAddProductDialog()
        }
    }

    private fun setupRecyclerViews() {
        pendingAdapter = ProductAdapter(
            onProductClick = { product -> viewModel.update(product) },
            onDeleteClick = { product -> viewModel.delete(product) }
        )

        completedAdapter = ProductAdapter(
            onProductClick = { product -> viewModel.update(product) },
            onDeleteClick = { product -> viewModel.delete(product) }
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
        viewModel.pendingProducts.observe(this) { products ->
            pendingAdapter.submitList(products)
        }

        viewModel.completedProducts.observe(this) { products ->
            completedAdapter.submitList(products)
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
                        category = category.ifEmpty { "General" }
                    )
                    viewModel.insert(product)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
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
}