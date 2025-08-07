package com.equalizer

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    private lateinit var eqContainer: LinearLayout
    private lateinit var eqToggle: Switch
    private val eqSliders = mutableListOf<SeekBar>()
    private val EQ_PREF_KEY = "eq_enabled"
    private val FILE_PICKER_REQUEST = 101
    private val BAND_COUNT = 150

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        eqContainer = findViewById(R.id.eqContainer)
        eqToggle = findViewById(R.id.eqToggle)

        // Restore toggle state
        val prefs = getSharedPreferences("eq_prefs", Context.MODE_PRIVATE)
        eqToggle.isChecked = prefs.getBoolean(EQ_PREF_KEY, false)

        eqToggle.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(EQ_PREF_KEY, isChecked).apply()
            Toast.makeText(this, "EQ ${if (isChecked) "Enabled" else "Disabled"}", Toast.LENGTH_SHORT).show()
        }

        // Create EQ bands
        for (i in 0 until BAND_COUNT) {
            val seekBar = SeekBar(this).apply {
                max = 100
                progress = 50
                layoutParams = LinearLayout.LayoutParams(10, LinearLayout.LayoutParams.MATCH_PARENT).apply {
                    setMargins(2, 0, 2, 0)
                }
                rotation = -90f // Make vertical
            }
            eqSliders.add(seekBar)
            eqContainer.addView(seekBar)
        }

        findViewById<Button>(R.id.loadEqFileButton).setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "text/plain"
            }
            startActivityForResult(Intent.createChooser(intent, "Select EQ File"), FILE_PICKER_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_PICKER_REQUEST && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                loadEqValuesFromFile(uri)
            }
        }
    }

    private fun loadEqValuesFromFile(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val lines = reader.readLines()

            for (i in lines.indices) {
                if (i >= BAND_COUNT) break
                val value = lines[i].trim().toIntOrNull()
                value?.let {
                    eqSliders[i].progress = value.coerceIn(0, 100)
                }
            }

            Toast.makeText(this, "EQ file loaded!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to load EQ file: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e("EQ_LOAD", "Error loading EQ file", e)
        }
    }
}
