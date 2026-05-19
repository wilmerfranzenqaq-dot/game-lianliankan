package utils;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

/**
 * 音乐+音效管理器 — BGM 循环播放 + SFX 一次性播放（叠加在 BGM 上）
 *
 * SFX 变调：修改 AudioFormat 的采样率，音调随采样率升高而变高。
 *   1.0x = 正常, >1.0x = 更高更快, <1.0x = 更低更慢
 */
public class MusicManager {

    public static final String DIR = "resource" + File.separator + "music" + File.separator;
    public static final String FALLBACK_DIR = "D:" + File.separator + "game-lianliankan"
            + File.separator + "resource" + File.separator + "music" + File.separator;

    private static Clip currentClip;
    private static String pendingTrack;
    private static boolean switching;
    private static float volume = 0.5f;

    // ── BGM ──

    public static void play(String name) {
        if (currentClip != null && currentClip.isRunning()) {
            pendingTrack = name;
            switching = true;
            currentClip.loop(0);
        } else {
            loadAndPlay(name);
        }
    }

    public static void stop() {
        pendingTrack = null;
        switching = false;
        if (currentClip != null) {
            currentClip.stop();
            currentClip.close();
            currentClip = null;
        }
    }

    // ── SFX ──

    /** 播放一次性音效（与 BGM 叠加） */
    public static void playSfx(String name) {
        playSfx(name, 1.0f);
    }

    /** 播放带变调的一次性音效 */
    public static void playSfx(String name, float pitchFactor) {
        try {
            File file = new File(DIR + name + ".wav");
            if (!file.exists()) file = new File(FALLBACK_DIR + name + ".wav");
            if (!file.exists()) return;

            AudioInputStream stream = AudioSystem.getAudioInputStream(file);

            // 变调：修改采样率
            if (Math.abs(pitchFactor - 1.0f) > 0.01f) {
                AudioFormat base = stream.getFormat();
                AudioFormat pitched = new AudioFormat(
                        base.getEncoding(),
                        base.getSampleRate() * pitchFactor,
                        base.getSampleSizeInBits(),
                        base.getChannels(),
                        base.getFrameSize(),
                        base.getFrameRate() * pitchFactor,
                        base.isBigEndian()
                );
                stream = AudioSystem.getAudioInputStream(pitched, stream);
            }

            Clip clip = AudioSystem.getClip();
            clip.open(stream);
            applySfxVolume(clip);
            clip.start();
            clip.addLineListener(e -> { if (e.getType() == LineEvent.Type.STOP) clip.close(); });
        } catch (Exception e) {
            System.err.println("sfx error: " + e.getMessage());
        }
    }

    // ── 音量 ──

    public static void setVolume(float vol) {
        volume = Math.max(0f, Math.min(1f, vol));
        applyVolume();
    }

    public static float getVolume() {
        return volume;
    }

    // ── 内部 ──

    private static void loadAndPlay(String name) {
        try {
            if (currentClip != null) { currentClip.close(); currentClip = null; }

            File file = new File(DIR + name + ".wav");
            if (!file.exists()) file = new File(FALLBACK_DIR + name + ".wav");
            if (!file.exists()) { System.err.println("音乐文件不存在: " + file); return; }

            AudioInputStream stream = AudioSystem.getAudioInputStream(file);
            currentClip = AudioSystem.getClip();

            currentClip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    if (switching && pendingTrack != null) {
                        String next = pendingTrack; pendingTrack = null; switching = false;
                        loadAndPlay(next);
                    } else if (!switching) {
                        currentClip.loop(Clip.LOOP_CONTINUOUSLY);
                    }
                }
            });

            currentClip.open(stream);
            applyVolume();
            currentClip.loop(Clip.LOOP_CONTINUOUSLY);
            switching = false;

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("播放音乐失败: " + name);
            e.printStackTrace();
        }
    }

    private static void applyVolume() {
        if (currentClip == null || !currentClip.isOpen()) return;
        try {
            if (currentClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gain = (FloatControl) currentClip.getControl(FloatControl.Type.MASTER_GAIN);
                float dB = volume <= 0f ? gain.getMinimum()
                        : gain.getMinimum() + (gain.getMaximum() - gain.getMinimum()) * (float) Math.log10(1 + 9 * volume);
                gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), dB)));
            }
        } catch (Exception ignored) {}
    }

    private static void applySfxVolume(Clip clip) {
        if (clip == null || !clip.isOpen()) return;
        try {
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float sfxVol = Math.min(1f, volume * 1.2f);
                float dB = sfxVol <= 0f ? gain.getMinimum()
                        : gain.getMinimum() + (gain.getMaximum() - gain.getMinimum()) * (float) Math.log10(1 + 9 * sfxVol);
                gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), dB)));
            }
        } catch (Exception ignored) {}
    }
}
