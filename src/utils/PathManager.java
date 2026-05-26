package utils;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * 统一路径管理器 — 从项目根目录加载资源，用户数据存储在 ~/.lianliankan/
 */
public class PathManager {

    public static final String SKIN_DEFAULT = "default";
    public static final String SKIN_FRUIT = "fruit";

    private static final File DATA_DIR;
    private static final File PROJECT_ROOT;

    static {
        PROJECT_ROOT = new File(System.getProperty("user.dir"));
        DATA_DIR = new File(System.getProperty("user.home"), ".lianliankan");
        if (!DATA_DIR.exists()) {
            DATA_DIR.mkdirs();
        }
    }

    /** 从项目根目录加载资源文件 */
    public static URL getResourceUrl(String relativePath) {
        File file = new File(PROJECT_ROOT, relativePath);
        if (file.exists()) {
            try { return file.toURI().toURL(); } catch (IOException ignored) {}
        }
        return null;
    }

    /** 加载图片资源 */
    public static ImageIcon getResourceImageIcon(String relativePath) {
        URL url = getResourceUrl(relativePath);
        if (url != null) return new ImageIcon(url);
        return null;
    }

    /** 加载音频资源（兼容 .wav / .WAV） */
    public static URL getAudioResourceUrl(String filename) {
        File file = new File(PROJECT_ROOT, "resource/music/" + filename + ".wav");
        if (!file.exists()) file = new File(PROJECT_ROOT, "resource/music/" + filename + ".WAV");
        if (file.exists()) {
            try { return file.toURI().toURL(); } catch (IOException ignored) {}
        }
        return null;
    }

    /** 皮肤标识符 → resource 子目录名（default → "", fruit → "fruit/"） */
    public static String getSkinDirName(String skinId) {
        if (SKIN_FRUIT.equals(skinId)) return "fruit/";
        return "";
    }

    public static File getUserDataDir() {
        return DATA_DIR;
    }

    public static File getUserFile() {
        return new File(DATA_DIR, "user.txt");
    }

    public static File getLeaderboardFile() {
        return new File(DATA_DIR, "leaderboard.dat");
    }

    public static File getSaveFile(String username, String mode, int slot) {
        String modeStr = mode.equals("困难模式") ? "hard" : "easy";
        return new File(DATA_DIR, "save_" + username + "_" + modeStr + "_" + slot + ".dat");
    }
}
