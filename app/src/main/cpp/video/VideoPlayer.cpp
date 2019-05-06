//
// Created by ether on 2019/3/14.
//



#include "VideoPlayer.h"
#include "../util/FFmpegError.h"


EGLUtils *eglUtils;
GLUtils *glUtils;


void VideoPlayer::init() {
    //    @formatter:off
    vertexArr = new GLfloat[12]{
             1.0F,  1.0F,
            -1.0F,  1.0F,
            -1.0F, -1.0F,
            -1.0F, -1.0F,
             1.0F, -1.0F,
             1.0F,  1.0F,
    };
    textureArr = new GLfloat[12]{
            1.0F, 0.0F,
            0.0F, 0.0F,
            0.0F, 1.0F,
            0.0F, 1.0F,
            1.0F, 1.0F,
            1.0F, 0.0F
    };
     colorArr = new GLfloat[18]{
            0.0F, 0.0F, 0.0F,
            0.0F, 0.0F, 1.0F,
            0.0F, 1.0F, 0.0F,
            0.0F, 1.0F, 0.0F,
            1.0F, 1.0F, 0.0F,
            1.0F, 1.0F, 1.0F
    };
    //    @formatter:on

    textureIds = glUtils->createTexture();
    if (textureIds == nullptr) {
        LOGE(VideoPlayer_TAG, "创建texture数组失败");
        return;
    }

    getLocation();
    glClearColor(1.0F, 0.0F, 1.0F, 0.0F);
}

void VideoPlayer::drawFrame(AVFrame *frame) {
    glClear(GL_COLOR_BUFFER_BIT || GL_DEPTH_BUFFER_BIT);
    glUseProgram(glUtils->program);
    bindTexture(frame);

    glVertexAttribPointer(aPosition, 2, GL_FLOAT, GL_FALSE, 0, vertexArr);
    glEnableVertexAttribArray(aPosition);
    glVertexAttribPointer(aTextureCoordinates, 2, GL_FLOAT, GL_FALSE, 0, textureArr);
    glEnableVertexAttribArray(aTextureCoordinates);

    glDrawArrays(GL_TRIANGLES, 0, 6);
    eglSwapBuffers(eglUtils->eglDisplay, eglUtils->eglSurface);
    av_usleep(20000);
}


void VideoPlayer::play(int w, int h) {
    eglUtils = new EGLUtils(window);
    glUtils = new GLUtils(vertexCode, fragCode);
    init();
    LOGE(VideoPlayer_TAG, "开始播放");
    glViewport(0, 0, w, h);
    AVPacket *packet = av_packet_alloc();
    AVFrame *pFrame = av_frame_alloc();
    int rst;
    while (!playerStatus->isStop()) {
        if (!playerStatus->isPause()) {

            isFinish = videoQueue->pop(packet, false);
            LOGE(VideoPlayer_TAG, "line in 80:videoSize=%d", packet->size);
            rst = avcodec_send_packet(pVideoCodecCtx, packet);
            if (rst >= 0) {
                rst = avcodec_receive_frame(pVideoCodecCtx, pFrame);
                if (rst == AVERROR(EAGAIN)) {
                    LOGE(VideoPlayer_TAG, "读取解码数据失败%d", rst);
                    continue;
                } else if (rst == AVERROR_EOF) {
                    LOGE(VideoPlayer_TAG, "%s", "EOF解码完成");
                    break;
                } else if (rst < 0) {
                    LOGE(VideoPlayer_TAG, "%s", "解码出错");
                    continue;
                }
                if (pFrame->format == AV_PIX_FMT_YUV420P) {
                    LOGE(VideoPlayer_TAG, "line in 109:解码成功");
                    drawFrame(pFrame);
                } else {
                    AVFrame *pFrame420P = av_frame_alloc();
                    int num = av_image_get_buffer_size(AV_PIX_FMT_YUV420P,
                                                       pVideoCodecCtx->width,
                                                       pVideoCodecCtx->height, 1);
                    uint8_t *buffer = static_cast<uint8_t *>(av_malloc(num * sizeof(uint8_t)));
                    av_image_fill_arrays(pFrame420P->data,
                                         pFrame420P->linesize,
                                         buffer,
                                         AV_PIX_FMT_YUV420P,
                                         pVideoCodecCtx->width,
                                         pVideoCodecCtx->height,
                                         1);
                    SwsContext *swsContext = sws_getContext(pVideoCodecCtx->width,
                                                            pVideoCodecCtx->height,
                                                            pVideoCodecCtx->pix_fmt,
                                                            pVideoCodecCtx->width,
                                                            pVideoCodecCtx->height,
                                                            AV_PIX_FMT_YUV420P,
                                                            SWS_BICUBIC, null, null, null
                    );
                    sws_scale(swsContext,
                              pFrame->data,
                              pFrame->linesize,
                              0,
                              pFrame->height,
                              pFrame420P->data,
                              pFrame420P->linesize);
                    drawFrame(pFrame420P);
                    av_frame_free(&pFrame420P);
                    av_free(pFrame420P);
                    av_free(buffer);
                    pFrame420P = nullptr;
                    sws_freeContext(swsContext);
                    LOGE(VideoPlayer_TAG, "line in 144:解码成功");
                }
            }
        } else {
            LOGE(VideoPlayer_TAG, "line in 137:暂停中");
            av_usleep(1000);
        }

    }
}


VideoPlayer::VideoPlayer(PlayerStatus *playerStatus, const char *vertexCode, const char *fragCode,
                         ANativeWindow *window) {
    this->vertexCode = vertexCode;
    this->fragCode = fragCode;
    this->window = window;
    this->playerStatus = playerStatus;
    videoQueue = new BlockQueue(200);

}

VideoPlayer::~VideoPlayer() {
    if (eglUtils != nullptr) {
        delete eglUtils;
        eglUtils = nullptr;
    }
    if (glUtils != nullptr) {
        delete glUtils;
        glUtils = nullptr;
    }
}

void VideoPlayer::getLocation() {
    aPosition = glGetAttribLocation(glUtils->program, "a_Position");
//    aColor = glGetAttribLocation(glUtils->program, "a_Color");
    aTextureCoordinates = glGetAttribLocation(glUtils->program, "a_TextureCoordinates");
    uTextureY = glGetUniformLocation(glUtils->program, "u_TextureY");
    uTextureU = glGetUniformLocation(glUtils->program, "u_TextureU");
    uTextureV = glGetUniformLocation(glUtils->program, "u_TextureV");

    uTextureArr = new GLint[3]{
            uTextureY, uTextureU, uTextureV
    };
}


void VideoPlayer::bindTexture(AVFrame *frame) {
    for (int i = 0; i < 3; ++i) {
        glActiveTexture(GL_TEXTURE0 + i);
        glBindTexture(GL_TEXTURE_2D, textureIds[i]);
        if (i == 0) {
            glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, frame->width, frame->height, 0,
                         GL_LUMINANCE, GL_UNSIGNED_BYTE, frame->data[i]);
        } else {
            glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, frame->width / 2, frame->height / 2, 0,
                         GL_LUMINANCE, GL_UNSIGNED_BYTE, frame->data[i]);
        }
        glUniform1i(uTextureArr[i], i);
    }
}


bool VideoPlayer::pause() {

}

void VideoPlayer::clear() {
    videoQueue->clear();
}


