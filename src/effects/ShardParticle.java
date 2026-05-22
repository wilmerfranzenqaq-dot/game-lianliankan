package effects;

import java.awt.*;

public class ShardParticle extends Particle{
    public float rotation;
    public float rotationSpeed;
    public float gravity;

    @Override
    public void update(){
        x += vx;
        y += vy;
        vy += gravity;
        rotation += rotationSpeed;
        life -= 0.015f;
    }

    @Override
    public void draw(Graphics2D g){
        if(life <= 0) return;

        int alpha = (int)(life * 255);
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));

        // 手动旋转三角形顶点，避免 new AffineTransform + g.create/dispose 的每帧开销
        int half = (int)(size / 2);
        int cx = (int)(x + half);
        int cy = (int)(y + half);
        double cos = Math.cos(rotation);
        double sin = Math.sin(rotation);

        int[] rx = new int[3];
        int[] ry = new int[3];
        // 顶点 0: (0, 0) 相对中心
        double dx0 = -half, dy0 = -half;
        rx[0] = (int)(cx + dx0 * cos - dy0 * sin);
        ry[0] = (int)(cy + dx0 * sin + dy0 * cos);
        // 顶点 1: (size, 0) 相对中心
        double dx1 = half, dy1 = -half;
        rx[1] = (int)(cx + dx1 * cos - dy1 * sin);
        ry[1] = (int)(cy + dx1 * sin + dy1 * cos);
        // 顶点 2: (size/2, size) 相对中心
        double dx2 = 0, dy2 = half;
        rx[2] = (int)(cx + dx2 * cos - dy2 * sin);
        ry[2] = (int)(cy + dx2 * sin + dy2 * cos);

        g.fillPolygon(rx, ry, 3);
    }

}
