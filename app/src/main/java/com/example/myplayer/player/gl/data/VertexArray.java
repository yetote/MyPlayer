package com.example.myplayer.player.gl.data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glVertexAttribPointer;

public class VertexArray {
private final FloatBuffer floatBuffer;

    public VertexArray(float[] vertexData) {
        floatBuffer= ByteBuffer
                .allocateDirect(vertexData.length*4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);
    }

    /**
     * 在缓冲区开辟区域
     * @param dataOffset 起始位置
     * @param attributeLocation 常量位置
     * @param componentCount 属性包含数据数量
     * @param stride  跨距
     */
    public void setVertexAttributePointer(int dataOffset,int attributeLocation,int componentCount,int stride){
        floatBuffer.position(dataOffset);
        glVertexAttribPointer(attributeLocation,componentCount,GL_FLOAT,false,stride,floatBuffer);
        glEnableVertexAttribArray(attributeLocation);
        floatBuffer.position(0);
    }

    /**
     * 向缓冲区添加数据（重复使用）
     * @param vertexData 要添加的数据
     * @param start 起始位置
     * @param count 数据个数
     */
    public void updateBuffer(float[] vertexData, int start, int count) {
        floatBuffer.position(start);
        floatBuffer.put(vertexData, start, count);
        floatBuffer.position(0);
    }
}
