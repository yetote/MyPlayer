//
// Created by ether on 2019/3/13.
//



#include "Decode.h"


#define  null NULL


void Decode::prepare(const char *path, const char *vertexCode, const char *fragCode,
                     ANativeWindow *window) {

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
    if (audioIndex != -1) {
        std::thread decodeAudioThread(&Decode::decodeAudio, this, audioIndex);
        decodeAudioThread.detach();
//        return;
    } else {
        LOGE("decode", "未找到音频流");
        playerStatus->setAudioPrepare(true);
        playerStatus->setAudioDecodeFinish(true);
    }
    if (videoIndex != -1) {
//        return;
        std::thread decodeVideoThread(&Decode::decodeVideo, this, videoIndex, vertexCode, fragCode,
                                      window);
        decodeVideoThread.detach();
    } else {
        LOGE("decode", "未找到视频流");
        playerStatus->setVideoPrepare(true);
        playerStatus->setVideoDecodeFinish(true);
    }

}

void Decode::decodeVideo(int videoIndex, const char *vertexCode, const char *fragCode,
                         ANativeWindow *window) {
    videoPlayer = new VideoPlayer(playerStatus, vertexCode, fragCode, window);
    int rst = 0;
    pVideoStream = pFmtCtx->streams[videoIndex];
    pVideoCodec = avcodec_find_decoder(pVideoStream->codecpar->codec_id);
    if (pVideoCodec == nullptr) {
        LOGE("decode", "未找到视频解码器");
        return;
    }
    videoPlayer->pVideoCodecCtx = avcodec_alloc_context3(pVideoCodec);
    if (videoPlayer->pVideoCodecCtx == nullptr) {
        LOGE("decode", "无法获取视频解码器环境");
        return;
    }
    rst = avcodec_parameters_to_context(videoPlayer->pVideoCodecCtx, pVideoStream->codecpar);
    if (rst < 0) {
        LOGE("decode", "无法复制视频解码器环境");
        return;
    }
    rst = avcodec_open2(videoPlayer->pVideoCodecCtx, pVideoCodec, null);
    if (rst != 0) {
        LOGE("decode", "打开视频解码器失败");
        return;
    }

    AVPacket *packet = av_packet_alloc();
    int i = 0;
    playerStatus->setVideoPrepare(true);
    playerStatus->checkPrepare();
    while (av_read_frame(pFmtCtx, packet) >= 0) {
        if (packet->stream_index == videoIndex) {
            videoPlayer->setData(packet);
            i++;
            LOGE("decode", "解码了%d帧", i);
            av_usleep(30000);
        }
    }
    playerStatus->setVideoDecodeFinish(true);
    av_packet_free(&packet);
    av_free(packet);
}

void Decode::decodeAudio(int audioIndex) {
    audioPlayer = new AudioPlayer(playerStatus);
    int rst = 0;
    pAudioStream = pFmtCtx->streams[audioIndex];
    pAudioCodec = avcodec_find_decoder(pAudioStream->codecpar->codec_id);
    if (pAudioCodec == nullptr) {
        LOGE("decode", "未找到音频解码器");
        return;
    }
    audioPlayer->audioCodecCtx = avcodec_alloc_context3(pAudioCodec);
    if (audioPlayer->audioCodecCtx == nullptr) {
        LOGE("decode", "无法获取音频解码器环境");
        return;
    }
    rst = avcodec_parameters_to_context(audioPlayer->audioCodecCtx, pAudioStream->codecpar);
    if (rst < 0) {
        LOGE("decode", "无法复制音频解码器环境");
        return;
    }
    rst = avcodec_open2(audioPlayer->audioCodecCtx, pAudioCodec, null);
    if (rst != 0) {
        LOGE("decode", "打开音频解码器失败");
        return;
    }
    AVPacket *packet = av_packet_alloc();
    audioPlayer->channelNum = pAudioStream->codecpar->channels;
    audioPlayer->sampleRate = pAudioStream->codecpar->sample_rate;
    audioPlayer->initOboe();
    playerStatus->setAudioPrepare(true);
    playerStatus->checkPrepare();
    int count = 0;
    int rst1 = 0;
    while (!playerStatus->isPause()) {
        rst1 = av_read_frame(pFmtCtx, packet);
        if (rst1 >= 0) {
            if (packet->stream_index == audioIndex) {
                audioPlayer->setData(packet);
                count++;
                LOGE(LOG_TAG, "音频解码了%d帧", count);
            }
        } else {
            LOGE(LOG_TAG, "读取失败%d", rst1);
        }
    }
    LOGE(LOG_TAG, "完成解码");

    playerStatus->setAudioDecodeFinish(true);
    av_packet_free(&packet);
    av_free(packet);
}

Decode::Decode(PlayerStatus *playerStatus) {
    this->playerStatus = playerStatus;
//    videoPlayer = new VideoPlayer(vertexCode, fragCode, window);
}

void Decode::play(int w, int h) {
    std::thread audioPlayThread(&Decode::audioPlay, this);
    audioPlayThread.detach();
    std::thread videoPlayThread(&Decode::videoPlay, this, w, h);
    videoPlayThread.detach();
}

void Decode::audioPlay() {
    audioPlayer->play();
}

void Decode::decode() {

}

void Decode::videoPlay(int w, int h) {
//    videoPlayer->play(w, h);
}


void Decode::pause() {
    playerStatus->setPause(true);

}

Decode::~Decode() {

}

void Decode::recover() {
    playerStatus->setPause(false);
}
