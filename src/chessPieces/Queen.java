package chessPieces;

import main.Board;
import utilz.LoadImage;

public class Queen extends Piece {
    public Queen( int x , int y ,  boolean isWhite){
        super(x, y , LoadImage.GetPieceImage(isWhite , "q") , isWhite);

    }

    @Override
    public  boolean logicMove( int oldRow , int oldCol , int newRow , int newCol , Board board){
        return Math.abs(oldCol - newCol) == Math.abs(oldRow - newRow)
                || oldRow == newRow
                || oldCol == newCol ;
    }

    @Override
    public int[] getBlockPieces(Board board, int newRow, int newCol) {
        int oldRow = this.getRow();
        int oldCol = this.getCol();

        int rowStep = Integer.compare(newRow, oldRow);
        int colStep = Integer.compare(newCol, oldCol);

        // If both change → Diagonal (Bishop move)
        // If only row changes → Vertical (Rook move)
        // If only col changes → Horizontal (Rook move)
        if ((Math.abs(newRow - oldRow) == Math.abs(newCol - oldCol)) ||
                (oldRow == newRow || oldCol == newCol)) {

            int row = oldRow + rowStep;
            int col = oldCol + colStep;

            while (row != newRow || col != newCol) {
                if (board.getPieceAt(col, row) != null) {
                    return new int[]{row, col}; // First blocking piece
                }
                row += rowStep;
                col += colStep;
            }
        }

        return null; // No blocking pieces
    }


}
