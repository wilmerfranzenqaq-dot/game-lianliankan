package model;

import java.awt.*;

/**
 * 屏幕空间矩形 — 描述格子在界面上的像素区域
 * 用于将棋盘坐标映射为屏幕坐标，供渲染和点击检测使用
 */
public class Rectangle {

    int x;
    int y;
    int width;
    int height;

    public Rectangle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    // ── 访问器 ──

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /** 返回矩形中心点的像素坐标，用于绘制连接线 */
    public Point getCenterPosition() {
        return new Point(x + width / 2, y + height / 2);
    }
}
