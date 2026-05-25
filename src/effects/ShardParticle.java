package effects;

import java.awt.*;

/**
 * 碎片粒子 — 棋盘消除时飞溅的旋转三角形碎片
 *
 * 相比父类 {@link Particle}，额外增加了旋转、重力模拟，
 * 使碎片呈现"炸开→下落→旋转消失"的视觉效果。
 */
public class ShardParticle extends Particle {

    // ── 当前旋转角度（弧度） ──
    public float rotation;

    // ── 每帧旋转增量（弧度/帧），正值顺时针，负值逆时针 ──
    public float rotationSpeed;

    // ── 重力加速度（像素/帧²），使碎片向下坠落 ──
    public float gravity;

    /**
     * 逐帧更新：匀速直线运动 + 重力下坠 + 旋转 + 生命衰减
     */
    @Override
    public void update() {
        x += vx;
        y += vy;
        vy += gravity;          // Y 轴方向叠加重力
        rotation += rotationSpeed;
        life -= 0.015f;         // 略慢于 Particle 的衰减，碎片存活更久
    }

    /**
     * 绘制旋转三角形碎片
     *
     * 不使用 AffineTransform + Graphics2D.create/dispose，
     * 而是手动计算三个顶点绕中心旋转后的坐标，
     * 避免每帧创建 Graphics 副本的 GC 开销。
     */
    @Override
    public void draw(Graphics2D g) {
        if (life <= 0) return;

        int alpha = (int) (life * 255);
        g.setColor(new Color(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                alpha
        ));

        // ── 三角形三个顶点相对于中心点的偏移 ──
        // 顶点布局（从左上角 (0,0) 计）:
        //    (0,0) ─── (size,0)
        //       \       /
        //        (size/2, size)
        int half = (int) (size / 2);
        int cx = (int) (x + half);      // 旋转中心 X
        int cy = (int) (y + half);      // 旋转中心 Y
        double cos = Math.cos(rotation);
        double sin = Math.sin(rotation);

        int[] rx = new int[3];
        int[] ry = new int[3];

        // 顶点 0: 左上角 (-half, -half) 绕中心旋转
        double dx0 = -half, dy0 = -half;
        rx[0] = (int) (cx + dx0 * cos - dy0 * sin);
        ry[0] = (int) (cy + dx0 * sin + dy0 * cos);

        // 顶点 1: 右上角 (+half, -half) 绕中心旋转
        double dx1 = half, dy1 = -half;
        rx[1] = (int) (cx + dx1 * cos - dy1 * sin);
        ry[1] = (int) (cy + dx1 * sin + dy1 * cos);

        // 顶点 2: 下中点 (0, +half) 绕中心旋转
        double dx2 = 0, dy2 = half;
        rx[2] = (int) (cx + dx2 * cos - dy2 * sin);
        ry[2] = (int) (cy + dx2 * sin + dy2 * cos);

        g.fillPolygon(rx, ry, 3);
    }
}
