package model;

import utils.PathManager;
import java.io.*;
import java.util.*;

/**
 * 排行榜数据层 — 管理所有游戏记录，持久化到 leaderboard.dat
 *
 * 每个模式取 TOP 5：按分数降序排列，分数相同时按用时升序（更快者优先）
 */
public class LeaderBoard {

    /** 排行榜文件 — 存储在 ~/.lianliankan/leaderboard.dat */
    public static final File FILE_NAME = PathManager.getLeaderboardFile();
    /** 每模式取前几名（仅 topRecords 用） */
    public static final int TOP_N = 5;

    // ── 数据 ──

    public List<LeaderRecord> records;

    public LeaderBoard() {
        records = new ArrayList<>();
        loadFromFile();
    }

    // ── 写入 ──

    /** 添加一条记录并立即持久化 */
    public void addRecord(LeaderRecord record) {
        records.add(record);
        saveToFile();
    }

    // ── 查询 ──

    /**
     * 获取指定模式的所有记录（已排序）
     * 排序规则：分数降序 → 用时升序
     */
    public List<LeaderRecord> getAllRecords(String mode) {
        List<LeaderRecord> modeRecords = new ArrayList<>();
        for (LeaderRecord r : records) {
            if (r.mode.equals(mode)) {
                modeRecords.add(r);
            }
        }
        Collections.sort(modeRecords, (a, b) -> {
            if (b.score != a.score) return b.score - a.score;
            return a.timeUsed - b.timeUsed;
        });
        return modeRecords;
    }

    /**
     * 获取指定模式的前 TOP_N 名
     * 排序规则：分数降序 → 用时升序
     */
    public List<LeaderRecord> getTopRecords(String mode) {
        List<LeaderRecord> modeRecords = new ArrayList<>();
        for (LeaderRecord r : records) {
            if (r.mode.equals(mode)) {
                modeRecords.add(r);
            }
        }

        Collections.sort(modeRecords, (a, b) -> {
            if (b.score != a.score) {
                return b.score - a.score;       // 分数降序
            }
            return a.timeUsed - b.timeUsed;     // 用时短的排前面
        });

        List<LeaderRecord> topRecords = new ArrayList<>();
        int count = Math.min(TOP_N, modeRecords.size());
        for (int i = 0; i < count; i++) {
            topRecords.add(modeRecords.get(i));
        }
        return topRecords;
    }

    // ── 持久化 ──

    /** 从 leaderboard.dat 加载所有记录 */
    public void loadFromFile() {
        if (!FILE_NAME.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                LeaderRecord record = LeaderRecord.fromLine(line.trim());
                if (record != null) {
                    records.add(record);
                }
            }
        } catch (IOException e) {
            // 文件读取失败则保持空列表
        }
    }

    /** 将所有记录写回 leaderboard.dat（全量覆盖） */
    public void saveToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME.getAbsolutePath()))) {
            for (LeaderRecord r : records) {
                writer.write(r.toLine());
                writer.newLine();
            }
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
