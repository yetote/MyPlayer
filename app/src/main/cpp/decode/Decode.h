//
// Created by ether on 2019/3/13.
//

#ifndef MYPLAYER_DECODE_H
#define MYPLAYER_DECODE_H

#include <string>
#include "../util/Log.h"
#include "../util/CallBack.h"
#include "../video/VideoPlayer.h"
#include "../util/PlayerStatus.h"
#include <pthread.h>
#include "../audio/AudioPlayer.h"

extern "C" {
#include "../ffmpeg/includes/libavformat/avformat.h"
#include "../ffmpeg/includes/libavutil/frame.h"
#include "../ffmpeg/includes/libavcodec/avcodec.h"
#include "../ffmpeg/includes/libavutil/time.h"
#include "../ffmpeg/includes/libavutil/imgutils.h"
#include "../ffmpeg/includes/libswscale/swscale.h"
};
#define Decode_TAG "Decode"

class Decode {
public:

    Decode(PlayerStatus *);

    void prepare(const char *path, const char *vertexCode, const char *fragCode,
                 ANativeWindow *window);

    void play(int w, int h);

    void audioPlay();

    void videoPlay(int w, int h);

    void pause();

    void recover();

    void seek(int secs);
    void stop();
    void rotate(int ,int);
    ~Decode();

private:
    PlayerStatus *playerStatus;
    int audioIndex, videoIndex;
    AVFormatContext *pFmtCtx = nullptr;

    AVStream *pAudioStream = nullptr;
    AVStream *pVideoStream = nullptr;

    AVFrame *pFrame = nullptr;

    AVCodec *pAudioCodec = nullptr;

    AVCodec *pVideoCodec = nullptr;
    VideoPlayer *videoPlayer;
    AudioPlayer *audioPlayer;


    void startDecode();

    void findCodec(AVCodecContext **, AVCodec *, AVStream *);


};


#endif //MYPLAYER_DECODE_H
