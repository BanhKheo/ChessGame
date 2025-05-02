package ai;

import chessPieces.Pawn;
import chessPieces.Piece;
import main.Board;
import utilz.MoveSnapshot;

import java.util.*;

public class ChessAI {
    private final boolean isWhiteAI;
    private final TranspositionTable transpositionTable = new TranspositionTable();

    public ChessAI(boolean isWhiteAI) {
        this.isWhiteAI = isWhiteAI;
    }

    public Move getBestMove(Board board, int maxDepth) {
        long startTime = System.currentTimeMillis();
        Move bestMove = null;
        int depth = 1;

        while (depth <= maxDepth) {
            System.out.println("Searching at depth " + depth);
            Result result = minimax(board, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, isWhiteAI, depth);
            if (result.move != null) {
                bestMove = result.move;
            }

            List<Move> legalMoves = board.getAllLegalMoves(isWhiteAI);
            if (legalMoves.isEmpty()) {
                System.out.println("No legal moves available for AI at depth " + depth);
                break;
            }
            depth++;
        }

        long endTime = System.currentTimeMillis();
        System.out.println("AI computed move in " + (endTime - startTime) + "ms at final depth " + (depth - 1));
        return bestMove;
    }

    private Result minimax(Board board, int depth, int alpha, int beta, boolean maximizingPlayer, int rootDepth) {
        long hash = Zobrist.computeHash(board.getBoard(), maximizingPlayer);

        TTEntry ttEntry = transpositionTable.get(hash);
        if (ttEntry != null && ttEntry.depth >= depth) {
            if (ttEntry.flag == 0) return new Result(ttEntry.score, ttEntry.bestMove);
            if (ttEntry.flag == -1 && ttEntry.score <= alpha) return new Result(alpha, ttEntry.bestMove);
            if (ttEntry.flag == 1 && ttEntry.score >= beta) return new Result(beta, ttEntry.bestMove);
        }

        if (depth == 0 || board.isGameEnded()) {
            int eval = evaluateBoard(board);
            return new Result(eval, null);
        }

        List<Move> allMoves = board.getAllLegalMoves(maximizingPlayer);
        allMoves.sort((m1, m2) -> Integer.compare(scoreMove(m2, board), scoreMove(m1, board)));

        Move bestMove = null;
        int bestScore = maximizingPlayer ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (Move move : allMoves) {
            MoveSnapshot snapshot = board.simulateMove(move.piece, move.toRow, move.toCol);
            int eval = minimax(board, depth - 1, alpha, beta, !maximizingPlayer, rootDepth).score;
            board.undoMove(snapshot);

            if (maximizingPlayer) {
                if (eval > bestScore) {
                    bestScore = eval;
                    bestMove = move;
                }
                alpha = Math.max(alpha, eval);
            } else {
                if (eval < bestScore) {
                    bestScore = eval;
                    bestMove = move;
                }
                beta = Math.min(beta, eval);
            }

            if (beta <= alpha) break;
        }

        // Save in TT
        int flag;
        if (bestScore <= alpha) flag = 1;           // Upper bound
        else if (bestScore >= beta) flag = -1;      // Lower bound
        else flag = 0;                               // Exact

        transpositionTable.put(hash, new TTEntry(depth, bestScore, flag, bestMove));
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

    private int scoreMove(Move move, Board board) {
        int score = 0;

        // Safety check
        if (move.toRow < 0 || move.toRow >= 8 || move.toCol < 0 || move.toCol >= 8) {
            return Integer.MIN_VALUE; // Treat as invalid move
        }

        if (move.capturedPiece != null) {
            int captureValue = move.capturedPiece.getValue();
            int attackerValue = move.piece.getValue();
            score += 10 * captureValue - attackerValue;
        }

        if ((move.toRow == 3 || move.toRow == 4) && (move.toCol == 3 || move.toCol == 4)) {
            score += 20;
        }

        if (move.piece instanceof Pawn && (move.toRow == 0 || move.toRow == 7)) {
            score += 800;
        }

        try {
            MoveSnapshot snapshot = board.simulateMove(move.piece, move.toRow, move.toCol);
            boolean givesCheck = board.isKingInCheck(!move.piece.isWhite());
            board.undoMove(snapshot);
            if (givesCheck) {
                score += 200;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Invalid move coordinates in scoreMove(): " + move.toRow + ", " + move.toCol);
            return Integer.MIN_VALUE;
        }

        return score;
    }





    private int getPositionalBonus(Piece piece, int row, int col) {
        int[][] pawnTable = {
                {0, 0, 0, 0, 0, 0, 0, 0},
                {5, 5, 5, 5, 5, 5, 5, 5},
                {1, 1, 2, 3, 3, 2, 1, 1},
                {0, 0, 0, 2, 2, 0, 0, 0},
                {0, 0, 0, 2, 2, 0, 0, 0},
                {1, -1,-2, 0, 0,-2,-1, 1},
                {1, 2, 2,-2,-2, 2, 2, 1},
                {0, 0, 0, 0, 0, 0, 0, 0}
        };

        if (piece instanceof Pawn) {
            return piece.isWhite() ? pawnTable[row][col] : pawnTable[7 - row][col];
        }

        return 0;
    }

    public static class Move {
        public Piece piece;
        public int fromRow, fromCol;
        public int toRow, toCol;
        public Piece capturedPiece;

        public Move(Piece piece, int toRow, int toCol) {
            this.piece = piece;
            this.fromRow = piece.getRow();
            this.fromCol = piece.getCol();
            this.toRow = toRow;
            this.toCol = toCol;
        }
    }

    private static class Result {
        int score;
        Move move;

        Result(int score, Move move) {
            this.score = score;
            this.move = move;
        }
    }
}
