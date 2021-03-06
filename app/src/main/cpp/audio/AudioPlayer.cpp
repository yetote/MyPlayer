//
// Created by ether on 2019/3/21.
//



#include "AudioPlayer.h"

#define null NULL


static const char *audioFormatStr[] = {
        "Invalid   非法格式", // = -1,
        "Unspecified  自动格式", // = 0,
        "I16",
        "Float",
};
static const AudioFormat audioFormatEnum[] = {
        AudioFormat::Invalid,
        AudioFormat::Unspecified,
        AudioFormat::I16,
        AudioFormat::Float,
};
static const int32_t audioFormatCount = sizeof(audioFormatEnum) /
                                        sizeof(audioFormatEnum[0]);

const char *FormatToString(AudioFormat format) {
    for (int32_t i = 0; i < audioFormatCount; ++i) {
        if (audioFormatEnum[i] == format)
            return audioFormatStr[i];
    }
    return "UNKNOW_AUDIO_FORMAT";
}

const char *audioApiToString(AudioApi api) {
    switch (api) {
        case AudioApi::AAudio:
            return "AAUDIO";
        case AudioApi::OpenSLES:
            return "OpenSL";
        case AudioApi::Unspecified:
            return "Unspecified";
    }
}

void printAudioStreamInfo(AudioStream *stream) {
    LOGE(AudioPlayer_TAG, "StreamID: %p", stream);

    LOGE(AudioPlayer_TAG, "缓冲区容量: %d", stream->getBufferCapacityInFrames());
    LOGE(AudioPlayer_TAG, "缓冲区大小: %d", stream->getBufferSizeInFrames());
    LOGE(AudioPlayer_TAG, "一次读写的帧数: %d", stream->getFramesPerBurst());
    //欠载和过载在官方文档的描述里，大致是欠载-消费者消费的速度大于生产的速度，过载就是生产的速度大于消费的速度
    LOGE(AudioPlayer_TAG, "欠载或过载的数量: %d", stream->getXRunCount());
    LOGE(AudioPlayer_TAG, "采样率: %d", stream->getSampleRate());
    LOGE(AudioPlayer_TAG, "声道布局: %d", stream->getChannelCount());
    LOGE(AudioPlayer_TAG, "音频设备id: %d", stream->getDeviceId());
    LOGE(AudioPlayer_TAG, "音频格式: %s", FormatToString(stream->getFormat()));
    LOGE(AudioPlayer_TAG, "流的共享模式: %s", stream->getSharingMode() == SharingMode::Exclusive ?
                                        "独占" : "共享");
    LOGE(AudioPlayer_TAG, "使用的音频的API：%s", audioApiToString(stream->getAudioApi()));
    PerformanceMode perfMode = stream->getPerformanceMode();
    std::string perfModeDescription;
    switch (perfMode) {
        case PerformanceMode::None:
            perfModeDescription = "默认模式";
            break;
        case PerformanceMode::LowLatency:
            perfModeDescription = "低延迟";
            break;
        case PerformanceMode::PowerSaving:
            perfModeDescription = "节能";
            break;
    }
    LOGE(AudioPlayer_TAG, "性能模式: %s", perfModeDescription.c_str());


    Direction dir = stream->getDirection();
    LOGE(AudioPlayer_TAG, "流方向: %s", (dir == Direction::Output ? "OUTPUT" : "INPUT"));
    if (dir == Direction::Output) {
        LOGE(AudioPlayer_TAG, "输出流读取的帧数: %d", (int32_t) stream->getFramesRead());
        LOGE(AudioPlayer_TAG, "输出流写入的帧数: %d", (int32_t) stream->getFramesWritten());
    } else {
        LOGE(AudioPlayer_TAG, "输入流读取的帧数: %d", (int32_t) stream->getFramesRead());
        LOGE(AudioPlayer_TAG, "输入流写入的帧数: %d", (int32_t) stream->getFramesWritten());
    }
}

void AudioPlayer::initOboe() {
    AudioStreamBuilder builder;
    setBuilderParams(&builder);

    result = builder.openStream(&stream);
    if (result != Result::OK) {
        LOGE(AudioPlayer_TAG, "打开流失败，error：%s", convertToText(result));
        return;
    }
    if (stream == nullptr) {
        LOGE(AudioPlayer_TAG, "创建流失败");
        return;
    }
    printAudioStreamInfo(stream);
    latencyTuner = new LatencyTuner(*stream);
}

AudioPlayer::AudioPlayer(PlayerStatus *playerStatus) {
    this->playerStatus = playerStatus;
    dataArray = new uint8_t[44100 * 2 * 4];
    outBuffer = static_cast<uint8_t *>(av_malloc(MAX_AUDIO_FRAME_SIZE));
    audioQueue = new BlockQueue(100);
}

void AudioPlayer::setBuilderParams(AudioStreamBuilder *builder) {
    builder->setChannelCount(ChannelCount::Stereo);
    builder->setDirection(Direction::Output);
    builder->setFormat(AudioFormat::I16);
    builder->setSampleRate(44100);
    builder->setPerformanceMode(PerformanceMode::LowLatency);
    builder->setSharingMode(SharingMode::Exclusive);
    builder->setCallback(this);
}

oboe::DataCallbackResult
AudioPlayer::onAudioReady(oboe::AudioStream *audioStream, void *audioData, int32_t numFrames) {

    latencyTuner->tune();
    auto buffer = static_cast<uint8_t *>(audioData);
    LOGE(AudioPlayer_TAG, "line in 128:currentTime=%d\nlastTime=%d", currentTime, lastTime);
    if (lastTime > currentTime) {
        lastTime = currentTime;
    }
    if (currentTime - lastTime >= 0.5) {
        playerStatus->callPlaying(currentTime);
        lastTime = currentTime;
    }

    while (remainSize < numFrames * 4) {
        checkSize(numFrames);
    }
    int readSize = 0;
    for (int i = 0; i < numFrames * 4; ++i) {
        buffer[i] = dataArray[i];
        readSize++;
    }
    remainSize -= readSize;
    for (int i = 0; i < remainSize; ++i) {
        dataArray[i] = dataArray[readSize + i];
    }

    return DataCallbackResult::Continue;
}


int AudioPlayer::pop() {
    memset(outBuffer, 0, MAX_AUDIO_FRAME_SIZE);
    while (true) {
        audioQueue->pop(packet, playerStatus->isAudioDecodeFinish());
        LOGE(AudioPlayer_TAG, "line in 157:audioSize=%d", packet->size);
        LOGE(AudioPlayer_TAG, "line in 157:audio_packet_index=%d", packet->stream_index);
        int ret;
        ret = avcodec_send_packet(audioCodecCtx, packet);
        if (ret == 0) {
            ret = avcodec_receive_frame(audioCodecCtx, pFrame);
            if (ret == AVERROR(EAGAIN)) {
                LOGE(AudioPlayer_TAG, "读取解码数据失败%d", ret);
                continue;
            } else if (ret == AVERROR_EOF) {
                LOGE(AudioPlayer_TAG, "解码完成");
                break;
            } else if (ret < 0) {
                LOGE(AudioPlayer_TAG, "解码出错");
                continue;
            }

            int frameCount = swr_convert(pSwrCtx,
                                         &outBuffer,
                                         44100 * 2,
                                         (const uint8_t **) (pFrame->data),
                                         pFrame->nb_samples);
            int outBufferSize = av_samples_get_buffer_size(nullptr, outChannelNum,
                                                           frameCount,
                                                           AV_SAMPLE_FMT_S16, 1);
            LOGE(AudioPlayer_TAG, "line in 187:时间为%f", pFrame->pts * av_q2d(timeBase));
            currentTime = pFrame->pts * av_q2d(timeBase);
            if (currentTime > totalTime) {
                currentTime = lastTime;
            }
            memcpy(dataArray + remainSize, outBuffer, size_t(outBufferSize));
            LOGE(AudioPlayer_TAG, "line in 191:outbufferSize=%d", outBufferSize);
            return outBufferSize;
        }
    }
    return 0;
}


void AudioPlayer::play() {
    LOGE(AudioPlayer_TAG, "play");
    packet = av_packet_alloc();
    pFrame = av_frame_alloc();
    pSwrCtx = swr_alloc();
    //采样格式
    enum AVSampleFormat inSampleFmt = audioCodecCtx->sample_fmt;
    enum AVSampleFormat outSampleFmt = AV_SAMPLE_FMT_S16;
    //采样率
    int inSampleRate = audioCodecCtx->sample_rate;
    int outSampleRate = 44100;
    //声道类别
    uint64_t inSampleChannel = audioCodecCtx->channel_layout;
    uint64_t outSampleChannel = AV_CH_LAYOUT_STEREO;
    //添加配置
    swr_alloc_set_opts(pSwrCtx,
                       outSampleChannel,
                       outSampleFmt,
                       outSampleRate,
                       inSampleChannel,
                       inSampleFmt,
                       inSampleRate,
                       0,
                       NULL);
    swr_init(pSwrCtx);
    outChannelNum = av_get_channel_layout_nb_channels(outSampleChannel);
    isPlaying = true;
    result = stream->requestStart();
    if (result != Result::OK) {
        LOGE(AudioPlayer_TAG, "请求打开流失败%s", convertToText(result));
        return;
    }
    latencyTuner = new LatencyTuner(*stream);
}

AudioPlayer::~AudioPlayer() {
    av_packet_free(&packet);
    av_frame_free(&pFrame);
    av_free(packet);
    av_free(pFrame);
    packet = nullptr;
    pFrame = nullptr;
    delete latencyTuner;
    delete[] dataArray;
    delete audioQueue;
    LOGE(AudioPlayer_TAG,"line in 241:destroyAudio");
}

void AudioPlayer::pause() {
    stream->requestPause();
}

void AudioPlayer::clear() {
    audioQueue->clear();
    memset(dataArray, 0, 44100 * 2 * 4);
    remainSize = 0;
    lastTime = 0;
}

void AudioPlayer::checkSize(int32_t numFrames) {
    if (remainSize >= numFrames * 4) {
        return;
    }
    memset(dataArray + remainSize, 0, MAX_AUDIO_FRAME_SIZE - remainSize);
    int addSize = pop();
    remainSize = addSize + remainSize;
}

void AudioPlayer::recover() {
    stream->requestStart();
}

void AudioPlayer::stop() {
    stream->requestStop();
    clear();
}
