package com.equalizer

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.equalizer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    companion object {
        init {
            System.loadLibrary("equalizer-native")
        }
    }

    // Native function
    external fun stringFromJNI(): String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Example native call
        val result = stringFromJNI()
        Log.d("NativeOutput", result)
    }
}
