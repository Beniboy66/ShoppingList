package com.example.shoppinglist

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private val firebaseRepository = FirebaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val addedCount = findViewById<TextView>(R.id.addedCount)
        val completedCount = findViewById<TextView>(R.id.completedCount)
        val userEmail = findViewById<TextView>(R.id.userEmail)

        // Mostrar email del usuario
        userEmail.text = firebaseRepository.getCurrentUser()?.email ?: "usuario@ejemplo.com"

        // Observar estadísticas
        lifecycleScope.launch {
            firebaseRepository.getUserStats().collect { (added, completed) ->
                addedCount.text = added.toString()
                completedCount.text = completed.toString()
            }
        }

        findViewById<Button>(R.id.clearCompletedButton).setOnClickListener {
            lifecycleScope.launch {
                firebaseRepository.deleteAllCompleted()
            }
        }

        findViewById<Button>(R.id.logoutButton).setOnClickListener {
            firebaseRepository.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }
    }
}