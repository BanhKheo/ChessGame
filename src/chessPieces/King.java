package chessPieces;

import main.Board;
import utilz.LoadImage;

public class King extends Piece {
    private boolean isMove = false; // Check the moving state of King piece

    public King(int x, int y, boolean isWhite) {
        super(x, y, LoadImage.GetPieceImage(isWhite, "k"), isWhite);
    }

    @Override
    public boolean logicMove(int oldRow, int oldCol, int newRow, int newCol, Piece[][] board) {
        // Checking castling
        if (!isMove && oldRow == newRow) {
            if (newCol == oldCol + 2) {
                Piece rook = board[oldRow][7];
                if (rook instanceof Rook && !((Rook) rook).isMoved()) {
                    if (board[oldRow][5] == null && board[oldRow][6] == null) {
                        isMove = true;
                        return true;
                    }
                }
            }
        }


        int rowDiff = Math.abs(newRow - oldRow);
        int colDiff = Math.abs(newCol - oldCol);

        if (rowDiff <= 1 && colDiff <= 1 && (rowDiff + colDiff) > 0) {
            Piece target = board[newRow][newCol];
            if (target == null || target.isWhite != this.isWhite) {
                isMove = true;
                return true;
            }
        }
        return false;
    }

    @Override
    public int[] getBlockPieces(Board board, int newRow, int newCol) {
        int oldRow = this.getRow();
        int oldCol = this.getCol();

        if (newRow == oldRow) {
            if (newCol == oldCol + 2) {
                if (board.getPieceAt(oldRow,5) == null && board.getPieceAt(oldRow,6) == null) {
                    return new int[]{oldRow, board.getPieceAt(oldRow,5) != null ? 5:6};
                }
            }
        }
        return null;
    }

    public boolean isMoved() {
        return isMove;
    }

    public void setMove(boolean moved) {
        this.isMove = moved;
    }
}
