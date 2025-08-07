#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_equalizer_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Equalizer Native Engine Ready";
    return env->NewStringUTF(hello.c_str());
}
