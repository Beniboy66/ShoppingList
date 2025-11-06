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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ProfileActivity : AppCompatActivity() {

    private val firebaseRepository = FirebaseRepository()
    private lateinit var pieChart: PieChart

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
        lifecycleScope.launch {
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
        lifecycleScope.launch {
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
            // Cerrar sesión
            firebaseRepository.signOut()

            // Ir a LoginActivity y limpiar el stack
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun updatePieChart(added: Int, completed: Int) {
        val pending = added - completed

        if (added == 0) {
            // No hay datos
            pieChart.clear()
            pieChart.setNoDataText("No hay datos aún")
            pieChart.invalidate()
            return
        }

        val entries = ArrayList<PieEntry>()

        if (completed > 0) {
            entries.add(PieEntry(completed.toFloat(), "Completados"))
        }
        if (pending > 0) {
            entries.add(PieEntry(pending.toFloat(), "Pendientes"))
        }

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = listOf(
            Color.parseColor("#FF9800"), // Orange para completados
            Color.parseColor("#4CAF50")  // Green para pendientes
        )
        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = Color.WHITE

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(pieChart))

        pieChart.data = data
        pieChart.description.isEnabled = false
        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(Color.WHITE)
        pieChart.holeRadius = 40f
        pieChart.transparentCircleRadius = 45f
        pieChart.setDrawEntryLabels(true)
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.setEntryLabelTextSize(12f)
        pieChart.animateY(1000)
        pieChart.legend.isEnabled = true
        pieChart.setUsePercentValues(true)

        pieChart.invalidate()
    }
}