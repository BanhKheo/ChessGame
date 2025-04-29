package chessPieces;

import utilz.LoadImage;

public class Knight extends Piece {
    public Knight( int x , int y ,  boolean isWhite){
        super(x, y , LoadImage.GetPieceImage(isWhite , "n") , isWhite);
    }

    @Override
    public  boolean logicMove( int oldRow , int oldCol , int newRow , int newCol, Piece[][] board){
        int rowDiff = Math.abs(newRow - oldRow);
        int colDiff = Math.abs(newCol - oldCol);

        return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
    }
    @Override
    public int getValue() {
        return 3;
    }
}
