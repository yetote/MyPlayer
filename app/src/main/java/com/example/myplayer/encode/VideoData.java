package com.example.myplayer.encode;

/**
 * @author ether QQ:503779938
 * @name MyPlayer
 * @class name：com.example.myplayer.encode
 * @class describe
 * @time 2019/5/22 17:05
 * @change
 * @chang time
 * @class describe
 */
public class VideoData {
    private long pts;
    private byte[] data;

    public VideoData(long pts, byte[] data) {
        this.pts = pts;
        this.data = data;
    }

    public long getPts() {
        return pts;
    }

    public byte[] getData() {
        return data;
    }
}
