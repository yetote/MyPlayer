//
// Created by ether on 2019/3/14.
//

#include "BlockQueue.h"

void BlockQueue::push(AVPacket *packet) {
    std::unique_lock<decltype(mutex)> lock(mutex);
    while (queue.size() >= 1000) {
        LOGE("blockQueue", "队列已满，阻塞中：%d", queue.size());
        cond.wait(lock);
    }
    queue.push(packet);
    LOGE("blockQueue", "packet入队，队列容量:%d", queue.size());
    cond.notify_all();
}

void BlockQueue::pop(AVPacket *packet1) {
    std::unique_lock<decltype(mutex)> lock(mutex);
    if (queue.empty()) {
        LOGE("blockQueue", "the messagequeue is empty ,waiting producer add message ");
        cond.wait(lock);
    } else {
        av_packet_ref(packet1, queue.front());
        LOGE("blockQueue", "packet出队，队列剩余:%d", queue.size());
        queue.pop();
    }
    cond.notify_all();
    if (isPushFinish && queue.empty()) {
        isFinish = true;
    }

}

void BlockQueue::init() {
    packet = av_packet_alloc();
}

void BlockQueue::stop() {
    av_packet_free(&packet);
}

void BlockQueue::setState(bool isPush) {
    std::unique_lock<decltype(mutex)> lock(mutex);
    isPushFinish = isPush;
    cond.notify_all();
}

