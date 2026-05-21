package effects;

import model.Position;
import ui.ThemeColors;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EffectManager {
    private List<ShardParticle> shardParticles;

    public EffectManager() {
        shardParticles = new ArrayList<>();
    }

    public void update() {
        Iterator<ShardParticle> iter = shardParticles.iterator();
        while (iter.hasNext()) {
            ShardParticle p = iter.next();
            p.update();
            if (p.life <= 0) iter.remove();
        }
    }

    public void draw(Graphics2D g) {
        for (ShardParticle p : shardParticles) {
            p.draw(g);
        }
    }

    public void createShatterEffect(Position pos1, Position pos2, int cellWidth, int cellHeight, int iconIndex) {
        // 计算两个棋子中心的像素坐标中点（相对于 BoardPanel）
        int x1 = pos1.getCol() * cellWidth + cellWidth / 2;
        int y1 = pos1.getRow() * cellHeight + cellHeight / 2;
        int x2 = pos2.getCol() * cellWidth + cellWidth / 2;
        int y2 = pos2.getRow() * cellHeight + cellHeight / 2;
        
        int centerX = (x1 + x2) / 2;
        int centerY = (y1 + y2) / 2;
        
        Color color = getColorByIconIndex(iconIndex);

        for (int i = 0; i < 30; i++) {
            ShardParticle shard = new ShardParticle();
            double angle = Math.random() * Math.PI * 2;
            double speed = Math.random() * 8 + 2;

            shard.x = centerX + (float)((Math.random() - 0.5) * 10);
            shard.y = centerY + (float)((Math.random() - 0.5) * 10);
            shard.vx = (float)(Math.cos(angle) * speed);
            shard.vy = (float)(Math.sin(angle) * speed) - 4;

            shard.size = (float)(Math.random() * 10 + 5);
            shard.rotation = (float)(Math.random() * Math.PI * 2);
            shard.rotationSpeed = (float)(Math.random() * 0.4 - 0.2);
            shard.life = 1.0f;
            shard.color = color;
            shard.gravity = 0.5f;

            shardParticles.add(shard);
        }
    }

    private Color getColorByIconIndex(int iconIndex) {
        // 金色系调色板 — 与深木 UI 一体，消除时显眼但不跳色
        Color[] colors = {
            ThemeColors.FX_SHATTER,      // 主金
            ThemeColors.FX_HIGHLIGHT,     // 暖白
            new Color(0xd4a017),          // 深金
            new Color(0xc9a96e),          // 暗金
            ThemeColors.FX_LINE_END,      // 暖橙
            new Color(0xf0d080),          // 浅金
            ThemeColors.FX_HIGHLIGHT,
            ThemeColors.FX_SHATTER,
            ThemeColors.FX_LINE_END,
            new Color(0xe0b060)
        };
        if (iconIndex >= 1 && iconIndex <= colors.length) {
            return colors[iconIndex - 1];
        }
        return ThemeColors.FX_HIGHLIGHT;
    }

    public void clearAll() {
        shardParticles.clear();
    }

    public boolean hasActiveEffects() {
        return !shardParticles.isEmpty();
    }
}
