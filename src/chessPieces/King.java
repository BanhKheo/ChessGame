package chessPieces;

import utilz.LoadImage;

public class King extends Piece {
    private boolean isMove;
    public King( int x , int y ,  boolean isWhite){
        super(x, y , LoadImage.GetPieceImage(isWhite , "k") ,  isWhite);
    }

    @Override
    public  boolean logicMove( int oldRow , int oldCol , int newRow , int newCol){
        return newRow == oldRow - 1 || newRow == oldRow + 1
            || newCol == oldCol - 1 || newCol == oldCol + 1 ;
    }
}
