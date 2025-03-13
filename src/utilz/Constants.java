package utilz;

import chessPieces.*;

public class Constants {
    public static final int ROOK = 1;
    public static final int KNIGHT = 2;
    public static final int BISHOP = 3;
    public static final int QUEEN = 4;
    public static final int KING = 5;
    public static final int PAWN = 6;

    public static int getPiecesState(Piece piece) {
        if (piece instanceof Rook) {
            return ROOK;
        } else if (piece instanceof Knight) {
            return KNIGHT;
        } else if (piece instanceof Bishop) {
            return BISHOP;
        } else if (piece instanceof Queen) {
            return QUEEN;
        } else if (piece instanceof King) {
            return KING;
        } else if (piece instanceof Pawn) {
            return PAWN;
        } else {
            return -1; // Unknown piece type
        }
    }

}
