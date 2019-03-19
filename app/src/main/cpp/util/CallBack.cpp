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
    isDecode = false;
    isFinish = false;
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

