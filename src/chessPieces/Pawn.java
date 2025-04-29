package chessPieces;

import utilz.LoadImage;

public class Pawn extends Piece {
    private boolean isMove = false;
    public Pawn(int x , int y , boolean isWhite){

        super(x, y , LoadImage.GetPieceImage(isWhite , "p") , isWhite);
    }

    @Override
    public  boolean logicMove( int oldRow , int oldCol , int newRow , int newCol, Piece[][] board){
        int direction = isWhite ? -1 : 1; // White pawn go up (-1), Black pawn go down (+1)
        if (oldCol == newCol) {
            if (newRow == oldRow + direction) {
                if (board [newRow][newCol] == null) { // If next step is null, can move
                    return true;
                }
            }

            if (!isMove && newRow == oldRow + 2*direction) {
                if (board [newRow][newCol] == null && board[oldRow + direction][newCol] == null) { // If next step is null, can move
                    isMove = true;
                    return true;
                }
            }
        }

        // Destroy enemy
        if (Math.abs(newCol - oldCol) == 1 && newRow == oldRow + direction) {
            Piece target = board[newRow][newCol];
            if(target != null && target.isWhite != this.isWhite) {
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
    @Override
    public int getValue() {
        return 1;
    }
}
