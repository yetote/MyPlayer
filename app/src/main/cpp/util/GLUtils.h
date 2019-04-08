//
// Created by ether on 2019/4/3.
//

#ifndef MYPLAYER_GLUTILS_H
#define MYPLAYER_GLUTILS_H

#include <GLES2/gl2.h>
#include "Log.h"

#define LOG_TAG "GLUtils"
#define null NULL

class GLUtils {
public:
    GLUtils(const char *vertexCode, const char *fragCode);

    ~GLUtils();

    GLuint program;

    void createProgram(const char *vertexCode, const char *fragCode);

    GLuint *createTexture();

private:
    GLuint loadShader(GLenum type, const char *code);
};


#endif //MYPLAYER_GLUTILS_H
