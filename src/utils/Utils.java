package utils;

import model.Cell;
import model.GameBoard;
import model.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * 连连看核心算法 — 路径查找与连通性判断
 *
 * 连通规则：两个相同图标的棋子之间，路径最多允许 2 次 90° 转弯（即 0 折 / 1 折 / 2 折），
 * 路径经过的所有格子必须为空（含棋盘四周的边框空位）。
 *
 * 算法分层：
 *   1. 0 折连接 — 同行或同列，中间无障碍
 *   2. 1 折连接 — L 形，通过一个拐角点连接
 *   3. 2 折连接 — Z/U 形，先从一个点四向延伸，再对每个延伸点尝试 1 折连接
 */
public class Utils {

    // ════════════════════════════════════════════════════════════
    // 四向延伸
    // ════════════════════════════════════════════════════════════

    /**
     * 从 posA 向上下左右四个方向直线延伸，收集所有可达的空格
     * 遇到非空格即停止该方向（不包含起点自身）
     */
    public static List<Cell> getReachablePointsInFourDirections(GameBoard gameBoard, Position posA) {
        List<Cell> res = new ArrayList<>();

        // 向下
        for (int i = posA.getRow() + 1; i < gameBoard.getRowCnt(); i++) {
            if (gameBoard.getCell(i, posA.getCol()).isEmpty()) {
                res.add(gameBoard.getCell(i, posA.getCol()));
            } else {
                break;
            }
        }
        // 向上
        for (int i = posA.getRow() - 1; i >= 0; i--) {
            if (gameBoard.getCell(i, posA.getCol()).isEmpty()) {
                res.add(gameBoard.getCell(i, posA.getCol()));
            } else {
                break;
            }
        }
        // 向右
        for (int i = posA.getCol() + 1; i < gameBoard.getColCnt(); i++) {
            if (gameBoard.getCell(posA.getRow(), i).isEmpty()) {
                res.add(gameBoard.getCell(posA.getRow(), i));
            } else {
                break;
            }
        }
        // 向左
        for (int i = posA.getCol() - 1; i >= 0; i--) {
            if (gameBoard.getCell(posA.getRow(), i).isEmpty()) {
                res.add(gameBoard.getCell(posA.getRow(), i));
            } else {
                break;
            }
        }
        return res;
    }

    // ════════════════════════════════════════════════════════════
    // 直线路径检查
    // ════════════════════════════════════════════════════════════

    /**
     * 检查 from 到 to 之间（不含两端）的所有格子是否为空
     * 仅当 from 和 to 在同一行或同一列时有效
     */
    public static boolean isPathClear(GameBoard gameBoard, Position from, Position to) {
        if (from.getRow() == to.getRow()) {
            // 同行 → 检查水平方向
            int minCol = Math.min(from.getCol(), to.getCol());
            int maxCol = Math.max(from.getCol(), to.getCol());
            for (int col = minCol + 1; col < maxCol; col++) {
                if (!gameBoard.getCell(from.getRow(), col).isEmpty()) {
                    return false;
                }
            }
            return true;
        } else if (from.getCol() == to.getCol()) {
            // 同列 → 检查垂直方向
            int minRow = Math.min(from.getRow(), to.getRow());
            int maxRow = Math.max(from.getRow(), to.getRow());
            for (int row = minRow + 1; row < maxRow; row++) {
                if (!gameBoard.getCell(row, from.getCol()).isEmpty()) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    // ════════════════════════════════════════════════════════════
    // 0 折连接（直线）
    // ════════════════════════════════════════════════════════════

    /** 判断两点是否可以 0 折连接（同行或同列 + 中间无障碍） */
    public static boolean findZeroTurn(GameBoard gameBoard, Position posA, Position posB) {
        if (posA.getCol() == posB.getCol() || posA.getRow() == posB.getRow()) {
            return isPathClear(gameBoard, posA, posB);
        }
        return false;
    }

    /** 获取 0 折连接路径（仅含起点和终点两个节点） */
    public static List<Position> findZeroTurnPath(GameBoard gameBoard, Position posA, Position posB) {
        if (findZeroTurn(gameBoard, posA, posB)) {
            List<Position> path = new ArrayList<>();
            path.add(posA);
            path.add(posB);
            return path;
        }
        return null;
    }

    // ════════════════════════════════════════════════════════════
    // 1 折连接（L 形）
    // ════════════════════════════════════════════════════════════

    /**
     * 判断两点是否可以 1 折连接
     * 尝试两个可能的拐角点：(rowA, colB) 和 (rowB, colA)
     * 拐角点必须为空，且从两端到拐角点均为直线通达
     */
    public static boolean findOneTurn(GameBoard gameBoard, Position posA, Position posB) {
        if (posA.getCol() != posB.getCol() && posA.getRow() != posB.getRow()) {
            Position corner1 = new Position(posA.getRow(), posB.getCol());
            Position corner2 = new Position(posB.getRow(), posA.getCol());

            if (findZeroTurn(gameBoard, posA, corner1)
                    && findZeroTurn(gameBoard, posB, corner1)
                    && gameBoard.getCell(posA.getRow(), posB.getCol()).isEmpty()) {
                return true;
            }
            if (findZeroTurn(gameBoard, posA, corner2)
                    && findZeroTurn(gameBoard, posB, corner2)
                    && gameBoard.getCell(posB.getRow(), posA.getCol()).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /** 获取 1 折连接路径（起点 → 拐点 → 终点） */
    public static List<Position> findOneTurnPath(GameBoard gameBoard, Position posA, Position posB) {
        if (posA.getCol() != posB.getCol() && posA.getRow() != posB.getRow()) {
            Position corner1 = new Position(posA.getRow(), posB.getCol());
            Position corner2 = new Position(posB.getRow(), posA.getCol());

            if (gameBoard.getCell(corner1.getRow(), corner1.getCol()).isEmpty()) {
                if (findZeroTurn(gameBoard, posA, corner1) && findZeroTurn(gameBoard, posB, corner1)) {
                    List<Position> path = new ArrayList<>();
                    path.add(posA);
                    path.add(corner1);
                    path.add(posB);
                    return path;
                }
            }

            if (gameBoard.getCell(corner2.getRow(), corner2.getCol()).isEmpty()) {
                if (findZeroTurn(gameBoard, posA, corner2) && findZeroTurn(gameBoard, posB, corner2)) {
                    List<Position> path = new ArrayList<>();
                    path.add(posA);
                    path.add(corner2);
                    path.add(posB);
                    return path;
                }
            }
        }
        return null;
    }

    // ════════════════════════════════════════════════════════════
    // 2 折连接（Z/U 形）
    // ════════════════════════════════════════════════════════════

    /**
     * 判断两点是否可以 2 折连接
     * 从 posA 四向延伸到所有可达空格，对每个空格检查与 posB 的 1 折可行性
     */
    public static boolean findTwoTurn(GameBoard gameBoard, Position posA, Position posB) {
        List<Cell> reachablePoints = getReachablePointsInFourDirections(gameBoard, posA);
        for (Cell c : reachablePoints) {
            if (findOneTurn(gameBoard, c.getPos(), posB)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取 2 折连接路径
     * 从 posA 四向延伸到所有可达空格，对每个空格尝试与 posB 建立 1 折路径
     * 找到后拼接完整路径返回
     */
    public static List<Position> findTwoTurnPath(GameBoard gameBoard, Position posA, Position posB) {
        List<Cell> reachablePoints = getReachablePointsInFourDirections(gameBoard, posA);
        for (Cell c : reachablePoints) {
            List<Position> subPath = findOneTurnPath(gameBoard, c.getPos(), posB);
            if (subPath != null) {
                List<Position> fullPath = new ArrayList<>();
                fullPath.add(posA);
                fullPath.addAll(subPath);
                return fullPath;
            }
        }
        return null;
    }

    // ════════════════════════════════════════════════════════════
    // 综合接口
    // ════════════════════════════════════════════════════════════

    /**
     * 查找两点之间的连接路径
     * 按优先级依次尝试 0 折 → 1 折 → 2 折，返回第一个找到的路径
     *
     * @return 路径节点列表（起点 → … → 终点），无法连接返回 null
     */
    public static List<Position> findPath(GameBoard gameBoard, Position posA, Position posB) {
        List<Position> path = findZeroTurnPath(gameBoard, posA, posB);
        if (path != null) return path;

        path = findOneTurnPath(gameBoard, posA, posB);
        if (path != null) return path;

        path = findTwoTurnPath(gameBoard, posA, posB);
        if (path != null) return path;

        return null;
    }

    /**
     * 判断两点是否可以连接（任意折数 ≤ 2）
     * 用于棋盘生成验证和玩家点击匹配
     */
    public static boolean canLinkAB(GameBoard gameBoard, Position posA, Position posB) {
        if (findZeroTurn(gameBoard, posA, posB)) return true;
        if (findOneTurn(gameBoard, posA, posB)) return true;
        if (findTwoTurn(gameBoard, posA, posB)) return true;
        return false;
    }
}
