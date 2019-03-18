//
// Created by ether on 2019/3/14.
//

#ifndef MYPLAYER_VIDEOPLAYER_H
#define MYPLAYER_VIDEOPLAYER_H


#include "../util/BlockQueue.h"

class VideoPlayer {
public:
    BlockQueue<int> blockQueue;
    void push(int i);
};


#endif //MYPLAYER_VIDEOPLAYER_H
