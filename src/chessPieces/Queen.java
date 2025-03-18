package chessPieces;

import main.Board;
import utilz.LoadImage;

public class Queen extends Piece {
    public Queen( int x , int y ,  boolean isWhite){
        super(x, y , LoadImage.GetPieceImage(isWhite , "q") , isWhite);

    }

    @Override
    public  boolean logicMove( int oldRow , int oldCol , int newRow , int newCol ){
        return Math.abs(oldCol - newCol) == Math.abs(oldRow - newRow)
                || oldRow == newRow
                || oldCol == newCol ;
    }

    @Override
    public int[] getBlockPieces(Board board, int newRow, int newCol) {
        int oldRow = this.getRow();
        int oldCol = this.getCol();

        // Use Rook's logic if moving like a rook
        if (oldRow == newRow || oldCol == newCol) {
            return new Rook(oldCol, oldRow, this.isWhite()).getBlockPieces(board, newRow, newCol);
        }

        // Use Bishop's logic if moving like a bishop
        if (Math.abs(newRow - oldRow) == Math.abs(newCol - oldCol)) {
            return new Bishop(oldCol, oldRow, this.isWhite()).getBlockPieces(board, newRow, newCol);
        }

        return null; // No blocking piece
    }



}
