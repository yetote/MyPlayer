////
//// Created by ether on 2019/3/14.
////
//
//#include "BlockQueue.h"
//
//void BlockQueue::push(int a) {
//    std::lock_guard<decltype(mutex)> lock(mutex);
//    queue.push(a);
//    cond.notify_all();
//}
//
//void BlockQueue::pop() {
//    std::unique_lock<decltype(mutex)> lock(mutex);
//    if (queue.empty()) {
//        LOGE("blockQueue", "the messagequeue is empty ,waiting producer add message ");
//        cond.wait(lock);
//    } else {
////        av_packet_ref(pPacket, queue.front());
//        LOGE("blockQueue", ":%d", queue.front());
//        queue.pop();
//    }
//    cond.notify_all();
//}
