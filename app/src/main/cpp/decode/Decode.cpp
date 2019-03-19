//
// Created by ether on 2019/3/13.
//



#include "Decode.h"


#define  null NULL

void Decode::prepare(const char *path) {
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
    if (audioIndex == -1) {
        LOGE("decode", "未找到音频流");
        return;
    }
    if (videoIndex == -1) {
        LOGE("decode", "未找到视频流");
        return;
    }
    callBack->onPrepare(callBack->CHILD_THREAD, true, 0);
    decodeAudio(audioIndex);
    decodeVideo(videoIndex);
}

void Decode::decodeVideo(int videoIndex) {
    videoPlayer->init();
    int rst = 0;
    pVideoStream = pFmtCtx->streams[videoIndex];
    pVideoCodec = avcodec_find_decoder(pVideoStream->codecpar->codec_id);
    if (pVideoCodec == nullptr) {
        LOGE("decode", "未找到视频解码器");
        return;
    }
    pVideoCodecCtx = avcodec_alloc_context3(pVideoCodec);
    if (pVideoCodecCtx == nullptr) {
        LOGE("decode", "无法获取视频解码器环境");
        return;
    }
    rst = avcodec_parameters_to_context(pVideoCodecCtx, pVideoStream->codecpar);
    if (rst < 0) {
        LOGE("decode", "无法复制视频解码器环境");
        return;
    }
    rst = avcodec_open2(pVideoCodecCtx, pVideoCodec, null);
    if (rst != 0) {
        LOGE("decode", "打开视频解码器失败");
        return;
    }

    AVPacket *packet = av_packet_alloc();
    int i = 0;
    while (av_read_frame(pFmtCtx, packet) >= 0) {
        if (packet->stream_index == videoIndex) {
            videoPlayer->setData(packet);
            i++;
            LOGE("decode", "解码了%d帧", i);
            av_usleep(30000);
        }
    }
    av_packet_free(&packet);
    avcodec_free_context(&pVideoCodecCtx);
    videoPlayer->setState(true);
}

void Decode::decodeAudio(int audioIndex) {

    int rst = 0;
    pAudioStream = pFmtCtx->streams[audioIndex];
    pAudioCodec = avcodec_find_decoder(pAudioStream->codecpar->codec_id);
    if (pAudioCodec == nullptr) {
        LOGE("decode", "未找到音频解码器");
        return;
    }
    pAudioCodecCtx = avcodec_alloc_context3(pAudioCodec);
    if (pAudioCodecCtx == nullptr) {
        LOGE("decode", "无法获取音频解码器环境");
        return;
    }
    rst = avcodec_parameters_to_context(pAudioCodecCtx, pAudioStream->codecpar);
    if (rst < 0) {
        LOGE("decode", "无法复制音频解码器环境");
        return;
    }
    rst = avcodec_open2(pAudioCodecCtx, pAudioCodec, null);
    if (rst != 0) {
        LOGE("decode", "打开音频解码器失败");
        return;
    }

}

Decode::Decode(CallBack *callback) {
    this->callBack = callback;
    videoPlayer = new VideoPlayer();
}

void Decode::play() {
    LOGE("decode", "开始播放");
    AVPacket *packet = av_packet_alloc();
    AVFrame *pFrame = av_frame_alloc();
    int num = av_image_get_buffer_size(AV_PIX_FMT_YUV420P, pFrame->width,
                                       pFrame->height,
                                       1);
    uint8_t *buffer = static_cast<uint8_t *>(av_malloc(num * sizeof(uint8_t)));
    av_image_fill_arrays(pFrame->data,
                         pFrame->linesize,
                         buffer,
                         AV_PIX_FMT_YUV420P,
                         pFrame->width,
                         pFrame->height,
                         1);
//    SwsContext *swsContext = sws_getContext(pVideoCodecCtx->width,
//                                            pVideoCodecCtx->height,
//                                            pVideoCodecCtx->pix_fmt,
//                                            pVideoCodecCtx->width,
//                                            pVideoCodecCtx->height,
//                                            AV_PIX_FMT_YUV420P,
//                                            SWS_BICUBIC,
//                                            null, null, null);
    while (!videoPlayer->isFinish) {
        int rst;
        videoPlayer->play(packet);
        rst = avcodec_send_packet(pVideoCodecCtx, packet);
        if (rst < 0) {
            LOGE("decode", "发送packet失败，错误码%d", rst);
            continue;
        }
        while (rst >= 0) {
            rst = avcodec_receive_frame(pVideoCodecCtx, pFrame);
            LOGE("decode", "接受%d", rst);
            if (rst == AVERROR(EAGAIN)) {
                LOGE("decode", "%s", "读取解码数据失败");
                break;
            } else if (rst == AVERROR_EOF) {
                LOGE("decode", "%s", "解码完成");
                return;
            } else if (rst < 0) {
                LOGE("decode", "%s", "解码出错");
                break;
            }
        }
//        sws_scale(swsContext, pFrame->data, pFrame->linesize, 0, pFrame->height,
//                  pFrame->data, pFrame->linesize);
        LOGE("decode", "format%d", pFrame->format == AV_PIX_FMT_YUV420P);
    }
    LOGE("decode", "解码完成");
    av_packet_free(&packet);

}
