//
// Created by ether on 2019/3/14.
//

#include "BlockQueue.h"

static BlockQueue::Type type;
static PlayerStatus *playerStatus;

void BlockQueue::push(AVPacket *packet) {
    std::unique_lock<decltype(mutex)> lock(mutex);
    while (queue.size() >= 100) {
        LOGE("blockQueue", "队列已满，阻塞中：%d", queue.size());
        cond.wait(lock);
    }
    queue.push(packet);
    LOGE("blockQueue", "packet入队，队列容量:%d", queue.size());
    cond.notify_all();
}

bool BlockQueue::pop(AVPacket *packet1) {
    std::unique_lock<decltype(mutex)> lock(mutex);

    if (getStatus && queue.empty()) {
        LOGE("blockQueue", "播放完成");
        cond.notify_all();
        return true;
    } else if (!getStatus && queue.empty()) {
        LOGE("blockQueue", "the messagequeue is empty ,waiting producer add message ");
        cond.wait(lock);
    } else {
        av_packet_ref(packet1, queue.front());
        LOGE("blockQueue", "packet出队，队列剩余:%d", queue.size());
        queue.pop();
    }
    cond.notify_all();
    return false;
}

void BlockQueue::init(PlayerStatus *playerStatus, BlockQueue::Type type) {
    packet = av_packet_alloc();
    type = type;
    playerStatus = playerStatus;
}

void BlockQueue::stop() {
    av_packet_free(&packet);
}


bool BlockQueue::getStatus() {
    if (BlockQueue::type == AUDIO_QUEUE) {
        return playerStatus->isAudioDecodeFinish();
    }
    return playerStatus->isVideoDecodeFinish();

}

BlockQueue::BlockQueue(PlayerStatus *playerStatus, BlockQueue::Type type) {

}


