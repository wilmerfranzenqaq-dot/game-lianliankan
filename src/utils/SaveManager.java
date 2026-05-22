package utils;

import model.Cell;
import model.GameBoard;
import model.Position;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**存档管理 -- 保存/读取/删除本地存档
 * 
 * 存档格式(save_{username}_{mode}_{slot}.dat):
 * username
 * mode
 * score
 * remainingSeconds
 * elapsedSeconds
 * comboCount
 * lastEliminationTime
 * gameboard
 * pairs
 */
public class SaveManager {
    public static String getSaveFilePath(String username, String mode, int slot) {
        String modeStr = mode.equals("困难模式") ? "hard" : "easy";
        return "save_" + username + "_" + modeStr + "_" + slot + ".dat";
    }

    public static boolean saveGame(String filepath, String username, String catName, String mode, int slot,
                                   int score, int remainingSeconds, int elapsedSeconds, int comboCount,
                                   long lastEliminationTime, GameBoard gameBoard) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filepath))) {
            writer.println(username);
            writer.println(catName);
            writer.println(mode);
            writer.println(slot);
            writer.println(score);
            writer.println(remainingSeconds);
            writer.println(elapsedSeconds);
            writer.println(comboCount);
            writer.println(lastEliminationTime);
            // 新增：保存 totalPairs
            writer.println(gameBoard.getTotalPairs());
            writer.println(gameBoard.getRowCnt() + "," + gameBoard.getColCnt());
            for (int i = 0; i < gameBoard.getRowCnt(); i++) {
                StringBuilder line = new StringBuilder();
                for (int j = 0; j < gameBoard.getColCnt(); j++) {
                    Cell cell = gameBoard.getCell(i, j);
                    line.append(cell.getIconIndex());
                    if (j < gameBoard.getColCnt() - 1) {
                        line.append(",");
                    }
                }
                writer.println(line.toString());
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static SaveData loadGame(String filepath) {
        File file = new File(filepath);
        if(!file.exists()){
            return null;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))){
            String username = reader.readLine();
            String mode = reader.readLine();
            int slot = Integer.parseInt(reader.readLine());
            int score = Integer.parseInt(reader.readLine());
            int remainingSeconds = Integer.parseInt(reader.readLine());
            int elapsedSeconds = Integer.parseInt(reader.readLine());
            int comboCount = Integer.parseInt(reader.readLine());
            long lastEliminationTime = Long.parseLong(reader.readLine());
            // 新增：读取 totalPairs
            String catName = reader.readLine();
            int totalPairs = Integer.parseInt(reader.readLine());
            
            String[] dimensions = reader.readLine().split(",");
            int rows = Integer.parseInt(dimensions[0]);
            int cols = Integer.parseInt(dimensions[1]);

            Cell[][] board = new Cell[rows][cols];
            for (int i = 0; i < rows; i++) {
                String line = reader.readLine();
                String[] icons = line.split(",");
                for(int j = 0; j < cols; j++) {
                    int iconIndex = Integer.parseInt(icons[j]);
                    boolean isEmpty = (iconIndex == 0);
                    board[i][j] = new Cell(new Position(i, j), isEmpty, iconIndex);
                }
            }
            GameBoard gameBoard = new GameBoard(rows, cols, board);
            gameBoard.totalPairs = totalPairs;
            
            SaveData data = new SaveData();
            data.username = username;
            data.catName = catName;
            data.mode = mode;
            data.slot = slot;
            data.score = score;
            data.remainingSeconds = remainingSeconds;
            data.elapsedSeconds = elapsedSeconds;
            data.comboCount = comboCount;
            data.lastEliminationTime = lastEliminationTime;
            data.gameBoard = gameBoard;
            return data;

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException | NumberFormatException e) {
            throw new RuntimeException(e);
        }
    }
    public static boolean hasSave(String username, String mode, int slot){
        String filePath = getSaveFilePath(username, mode, slot);
        return new File(filePath).exists();
    }
    public static List<Integer> getAvailableSlots(String username, String mode){
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i <= 9; i++) {
            if(hasSave(username, mode, i)){
                slots.add(i);
            }
        }
        return slots;
    }
    public static int getNextAvailableSlot(String username, String mode){
        for (int i = 0; i <= 9; i++) {
            if(!hasSave(username, mode, i)){
                return i;
            }
        }
        return -1;
    }
    public static boolean deleteSave(String username, String mode, int slot){
        String filePath = getSaveFilePath(username, mode, slot);
        File file = new File(filePath);
        if(file.exists()){
            return file.delete();
        }
        return false;
    }
    public static class SaveData{
        public String username;
        public String catName;
        public String mode;
        public int slot;
        public int score;
        public int remainingSeconds;
        public int elapsedSeconds;
        public int comboCount;
        public long lastEliminationTime;
        public GameBoard gameBoard;
    }
}
