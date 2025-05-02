package ai;

import java.util.Random;
import chessPieces.Piece;

import static utilz.Constants.getType;

public class Zobrist {
    private static final long[][][] pieceHash = new long[2][6][64]; // [color][pieceType 0–5][square]
    private static final long whiteToMoveHash;

    static {
        Random rand = new Random(2025); // Fixed seed for reproducibility
        for (int color = 0; color < 2; color++) {
            for (int pieceType = 0; pieceType < 6; pieceType++) {
                for (int square = 0; square < 64; square++) {
                    pieceHash[color][pieceType][square] = rand.nextLong();
                }
            }
        }
        whiteToMoveHash = rand.nextLong();
    }

    public static long computeHash(Piece[][] board, boolean whiteToMove) {
        long hash = 0;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece p = board[row][col];
                if (p != null) {
                    int color = p.isWhite() ? 0 : 1;
                    int type = getType(p); // 1 to 6
                    if (type >= 1 && type <= 6) {
                        int adjustedType = type - 1; // convert 1–6 to 0–5
                        int squareIndex = row * 8 + col;
                        hash ^= pieceHash[color][adjustedType][squareIndex];
                    }
                }
            }
        }
        if (whiteToMove) hash ^= whiteToMoveHash;
        return hash;
    }
}
