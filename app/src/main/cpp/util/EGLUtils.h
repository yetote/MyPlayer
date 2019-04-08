//
// Created by ether on 2019/4/2.
//

#ifndef MYPLAYER_EGLUTILS_H
#define MYPLAYER_EGLUTILS_H

#include <EGL/egl.h>
#include <android/window.h>

#include "Log.h"

#define null NULL
#define LOG_TAG "EGLUtil"

class EGLUtils {
public:


    EGLUtils(ANativeWindow *);

    ~EGLUtils();

    EGLDisplay eglDisplay;
    EGLSurface eglSurface;
private:

    ANativeWindow *window;
    EGLContext eglContext;
    EGLConfig eglConfig;

    void initEGL();

    void destroyEGL();
};


#endif //MYPLAYER_EGLUTILS_H
