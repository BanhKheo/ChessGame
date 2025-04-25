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
        int rowDiff = Math.abs(newRow - oldRow);
        int colDiff = Math.abs(newCol - oldCol);


        // Checking castling
        if (!isMove && oldRow == newRow) {

            // King-side Castling
            if (newCol == oldCol + 2) {
                Piece rook = board[oldRow][7];
                if (rook instanceof Rook && !((Rook) rook).isMoved()) {
                    if (board[oldRow][5] == null && board[oldRow][6] == null &&
                        !isSquareUnderAttack(board, newCol, newCol , isWhite  )) {
                        return true;
                    }
                }
            }

            // Queen-side Castling
            if (newCol == oldCol - 2) {
                Piece rook = board[oldRow][0];
                if (rook instanceof Rook && !((Rook) rook).isMoved()) {
                    if (board[oldRow][1] == null && board[oldRow][2] == null) {
                        return true;
                    }
                }
            }
        }


        if (rowDiff <= 1 && colDiff <= 1 && (rowDiff + colDiff) > 0) {
            Piece target = board[newRow][newCol];
            //Only check opponent piece
            if ((target == null || target.isWhite != this.isWhite) &&
                    !isSquareUnderAttack(board, newRow, newCol, isWhite)) { //Check whether the target position is underattack of any pieces
                return true;
            }
        }
        return false;
    }

    @Override
    public int[] getBlockPieces(Board board, int newRow, int newCol) {
        int oldRow = this.getRow();
        int oldCol = this.getCol();

        if (newCol == oldCol + 2) {
            return new int[]{oldRow, 5, 6};
        }

        if (newCol == oldCol - 2) {
            return new int[]{oldRow, 1, 2};
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
