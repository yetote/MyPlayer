//
// Created by ether on 2019/4/10.
//

#ifndef MYPLAYER_PLAYERSTATUS_H
#define MYPLAYER_PLAYERSTATUS_H


#include "CallBack.h"

#define LOG_TAG "playerStatus"

class PlayerStatus {
public:
    PlayerStatus(CallBack *);

    ~PlayerStatus();

    bool isVideoDecodeFinish() const;

    void setVideoDecodeFinish(bool videoDecodeFinish);

    bool isAudioDecodeFinish() const;

    void setAudioDecodeFinish(bool audioDecodeFinish);

    bool isVideoPlayFinish() const;

    void setVideoPlayFinish(bool videoPlayFinish);

    bool isAudioPlayFinish() const;

    void setAudioPlayFinish(bool audioPlayFinish);

    bool isAudioPrepare() const;

    void setAudioPrepare(bool audioPrepare);

    bool isVideoPrepare() const;

    void setVideoPrepare(bool videoPrepare);

    bool isStop() const;

    void setStop(bool stop);

    void checkPrepare();

    void checkFinish();

private:
    bool videoDecodeFinish;
    bool audioDecodeFinish;

    bool videoPlayFinish;
    bool audioPlayFinish;

    bool audioPrepare;
    bool videoPrepare;
    bool isPrepare;
    bool stop;
    CallBack *callBack;


};


#endif //MYPLAYER_PLAYERSTATUS_H
