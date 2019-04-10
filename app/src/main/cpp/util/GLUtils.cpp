//
// Created by ether on 2019/4/3.
//

#include "GLUtils.h"


void GLUtils::createProgram(const char *vertexCode, const char *fragCode) {
    program = glCreateProgram();
    if (program == 0) {
        char szLog[1024] = {0};
        GLsizei logLen = 0;
        glGetProgramInfoLog(program, 1024, &logLen, szLog);
        LOGE(LOG_TAG, "创建program失败: %s ", szLog);
        return;
    }
    GLuint vertexId = loadShader(GL_VERTEX_SHADER, vertexCode);
    GLuint fragId = loadShader(GL_FRAGMENT_SHADER, fragCode);
    if (vertexId == 0 || fragId == 0) {
        LOGE(LOG_TAG, "创建shader失败");
        return;
    }
    glAttachShader(program, vertexId);
    glAttachShader(program, fragId);
    glLinkProgram(program);
    GLint linkStatus;
    glGetProgramiv(program, GL_LINK_STATUS, &linkStatus);
    if (linkStatus == 0) {
        glDeleteProgram(program);
        LOGE(LOG_TAG, "程序连接失败");
        return;
    }
    LOGE(LOG_TAG, "创建program成功");
}

GLuint GLUtils::loadShader(GLenum type, const char *code) {
    GLuint shader = glCreateShader(type);
    if (shader == 0) {
        LOGE(LOG_TAG, "创建shader失败");
        return 0;
    }
    glShaderSource(shader, 1, &code, null);
    glCompileShader(shader);
    GLint compileStatus = 0;
    glGetShaderiv(shader, GL_COMPILE_STATUS, &compileStatus);
    if (!compileStatus) {
        char szLog[1024] = {0};
        GLsizei logLen = 0;
        glGetShaderInfoLog(shader, 1024, &logLen, szLog);
        LOGE(LOG_TAG,"加载着色器代码失败: %s ,shader code:%s", szLog, code);
        glDeleteShader(shader);
        return 0;
    }
    return shader;
}

GLuint *GLUtils::createTexture() {
    GLuint *textureArr = new GLuint[3];
    glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
    glGenTextures(3, textureArr);
    if (textureArr[0] == 0) {
        LOGE(LOG_TAG, "创建纹理失败");
        return nullptr;
    }
    for (int i = 0; i < 3; ++i) {
        glActiveTexture(GL_TEXTURE0 + i);
        glBindTexture(GL_TEXTURE_2D, textureArr[i]);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    }
    return textureArr;
}

GLUtils::GLUtils(const char *vertexCode, const char *fragCode) {
    createProgram(vertexCode, fragCode);
}

GLUtils::~GLUtils() {

}


