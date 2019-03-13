//
// Created by ether on 2019/3/13.
//

#ifndef MYPLAYER_DECODE_H
#define MYPLAYER_DECODE_H

#include <string>
#include "../util/Log.h"

extern "C" {
#include "../ffmpeg/includes/libavformat/avformat.h"
#include "../ffmpeg/includes/libavutil/frame.h"
#include "../ffmpeg/includes/libavcodec/avcodec.h"
};


class Decode {
public:
    int audioIndex, videoIndex;

    void prepare(const char *path);

private:
    AVFormatContext *pFmtCtx = nullptr;

    AVStream *pAudioStream = nullptr;
    AVStream *pVideoStream = nullptr;

    AVFrame *pFrame = nullptr;

    AVCodecContext *pAudioCodecCtx = nullptr;
    AVCodec *pAudioCodec = nullptr;

    AVCodecContext *pVideoCodecCtx = nullptr;
    AVCodec *pVideoCodec = nullptr;

    void decodeVideo(int videoIndex);
    void decodeAudio(int audioIndex);
};


#endif //MYPLAYER_DECODE_H
