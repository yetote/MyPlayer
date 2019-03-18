#ifndef BLOCKQUEUE_BLOCKQUEUE_H
#define BLOCKQUEUE_BLOCKQUEUE_H

#include <queue>
#include <mutex>

enum popResult {
    POP_OK, POP_STOP,POP_UNEXPECTED
};

template<typename T>
class BlockQueue : public std::queue<T> {
public:
    std::mutex mLock;
    std::condition_variable mCond;
    std::queue<T> queue;
    bool stopFlag = false;
    virtual ~BlockQueue() = default;
    void push(const T &value) {
        std::lock_guard<decltype(mLock)> lock(mLock);
        queue.push(value);
        mCond.notify_one();
    }

    void push(const T &&value) {
        std::lock_guard<decltype(mLock)> lock(mLock);
        queue.push(std::move(value));
        mCond.notify_one();
    }

    popResult pop(T &out) {
        std::unique_lock<decltype(mLock)> lock(mLock);
        if (stopFlag && queue.empty()) return POP_STOP;

        if (queue.empty()) mCond.wait(lock);
        if (stopFlag && queue.empty()) return POP_STOP;
        if (queue.empty()) return POP_UNEXPECTED;
        out = std::move(queue.front());
        queue.pop();
        return POP_OK;
    }

    void stop() {
        std::lock_guard<decltype(mLock)> lock(mLock);
        stopFlag = true;
        mCond.notify_all();
    }
};


#endif //BLOCKQUEUE_BLOCKQUEUE_H