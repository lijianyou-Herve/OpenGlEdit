package com.example.opengledit.opengl.drawer;

public interface IRender {
    /**
     * 创建
     */
    void onCreate();

    /**
     * 设置尺寸
     */
    void onChange(int width, int height);

    /**
     * 绘制
     */
    void onDraw(int textureId);

    /**
     * 释放资源
     */
    void onRelease();
}
