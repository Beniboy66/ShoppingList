package com.example.shoppinglist

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ProfileActivity : AppCompatActivity() {

    private val firebaseRepository = FirebaseRepository()
    private lateinit var pieChart: PieChart

    // IMPORTANTE: Jobs para cancelar los listeners
    private var userDataJob: Job? = null
    private var statsJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        pieChart = findViewById(R.id.pieChart)
        val addedCount = findViewById<TextView>(R.id.addedCount)
        val completedCount = findViewById<TextView>(R.id.completedCount)
        val userName = findViewById<TextView>(R.id.userName)
        val userEmail = findViewById<TextView>(R.id.userEmail)
        val memberSince = findViewById<TextView>(R.id.memberSince)

        // Mostrar email del usuario
        userEmail.text = firebaseRepository.getCurrentUser()?.email ?: "usuario@ejemplo.com"

        // Observar datos del usuario
        userDataJob = lifecycleScope.launch {
            firebaseRepository.getUserData().collect { user ->
                user?.let {
                    userName.text = it.displayName.ifEmpty { "Usuario" }

                    val dateFormat = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
                    val date = Date(it.createdAt)
                    memberSince.text = "Miembro desde ${dateFormat.format(date)}"
                }
            }
        }

        // Observar estadísticas
        statsJob = lifecycleScope.launch {
            firebaseRepository.getUserStats().collect { (added, completed) ->
                addedCount.text = added.toString()
                completedCount.text = completed.toString()

                // Actualizar gráfico
                updatePieChart(added, completed)
            }
        }

        findViewById<Button>(R.id.clearCompletedButton).setOnClickListener {
            lifecycleScope.launch {
                firebaseRepository.deleteAllCompleted()
                Toast.makeText(
                    this@ProfileActivity,
                    "Productos completados eliminados",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        findViewById<Button>(R.id.logoutButton).setOnClickListener {
            logout()
        }
    }

    private fun logout() {
        try {
            // IMPORTANTE: Cancelar todos los listeners ANTES de cerrar sesión
            userDataJob?.cancel()
            statsJob?.cancel()

            // Cerrar sesión de Firebase
            firebaseRepository.signOut()

            // Crear intent para LoginActivity
            val intent = Intent(this@ProfileActivity, LoginActivity::class.java).apply {
                putExtra("FROM_LOGOUT", true)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            // Iniciar LoginActivity
            startActivity(intent)

            // Terminar esta actividad
            finish()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                this@ProfileActivity,
                "Error al cerrar sesión: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Limpiar los jobs cuando se destruye la actividad
        userDataJob?.cancel()
        statsJob?.cancel()
    }

    private fun updatePieChart(added: Int, completed: Int) {
        if (added == 0) {
            pieChart.clear()
            pieChart.setNoDataText("No hay datos aún")
            pieChart.invalidate()
            return
        }

        val entries = ArrayList<PieEntry>()

        // Siempre mostrar ambos valores
        entries.add(PieEntry(added.toFloat(), "Agregados ($added)"))
        entries.add(PieEntry(completed.toFloat(), "Comprados ($completed)"))

        val dataSet = PieDataSet(entries, "Estadísticas de Compras")
        dataSet.colors = listOf(
            Color.parseColor("#4CAF50"),  // Verde para agregados
            Color.parseColor("#FF9800")   // Naranja para comprados
        )
        dataSet.valueTextSize = 16f
        dataSet.valueTextColor = Color.WHITE
        dataSet.sliceSpace = 3f

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(pieChart))

        pieChart.data = data
        pieChart.description.isEnabled = false
        pieChart.centerText = "Total\n${added + completed}"
        pieChart.setCenterTextSize(18f)
        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(Color.WHITE)
        pieChart.holeRadius = 40f
        pieChart.transparentCircleRadius = 45f
        pieChart.setDrawEntryLabels(true)
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.setEntryLabelTextSize(11f)
        pieChart.animateY(1000)
        pieChart.legend.isEnabled = true
        pieChart.legend.textSize = 12f
        pieChart.setUsePercentValues(true)

        pieChart.invalidate()
    }
}