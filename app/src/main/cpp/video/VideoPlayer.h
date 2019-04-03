//
// Created by ether on 2019/3/14.
//

#ifndef MYPLAYER_VIDEOPLAYER_H
#define MYPLAYER_VIDEOPLAYER_H


#include "../util/BlockQueue.h"
#include <thread>
#include "../util/EGLUtils.h"

class VideoPlayer {
public:
    bool isFinish;
    static bool isPushFinish;

    void init(ANativeWindow *);

    void setData(AVPacket *packet);

    void setState(bool isPush);

    void stop();

    void play(AVPacket *packet1);

private:
    static void push(AVPacket *packet);
};


#endif //MYPLAYER_VIDEOPLAYER_H
