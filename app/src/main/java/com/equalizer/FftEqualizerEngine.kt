package com.equalizer

class FftEqualizerEngine {
    init {
        System.loadLibrary("ffteq")
    }

    fun loadCurve(data: List<Pair<Float, Float>>) {
        val freqs = data.map { it.first }.toFloatArray()
        val gains = data.map { it.second }.toFloatArray()
        nativeLoadCurve(freqs, gains)
    }

    fun setEnabled(on: Boolean) {
        nativeSetEnabled(on)
    }

    private external fun nativeLoadCurve(freqs: FloatArray, gains: FloatArray)
    private external fun nativeSetEnabled(on: Boolean)
}
