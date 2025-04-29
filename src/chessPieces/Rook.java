package chessPieces;

import main.Board;
import utilz.LoadImage;

public class Rook extends Piece {
    private boolean isMove = false;

    public Rook(int x, int y, boolean isWhite) {
        super(x, y, LoadImage.GetPieceImage(isWhite, "r"), isWhite);
    }

    @Override
    public boolean logicMove(int oldRow, int oldCol, int newRow, int newCol, Piece[][] board) {
        if (oldCol == newCol || oldRow == newRow) {
            Piece target = board[newRow][newCol];
            return target == null || target.isWhite != this.isWhite;
        }
        return false;
    }

    @Override
    public int[] getBlockPieces(Board board, int newRow, int newCol) {
        int oldRow = this.getRow();
        int oldCol = this.getCol();

        // Moving vertically
        if (oldCol == newCol) {
            int step = (newRow > oldRow) ? 1 : -1;
            int r = oldRow + step;
            while (r != newRow) {  // Loop until reaching target row
                if (board.getPieceAt(r, oldCol) != null) {  // Corrected row-col order
                    return new int[]{r, oldCol}; // Blocked piece found
                }
                r += step;
            }
        }

        // Moving horizontally
        else if (oldRow == newRow) {
            int step = (newCol > oldCol) ? 1 : -1;
            int c = oldCol + step;
            while (c != newCol) {  // Loop until reaching target column
                if (board.getPieceAt(oldRow, c) != null) {  // Corrected row-col order
                    return new int[]{oldRow, c}; // Blocked piece found
                }
                c += step;
            }
        }

        return null; // No blocking piece
    }

    public boolean isMoved() {
        return isMove;
    }

    public void setMove(boolean moved) {
        this.isMove = moved;
    }

    @Override
    public int getValue() {
        return 5;
    }

}
