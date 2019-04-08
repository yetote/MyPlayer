//
// Created by ether on 2019/4/2.
//

#include "EGLUtils.h"


typedef log_id_t;;

void EGLUtils::initEGL() {
    EGLBoolean result;
    eglDisplay = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    if (eglDisplay == null) {
        LOGE(LOG_TAG, "无法获取屏幕设备");
        return;
    }

    result = eglInitialize(eglDisplay, 0, 0);
    if (result == EGL_FALSE) {
        LOGE(LOG_TAG, "无法初始化屏幕设备");
        return;
    }
    int eglConfigureAttr[] = {
            EGL_BUFFER_SIZE, 16,
            EGL_RED_SIZE, 5,
            EGL_BLUE_SIZE, 6,
            EGL_GREEN_SIZE, 5,
            EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
            EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
            EGL_NONE
    };
    int *numConfig = new int[1];
    EGLConfig *configArr = new EGLConfig[1];
    result = eglChooseConfig(eglDisplay,
                             eglConfigureAttr,
                             configArr,
                             sizeof(numConfig) / sizeof(numConfig[1]),
                             numConfig);
    if (result == EGL_FALSE) {
        LOGE(LOG_TAG, "egl配置失败");
        delete[] configArr;
        return;
    }
    delete[] configArr;
    eglConfig = configArr[0];

    int eglContextAttr[] = {
            EGL_CONTEXT_CLIENT_VERSION, 2,
            EGL_NONE
    };
    eglContext = eglCreateContext(eglDisplay, eglConfig, null, eglContextAttr);
    if (eglContext == null) {
        LOGE(LOG_TAG, "创建eglContext失败");
        return;
    }
    int eglSurfaceAttr[] = {
            EGL_NONE
    };
    eglSurface = eglCreateWindowSurface(eglDisplay, eglConfig, window, eglSurfaceAttr);
    if (eglSurface == null) {
        LOGE(LOG_TAG, "创建surface失败");
        return;
    }
    result = eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext);
    if (result == EGL_FALSE) {
        LOGE(LOG_TAG, "关联egl失败");
        return;
    }
    LOGE(LOG_TAG, "配置egl成功,well done, boy");
}

EGLUtils::EGLUtils(ANativeWindow *window1) {
    this->window = window1;
    initEGL();
}

EGLUtils::~EGLUtils() {
    destroyEGL();
}

void EGLUtils::destroyEGL() {
    if (eglSurface != null) {
        eglDestroySurface(eglDisplay, eglSurface);
        eglSurface = null;
    }
    if (eglContext != null) {
        eglDestroyContext(eglDisplay, eglContext);
        eglContext = null;
    }
    if (eglDisplay != null) {
        eglGetDisplay(eglDisplay);
        eglDisplay = null;
    }
}
