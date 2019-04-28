//
// Created by ether on 2019/4/17.
//

#ifndef FFMPEGANDOBOE_FFMPEGERROR_H
#define FFMPEGANDOBOE_FFMPEGERROR_H

#include <string>

extern "C" {
#include <libavutil/error.h>
};

class FFmpegError {
public:
    static const char *showError(int errorCode);

private:
};


#endif //FFMPEGANDOBOE_FFMPEGERROR_H
