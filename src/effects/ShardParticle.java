package effects;

import java.awt.*;
import java.awt.geom.AffineTransform;

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
        if(life <=  0) return;
        
        // 使用 create() 创建局部拷贝，避免影响全局坐标系
        Graphics2D g2d = (Graphics2D) g.create();
        
        AffineTransform transform = new AffineTransform();
        transform.translate(x + size / 2, y + size / 2);
        transform.rotate(rotation);
        transform.translate(-size / 2, -size / 2);
        g2d.setTransform(transform);
        
        g2d.setColor(new Color(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                (int)(life * 255)
        ));
        int[] xPoints = {0, (int) size, (int)(size / 2)};
        int[] yPoints = {0, 0, (int)size};
        g2d.fillPolygon(xPoints, yPoints, 3);
        
        // 销毁局部拷贝，恢复原始坐标系
        g2d.dispose();
    }

}
