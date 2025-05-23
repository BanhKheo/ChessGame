package ai;

import chessPieces.*;
import main.Board;
import utilz.MoveSnapshot;

import java.util.*;

public class ChessAI {
    private final boolean isWhiteAI;
    private final TranspositionTable transpositionTable = new TranspositionTable();

    // Piece-Square Tables (standard values for stronger eval)
    private static final int[][] PAWN_TABLE = {
            {0, 0, 0, 0, 0, 0, 0, 0},
            {5, 5, 5, 5, 5, 5, 5, 5},
            {1, 1, 2, 3, 3, 2, 1, 1},
            {0, 0, 0, 2, 2, 0, 0, 0},
            {0, 0, 0, 2, 2, 0, 0, 0},
            {1, -1,-2, 0, 0,-2,-1, 1},
            {1, 2, 2,-2,-2, 2, 2, 1},
            {0, 0, 0, 0, 0, 0, 0, 0}
    };


    public ChessAI(boolean isWhiteAI) {
        this.isWhiteAI = isWhiteAI;
    }

    public Move getBestMove(Board board, int maxDepth) {
        long startTime = System.currentTimeMillis();
        Move bestMove = null;
        int lastDepth = 0;

        for (int depth = 1; depth <= maxDepth; depth++) {
            Result result = minimax(board, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, isWhiteAI);
            if (result.move != null) bestMove = result.move;

            if (board.getAllLegalMoves(isWhiteAI).isEmpty()) {
                System.out.println("No legal moves available for AI at depth " + depth);
                break;
            }
            lastDepth = depth;
        }

        System.out.printf("AI computed move in %dms at final depth %d%n",
                System.currentTimeMillis() - startTime, lastDepth);
        return bestMove;
    }

    private Result minimax(Board board, int depth, int alpha, int beta, boolean maximizingPlayer) {
        long hash = Zobrist.computeHash(board.getBoard(), maximizingPlayer);
        TranspositionTable.TTEntry ttEntry = transpositionTable.get(hash);

        if (ttEntry != null && ttEntry.depth >= depth) {
            switch (ttEntry.flag) {
                case 0: return new Result(ttEntry.score, ttEntry.bestMove);
                case -1: if (ttEntry.score <= alpha) return new Result(alpha, ttEntry.bestMove); break;
                case 1: if (ttEntry.score >= beta) return new Result(beta, ttEntry.bestMove); break;
            }
        }

        if (depth == 0 || board.isGameEnded()) {
            return new Result(evaluateBoard(board), null);
        }

        List<Move> moves = board.getAllLegalMoves(maximizingPlayer);
        moves.sort(Comparator.comparingInt(m -> -scoreMove(m, board))); // Improved move ordering

        Move bestMove = null;
        int bestScore = maximizingPlayer ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        int alphaOrig = alpha;

        for (Move move : moves) {
            MoveSnapshot snapshot = board.simulateMove(move.piece, move.toRow, move.toCol);
            int eval = minimax(board, depth - 1, alpha, beta, !maximizingPlayer).score;
            board.undoMove(snapshot);

            if (maximizingPlayer) {
                if (eval > bestScore) {
                    bestScore = eval;
                    bestMove = move;
                }
                alpha = Math.max(alpha, bestScore);
            } else {
                if (eval < bestScore) {
                    bestScore = eval;
                    bestMove = move;
                }
                beta = Math.min(beta, bestScore);
            }
            if (beta <= alpha) break;
        }

        // Store TT entry
        int flag = 0;
        if (bestScore <= alphaOrig) flag = 1; // Upper bound
        else if (bestScore >= beta) flag = -1; // Lower bound

        transpositionTable.put(hash, new TranspositionTable.TTEntry(depth, bestScore, flag, bestMove));
        return new Result(bestScore, bestMove);
    }

    private int evaluateBoard(Board board) {
        int score = 0;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece p = board.getPieceAt(row, col);
                if (p != null) {
                    int value = p.getValue();
                    int bonus = getPositionalBonus(p, row, col);
                    score += (p.isWhite() ? value + bonus : -value - bonus);
                }
            }
        }
        return score;
    }

    private int getPositionalBonus(Piece piece, int row, int col) {
        if (piece instanceof Pawn)    return piece.isWhite() ? PAWN_TABLE[row][col]   : PAWN_TABLE[7 - row][col];
        return 0;
    }

    // Improved move ordering: prioritize captures, checks, promotions, center control
    private static int scoreMove(Move move, Board board) {
        int score = 0;
        if (move.toRow < 0 || move.toRow >= 8 || move.toCol < 0 || move.toCol >= 8)
            return Integer.MIN_VALUE;

        if (move.capturedPiece != null) {
            score += 10 * move.capturedPiece.getValue() - move.piece.getValue();
        }
        if ((move.toRow == 3 || move.toRow == 4) && (move.toCol == 3 || move.toCol == 4)) score += 20;
        if (move.piece instanceof Pawn && (move.toRow == 0 || move.toRow == 7)) score += 800;

        try {
            MoveSnapshot snapshot = board.simulateMove(move.piece, move.toRow, move.toCol);
            if (board.isKingInCheck(!move.piece.isWhite())) score += 200;
            board.undoMove(snapshot);
        } catch (ArrayIndexOutOfBoundsException e) {
            return Integer.MIN_VALUE;
        }
        return score;
    }

    public static class Move {
        public final Piece piece;
        public final int fromRow, fromCol;
        public final int toRow, toCol;
        public final Piece capturedPiece;

        public Move(Piece piece, int toRow, int toCol) {
            this.piece = piece;
            this.fromRow = piece.getRow();
            this.fromCol = piece.getCol();
            this.toRow = toRow;
            this.toCol = toCol;
            this.capturedPiece = null; // Optionally, Board can provide this
        }

        public Move(Piece piece, int toRow, int toCol, Piece capturedPiece) {
            this.piece = piece;
            this.fromRow = piece.getRow();
            this.fromCol = piece.getCol();
            this.toRow = toRow;
            this.toCol = toCol;
            this.capturedPiece = capturedPiece;
        }
    }

    private static class Result {
        final int score;
        final Move move;
        Result(int score, Move move) {
            this.score = score;
            this.move = move;
        }
    }
}