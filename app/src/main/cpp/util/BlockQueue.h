//
// Created by ether on 2019/3/14.
//

#ifndef MYPLAYER_BLOCKQUEUE_H
#define MYPLAYER_BLOCKQUEUE_H

#include "Log.h"

#include <queue>
#include <mutex>

extern "C" {
#include <libavcodec/avcodec.h>
}


class BlockQueue {
public:

    bool isPushFinish;
    bool isFinish;

    void setState(bool isPush);

    void push(AVPacket *packet);

    void init();

    void pop(AVPacket *packet1);

private:
    AVPacket *packet;
    std::queue<AVPacket *> queue;
    std::condition_variable cond;
    std::mutex mutex;

    void stop();
};


#endif //MYPLAYER_BLOCKQUEUE_H
