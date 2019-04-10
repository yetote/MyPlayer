//
// Created by ether on 2019/4/10.
//

#include "PlayerStatus.h"
#include "CallBack.h"


PlayerStatus::~PlayerStatus() {

}

PlayerStatus::PlayerStatus(CallBack *callBack) {
    bool videoDecodeFinish = false;
    bool audioDecodeFinish = false;

    bool videoPlayFinish = false;
    bool audioPlayFinish = false;

    bool audioPrepare = false;
    bool videoPrepare = false;

    bool stop = false;
    this->callBack = callBack;
}

bool PlayerStatus::isFinish() {
    return false;
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
    PlayerStatus::audioPrepare = audioPrepare;
}

bool PlayerStatus::isVideoPrepare() const {
    return videoPrepare;
}

void PlayerStatus::setVideoPrepare(bool videoPrepare) {
    PlayerStatus::videoPrepare = videoPrepare;
}

bool PlayerStatus::isStop() const {
    return stop;
}

void PlayerStatus::setStop(bool stop) {
    PlayerStatus::stop = stop;
}

void PlayerStatus::checkPrepare() {
    if (audioPrepare && videoPrepare) {
        callBack->onPrepare(callBack->CHILD_THREAD, true, 0);
    }
}

void PlayerStatus::checkFinish() {
    if (audioPlayFinish && videoPlayFinish) {
        callBack->onFinish(callBack->CHILD_THREAD);
    }
}


