package utils;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * 统一路径管理器 — 集中处理所有资源加载与用户数据存储路径
 *
 * 两种路径策略：
 *   资源（只读）：优先从 classpath 加载（支持 JAR 内运行），
 *               回退到项目根目录下的文件系统路径（支持 IDE 直接运行）
 *   用户数据（可写）：统一存储在 ~/.lianliankan/ 下
 *                 首次运行自动迁移旧数据（从 user.dir）
 */
public class PathManager {

    // ── 皮肤标识符常量（替代原先的 "resource" / "resource/fruit" 路径字符串） ──
    public static final String SKIN_DEFAULT = "default";
    public static final String SKIN_FRUIT = "fruit";

    // ── 用户数据目录：~/.lianliankan/ ──
    private static final File DATA_DIR;

    // ── 项目根目录（用于文件系统回退） ──
    private static final File PROJECT_ROOT;

    static {
        PROJECT_ROOT = new File(System.getProperty("user.dir"));
        DATA_DIR = new File(System.getProperty("user.home"), ".lianliankan");
        if (!DATA_DIR.exists()) {
            DATA_DIR.mkdirs();
        }
        migrateLegacyData();
    }

    // ════════════════════════════════════════════════════
    // 资源加载（只读）
    // ════════════════════════════════════════════════════

    /**
     * 通用资源加载：优先 classpath，回退文件系统
     *
     * @param classpathPath classpath 路径（如 "0.png"、"fruit/0.png"、"music/click.wav"）
     * @param filePath      文件系统相对路径（如 "resource/0.png"）
     * @return 资源 URL，找不到返回 null
     */
    public static URL getResourceUrl(String classpathPath, String filePath) {
        URL url = PathManager.class.getResource("/" + classpathPath);
        if (url != null) return url;
        File file = new File(PROJECT_ROOT, filePath);
        if (file.exists()) {
            try { return file.toURI().toURL(); } catch (IOException ignored) {}
        }
        return null;
    }

    /**
     * 加载图片资源并返回 ImageIcon
     */
    public static ImageIcon getResourceImageIcon(String classpathPath, String filePath) {
        URL url = getResourceUrl(classpathPath, filePath);
        if (url != null) return new ImageIcon(url);
        return null;
    }

    /**
     * 加载音频资源 URL（处理 .wav / .WAV 大小写兼容）
     */
    public static URL getAudioResourceUrl(String filename) {
        // 先尝试 classpath
        URL url = PathManager.class.getResource("/music/" + filename + ".wav");
        if (url == null) url = PathManager.class.getResource("/music/" + filename + ".WAV");
        if (url != null) return url;

        // 回退文件系统
        File file = new File(PROJECT_ROOT, "resource/music/" + filename + ".wav");
        if (!file.exists()) file = new File(PROJECT_ROOT, "resource/music/" + filename + ".WAV");
        if (file.exists()) {
            try { return file.toURI().toURL(); } catch (IOException ignored) {}
        }
        return null;
    }

    /**
     * 皮肤标识符 → classpath 前缀映射
     * SKIN_DEFAULT → ""（图片直接位于 classpath 根）
     * SKIN_FRUIT   → "fruit/"
     */
    public static String getSkinClasspathPrefix(String skinId) {
        if (SKIN_FRUIT.equals(skinId)) return "fruit/";
        return "";
    }

    // ════════════════════════════════════════════════════
    // 用户数据文件（可写）
    // ════════════════════════════════════════════════════

    public static File getUserDataDir() {
        return DATA_DIR;
    }

    public static File getUserFile() {
        return new File(DATA_DIR, "user.txt");
    }

    public static File getLeaderboardFile() {
        return new File(DATA_DIR, "leaderboard.dat");
    }

    /**
     * 获取存档文件路径
     */
    public static File getSaveFile(String username, String mode, int slot) {
        String modeStr = mode.equals("困难模式") ? "hard" : "easy";
        return new File(DATA_DIR, "save_" + username + "_" + modeStr + "_" + slot + ".dat");
    }

    // ════════════════════════════════════════════════════
    // 旧数据迁移
    // ════════════════════════════════════════════════════

    /**
     * 首次运行时，将项目目录下的旧用户数据迁移到 ~/.lianliankan/
     */
    private static void migrateLegacyData() {
        File[] legacyFiles = PROJECT_ROOT.listFiles((dir, name) ->
                name.equals("user.txt") || name.equals("leaderboard.dat") ||
                        (name.startsWith("save_") && name.endsWith(".dat")));
        if (legacyFiles == null) return;

        for (File legacy : legacyFiles) {
            File target = new File(DATA_DIR, legacy.getName());
            if (!target.exists()) {
                try {
                    Files.copy(legacy.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("[PathManager] 已迁移: " + legacy.getName() + " → " + target);
                } catch (IOException e) {
                    System.err.println("[PathManager] 迁移失败: " + legacy.getName() + " — " + e.getMessage());
                }
            }
        }
    }
}
