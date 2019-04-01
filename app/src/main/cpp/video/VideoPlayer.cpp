//
// Created by ether on 2019/3/14.
//

#include "VideoPlayer.h"

static BlockQueue videoQueue;

void VideoPlayer::push(AVPacket *packet) {
    videoQueue.push(packet);
}

void VideoPlayer::setData(AVPacket *packet) {
    std::thread video(push, packet);
    video.join();
}

void VideoPlayer::init() {
    videoQueue.init();
}


void VideoPlayer::play(AVPacket* packet1) {
    videoQueue.pop(packet1);
    isFinish = videoQueue.isFinish;
}

void VideoPlayer::setState(bool isPush) {
    videoQueue.setState(isPush);
}


