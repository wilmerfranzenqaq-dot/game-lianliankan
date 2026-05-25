package effects;

import model.Position;
import ui.ThemeColors;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 特效管理器 — 棋盘消除破碎特效的生命周期控制
 *
 * 职责：
 *   - 维护活跃的 {@link ShardParticle} 列表
 *   - 逐帧更新粒子状态，回收已死亡的粒子
 *   - 在消除位置生成碎片爆炸特效
 *   - 将粒子绘制到 BoardPanel 的最上层
 *
 * 使用方式：
 *   {@link #createShatterEffect(Position, Position, int, int, int)} 创建特效，
 *   由外部定时器驱动 {@link #update()} 和 {@link #draw(Graphics2D)}。
 */
public class EffectManager {

    // ── 活跃碎片粒子列表 ──
    private List<ShardParticle> shardParticles;

    public EffectManager() {
        shardParticles = new ArrayList<>();
    }

    /**
     * 逐帧更新所有粒子的位置、旋转和生命值，
     * 移除已死亡（life ≤ 0）的粒子。
     */
    public void update() {
        Iterator<ShardParticle> iter = shardParticles.iterator();
        while (iter.hasNext()) {
            ShardParticle p = iter.next();
            p.update();
            if (p.life <= 0) {
                iter.remove();
            }
        }
    }

    /**
     * 将所有活跃粒子绘制到画布上
     */
    public void draw(Graphics2D g) {
        for (ShardParticle p : shardParticles) {
            p.draw(g);
        }
    }

    /**
     * 在两个消除棋子的中点位置创建碎片爆炸特效
     *
     * @param pos1       第一个棋子的棋盘坐标
     * @param pos2       第二个棋子的棋盘坐标
     * @param cellWidth  单个格子的像素宽度
     * @param cellHeight 单个格子的像素高度
     * @param iconIndex  被消除棋子的图标索引，用于查表取色
     */
    public void createShatterEffect(Position pos1, Position pos2,
                                     int cellWidth, int cellHeight, int iconIndex) {
        // ── 计算两棋子中点作为爆炸中心 ──
        int x1 = pos1.getCol() * cellWidth + cellWidth / 2;
        int y1 = pos1.getRow() * cellHeight + cellHeight / 2;
        int x2 = pos2.getCol() * cellWidth + cellWidth / 2;
        int y2 = pos2.getRow() * cellHeight + cellHeight / 2;

        int centerX = (x1 + x2) / 2;
        int centerY = (y1 + y2) / 2;

        Color color = getColorByIconIndex(iconIndex);

        // ── 生成 20 个碎片粒子，随机初始速度/方向/大小/旋转 ──
        for (int i = 0; i < 20; i++) {
            ShardParticle shard = new ShardParticle();

            // 360° 随机方向
            double angle = Math.random() * Math.PI * 2;
            // 随机速率 2~10
            double speed = Math.random() * 8 + 2;

            // 初始位置在中心点附近微调，避免完全重叠
            shard.x = centerX + (float) ((Math.random() - 0.5) * 10);
            shard.y = centerY + (float) ((Math.random() - 0.5) * 10);

            // 速度分解（Y 轴额外上抛 4 像素，先上升后下落）
            shard.vx = (float) (Math.cos(angle) * speed);
            shard.vy = (float) (Math.sin(angle) * speed) - 4;

            // 随机大小 5~15
            shard.size = (float) (Math.random() * 10 + 5);
            // 随机初始旋转角度
            shard.rotation = (float) (Math.random() * Math.PI * 2);
            // 随机旋转速度 -0.2~0.2 弧度/帧
            shard.rotationSpeed = (float) (Math.random() * 0.4 - 0.2);
            shard.life = 1.0f;
            shard.color = color;
            shard.gravity = 0.5f;

            shardParticles.add(shard);
        }
    }

    /**
     * 根据图标索引从金色系调色板选取粒子颜色
     *
     * 调色板与深木色 UI 背景协调，消除时显眼但不跳色。
     * 不同图标的碎片颜色有微妙差异，增加视觉丰富度。
     */
    private Color getColorByIconIndex(int iconIndex) {
        Color[] colors = {
                ThemeColors.FX_SHATTER,       // 主金
                ThemeColors.FX_HIGHLIGHT,     // 暖白
                new Color(0xd4a017),          // 深金
                new Color(0xc9a96e),          // 暗金
                ThemeColors.FX_LINE_END,      // 暖橙
                new Color(0xf0d080),          // 浅金
                ThemeColors.FX_HIGHLIGHT,
                ThemeColors.FX_SHATTER,
                ThemeColors.FX_LINE_END,
                new Color(0xe0b060)           // 中金
        };
        if (iconIndex >= 1 && iconIndex <= colors.length) {
            return colors[iconIndex - 1];
        }
        // 默认回退到暖白
        return ThemeColors.FX_HIGHLIGHT;
    }

    /**
     * 清除所有活跃特效（RESTART / 恢复存档时调用）
     */
    public void clearAll() {
        shardParticles.clear();
    }

    /**
     * 是否有活跃特效正在播放
     *
     * @return true 表示还有粒子存活，需要继续 repaint
     */
    public boolean hasActiveEffects() {
        return !shardParticles.isEmpty();
    }
}
