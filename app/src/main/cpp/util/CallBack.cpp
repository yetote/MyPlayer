//
// Created by ether on 2019/3/14.
//

#include "CallBack.h"
#include "Log.h"

CallBack::CallBack(JavaVM *jvmParam, JNIEnv *envParam, jobject objParam) {
    jvm = jvmParam;
    env = envParam;
    this->obj = env->NewGlobalRef(objParam);
    jlz = env->GetObjectClass(obj);
    preparedId = env->GetMethodID(jlz, "onPrepared", "(ZI)V");
    finishId = env->GetMethodID(jlz, "onFinish", "()V");
    pauseId = env->GetMethodID(jlz, "onPause", "()V");
    checkSupportId = env->GetMethodID(jlz, "isSupport", "(Ljava/lang/String;)Z");
    onPlayingId = env->GetMethodID(jlz, "onPlaying", "(I)V");
}

void CallBack::onPrepare(CallBack::THREAD_TYPE threadType, bool isSuccess, int totalTime) {

    if (threadType == MAIN_THREAD) {
        env->CallVoidMethod(obj, preparedId, isSuccess, totalTime);
    } else {
        JNIEnv *env;
        jvm->AttachCurrentThread(&env, 0);
        env->CallVoidMethod(obj, preparedId, isSuccess, totalTime);
        jvm->DetachCurrentThread();
    }
}

void CallBack::onFinish(CallBack::THREAD_TYPE threadType) {
    if (threadType == MAIN_THREAD) {
        env->CallVoidMethod(obj, finishId);
    } else {
        JNIEnv *env;
        jvm->AttachCurrentThread(&env, 0);
        env->CallVoidMethod(obj, finishId);
        jvm->DetachCurrentThread();
    }
}

CallBack::~CallBack() {

}

void CallBack::onPause(CallBack::THREAD_TYPE threadType) {
    if (threadType == MAIN_THREAD) {
        env->CallVoidMethod(obj, pauseId);
    } else {
        JNIEnv *env;
        jvm->AttachCurrentThread(&env, 0);
        env->CallVoidMethod(obj, pauseId);
        jvm->DetachCurrentThread();
    }
}


bool CallBack::onCheckSupport(CallBack::THREAD_TYPE threadType, const char *namePram) {
    bool isSupport;
    if (threadType == MAIN_THREAD) {
        jstring name = env->NewStringUTF(namePram);
        isSupport = env->CallBooleanMethod(obj, checkSupportId, name);
        env->DeleteLocalRef(name);
    } else {
        JNIEnv *env;
        jvm->AttachCurrentThread(&env, 0);
        jstring name = env->NewStringUTF(namePram);
        isSupport = env->CallBooleanMethod(obj, checkSupportId, name);
        env->DeleteLocalRef(name);
        jvm->DetachCurrentThread();
    }
    LOGE(CallBack_TAG, "line in 67:isSupport%d", isSupport);
    return isSupport;
}

void CallBack::onPlaying(CallBack::THREAD_TYPE threadType, int currentTime) {
    if (threadType == MAIN_THREAD) {
        env->CallVoidMethod(obj, onPlayingId, currentTime);
    } else {
        JNIEnv *env;
        jvm->AttachCurrentThread(&env, 0);
        env->CallVoidMethod(obj, onPlayingId, currentTime);
        jvm->DetachCurrentThread();
    }
}



