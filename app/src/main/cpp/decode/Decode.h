//
// Created by ether on 2019/3/13.
//

#ifndef MYPLAYER_DECODE_H
#define MYPLAYER_DECODE_H

#include <string>
#include "../util/Log.h"
#include "../util/CallBack.h"
#include "../video/VideoPlayer.h"
#include <pthread.h>

extern "C" {
#include "../ffmpeg/includes/libavformat/avformat.h"
#include "../ffmpeg/includes/libavutil/frame.h"
#include "../ffmpeg/includes/libavcodec/avcodec.h"
#include "../ffmpeg/includes/libavutil/time.h"
#include "../ffmpeg/includes/libavutil/imgutils.h"
#include "../ffmpeg/includes/libswscale/swscale.h"
};


class Decode {
public:
    int audioIndex, videoIndex;

    void prepare(const char *, ANativeWindow *);

    void play();

    void audioPlay();

    void videoPlay();

    Decode(CallBack *callback);

    ~Decode();

private:
    CallBack *callBack;
    AVFormatContext *pFmtCtx = nullptr;

    AVStream *pAudioStream = nullptr;
    AVStream *pVideoStream = nullptr;

    AVFrame *pFrame = nullptr;

    AVCodecContext *pAudioCodecCtx = nullptr;
    AVCodec *pAudioCodec = nullptr;

    AVCodecContext *pVideoCodecCtx = nullptr;
    AVCodec *pVideoCodec = nullptr;
    VideoPlayer *videoPlayer;

    void decodeVideo(int videoIndex);

    void decodeAudio(int audioIndex);

    void decode();
};


#endif //MYPLAYER_DECODE_H
