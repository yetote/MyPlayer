//
// Created by ether on 2019/3/14.
//


#include "VideoPlayer.h"


static BlockQueue videoQueue;
EGLUtils *eglUtils;
GLUtils *glUtils;

void VideoPlayer::push(AVPacket *packet) {
    videoQueue.push(packet);
}

void VideoPlayer::setData(AVPacket *packet) {
    std::thread video(push, packet);
    video.join();
}

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
    //    @formatter:on

    textureIds = glUtils->createTexture();
    if (textureIds == nullptr) {
        LOGE(LOG_TAG, "创建texture数组失败");
        return;
    }

    getLocation();
    glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
    videoQueue.init();
}

void VideoPlayer::draw(AVFrame *pFrame) {
    glClear(GL_COLOR_BUFFER_BIT || GL_DEPTH_BUFFER_BIT);
    glUseProgram(glUtils->program);
    bindTexture(pFrame);
    glVertexAttribPointer(aPosition, 2, GL_FLOAT, GL_FALSE, 0, vertexArr);
    glEnableVertexAttribArray(aPosition);
    glVertexAttribPointer(aTextureCoordinates, 2, GL_FLOAT, GL_FALSE, 0, textureArr);
    glEnableVertexAttribArray(aTextureCoordinates);
    glDrawArrays(GL_TRIANGLES, 0, 6);
    eglSwapBuffers(eglUtils->eglDisplay, eglUtils->eglSurface);
}


void VideoPlayer::play() {
    LOGE(LOG_TAG, "开始播放");
//    videoQueue.pop(packet1);
    isFinish = videoQueue.isFinish;
    AVPacket *packet = av_packet_alloc();
    AVFrame *pFrame = av_frame_alloc();
    int rst;
    while (!isFinish) {
        videoQueue.pop(packet);
        rst = avcodec_send_packet(pVideoCodecCtx, packet);
        while (rst >= 0) {
            rst = avcodec_receive_frame(pVideoCodecCtx, pFrame);
            if (rst == AVERROR(EAGAIN)) {
                LOGE(LOG_TAG, "%s", "读取解码数据失败");
                break;
            } else if (rst == AVERROR_EOF) {
                LOGE(LOG_TAG, "%s", "EOF解码完成");
                break;
            } else if (rst < 0) {
                LOGE(LOG_TAG, "%s", "解码出错");
                break;
            }
            if (pFrame->format == AV_PIX_FMT_YUV420P) {
                LOGE(LOG_TAG, "获取frame成功,解码后的格式是YUV420P");
            } else {
                LOGE(LOG_TAG, "获取frame成功,格式为%d", pFrame->format);
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
                av_frame_free(&pFrame420P);
                av_free(pFrame420P);
                av_free(buffer);
                pFrame420P = null;
                sws_freeContext(swsContext);
            }
            draw(pFrame);
        }
    }
}

void VideoPlayer::setState(bool isPush) {
    videoQueue.setState(isPush);
}

VideoPlayer::VideoPlayer(const char *vertexCode, const char *fragCode, ANativeWindow *window) {
    eglUtils = new EGLUtils(window);
    glUtils = new GLUtils(vertexCode, fragCode);

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
    aPosition = glGetAttribLocation(glUtils->program, "a_position");
    aTextureCoordinates = glGetAttribLocation(glUtils->program, "a_TextureCoordinates");
    uTextureY = glGetUniformLocation(glUtils->program, "u_TextureY");
    uTextureU = glGetUniformLocation(glUtils->program, "u_TextureU");
    uTextureV = glGetUniformLocation(glUtils->program, "u_TextureV");
    uTextureArr = new GLint[3]{
            uTextureY, uTextureU, uTextureV
    };
}

void VideoPlayer::stop() {

}

void VideoPlayer::bindTexture(AVFrame *pFrame) {
    for (int i = 0; i < 3; ++i) {
        glActiveTexture(GL_TEXTURE0 + i);
        glBindTexture(GL_TEXTURE_2D, textureIds[i]);
        if (i == 0) {
            glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, pFrame->width, pFrame->height, 0,
                         GL_LUMINANCE, GL_UNSIGNED_BYTE, pFrame->data[i]);
        } else {
            glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, pFrame->width / 2, pFrame->height / 2, 0,
                         GL_LUMINANCE, GL_UNSIGNED_BYTE, pFrame->data[i]);
        }
        glUniform1i(uTextureArr[i], i);
    }
}


