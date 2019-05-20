//
// Created by ether on 2019/3/13.
//



#include "Decode.h"


#define  null NULL


void Decode::prepare(const char *path, const char *vertexCode, const char *fragCode,
                     ANativeWindow *window) {
    videoPlayer = new VideoPlayer(playerStatus, vertexCode, fragCode, window);
    audioPlayer = new AudioPlayer(playerStatus);
    int rst = 0;
    av_register_all();
    avformat_network_init();
    pFmtCtx = avformat_alloc_context();
    rst = avformat_open_input(&pFmtCtx, path, null, null);
    if (rst != 0) {
        LOGE("decode", "打开文件失败，errorcode：%d", rst);
        return;
    }
    rst = avformat_find_stream_info(pFmtCtx, null);
    if (rst < 0) {
        LOGE("decode", "寻找流信息失败，errorcode：%d", rst);
        return;
    }
    audioIndex = videoIndex = -1;
    for (int i = 0; i < pFmtCtx->nb_streams; ++i) {
        if (pFmtCtx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_AUDIO) {
            audioIndex = i;
        }
        if (pFmtCtx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
            videoIndex = i;
        }
    }
    int totalTime = pFmtCtx->duration / AV_TIME_BASE;
    if (audioIndex != -1) {
        pAudioStream = pFmtCtx->streams[audioIndex];
        findCodec(&audioPlayer->audioCodecCtx, pAudioCodec, pAudioStream);
        audioPlayer->channelNum = pAudioStream->codecpar->channels;
        audioPlayer->sampleRate = pAudioStream->codecpar->sample_rate;
        audioPlayer->initOboe();
        audioPlayer->timeBase = pAudioStream->time_base;
        audioPlayer->totalTime = totalTime;
        playerStatus->setAudioPrepare(true);
        playerStatus->checkPrepare(totalTime);
    } else {
        LOGE("decode", "未找到音频流");
        playerStatus->setAudioPrepare(true);
        playerStatus->setAudioDecodeFinish(true);
    }
    if (videoIndex != -1) {
        pVideoStream = pFmtCtx->streams[videoIndex];
        findCodec(&videoPlayer->pVideoCodecCtx, pVideoCodec, pVideoStream);
        playerStatus->setVideoPrepare(true);
        playerStatus->checkPrepare(totalTime);
    } else {
        LOGE("decode", "未找到视频流");
        playerStatus->setVideoPrepare(true);
        playerStatus->setVideoDecodeFinish(true);
    }
    LOGE(Decode_TAG, "line in 81:audioIndex=%d,\n vidoeIndex=%d", audioIndex, videoIndex);
    std::thread startDecodeThread(&Decode::startDecode, this);
    startDecodeThread.detach();
}

Decode::Decode(PlayerStatus *playerStatus) {
    this->playerStatus = playerStatus;
}

void Decode::play(int w, int h) {
//    if (audioIndex != -1) {
//        std::thread audioPlayThread(&Decode::audioPlay, this);
//        audioPlayThread.detach();
//    }
    if (videoIndex != -1) {
        std::thread videoPlayThread(&Decode::videoPlay, this, w, h);
        videoPlayThread.detach();
    }
}

void Decode::audioPlay() {
    audioPlayer->play();
}


void Decode::videoPlay(int w, int h) {
    videoPlayer->play(w, h);
}


void Decode::pause() {
    playerStatus->setPause(true);

//    audioPlayer->pause();
    LOGE(Decode_TAG, "line in 100:暂停");
}

Decode::~Decode() {
    LOGE(Decode_TAG, "line in 104:销毁");
}

void Decode::recover() {
    playerStatus->setPause(false);
//    audioPlayer->recover();
    LOGE(Decode_TAG, "line in 110:继续播放");
}

void Decode::seek(int secs) {
    int64_t rel = secs * AV_TIME_BASE;
    avformat_seek_file(pFmtCtx, -1, INT64_MIN, rel, INT64_MAX, 0);
    if (audioPlayer != nullptr) {
//        audioPlayer->clear();
//        avcodec_flush_buffers(audioPlayer->audioCodecCtx);
    }
    if (videoPlayer != nullptr) {
        videoPlayer->clear();
        avcodec_flush_buffers(videoPlayer->pVideoCodecCtx);
    }
    LOGE(Decode_TAG, "line in 124:跳转");
}

void Decode::findCodec(AVCodecContext **codecCtx, AVCodec *codec, AVStream *stream) {
    int rst;
    codec = avcodec_find_decoder(stream->codecpar->codec_id);
    if (codec == nullptr) {
        LOGE("codec", "未找到解码器");
        return;
    }
    *codecCtx = avcodec_alloc_context3(codec);
    if (*codecCtx == nullptr) {
        LOGE("codec", "无法获取解码器环境");
        return;
    }
    rst = avcodec_parameters_to_context(*codecCtx, stream->codecpar);
    if (rst < 0) {
        LOGE("codec", "无法复制解码器环境");
        return;
    }
    bool isSupport = playerStatus->checkSupport((*codecCtx)->codec->name);
//    if (isSupport) {
//        codec = nullptr;
//        LOGE(Decode_TAG, "line in 162:%s支持硬解", (*codecCtx)->codec->name);
//        char hardwareCodecName[20];
//        strcpy(hardwareCodecName, (*codecCtx)->codec->name);
//        strcat(hardwareCodecName, "_mediacodec");
//        LOGE(Decode_TAG, "line in 148:整理后的硬解格式为%s", hardwareCodecName);
//        codec = avcodec_find_decoder_by_name(hardwareCodecName);
//        if (codec == nullptr) {
//            LOGE(Decode_TAG, "line in 153:未找到对应的解码器");
//            return;
//        }
//    } else {
//        codec = avcodec_find_decoder(stream->codecpar->codec_id);
//    }
//    LOGE(Decode_TAG, "line in 151:name %s", codec->name);

    rst = avcodec_open2(*codecCtx, codec, null);
    if (rst != 0) {
        LOGE("codec", "打开解码器失败");
        return;
    }
}

void Decode::startDecode() {

    while (!playerStatus->isStop()) {
        if (audioPlayer->audioQueue->queue.size() >= 10 ||
            videoPlayer->videoQueue->queue.size() >= 10) {
            av_usleep(1000);
            continue;
        }
        AVPacket *packet = av_packet_alloc();
        AVPacket audioPacket{0};
        AVPacket videoPacket{0};
        if (av_read_frame(pFmtCtx, packet) >= 0) {
            if (packet->stream_index == audioIndex) {
//                av_packet_ref(&audioPacket, packet);
//                audioPlayer->audioQueue->push(&audioPacket);
            } else if (packet->stream_index == videoIndex) {
                av_packet_ref(&videoPacket, packet);
                videoPlayer->videoQueue->push(&videoPacket);
            }
        }
        av_packet_unref(packet);
    }
//    av_packet_free(&packet);
//    av_packet_free(&packet);
}

void Decode::stop() {
    playerStatus->setStop(true);
    if (audioPlayer != nullptr) {
        audioPlayer->stop();
    }
    if (videoPlayer != nullptr) {
        videoPlayer->stop();
    }
    LOGE(Decode_TAG, "line in 203:停止");
}

void Decode::rotate(int w, int h) {
    videoPlayer->rotate(w, h);
}




