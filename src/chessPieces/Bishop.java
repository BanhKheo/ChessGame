package chessPieces;

import main.Board;
import utilz.LoadImage;

public class Bishop extends Piece {
    public Bishop(int x, int y, boolean isWhite) {
        super(x, y, LoadImage.GetPieceImage(isWhite, "b"), isWhite);
    }

    @Override
    public boolean logicMove(int oldRow, int oldCol, int newRow, int newCol) {
        return Math.abs(oldRow - newRow) == Math.abs(oldCol - newCol);
    }

    @Override
    public int[] getBlockPieces(Board board, int newRow, int newCol) {
        int oldRow = this.getRow();
        int oldCol = this.getCol();

        if (Math.abs(newRow - oldRow) == Math.abs(newCol - oldCol)) {
            int rowStep = (newRow > oldRow) ? 1 : -1;
            int colStep = (newCol > oldCol) ? 1 : -1;

            int r = oldRow + rowStep, c = oldCol + colStep;
            while (r != newRow || c != newCol) {  // Loop until reaching target square
                if (board.getPieceAt(r, c) != null) {  // Check (r, c) instead of (c, r)
                    return new int[]{r, c};  // Blocked position
                }
                r += rowStep;
                c += colStep;
            }
        }
        return null;  // No blocking piece
    }
}
