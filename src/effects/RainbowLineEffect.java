package effects;

import java.awt.*;
import java.util.List;
public class RainbowLineEffect {
    // 將光暈層級厚度減半，使外圍發光更細膩
    public static final int[][] GLOW_LAYERS = {
            {10, 18},
            {6, 45},
            {3, 100},
    };
    public static void draw(Graphics2D g, List<Point> points){
        if(points == null || points.size() < 2){
            return;
        }
        int segmentCount = points.size() - 1;
        float[] segmentLengths = new float[segmentCount];
        float totalLength = 0;
        for (int i = 0; i < segmentCount; i++) {
            Point a = points.get(i);
            Point b = points.get(i + 1);
            segmentLengths[i] = (float) Math.sqrt(Math.pow(b.x - a.x, 2) + Math.pow(b.y - a.y, 2));
            totalLength += segmentLengths[i];
        }
        if(totalLength == 0){
            return;
        }

        float[] accumulatedLengths = new float[segmentCount];
        float runningSum = 0;
        for (int i = 0; i < segmentCount; i++) {
            accumulatedLengths[i] = runningSum;
            runningSum += segmentLengths[i];
        }

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        for(int[] layer : GLOW_LAYERS){
            int lineWidth = layer[0];
            int alpha = layer[1];
            g2d.setStroke(new BasicStroke(
                    lineWidth,
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND
            ));
            drawRainbowSegments(g2d, points, accumulatedLengths, segmentLengths, totalLength, alpha);
        }

        // 核心亮線寬度從 3.5 降為 2.0
        g2d.setStroke(new BasicStroke(
                2.0f,
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND
        ));
        drawRainbowSegments(g2d, points, accumulatedLengths, segmentLengths, totalLength, 255);

        // 內核高亮線寬度從 1.5 降為 1.0
        g2d.setStroke(new BasicStroke(
                1.0f,
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND
        ));
        drawRainbowSegments(g2d, points, accumulatedLengths,
                segmentLengths, totalLength, 200);

        g2d.dispose();
    }
    public static void drawRainbowSegments(Graphics2D g, List<Point> points,
                                          float[] accLengths,
                                          float[] segLengths,
                                          float totalLength,
                                          int alpha){
        int segmentCount = points.size() - 1;
        for (int i = 0; i < segmentCount; i++) {
            float tStart = accLengths[i] / totalLength;
            float tEnd = (accLengths[i] + segLengths[i]) / totalLength;
            Color colorStart = hueColor(tStart,alpha);
            Color colorEnd = hueColor(tEnd,alpha);
            g.setPaint(new GradientPaint(
                    points.get(i), colorStart,
                    points.get(i + 1), colorEnd
            ));

            Point a = points.get(i);
            Point b = points.get(i + 1);
            g.drawLine(a.x, a.y, b.x, b.y);
        }
    }

    public static Color hueColor(float t, int alpha){
        // 金→橙窄色谱，hue 0.08-0.12，饱和度降低以融入深木背景
        float hue = 0.08f + t * 0.04f;
        Color hsbColor = Color.getHSBColor(hue, 0.7f, 0.9f);
        return new Color(hsbColor.getRed(), hsbColor.getGreen(), hsbColor.getBlue(), alpha);
    }
}
