//
// Created by ether on 2019/3/13.
//

#ifndef MYPLAYER_LOG_H
#define MYPLAYER_LOG_H

#endif //MYPLAYER_LOG_H

#include <android/log.h>

#define LOGE(LOG_TAG,...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)