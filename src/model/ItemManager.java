package model;

import utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static utils.Utils.canLinkAB;

public class ItemManager {
    private int hintCount;
    private int shuffleCount;
    private int bombCount;
    private int freezeTimeCount;

    private GameBoard gameBoard;
    private Random random;

    public ItemManager(GameBoard board) {
        this.gameBoard = board;
        this.random = new Random();
        this.hintCount = 3;
        this.shuffleCount = 3;
        this.bombCount = 2;
        this.freezeTimeCount = 2;
    }

    public Position[] useHint() {
        if (hintCount <= 0) {
            return null;
        }
        hintCount--;
        List<Position> nonEmptyCells = new ArrayList<>();
        for (int i = 1; i < gameBoard.getRowCnt() - 1; i++) {
            for (int j = 1; j < gameBoard.getColCnt() - 1; j++) {
                Cell cell = gameBoard.getCell(i, j);
                if (!cell.isEmpty()) {
                    nonEmptyCells.add(new Position(i, j));
                }
            }
        }

        for (int i = 0; i < nonEmptyCells.size(); i++) {
            for (int j = i + 1; j < nonEmptyCells.size(); j++) {
                Position pos1 = nonEmptyCells.get(i);
                Position pos2 = nonEmptyCells.get(j);
                Cell cell1 = gameBoard.getCell(pos1.getRow(), pos1.getCol());
                Cell cell2 = gameBoard.getCell(pos2.getRow(), pos2.getCol());

                if (cell1.getIconIndex() == cell2.getIconIndex() &&
                        canLinkAB(gameBoard, pos1, pos2)) {
                    return new Position[]{pos1, pos2};
                }
            }
        }
        return null;
    }

    public boolean useShuffle() {
        if (shuffleCount <= 0) {
            return false;
        }
        shuffleCount--;
        gameBoard.clearAllChosen();
        List<Position> positions = new ArrayList<>();
        List<Integer> icons = new ArrayList<>();
        for (int i = 1; i < gameBoard.getRowCnt() - 1; i++) {
            for (int j = 1; j < gameBoard.getColCnt() - 1; j++) {
                Cell cell = gameBoard.getCell(i, j);
                if (!cell.isEmpty()) {
                    positions.add(new Position(i, j));
                    icons.add(cell.getIconIndex());
                }
            }
        }
        for (int i = icons.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int temp = icons.get(i);
            icons.set(i, icons.get(j));
            icons.set(j, temp);
        }
        for (int i = 0; i < positions.size(); i++) {
            Position pos = positions.get(i);
            Cell cell = gameBoard.getCell(pos.getRow(), pos.getCol());
            // 通过反射或直接设置iconIndex（需要修改Cell类）
            cell.setIconIndex(icons.get(i));
        }
        return true;
    }
    public Position[] useBomb(Position selectedPos) {
        if (bombCount <= 0) {
            return null;
        }

        Cell selectedCell = gameBoard.getCell(selectedPos.getRow(), selectedPos.getCol());
        if (selectedCell.isEmpty()) {
            return null;
        }

        int targetIcon = selectedCell.getIconIndex();

        for (int i = 1; i < gameBoard.getRowCnt() - 1; i++) {
            for (int j = 1; j < gameBoard.getColCnt() - 1; j++) {
                Position pairPos = new Position(i, j);
                Cell cell = gameBoard.getCell(i, j);

                if (!cell.isEmpty() && cell.getIconIndex() == targetIcon
                        && !pairPos.equals(selectedPos)) {
                    bombCount--;
                    selectedCell.setEmpty(true);
                    cell.setEmpty(true);
                    return new Position[]{selectedPos, pairPos};
                }

            }
        }

        return null;
    }
    public int useFreezeTime() {
        if (freezeTimeCount <= 0) {
            return 0;
        }

        freezeTimeCount--;
        return 5; // 冻结 5 秒
    }
    public int getHintCount() {
        return hintCount;
    }

    public int getShuffleCount() {
        return shuffleCount;
    }

    public int getBombCount() {
        return bombCount;
    }

    public int getFreezeTimeCount() {
        return freezeTimeCount;
    }

    public GameBoard getGameBoard() {
        return gameBoard;
    }
}