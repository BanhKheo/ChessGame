package chessPieces;

import main.Board;
import utilz.LoadImage;

public class Rook extends Piece {
    private boolean isMove;
    public Rook( int x , int y ,  boolean isWhite){
        super(x, y , LoadImage.GetPieceImage(isWhite , "r") , isWhite);
    }



    @Override
    public  boolean logicMove( int oldRow , int oldCol , int newRow , int newCol , Board board){

        return oldCol == newCol || oldRow == newRow;
    }

    public int[] getBlockPieces(Board board, int newRow, int newCol) {
        int oldRow = this.getRow();
        int oldCol = this.getCol();

        // Moving vertically
        if (oldCol == newCol) {
            int step = (newRow > oldRow) ? 1 : -1;
            for (int r = oldRow + step; r != newRow; r += step) {
                if (board.getPieceAt(oldCol, r) != null) {
                    return new int[]{r, oldCol}; // Blocked piece found
                }
            }
        }

        // Moving horizontally
        else if (oldRow == newRow) {
            int step = (newCol > oldCol) ? 1 : -1;
            for (int c = oldCol + step; c != newCol; c += step) {
                if (board.getPieceAt(c, oldRow) != null) {
                    return new int[]{oldRow, c}; // Blocked piece found
                }
            }
        }

        return null; // No blocking piece
    }

}
