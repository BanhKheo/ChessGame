package chessPieces;

import utilz.LoadImage;

public class Pawn extends Piece {
    private boolean isMove = false;
    public Pawn( int x , int y , boolean isWhite){

        super(x, y , LoadImage.GetPieceImage(isWhite , "p") , isWhite);
    }

    @Override
    public  boolean logicMove( int oldRow , int oldCol , int newRow , int newCol ){
//        if( oldCol == newCol){
//            if(!isMove)
//            {
//                if( newRow == oldRow - 2 || newRow == oldRow - 1 ){
//                    isMove = true;
//                    return true;
//                }
//            }
//            else
//            {
//                if( newRow == oldRow - 1 )
//                    return true;
//            }
//        }
        int direction = isWhite ? -1 : 1;
        if (oldCol == newCol) {
            if (newRow == oldRow + direction) {
                return true;
            }
            if (!isMove && newRow == oldRow + 2*direction) {
                isMove = true;
                return true;
            }
        }
        return false;
    }

    public boolean isMoved () {
        return isMove;
    }

    public void setMove (boolean move) {
        this.isMove = move;
    }
}
