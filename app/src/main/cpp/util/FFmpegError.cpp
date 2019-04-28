//
// Created by ether on 2019/4/17.
//


#include "FFmpegError.h"

const char* FFmpegError::showError(int errorCode) {
    char errorbuf[1024] = {0};
    av_strerror(errorCode, errorbuf, 1024);
    return errorbuf;
}
