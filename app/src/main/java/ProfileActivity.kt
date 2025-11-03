package com.example.shoppinglist

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.MaterialToolbar

class ProfileActivity : AppCompatActivity() {

    private lateinit var viewModel: ProductViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        viewModel = ViewModelProvider(this)[ProductViewModel::class.java]

        val addedCount = findViewById<TextView>(R.id.addedCount)
        val completedCount = findViewById<TextView>(R.id.completedCount)

        viewModel.pendingCount.observe(this) { count ->
            addedCount.text = count.toString()
        }

        viewModel.completedCount.observe(this) { count ->
            completedCount.text = count.toString()
        }

        findViewById<Button>(R.id.clearCompletedButton).setOnClickListener {
            viewModel.deleteAllCompleted()
        }

        findViewById<Button>(R.id.logoutButton).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }
    }
}