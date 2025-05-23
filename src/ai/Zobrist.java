package ai;

import java.util.concurrent.ThreadLocalRandom;
import chessPieces.Piece;

import static utilz.Constants.getType;


public class Zobrist {
    // [color][pieceType][squareIndex], color: 0=white, 1=black, pieceType: 0-5 (P,N,B,R,Q,K)
    private static final long[][][] PIECE_HASH = new long[2][6][64];
    private static final long WHITE_TO_MOVE_HASH;

    static {
        // Use a fixed seed for reproducibility in debugging and testing
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        RandomSeeded seededRand = new RandomSeeded(2025);

        for (int color = 0; color < 2; color++) {
            for (int pieceType = 0; pieceType < 6; pieceType++) {
                for (int square = 0; square < 64; square++) {
                    PIECE_HASH[color][pieceType][square] = seededRand.nextLong();
                }
            }
        }
        WHITE_TO_MOVE_HASH = seededRand.nextLong();
    }

    /**
     * Computes the Zobrist hash for the given board and side to move.
     * Only piece placement and side to move are included (no castling/en passant).
     */
    public static long computeHash(Piece[][] board, boolean whiteToMove) {
        long hash = 0L;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece p = board[row][col];
                if (p != null) {
                    int color = p.isWhite() ? 0 : 1;
                    int type = getType(p); // 1 to 6 (PAWN=1, ..., KING=6)
                    if (type >= 1 && type <= 6) {
                        int adjustedType = type - 1;
                        int sq = row * 8 + col;
                        hash ^= PIECE_HASH[color][adjustedType][sq];
                    }
                }
            }
        }
        if (whiteToMove) hash ^= WHITE_TO_MOVE_HASH;
        return hash;
    }

    // Utility for deterministic random generation
    private static class RandomSeeded {
        private long seed;

        RandomSeeded(long seed) { this.seed = (seed ^ 0x5DEECE66DL) & ((1L << 48) - 1); }

        long nextLong() {
            // Linear Congruential Generator, same as java.util.Random
            seed = (seed * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
            return (seed >>> 16) ^ (seed << 32);
        }
    }
}