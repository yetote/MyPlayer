//
// Created by ether on 2019/4/10.
//

#include "PlayerStatus.h"
#include "CallBack.h"
#include "Log.h"


PlayerStatus::~PlayerStatus() {

}

PlayerStatus::PlayerStatus(CallBack *callBack) {
    videoDecodeFinish = false;
    audioDecodeFinish = false;

    videoPlayFinish = false;
    audioPlayFinish = false;

    audioPrepare = false;
    videoPrepare = false;

    stop = false;
    pause = false;
    this->callBack = callBack;
}


bool PlayerStatus::isVideoDecodeFinish() const {
    return videoDecodeFinish;
}

void PlayerStatus::setVideoDecodeFinish(bool videoDecodeFinish) {
    PlayerStatus::videoDecodeFinish = videoDecodeFinish;
}

bool PlayerStatus::isAudioDecodeFinish() const {
    return audioDecodeFinish;
}

void PlayerStatus::setAudioDecodeFinish(bool audioDecodeFinish) {
    PlayerStatus::audioDecodeFinish = audioDecodeFinish;
}

bool PlayerStatus::isVideoPlayFinish() const {
    return videoPlayFinish;
}

void PlayerStatus::setVideoPlayFinish(bool videoPlayFinish) {
    PlayerStatus::videoPlayFinish = videoPlayFinish;
}

bool PlayerStatus::isAudioPlayFinish() const {
    return audioPlayFinish;
}

void PlayerStatus::setAudioPlayFinish(bool audioPlayFinish) {
    PlayerStatus::audioPlayFinish = audioPlayFinish;
}

bool PlayerStatus::isAudioPrepare() const {
    return audioPrepare;
}

void PlayerStatus::setAudioPrepare(bool audioPrepare) {
    LOGE(LOG_TAG, "videoPrepare%d", videoPrepare);
    PlayerStatus::audioPrepare = audioPrepare;

}

bool PlayerStatus::isVideoPrepare() const {
    return videoPrepare;
}

void PlayerStatus::setVideoPrepare(bool videoPrepare) {
    LOGE(LOG_TAG, "audioPrepare%d", audioPrepare);
    PlayerStatus::videoPrepare = videoPrepare;
}

bool PlayerStatus::isStop() const {
    return stop;
}

void PlayerStatus::setStop(bool stop) {
    PlayerStatus::stop = stop;
}

void PlayerStatus::checkPrepare(int totalTime) {
    LOGE(LOG_TAG, "audioPrepare:%d,videoPrepare%d", audioPrepare, videoPrepare);
    if (audioPrepare && videoPrepare) {
        callBack->onPrepare(callBack->CHILD_THREAD, true, totalTime);
    }
}

void PlayerStatus::checkFinish() {
    if (audioPlayFinish && videoPlayFinish) {
        callBack->onFinish(callBack->CHILD_THREAD);
    }
}

void PlayerStatus::checkPause() {
    callBack->onPause(callBack->CHILD_THREAD);
}

bool PlayerStatus::isPause() const {
    return pause;
}

void PlayerStatus::setPause(bool pause) {
    PlayerStatus::pause = pause;
}

bool PlayerStatus::checkSupport(const char *name) {

    return callBack->onCheckSupport(callBack->CHILD_THREAD, name);
}

void PlayerStatus::callPlaying(int currentTime) {
    callBack->onPlaying(callBack->CHILD_THREAD, currentTime);
}



