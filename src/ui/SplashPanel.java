package ui;

import utils.MusicManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

/**
 * 启动动画页 — 垂钓猫咪主题的闪屏界面
 *
 * 绘制内容：
 *   1. 暖棕渐变背景
 *   2. 水面波浪动画
 *   3. 鱼竿 + 鱼线 + 浮标（上下摆动）
 *   4. 橙色小鱼（左右游动，随机跳跃出水花）
 *   5. 木桩码头 + 像素猫 emoji
 *   6. "HAJIMI MATCH" 标题淡入
 *   7. "click to start" 呼吸提示
 *
 * 点击任意位置 → 切到登录页面
 */
public class SplashPanel extends JPanel implements ActionListener {

    private GameFrame parent;
    private Timer timer;

    // ── 动画参数 ──
    private float bobAngle;        // 浮标摆动相位
    private float fishAngle;       // 鱼游动相位
    private float fishBob;         // 鱼跳跃高度偏移
    private boolean fishJumping;   // 是否正在跳跃
    private int jumpTimer;         // 跳跃帧计数
    private float titleAlpha;      // 标题透明度（淡入）
    private float pulse;           // 点击提示呼吸
    private float rodPhase;        // 鱼竿摆动相位偏移

    public SplashPanel(GameFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        setBackground(new Color(0x5c4a3a));

        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // 播放启动页音乐
        MusicManager.play("splash");

        // 60fps 动画定时器
        timer = new Timer(16, this);
        timer.start();

        // 点击跳转到登录页
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                parent.showPage("login");
                MusicManager.play("login");
            }
        });
    }

    // ── 动画帧更新 ──

    @Override
    public void actionPerformed(ActionEvent e) {
        bobAngle += 0.03f;
        fishAngle += 0.025f;

        // 随机触发鱼跳跃
        if (!fishJumping && Math.random() < 0.005) {
            fishJumping = true;
            jumpTimer = 0;
        }
        if (fishJumping) {
            jumpTimer++;
            fishBob = (float) Math.sin(jumpTimer * 0.15f) * 25;
            if (jumpTimer > 20) {
                fishJumping = false;
                fishBob = 0;
            }
        }

        // 标题淡入
        if (titleAlpha < 1) titleAlpha += 0.02f;
        // 点击提示呼吸
        pulse = (float) Math.min(1.0, 0.3 + 0.7 * Math.abs(Math.sin(System.currentTimeMillis() * 0.003)));
        repaint();
    }

    // ── 绘制 ──

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        if (w == 0 || h == 0) return;

        // 1. 暖棕渐变背景
        g2.setPaint(new GradientPaint(0, 0, new Color(0x5c4a3a), 0, h, new Color(0x3d2e1e)));
        g2.fillRect(0, 0, w, h);

        // 2. 水面
        int waterY = h * 2 / 3;
        g2.setPaint(new GradientPaint(0, waterY, new Color(74, 107, 92, 180),
                0, h, new Color(45, 90, 74, 200)));
        g2.fillOval(-50, waterY - 20, w + 100, h - waterY + 60);

        // 波浪线
        g2.setStroke(new BasicStroke(2f));
        g2.setColor(new Color(90, 138, 122, 100));
        for (int x = 0; x < w; x += 3) {
            int yy = waterY - 10 + (int) (Math.sin((x + bobAngle * 50) * 0.03) * 6);
            g2.drawLine(x, yy, x + 2, yy);
        }

        // 3. 鱼竿 + 鱼线 + 浮标（鱼竿整体上下摆动）
        int dockX = w / 2 + 40;
        int rodTipX = dockX - 60;
        int rodTipY = 30 + (int) (Math.sin(bobAngle + rodPhase) * 8);
        int rodBaseY = 70;
        int bobberX = dockX;
        int bobberY = waterY - 20 + (int) (Math.sin(bobAngle) * 8);

        // 鱼竿杆身（从固定底座到摆动的竿梢）
        g2.setStroke(new BasicStroke(3f));
        g2.setColor(new Color(0x8D6E63));
        g2.drawLine(dockX, rodBaseY, rodTipX, rodTipY);

        // 鱼线（从摆动的竿梢到浮标）
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(new Color(188, 170, 164, 180));
        g2.drawLine(rodTipX, rodTipY, bobberX, bobberY);

        // 浮标（独立上下浮动）
        g2.setColor(new Color(0xE53935));
        g2.fillOval(bobberX - 6, bobberY - 6, 12, 12);
        g2.setColor(Color.WHITE);
        g2.fillOval(bobberX - 3, bobberY - 8, 6, 6);

        // 4. 鱼（随鱼竿摆动游动）
        int fishX = (int) (w / 2 + Math.cos(fishAngle) * 80);
        int fishY = waterY + 30 + (int) (Math.sin(fishAngle * 1.3f) * 20) - (int) fishBob;

        AffineTransform saved = g2.getTransform();
        g2.translate(fishX, fishY);
        float dir = (float) (Math.cos(fishAngle) > 0 ? 0.3 : -Math.PI - 0.3);
        g2.rotate(dir);

        // 鱼身
        g2.setColor(new Color(0xFF8A65));
        g2.fillOval(-8, -4, 16, 8);
        // 鱼尾
        g2.fillPolygon(new int[]{8, 14, 14}, new int[]{0, -4, 4}, 3);
        // 鱼眼
        g2.setColor(Color.WHITE);
        g2.fillOval(-3, -2, 4, 4);
        g2.setColor(Color.BLACK);
        g2.fillOval(-2, -1, 2, 2);

        g2.setTransform(saved);

        // 跳跃水花特效
        if (fishJumping) {
            g2.setColor(new Color(128, 203, 196, 150));
            g2.fillOval(fishX - 8, fishY + 16 - (int) fishBob, 16, 8);
            g2.fillOval(fishX - 5, fishY + 24 - (int) fishBob, 10, 6);
        }

        // 5. 木桩码头 + 猫
        g2.setColor(new Color(0x6D4C41));
        g2.fillRect(dockX - 10, h * 2 / 3 - 80, 20, 80);
        g2.setColor(new Color(0x5D4037));
        g2.fillRect(dockX - 25, h * 2 / 3 - 85, 50, 10);
        // 木板纹路
        g2.setColor(new Color(78, 52, 46, 100));
        for (int i = 0; i < 4; i++) {
            g2.fillRect(dockX - 25, h * 2 / 3 - 85 + i * 3, 50, 1);
        }
        // 猫 emoji
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 30));
        g2.drawString("😸", dockX - 18, h * 2 / 3 - 92);

        // 6. 标题（淡入效果）
        float ta = Math.min(1f, titleAlpha);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, ta));
        g2.setFont(new Font("Arial", Font.BOLD, 42));
        g2.setColor(new Color(0xf5e6c8));
        g2.drawString("HAJIMI", w / 2 - 90, 90);
        g2.setColor(new Color(0xe8c87a));
        g2.drawString("MATCH", w / 2 - 95, 135);
        // 标题下划线
        g2.setColor(new Color(122, 106, 85, 150));
        g2.fillRect(w / 2 - 60, 148, 120, 2);
        // 副标题（闪烁呼吸）
        float subAlpha = Math.min(1f, (float) (0.4 + 0.6 * Math.abs(Math.sin(System.currentTimeMillis() * 0.004))));
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, ta * subAlpha));
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        g2.setColor(new Color(0xc4b091));
        g2.drawString("~ Fish ~ Feed ~ Grow ~", w / 2 - 80, 175);

        // 7. 点击提示（呼吸闪烁）
        float p = Math.min(1f, pulse);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, p));
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        g2.setColor(new Color(0x8a7a65));
        g2.drawString("click to start", w / 2 - 40, h - 40);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }
}
