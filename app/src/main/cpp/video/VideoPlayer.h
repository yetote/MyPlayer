//
// Created by ether on 2019/3/14.
//

#ifndef MYPLAYER_VIDEOPLAYER_H
#define MYPLAYER_VIDEOPLAYER_H


#include "../util/BlockQueue.h"
#include <thread>
#include "../util/EGLUtils.h"
#include "../util/GLUtils.h"

extern "C" {
#include "../ffmpeg/includes/libswscale/swscale.h"
#include "../ffmpeg/includes/libavcodec/avcodec.h"
#include "../ffmpeg/includes/libavutil/imgutils.h"
#include "../ffmpeg/includes/libavutil/time.h"
};

class VideoPlayer {
public:
    VideoPlayer(PlayerStatus *, const char *vertexCode, const char *fragCode,
                ANativeWindow *);

    ~VideoPlayer();

    AVCodecContext *pVideoCodecCtx;
    bool isFinish;
    static bool isPushFinish;

    void init();

    void setData(AVPacket *packet);

    bool pause();

    void play(int w, int h);

private:
    PlayerStatus *playerStatus;

    GLfloat *vertexArr;
    GLfloat *textureArr;
    GLfloat *colorArr;
    GLuint *textureIds = nullptr;

    GLint aPosition;
    GLint aTextureCoordinates;
    GLint aColor;
    GLint uTextureY;
    GLint uTextureU;
    GLint uTextureV;

    GLint *uTextureArr;

    const char *vertexCode;
    const char *fragCode;
    ANativeWindow *window;

    static void push(AVPacket *packet);

    void getLocation();

    void drawFrame(AVFrame *);

    void bindTexture(AVFrame *frame);

};


#endif //MYPLAYER_VIDEOPLAYER_H
