package com.example.shoppinglist

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button

    private val firebaseRepository = FirebaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Verificar si ya hay un usuario logueado
        if (firebaseRepository.getCurrentUser() != null) {
            goToMainActivity()
            return
        }

        setContentView(R.layout.activity_login)

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerButton)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        registerButton.setOnClickListener {
            showRegisterDialog()
        }
    }

    private fun loginUser(email: String, password: String) {
        // Deshabilitar botón y mostrar progreso
        loginButton.isEnabled = false
        loginButton.text = "Iniciando sesión..."

        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    firebaseRepository.loginUser(email, password)
                }

                // Volver al hilo principal para actualizar UI
                withContext(Dispatchers.Main) {
                    result.onSuccess {
                        Toast.makeText(
                            this@LoginActivity,
                            "Inicio de sesión exitoso",
                            Toast.LENGTH_SHORT
                        ).show()
                        goToMainActivity()
                    }.onFailure { exception ->
                        loginButton.isEnabled = true
                        loginButton.text = "Iniciar Sesión"

                        val errorMessage = when {
                            exception.message?.contains("password") == true ->
                                "Contraseña incorrecta"
                            exception.message?.contains("user") == true ->
                                "Usuario no encontrado"
                            exception.message?.contains("network") == true ->
                                "Error de conexión. Verifica tu internet"
                            else ->
                                "Error: ${exception.message}"
                        }

                        Toast.makeText(
                            this@LoginActivity,
                            errorMessage,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    loginButton.isEnabled = true
                    loginButton.text = "Iniciar Sesión"
                    Toast.makeText(
                        this@LoginActivity,
                        "Error inesperado: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun showRegisterDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_register, null)
        val nameInput = dialogView.findViewById<EditText>(R.id.nameInput)
        val emailInput = dialogView.findViewById<EditText>(R.id.emailInput)
        val passwordInput = dialogView.findViewById<EditText>(R.id.passwordInput)
        val confirmPasswordInput = dialogView.findViewById<EditText>(R.id.confirmPasswordInput)

        AlertDialog.Builder(this)
            .setTitle("Registro")
            .setView(dialogView)
            .setPositiveButton("Registrar") { _, _ ->
                val name = nameInput.text.toString().trim()
                val email = emailInput.text.toString().trim()
                val password = passwordInput.text.toString().trim()
                val confirmPassword = confirmPasswordInput.text.toString().trim()

                when {
                    name.isEmpty() || email.isEmpty() || password.isEmpty() -> {
                        Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                    }
                    password != confirmPassword -> {
                        Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                    }
                    password.length < 6 -> {
                        Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        registerUser(email, password, name)
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun registerUser(email: String, password: String, displayName: String) {
        registerButton.isEnabled = false

        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    firebaseRepository.registerUser(email, password, displayName)
                }

                withContext(Dispatchers.Main) {
                    result.onSuccess {
                        Toast.makeText(
                            this@LoginActivity,
                            "Registro exitoso. ¡Bienvenido!",
                            Toast.LENGTH_SHORT
                        ).show()
                        goToMainActivity()
                    }.onFailure { exception ->
                        registerButton.isEnabled = true

                        val errorMessage = when {
                            exception.message?.contains("email-already") == true ->
                                "Este correo ya está registrado"
                            exception.message?.contains("invalid-email") == true ->
                                "Correo electrónico inválido"
                            exception.message?.contains("weak-password") == true ->
                                "Contraseña muy débil"
                            else ->
                                "Error al registrar: ${exception.message}"
                        }

                        Toast.makeText(
                            this@LoginActivity,
                            errorMessage,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    registerButton.isEnabled = true
                    Toast.makeText(
                        this@LoginActivity,
                        "Error inesperado: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}