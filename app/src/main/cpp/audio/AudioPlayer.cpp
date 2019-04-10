//
// Created by ether on 2019/3/21.
//



#include "AudioPlayer.h"

#define null NULL

static BlockQueue audioQueue;
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
    LOGE(LOG_TAG, "StreamID: %p", stream);

    LOGE(LOG_TAG, "缓冲区容量: %d", stream->getBufferCapacityInFrames());
    LOGE(LOG_TAG, "缓冲区大小: %d", stream->getBufferSizeInFrames());
    LOGE(LOG_TAG, "一次读写的帧数: %d", stream->getFramesPerBurst());
    //欠载和过载在官方文档的描述里，大致是欠载-消费者消费的速度大于生产的速度，过载就是生产的速度大于消费的速度
    LOGE(LOG_TAG, "欠载或过载的数量: %d", stream->getXRunCount());
    LOGE(LOG_TAG, "采样率: %d", stream->getSampleRate());
    LOGE(LOG_TAG, "声道布局: %d", stream->getChannelCount());
    LOGE(LOG_TAG, "音频设备id: %d", stream->getDeviceId());
    LOGE(LOG_TAG, "音频格式: %s", FormatToString(stream->getFormat()));
    LOGE(LOG_TAG, "流的共享模式: %s", stream->getSharingMode() == SharingMode::Exclusive ?
                                "独占" : "共享");
    LOGE(LOG_TAG, "使用的音频的API：%s", audioApiToString(stream->getAudioApi()));
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
    LOGE(LOG_TAG, "性能模式: %s", perfModeDescription.c_str());


    Direction dir = stream->getDirection();
    LOGE(LOG_TAG, "流方向: %s", (dir == Direction::Output ? "OUTPUT" : "INPUT"));
    if (dir == Direction::Output) {
        LOGE(LOG_TAG, "输出流读取的帧数: %d", (int32_t) stream->getFramesRead());
        LOGE(LOG_TAG, "输出流写入的帧数: %d", (int32_t) stream->getFramesWritten());
    } else {
        LOGE(LOG_TAG, "输入流读取的帧数: %d", (int32_t) stream->getFramesRead());
        LOGE(LOG_TAG, "输入流写入的帧数: %d", (int32_t) stream->getFramesWritten());
    }
}

void AudioPlayer::initOboe() {
    AudioStreamBuilder builder;
    setBuilderParams(&builder);

    result = builder.openStream(&stream);
    if (result != Result::OK) {
        LOGE(LOG_TAG, "打开流失败，error：%s", convertToText(result));
        return;
    }
    if (stream == nullptr) {
        LOGE(LOG_TAG, "创建流失败");
        return;
    }
    stream->setBufferSizeInFrames(44100 * 2 * 2);
    printAudioStreamInfo(stream);

}

AudioPlayer::AudioPlayer(PlayerStatus *playerStatus) {
    this->playerStatus = playerStatus;
}

void AudioPlayer::setBuilderParams(AudioStreamBuilder *builder) {
    builder->setChannelCount(channelNum);
    builder->setDirection(Direction::Output);
    builder->setFormat(AudioFormat::I16);
    builder->setSampleRate(44100);
    builder->setPerformanceMode(PerformanceMode::LowLatency);
    builder->setSharingMode(SharingMode::Exclusive);
    builder->setCallback(this);
    builder->setBufferCapacityInFrames(44100 * 2 * 4);
}

oboe::DataCallbackResult
AudioPlayer::onAudioReady(oboe::AudioStream *audioStream, void *audioData, int32_t numFrames) {
    LOGE(LOG_TAG, "回调");

    auto *outBuffer = static_cast<uint8_t *>(audioData);
    DataCallbackResult res = pop(outBuffer, numFrames);
//    pop(outBuffer, numFrames);
    if (res == DataCallbackResult::Stop) {
        playerStatus->setAudioPlayFinish(true);
        playerStatus->checkFinish();
    }
    return res;
}

void AudioPlayer::setData(AVPacket *packet) {
    std::thread audio(push, packet);
    audio.join();
}

void AudioPlayer::push(AVPacket *packet) {
    audioQueue.push(packet);
}


oboe::DataCallbackResult AudioPlayer::pop(uint8_t *outBuffer, int num) {
    //todo 杂音，怀疑是oboe缓冲区或者队列的问题
    bool isFinish;
    do {
        isFinish = audioQueue.pop(packet);
        int ret;
        ret = avcodec_send_packet(audioCodecCtx, packet);
        LOGE(LOG_TAG, "%d", ret);
        while (ret >= 0) {
            ret = avcodec_receive_frame(audioCodecCtx, pFrame);
            if (ret == AVERROR(EAGAIN)) {
                LOGE(LOG_TAG, "读取解码数据失败");
                continue;
            } else if (ret == AVERROR_EOF) {
                LOGE(LOG_TAG, "解码完成");
                break;
            } else if (ret < 0) {
                LOGE(LOG_TAG, "解码出错");
                continue;
            }

            int rst = swr_convert(pSwrCtx,
                                  &outBuffer,
                                  num,
                                  (const uint8_t **) (pFrame->data),
                                  pFrame->nb_samples);
            return DataCallbackResult::Continue;
        }

    } while (!isFinish);
    return DataCallbackResult::Stop;
}


void AudioPlayer::play() {
    LOGE(LOG_TAG, "play");
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
//    int outChannelNum = av_get_channel_layout_nb_channels(outSampleChannel);
    isPlaying = true;
    result = stream->requestStart();
    if (result != Result::OK) {
        LOGE(LOG_TAG, "请求打开流失败%s", convertToText(result));
        return;
    }
}

AudioPlayer::~AudioPlayer() {
    av_packet_unref(packet);
}