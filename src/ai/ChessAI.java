package ai;

import chessPieces.Pawn;
import chessPieces.Piece;

import java.util.Comparator;
import java.util.List;

import main.Board;
import utilz.MoveSnapshot;

public class ChessAI {

    private final boolean isWhiteAI;
    private final TranspositionTable transpositionTable = new TranspositionTable();

    public ChessAI(boolean isWhiteAI) {
        this.isWhiteAI = isWhiteAI;
    }

    public Move getBestMove(Board board, int depth) {
        long startTime = System.currentTimeMillis();
        Result result = minimax(board, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, isWhiteAI);
        long endTime = System.currentTimeMillis();
        System.out.println("AI computed move in " + (endTime - startTime) + "ms at depth " + depth);
        return result.move;
    }

    private Result minimax(Board board, int depth, int alpha, int beta, boolean maximizingPlayer) {
        long hash = Zobrist.computeHash(board.getBoard(), maximizingPlayer);
        TranspositionTable.TTEntry ttEntry = transpositionTable.get(hash);

        if (ttEntry != null && ttEntry.depth >= depth) {
            switch (ttEntry.flag) {
                case 0: return new Result(ttEntry.score, ttEntry.bestMove);
                case -1: if (ttEntry.score >= beta) return new Result(ttEntry.score, ttEntry.bestMove); break;
                case 1: if (ttEntry.score <= alpha) return new Result(ttEntry.score, ttEntry.bestMove); break;
            }
        }

        if (depth == 0 || board.isGameEnded()) {
            return new Result(evaluateBoard(board), null);
        }

        List<Move> moves = board.getAllLegalMoves(maximizingPlayer);
        // Filter out any out-of-bounds moves (shouldn't be needed if move gen is correct)
        moves.removeIf(m -> m.toRow < 0 || m.toRow >= 8 || m.toCol < 0 || m.toCol >= 8);
        moves.sort(Comparator.comparingInt(m -> -scoreMove(m, board)));

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
        if (bestScore >= beta) flag = -1; // LOWERBOUND
        else if (bestScore <= alphaOrig) flag = 1; // UPPERBOUND

        transpositionTable.put(hash, new TranspositionTable.TTEntry(depth, bestScore, flag, bestMove));
        return new Result(bestScore, bestMove);
    }

    // Improved move ordering: prioritize captures, checks, promotions, center control
    private static int scoreMove(Move move, Board board) {
        int row = move.toRow, col = move.toCol;
        if (row < 0 || row >= 8 || col < 0 || col >= 8) return Integer.MIN_VALUE;

        int score = 0;
        if (move.capturedPiece != null) {
            score += 10 * move.capturedPiece.getValue() - move.piece.getValue();
        }
        if ((row == 3 || row == 4) && (col == 3 || col == 4)) score += 20;
        if (move.piece instanceof Pawn && (row == 0 || row == 7)) score += 800;

        // If you trust simulateMove will never go out of bounds now, you can remove try-catch
        MoveSnapshot snapshot = board.simulateMove(move.piece, row, col);
        if (board.isKingInCheck(!move.piece.isWhite())) score += 200;
        board.undoMove(snapshot);

        return score;
    }

    private int evaluateBoard(Board board) {
        int score = 0;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece p = board.getPieceAt(row, col);
                if (p != null) {
                    int value = p.getValue();
                    score += p.isWhite() ? value : -value;
                }
            }
        }
        return score;
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