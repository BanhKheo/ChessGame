package utilz;

import chessPieces.*;

public class Constants {
    public static final int ROOK = 1;
    public static final int KNIGHT = 2;
    public static final int BISHOP = 3;
    public static final int QUEEN = 4;
    public static final int KING = 5;
    public static final int PAWN = 6;


    public static int getType(Piece piece) {
        if (piece == null) return -1;

        if (piece instanceof Rook) return ROOK;
        if (piece instanceof Knight) return KNIGHT;
        if (piece instanceof Bishop) return BISHOP;
        if (piece instanceof Queen) return QUEEN;
        if (piece instanceof King) return KING;
        if (piece instanceof Pawn) return PAWN;

        return -1;
    }



}
