package chessPieces;

import main.Board;
import utilz.LoadImage;

public class Bishop extends Piece {
    public Bishop( int x , int y , boolean isWhite){
        super(x, y , LoadImage.GetPieceImage(isWhite , "b") , isWhite );
    }


    @Override
    public  boolean logicMove(int oldRow , int oldCol , int newRow , int newCol , Board board){
        return Math.abs(oldRow - newRow) == Math.abs(oldCol - newCol);
    }

    @Override
    public int[] getBlockPieces(Board board, int newRow, int newCol) {
        int oldRow = this.getRow();
        int oldCol = this.getCol();

        // Check if movement is strictly diagonal
        if (Math.abs(newRow - oldRow) != Math.abs(newCol - oldCol)) {
            return null; // Invalid move for a bishop
        }

        int rowStep = Integer.compare(newRow, oldRow); // -1 (up), 1 (down)
        int colStep = Integer.compare(newCol, oldCol); // -1 (left), 1 (right)

        int row = oldRow + rowStep;
        int col = oldCol + colStep;

        while (row != newRow && col != newCol) {
            if (board.getPieceAt(col, row) != null) {
                return new int[]{row, col}; // First blocking piece
            }
            row += rowStep;
            col += colStep;
        }

        return null; // No blocking pieces
    }
}
