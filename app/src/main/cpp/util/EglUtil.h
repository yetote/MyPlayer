//
// Created by ether on 2019/4/2.
//

#ifndef MYPLAYER_EGLUTIL_H
#define MYPLAYER_EGLUTIL_H

#include <EGL/egl.h>
#include <oboe/Oboe.h>

class EglUtil {
public:
    EGLDisplay *eglDisplay;
    EGLConfig *eglConfig;
    EGLSurface *eglSurface;
    EGLContext *eglContext;
private:
};


#endif //MYPLAYER_EGLUTIL_H
