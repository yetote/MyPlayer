//
// Created by ether on 2019/3/14.
//

#include "CallBack.h"

CallBack::CallBack(JavaVM *jvmParam, JNIEnv *envParam, jobject objParam) {
    jvm = jvmParam;
    env = envParam;
    this->obj = env->NewGlobalRef(objParam);
    jlz = env->GetObjectClass(obj);
    preparedId = env->GetMethodID(jlz, "onPrepared", "(ZI)V");
    finishId = env->GetMethodID(jlz, "onFinish", "()V");
    pauseId = env->GetMethodID(jlz, "onPause", "()V");
}

void CallBack::onPrepare(CallBack::THREAD_TYPE threadType, bool isSuccess, int errorCode) {

    if (threadType == MAIN_THREAD) {
        env->CallVoidMethod(obj, preparedId, isSuccess, errorCode);
    } else {
        JNIEnv *env;
        jvm->AttachCurrentThread(&env, 0);
        env->CallVoidMethod(obj, preparedId, isSuccess, errorCode);
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



