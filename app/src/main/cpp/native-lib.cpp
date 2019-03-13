#include <jni.h>
#include <string>
#include "decode/Decode.h"

Decode decode;
extern "C" JNIEXPORT jstring JNICALL
Java_com_example_myplayer_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_myplayer_player_MyPlayer_prepare(JNIEnv *env, jobject instance, jstring path_) {
    const char *path = env->GetStringUTFChars(path_, 0);

    decode.prepare(path);

    env->ReleaseStringUTFChars(path_, path);
}