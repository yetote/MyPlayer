#include <jni.h>
#include <string>
#include "decode/Decode.h"
#include "util/CallBack.h"
#include <unistd.h>
#include <android/native_window.h>
#include <android/native_window_jni.h>

Decode *decode;
CallBack *callBack;
JavaVM *jvm;

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    jvm = vm;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }
    return JNI_VERSION_1_6;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_myplayer_player_MyPlayer_play(JNIEnv *env, jobject instance) {
//    std::thread decodeThread(&Decode::audioPlay, decode);
//    decodeThread.detach();
//    decode->audioPlay();

}extern "C"
JNIEXPORT void JNICALL
Java_com_example_myplayer_player_MyPlayer_prepare(JNIEnv *env, jobject instance, jstring path_,
                                                  jobject surface) {
    const char *path = env->GetStringUTFChars(path_, 0);

    ANativeWindow *window = ANativeWindow_fromSurface(env, surface);
    callBack = new CallBack(jvm, env, instance);
    decode = new Decode(callBack);
    std::thread decodeThread(&Decode::prepare, decode, path, window);
    decodeThread.detach();

    env->ReleaseStringUTFChars(path_, path);
}