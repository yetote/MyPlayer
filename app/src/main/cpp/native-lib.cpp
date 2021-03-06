#include <jni.h>
#include <string>
#include "decode/Decode.h"
#include "util/CallBack.h"
#include <unistd.h>
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include "util/PlayerStatus.h"

#define NATIVE_TAG "native-lib"
Decode *decode;
CallBack *callBack;
JavaVM *jvm;
PlayerStatus *playerStatus;

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
Java_com_example_myplayer_player_MyPlayer_prepare(JNIEnv *env, jobject instance, jstring path_,
                                                  jstring vertexCode_, jstring fragCode_,
                                                  jobject surface) {
    const char *path = env->GetStringUTFChars(path_, 0);
    const char *vertexCode = env->GetStringUTFChars(vertexCode_, 0);
    const char *fragCode = env->GetStringUTFChars(fragCode_, 0);

    ANativeWindow *window = ANativeWindow_fromSurface(env, surface);
    callBack = new CallBack(jvm, env, instance);
    playerStatus = new PlayerStatus(callBack);
    decode = new Decode(playerStatus);
    std::thread decodeThread(&Decode::prepare, decode, path, vertexCode, fragCode, window);
    decodeThread.detach();
//    env->ReleaseStringUTFChars(path_, path);
//    env->ReleaseStringUTFChars(vertexCode_, vertexCode);
//    env->ReleaseStringUTFChars(fragCode_, fragCode);
}extern "C"
JNIEXPORT void JNICALL
Java_com_example_myplayer_player_MyPlayer_play(JNIEnv *env, jobject instance, jint w, jint h) {

    decode->play(w, h);

}extern "C"
JNIEXPORT void JNICALL
Java_com_example_myplayer_player_MyPlayer_pause(JNIEnv *env, jobject instance) {
    decode->pause();

}extern "C"
JNIEXPORT void JNICALL
Java_com_example_myplayer_player_MyPlayer_recover(JNIEnv *env, jobject instance) {

    decode->recover();

}extern "C"
JNIEXPORT void JNICALL
Java_com_example_myplayer_player_MyPlayer_seek(JNIEnv *env, jobject instance, jint seekTime) {

//    decode->pause();
    decode->seek(seekTime);
//    decode->recover();
}extern "C"
JNIEXPORT void JNICALL
Java_com_example_myplayer_player_MyPlayer_stop(JNIEnv *env, jobject thiz) {
    // TODO: implement stop()
    LOGE(NATIVE_TAG, "line in 70:stop");
    decode->stop();
}extern "C"
JNIEXPORT void JNICALL
Java_com_example_myplayer_player_MyPlayer_ratote(JNIEnv *env, jobject thiz, jint w, jint h) {
    // TODO: implement ratote()
    decode->rotate(w, h);
}