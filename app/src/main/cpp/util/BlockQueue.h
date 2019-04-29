//
// Created by ether on 2019/3/14.
//

#ifndef MYPLAYER_BLOCKQUEUE_H
#define MYPLAYER_BLOCKQUEUE_H

#include "Log.h"
#include "PlayerStatus.h"

#include <queue>
#include <mutex>

#define BlockQueue_TAG "BlockQueue"
extern "C" {
#include <libavcodec/avcodec.h>
}


class BlockQueue {
public:

    void push(AVPacket *packet);

    void init();

    bool pop(AVPacket *packet1, bool isFinish);

    void clear();

    void setMaxSize(int size);

    BlockQueue(int maxSize);

    virtual ~BlockQueue();

    std::queue<AVPacket *> queue;
private:
    AVPacket *packet;
    std::condition_variable cond;
    std::mutex mutex;
    int maxSize = 0;

    void stop();
};


#endif //MYPLAYER_BLOCKQUEUE_H
