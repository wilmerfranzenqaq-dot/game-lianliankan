package effects;

import java.awt.*;
import java.util.List;

/**
 * 彩虹连接线特效 — 棋子消除时从第一个棋子到第二个棋子的渐变连线
 *
 * 核心思路：
 *   1. 先按路径总长度归一化每个线段的位置（0.0 ~ 1.0）
 *   2. 用 HSL 色彩空间在金色窄色谱内插值（hue 0.08 → 0.12）
 *   3. 分层绘制：外层光晕 + 中间亮线 + 内层高亮核心
 *
 * 所有方法均为静态，无需实例化。
 */
public class RainbowLineEffect {

    /**
     * 光晕层级配置
     * 每层为 {线宽, 透明度}，由外到内越来越细、越来越不透明。
     * 外层粗而透 → 内层细而亮，形成发光效果。
     */
    public static final int[][] GLOW_LAYERS = {
            {10, 18},   // 外层：宽 10px，alpha 18  — 大面积柔和光晕
            {6, 45},    // 中层：宽 6px，  alpha 45  — 过渡
            {3, 100},   // 内层：宽 3px，  alpha 100 — 接近实色
    };

    /**
     * 主入口：绘制完整彩虹连接线（光晕 + 核心线 + 高亮核心）
     *
     * @param g      图形上下文
     * @param points 路径拐点列表（棋盘坐标映射后的像素坐标）
     */
    public static void draw(Graphics2D g, List<Point> points) {
        if (points == null || points.size() < 2) {
            return;
        }

        // ── 计算各段长度及总长度，用于颜色渐变的 t 值归一化 ──
        int segmentCount = points.size() - 1;
        float[] segmentLengths = new float[segmentCount];
        float totalLength = 0;
        for (int i = 0; i < segmentCount; i++) {
            Point a = points.get(i);
            Point b = points.get(i + 1);
            segmentLengths[i] = (float) Math.sqrt(
                    Math.pow(b.x - a.x, 2) + Math.pow(b.y - a.y, 2)
            );
            totalLength += segmentLengths[i];
        }
        if (totalLength == 0) {
            return;
        }

        // ── 累积长度数组：accumulatedLengths[i] = 第 i 段之前的总长度 ──
        float[] accumulatedLengths = new float[segmentCount];
        float runningSum = 0;
        for (int i = 0; i < segmentCount; i++) {
            accumulatedLengths[i] = runningSum;
            runningSum += segmentLengths[i];
        }

        // 创建 Graphics2D 副本，开启抗锯齿，不影响外部绘制状态
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        // ── 第一遍：绘制三层光晕（由外到内） ──
        for (int[] layer : GLOW_LAYERS) {
            int lineWidth = layer[0];
            int alpha = layer[1];
            g2d.setStroke(new BasicStroke(
                    lineWidth,
                    BasicStroke.CAP_ROUND,    // 圆头端点，线段连接处不突兀
                    BasicStroke.JOIN_ROUND    // 圆角连接
            ));
            drawRainbowSegments(g2d, points, accumulatedLengths,
                    segmentLengths, totalLength, alpha);
        }

        // ── 第二遍：核心实色亮线（alpha 255） ──
        g2d.setStroke(new BasicStroke(
                2.0f,
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND
        ));
        drawRainbowSegments(g2d, points, accumulatedLengths,
                segmentLengths, totalLength, 255);

        // ── 第三遍：内核高亮白线（alpha 200，超细 1px） ──
        g2d.setStroke(new BasicStroke(
                1.0f,
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND
        ));
        drawRainbowSegments(g2d, points, accumulatedLengths,
                segmentLengths, totalLength, 200);

        g2d.dispose();
    }

    /**
     * 逐线段绘制渐变颜色
     *
     * 每个线段从起点颜色渐变到终点颜色，
     * 颜色由 {@link #hueColor(float, int)} 根据归一化位置 t 计算。
     *
     * @param g           图形上下文
     * @param points      路径拐点
     * @param accLengths  各段之前的累积长度
     * @param segLengths  各段的实际长度
     * @param totalLength 路径总长度
     * @param alpha       统一透明度
     */
    private static void drawRainbowSegments(Graphics2D g, List<Point> points,
                                             float[] accLengths, float[] segLengths,
                                             float totalLength, int alpha) {
        int segmentCount = points.size() - 1;
        for (int i = 0; i < segmentCount; i++) {
            // 当前线段起点/终点的归一化位置 t ∈ [0, 1]
            float tStart = accLengths[i] / totalLength;
            float tEnd = (accLengths[i] + segLengths[i]) / totalLength;

            Color colorStart = hueColor(tStart, alpha);
            Color colorEnd = hueColor(tEnd, alpha);

            // 使用 GradientPaint 在线段两端之间线性插值
            g.setPaint(new GradientPaint(
                    points.get(i), colorStart,
                    points.get(i + 1), colorEnd
            ));

            Point a = points.get(i);
            Point b = points.get(i + 1);
            g.drawLine(a.x, a.y, b.x, b.y);
        }
    }

    /**
     * 根据路径归一化位置 t 计算 HSL 颜色
     *
     * 使用金色窄色谱：hue 0.08（金黄）→ 0.12（暖橙），
     * 饱和度 0.7（不刺眼），亮度 0.9（偏亮以配合深色背景）。
     *
     * @param t     路径归一化位置 0.0 ~ 1.0
     * @param alpha 透明度
     * @return 对应位置的 ARGB 颜色
     */
    private static Color hueColor(float t, int alpha) {
        float hue = 0.08f + t * 0.04f;
        Color hsbColor = Color.getHSBColor(hue, 0.7f, 0.9f);
        return new Color(
                hsbColor.getRed(),
                hsbColor.getGreen(),
                hsbColor.getBlue(),
                alpha
        );
    }
}
