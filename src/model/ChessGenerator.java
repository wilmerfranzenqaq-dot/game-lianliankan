package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import utils.Utils;

/**
 * 连连看棋子生成器 — 支持简单/困难两种模式
 *
 * 简单模式：两块互不相邻的 4×4 独立网格，5 种棋子类型
 * 困难模式：完整的 10×9 连续网格，12 种棋子类型
 *
 * 生成保证至少存在一对可消除的棋子（带 100 次重试机制），避免死局
 */
public class ChessGenerator {

    /** 边框/空格子的图标索引 */
    private static final int BORDER_ICON_INDEX = 0;
    /** 无解棋盘的最大重试生成次数 */
    private static final int MAX_RETRY_COUNT = 100;

    // ── 公开生成接口 ──

    /**
     * 生成简单模式棋盘
     * 两块 4×4 网格分别位于左上和右下区域，彼此隔开
     */
    public Cell[][] generateEasyBoard() {
        int totalRow = 12;
        int totalCol = 12;
        List<Position> validPositions = new ArrayList<>();

        // 第一块 4×4：行 1-4，列 2-5（0-indexed）
        for (int r = 1; r <= 4; r++) {
            for (int c = 2; c <= 5; c++) {
                validPositions.add(new Position(r, c));
            }
        }
        // 第二块 4×4：行 6-9，列 7-10（0-indexed）
        for (int r = 6; r <= 9; r++) {
            for (int c = 7; c <= 10; c++) {
                validPositions.add(new Position(r, c));
            }
        }

        return generateBoard(validPositions, totalRow, totalCol, 5);
    }

    /**
     * 生成困难模式棋盘
     * 单个 10×9 连续网格，棋子种类更多（12 种）
     */
    public Cell[][] generateHardBoard() {
        int totalRow = 12;
        int totalCol = 11;
        List<Position> validPositions = new ArrayList<>();

        // 10 行 × 9 列连续网格：行 1-10，列 1-9（0-indexed）
        for (int r = 1; r <= 10; r++) {
            for (int c = 1; c <= 9; c++) {
                validPositions.add(new Position(r, c));
            }
        }

        return generateBoard(validPositions, totalRow, totalCol, 12);
    }

    // ── 内部生成逻辑 ──

    /**
     * 通用棋盘生成 — 带重试机制确保至少有一对可消除
     *
     * @param validPositions 需要填充的有效位置列表（数量必须为偶数）
     * @param totalRow       棋盘总行数（含边框）
     * @param totalCol       棋盘总列数（含边框）
     * @param chessTypeCount 棋子图标种类数量
     */
    private Cell[][] generateBoard(List<Position> validPositions, int totalRow, int totalCol, int chessTypeCount) {
        if (validPositions.size() % 2 != 0) {
            throw new IllegalArgumentException("有效位置数量必须为偶数");
        }

        // 最多重试 MAX_RETRY_COUNT 次，确保生成有解的棋盘
        for (int attempt = 0; attempt < MAX_RETRY_COUNT; attempt++) {
            Cell[][] board = new Cell[totalRow][totalCol];

            // 先全部初始化为空格
            for (int i = 0; i < totalRow; i++) {
                for (int j = 0; j < totalCol; j++) {
                    board[i][j] = new Cell(new Position(i, j), true, BORDER_ICON_INDEX);
                }
            }

            // 成对生成图标并随机打乱后填入
            List<Integer> iconList = generatePairedIconList(validPositions.size(), chessTypeCount);
            Collections.shuffle(iconList);

            for (int idx = 0; idx < validPositions.size(); idx++) {
                Position pos = validPositions.get(idx);
                board[pos.getRow()][pos.getCol()] = new Cell(pos, false, iconList.get(idx));
            }

            // 存在有效配对则返回
            if (isFullySolvable(board)) {
                return board;
            }
        }

        // 超过重试次数 — 进入无限重试（极端情况极少发生）
        while (true) {
            Cell[][] board = new Cell[totalRow][totalCol];
            for (int i = 0; i < totalRow; i++) {
                for (int j = 0; j < totalCol; j++) {
                    board[i][j] = new Cell(new Position(i, j), true, BORDER_ICON_INDEX);
                }
            }
            List<Integer> iconList = generatePairedIconList(validPositions.size(), chessTypeCount);
            Collections.shuffle(iconList);
            for (int idx = 0; idx < validPositions.size(); idx++) {
                Position pos = validPositions.get(idx);
                board[pos.getRow()][pos.getCol()] = new Cell(pos, false, iconList.get(idx));
            }
            if (isFullySolvable(board)) {
                return board;
            }
        }
    }

    /**
     * 检查棋盘中是否存在至少一对可消除（同图标且可连接）的棋子
     */
    private boolean isFullySolvable(Cell[][] board) {
        int totalRow = board.length;
        int totalCol = board[0].length;
        List<Position> initialCells = new ArrayList<>();

        for (int i = 0; i < totalRow; i++) {
            for (int j = 0; j < totalCol; j++) {
                if (!board[i][j].isEmpty()) {
                    initialCells.add(new Position(i, j));
                }
            }
        }

        if(initialCells.isEmpty()) return true;
        if(initialCells.size() % 2 != 0) return false;

        int maxAttempts = 8;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            Cell[][] simBoard = cloneBoard(board, totalRow, totalCol);
            List<Position> remaining = new ArrayList<>(initialCells);
            if(attempt > 0){
                Collections.shuffle(remaining);
            }
        boolean deadlocked = false;
        while(!remaining.isEmpty() && !deadlocked){
            deadlocked = true;
            GameBoard gb = new GameBoard(totalRow, totalCol, simBoard);

            outer:
            for (int i = 0; i < remaining.size(); i++) {
                for (int j = i + 1; j < remaining.size(); j++) {
                    Position posA = remaining.get(i);
                    Position posB = remaining.get(j);
                    Cell cellA = simBoard[posA.getRow()][posA.getCol()];
                    Cell cellB = simBoard[posB.getRow()][posB.getCol()];

                    if (cellA.getIconIndex() == cellB.getIconIndex()
                            && cellA.getIconIndex() != 0
                            && Utils.canLinkAB(gb, posA, posB)) {
                        simBoard[posA.getRow()][posA.getCol()].setEmpty(true);
                        simBoard[posB.getRow()][posB.getCol()].setEmpty(true);
                        remaining.remove(j);   // 先删大的索引
                        remaining.remove(i);
                        deadlocked = false;
                        break outer;
                    }
                }
            }
        }
        if(remaining.isEmpty()){
            return true;
        }
    }
        return false;
    }

    private Cell[][] cloneBoard(Cell[][] board, int totalRow, int totalCol) {
        Cell[][] clone = new Cell[totalRow][totalCol];
        for (int i = 0; i < totalRow; i++) {
        for (int j = 0; j < totalCol; j++) {
            clone[i][j] = new Cell(new Position(i, j),
                    board[i][j].isEmpty(), board[i][j].getIconIndex());
            }
        }
        return clone;
    }

    /**
     * 生成成对图标列表 — 每种图标类型均匀分配，且每种出现偶数次
     *
     * @param totalCells     需填充的格子总数
     * @param chessTypeCount 图标种类数（1 ~ totalCells/2）
     * @return 长度为 totalCells 的图标索引列表
     */
    private List<Integer> generatePairedIconList(int totalCells, int chessTypeCount) {
        List<Integer> iconList = new ArrayList<>();
        int pairCount = totalCells / 2;
        int basePairs = pairCount / chessTypeCount;
        int extraPairs = pairCount % chessTypeCount;

        for (int type = 0; type < chessTypeCount; type++) {
            int pairsForThisType = basePairs + (type < extraPairs ? 1 : 0);
            for (int p = 0; p < pairsForThisType; p++) {
                iconList.add(type + 1);  // 图标从 1 开始编号（0 留给空格）
                iconList.add(type + 1);
            }
        }
        return iconList;
    }
}
