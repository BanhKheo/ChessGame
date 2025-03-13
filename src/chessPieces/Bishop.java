package chessPieces;

import utilz.LoadImage;

public class Bishop extends Piece {
    public Bishop( int x , int y , boolean isWhite){
        super(x, y , LoadImage.GetPieceImage(isWhite , "b") , isWhite );
    }


    @Override
    public  boolean logicMove( int oldRow , int oldCol , int newRow , int newCol){
        return Math.abs(oldRow - newRow) == Math.abs(oldCol - newCol);
    }
}
