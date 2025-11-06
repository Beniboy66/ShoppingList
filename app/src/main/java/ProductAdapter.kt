package com.example.shoppinglist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class ProductAdapter(
    private val onProductClick: (Product) -> Unit,
    private val onDeleteClick: (Product) -> Unit
) : ListAdapter<Product, ProductAdapter.ProductViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
        private val productName: TextView = itemView.findViewById(R.id.productName)
        private val productQuantity: TextView = itemView.findViewById(R.id.productQuantity)
        private val productCategory: TextView = itemView.findViewById(R.id.productCategory)
        private val productCreator: TextView = itemView.findViewById(R.id.productCreator)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)

        fun bind(product: Product) {
            productName.text = product.name
            productQuantity.text = product.quantity
            productCategory.text = product.category
            checkBox.isChecked = product.isCompleted

            // Mostrar quién agregó el producto
            if (product.createdByEmail.isNotEmpty()) {
                productCreator.visibility = View.VISIBLE
                productCreator.text = "Agregado por: ${product.createdByEmail}"
            } else {
                productCreator.visibility = View.GONE
            }

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                onProductClick(product.copy(isCompleted = isChecked))
            }

            deleteButton.setOnClickListener {
                onDeleteClick(product)
            }
        }
    }

    class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }
}