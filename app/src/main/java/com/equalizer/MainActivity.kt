package com.equalizer

import android.media.audiofx.Equalizer
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.equalizer.databinding.ActivityMainBinding
import java.io.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var equalizer: Equalizer? = null
    private var isEQEnabled = false
    private val sliders = mutableListOf<SeekBar>()
    private val bandCount = 150

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupEqualizer()
        setupUI()
    }

    private fun setupEqualizer() {
        equalizer = Equalizer(0, 0)
        equalizer?.enabled = false
    }

    private fun setupUI() {
        // Create 150 sliders
        for (i in 0 until bandCount) {
            val seekBar = SeekBar(this).apply {
                max = 2000 // -1000 to +1000 in millibels
                progress = 1000
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1f
                )
                rotation = -90f
            }
            binding.sliderContainer.addView(seekBar)
            sliders.add(seekBar)
        }

        binding.enableEqButton.setOnCheckedChangeListener { _, isChecked ->
            isEQEnabled = isChecked
            equalizer?.enabled = isChecked
            if (isChecked) applyEqFromSliders()
        }

        binding.resetButton.setOnClickListener {
            sliders.forEach { it.progress = 1000 }
            if (isEQEnabled) applyEqFromSliders()
        }

        binding.saveButton.setOnClickListener {
            try {
                val file = File(filesDir, "eq.txt")
                val output = file.printWriter()
                sliders.forEach { output.println(it.progress) }
                output.close()
                Toast.makeText(this, "Saved to eq.txt", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show()
            }
        }

        binding.loadButton.setOnClickListener {
            try {
                val file = File(filesDir, "eq.txt")
                val lines = file.readLines()
                sliders.forEachIndexed { index, seekBar ->
                    if (index < lines.size) {
                        seekBar.progress = lines[index].toInt()
                    }
                }
                if (isEQEnabled) applyEqFromSliders()
                Toast.makeText(this, "Loaded from eq.txt", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Load failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun applyEqFromSliders() {
        val minEQLevel = equalizer?.bandLevelRange?.get(0) ?: return
        val maxEQLevel = equalizer?.bandLevelRange?.get(1) ?: return
        val bandLevelRange = maxEQLevel - minEQLevel

        val numBands = equalizer?.numberOfBands ?: return
        for (i in 0 until numBands) {
            val sliderIndex = (i * bandCount) / numBands
            val level = ((sliders[sliderIndex].progress - 1000) / 1000.0f * bandLevelRange).toInt()
            equalizer?.setBandLevel(i.toShort(), level.toShort())
        }
    }

    override fun onPause() {
        super.onPause()
        equalizer?.enabled = isEQEnabled
    }

    override fun onDestroy() {
        super.onDestroy()
        equalizer?.release()
    }
}
