//
// Created by ether on 2019/3/14.
//

#ifndef MYPLAYER_CALLBACK_H
#define MYPLAYER_CALLBACK_H


#include <jni.h>

class CallBack {
public:

    enum THREAD_TYPE {
        MAIN_THREAD,
        CHILD_THREAD
    };

    CallBack(JavaVM *jvmParam, JNIEnv *envParam, jobject objParam);

    ~CallBack();

    void onPrepare(THREAD_TYPE threadType, bool isSuccess, int errorCode);

private:
    JavaVM *jvm;
    JNIEnv *env;
    jobject obj;
    jclass jlz;

    jmethodID preparedId;
};


#endif //MYPLAYER_CALLBACK_H