#include <jni.h>
#include <vector>
#include <cmath>

static bool enabled = false;
static std::vector<float> gainTable(2048, 1.0f); // 2048-bin FFT gain

extern "C" JNIEXPORT void JNICALL
Java_com_equalizer_FftEqualizerEngine_nativeSetEnabled(JNIEnv *, jobject, jboolean on) {
    enabled = on;
}

extern "C" JNIEXPORT void JNICALL
Java_com_equalizer_FftEqualizerEngine_nativeLoadCurve(JNIEnv* env, jobject,
                                                      jfloatArray freqArray,
                                                      jfloatArray gainArray) {
    jsize len = env->GetArrayLength(freqArray);
    jfloat* freqs = env->GetFloatArrayElements(freqArray, nullptr);
    jfloat* gains = env->GetFloatArrayElements(gainArray, nullptr);

    for (int i = 0; i < 2048; ++i) {
        float binFreq = 20.0f * std::pow(10.0f, i / 2048.0f * std::log10(20000.0f / 20.0f));
        float closestGain = 0.0f;
        float minDiff = 1e9;
        for (int j = 0; j < len; ++j) {
            float diff = std::abs(freqs[j] - binFreq);
            if (diff < minDiff) {
                minDiff = diff;
                closestGain = gains[j];
            }
        }
        gainTable[i] = std::pow(10.0f, closestGain / 20.0f); // dB to linear
    }

    env->ReleaseFloatArrayElements(freqArray, freqs, 0);
    env->ReleaseFloatArrayElements(gainArray, gains, 0);
}
