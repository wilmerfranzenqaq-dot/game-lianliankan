package ui;

import java.awt.Color;
import java.awt.Font;

/**
 * 全局 Design Token 定义
 * 所有 UI 组件从本文件获取色值、字体、间距，改 token 全局联动。
 */
public final class ThemeColors {

    private ThemeColors() {} // utility class

    // ── 色板 ──
    /** 最深背景，全屏/弹窗容器 */
    public static final Color CANVAS     = new Color(0x2a221a);
    /** 卡片/面板底色 */
    public static final Color SURFACE    = new Color(0x3a3023);
    /** 第二面板色（交替行、次级面板、悬浮预选） */
    public static final Color SURFACE_2  = new Color(0x4a3d2e);
    /** 主色调（金色）— 按钮高亮、标题、选中态 */
    public static final Color PRIMARY    = new Color(0xe8c87a);
    /** 正文色 */
    public static final Color TEXT       = new Color(0xd4c5a9);
    /** 辅助/次要文本色 */
    public static final Color TEXT_MUTED = new Color(0x9a8b78);
    /** 深色文本质感（选中的金色背景上的文字） */
    public static final Color TEXT_ON_GOLD = new Color(0x1a1510);
    /** 面板分隔线 / 边框 */
    public static final Color BORDER     = new Color(0x5a4d3e);
    /** 危险操作（删除、警告按钮） */
    public static final Color DANGER     = new Color(0xc0392b);
    /** 成功/正向操作（保存确认、完成） */
    public static final Color SUCCESS    = new Color(0x5c8a4a);
    /** 提示/信息色（提示按钮、高亮文字） */
    public static final Color ACCENT     = new Color(0xc9a96e);

    // ── 字体 ──
    public static final Font FONT_TITLE  = new Font("Microsoft YaHei", Font.BOLD, 20);
    public static final Font FONT_H2    = new Font("Microsoft YaHei", Font.BOLD, 16);
    public static final Font FONT_BODY  = new Font("Microsoft YaHei", Font.PLAIN, 14);
    public static final Font FONT_SMALL = new Font("Microsoft YaHei", Font.PLAIN, 12);
    public static final Font FONT_CAPTION = new Font("Microsoft YaHei", Font.PLAIN, 11);
    public static final Font FONT_NUM   = new Font("Arial", Font.BOLD, 28); // 计时/分数数字

    // ── 间距 & 圆角 ──
    public static final int RADIUS_BTN   = 8;   // 按钮圆角
    public static final int RADIUS_CARD  = 12;  // 卡片圆角
    public static final int PAD_CARD     = 24;  // 卡片内边距
    public static final int PAD_SECTION  = 16;  // section 间距
    public static final int PAD_BTN_GROUP = 10; // 按钮组间距

    // ── 工具方法 ──
    /** 混合色：返回 background 和 overlay 的 alpha 混合 */
    public static Color blend(Color bg, Color overlay, float alpha) {
        int r = (int)(bg.getRed() * (1 - alpha) + overlay.getRed() * alpha);
        int g = (int)(bg.getGreen() * (1 - alpha) + overlay.getGreen() * alpha);
        int b = (int)(bg.getBlue() * (1 - alpha) + overlay.getBlue() * alpha);
        return new Color(Math.min(r, 255), Math.min(g, 255), Math.min(b, 255));
    }
}
