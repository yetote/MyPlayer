//
// Created by ether on 2019/3/21.
//

#ifndef MYPLAYER_AUDIOPLAYER_H
#define MYPLAYER_AUDIOPLAYER_H


#include "../util/BlockQueue.h"
#include  <oboe/Oboe.h>
#include <thread>

extern "C" {
#include <libswresample/swresample.h>
#include "../ffmpeg/includes/libavformat/avformat.h"
#include "../ffmpeg/includes/libavutil/frame.h"
#include "../ffmpeg/includes/libavcodec/avcodec.h"
#include "../ffmpeg/includes/libavutil/time.h"
#include "../ffmpeg/includes/libavutil/imgutils.h"
#include "../ffmpeg/includes/libswscale/swscale.h"
};


#define LOG_TAG "AudioPlayer"
#define MAX_AUDIO_FRAME_SIZE 44100*4
using namespace oboe;

class AudioPlayer : AudioStreamCallback {
public:
    int32_t channelNum;
    int32_t sampleRate;

    AVCodecContext *audioCodecCtx;

    AudioPlayer(PlayerStatus *);

    ~AudioPlayer();


    void setData(AVPacket *packet);

    void play();

    bool pause();


    void initOboe();

    void clear();

private:
    Result result;
    bool isPlaying;
    AVPacket *packet;
    AVFrame *pFrame;
    SwrContext *pSwrCtx;
    AudioStream *stream;
    PlayerStatus *playerStatus;
    LatencyTuner *latencyTuner;

    DataCallbackResult
    onAudioReady(oboe::AudioStream *audioStream, void *audioData, int32_t numFrames);

    void setBuilderParams(AudioStreamBuilder *pBuilder);

    int pop();

    static void push(AVPacket *packet);

    uint8_t *dataArray;
    uint8_t *outBuffer;
    int outChannelNum;
    int currentTime, lastTime;
    int32_t remainSize;

    void checkSize(int32_t frames);

    AVRational timeBase;
};


#endif //MYPLAYER_AUDIOPLAYER_H
