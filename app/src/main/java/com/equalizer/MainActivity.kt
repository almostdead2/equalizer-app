package com.equalizer

import android.app.Activity
import android.content.*
import android.media.AudioManager
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : Activity() {
    private lateinit var eqSwitch: Switch
    private lateinit var loadButton: Button
    private lateinit var engine: FftEqualizerEngine

    private val headsetReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateUI(isHeadphonesConnected())
        }
    }

    private fun updateUI(headphonesConnected: Boolean) {
        eqSwitch.isEnabled = headphonesConnected
        loadButton.isEnabled = headphonesConnected
        if (!headphonesConnected) {
            engine.setEnabled(false)
            eqSwitch.isChecked = false
            Toast.makeText(this, "EQ off – no headphones connected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isHeadphonesConnected(): Boolean {
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return am.isWiredHeadsetOn || am.isBluetoothA2dpOn
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        eqSwitch = findViewById(R.id.eq_switch)
        loadButton = findViewById(R.id.load_button)
        engine = FftEqualizerEngine()

        updateUI(isHeadphonesConnected())

        eqSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isHeadphonesConnected()) {
                engine.setEnabled(isChecked)
            } else {
                Toast.makeText(this, "Please connect headphones first.", Toast.LENGTH_SHORT).show()
                eqSwitch.isChecked = false
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_HEADSET_PLUG)
            addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
        }
        registerReceiver(headsetReceiver, filter)

        val filePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                val stream = contentResolver.openInputStream(it)
                val reader = BufferedReader(InputStreamReader(stream))
                val freqData = mutableListOf<Pair<Float, Float>>()
                reader.useLines { lines ->
                    lines.forEach { line ->
                        val parts = line.split(",")
                        if (parts.size == 2) {
                            val freq = parts[0].toFloatOrNull()
                            val gain = parts[1].toFloatOrNull()
                            if (freq != null && gain != null) {
                                freqData.add(freq to gain)
                            }
                        }
                    }
                }
                engine.loadCurve(freqData)
                Toast.makeText(this, "EQ file loaded", Toast.LENGTH_SHORT).show()
            }
        }

        loadButton.setOnClickListener {
            if (isHeadphonesConnected()) {
                filePicker.launch("*/*")
            } else {
                Toast.makeText(this, "Connect headphones to load EQ.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(headsetReceiver)
    }
}
