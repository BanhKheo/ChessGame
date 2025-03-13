package chessPieces;

import utilz.LoadImage;

public class Queen extends Piece {
    public Queen( int x , int y ,  boolean isWhite){
        super(x, y , LoadImage.GetPieceImage(isWhite , "q") , isWhite);

    }

    @Override
    public  boolean logicMove( int oldRow , int oldCol , int newRow , int newCol){
        return Math.abs(oldCol - newCol) == Math.abs(oldRow - newRow)
                || oldRow == newRow
                || oldCol == newCol ;
    }

}
