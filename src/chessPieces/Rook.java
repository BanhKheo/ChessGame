package chessPieces;

import utilz.LoadImage;

public class Rook extends Piece {
    private boolean isMove;
    public Rook( int x , int y ,  boolean isWhite){
        super(x, y , LoadImage.GetPieceImage(isWhite , "r") , isWhite);
    }



    @Override
    public  boolean logicMove( int oldRow , int oldCol , int newRow , int newCol){
        return oldCol == newCol || oldRow == newRow;
    }
}
