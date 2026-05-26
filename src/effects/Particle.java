package effects;

import java.awt.*;

/**
 * 粒子基类 — 所有特效粒子的公共数据与行为
 *
 * 子类继承此类并重写 {@link #update()} / {@link #draw(Graphics2D)}
 * 即可获得不同的运动轨迹和渲染样式。
 */
public class Particle {

    // ── 位置（像素坐标，相对于 BoardPanel） ──
    public float x, y;

    // ── 速度分量（像素/帧） ──
    public float vx, vy;

    // ── 生命值 1.0 → 0.0，归零后由 EffectManager 回收 ──
    public float life;

    // ── 粒子颜色 ──
    public Color color;

    // ── 粒子大小（像素） ──
    public float size;

    /**
     * 逐帧更新：匀速直线运动，生命衰减
     */
    public void update() {
        x += vx;
        y += vy;
        life -= 0.02f;
    }

    /**
     * 基类仅设置带透明度的颜色，具体形状由子类绘制
     */
    public void draw(Graphics2D g) {
        if (life <= 0) return;
        g.setColor(new Color(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                (int) (255 * life)
        ));
    }
}
