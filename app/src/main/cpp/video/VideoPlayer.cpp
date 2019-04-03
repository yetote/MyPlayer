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

void VideoPlayer::init(ANativeWindow* window1) {
    videoQueue.init();
    EGLUtils *eglUtil = new EGLUtils(window1);
}


void VideoPlayer::play(AVPacket *packet1) {
    videoQueue.pop(packet1);
    isFinish = videoQueue.isFinish;

}

void VideoPlayer::setState(bool isPush) {
    videoQueue.setState(isPush);
}


