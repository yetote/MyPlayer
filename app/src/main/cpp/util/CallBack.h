//
// Created by ether on 2019/3/14.
//

#ifndef MYPLAYER_CALLBACK_H
#define MYPLAYER_CALLBACK_H


#include <jni.h>

#define CallBack_TAG "CallBack"

#include <string>

class CallBack {
public:

    enum THREAD_TYPE {
        MAIN_THREAD,
        CHILD_THREAD
    };

    CallBack(JavaVM *jvmParam, JNIEnv *envParam, jobject objParam);

    ~CallBack();

    void onPrepare(THREAD_TYPE threadType, bool isSuccess, int totalTime);

    void onFinish(THREAD_TYPE threadType);

    void onPause(THREAD_TYPE threadType);

    bool onCheckSupport(THREAD_TYPE threadType, const char *name);

    void onPlaying(THREAD_TYPE ,int);

private:
    JavaVM *jvm;
    JNIEnv *env;
    jobject obj;
    jclass jlz;

    jmethodID preparedId;
    jmethodID finishId;
    jmethodID pauseId;
    jmethodID checkSupportId;
    jmethodID onPlayingId;

};


#endif //MYPLAYER_CALLBACK_H
