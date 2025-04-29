package ai;



import chessPieces.Piece;

import java.util.List;

import main.Board;



public class ChessAI {

    private final boolean isWhiteAI;



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



    //alpha store the maximizing score AI can guarantee, beta store the minimizing score player can guarantee

    private Result minimax(Board board, int depth, int alpha, int beta, boolean maximizingPlayer) {

        if (depth == 0 || board.isCheckmate(!maximizingPlayer)) {

            return new Result(evaluateBoard(board), null);

        }



        List<Move> allMoves = board.getAllLegalMoves(maximizingPlayer);

        Move bestMove = null;



        //Ai turn

        if (maximizingPlayer) {

            int maxEval = Integer.MIN_VALUE;

            for (Move move : allMoves) {

                //Make the move and calculate the score of all valid move

                board.simulateMove(move);



                //Switch the player turn to find worst move

                int eval = minimax(board, depth - 1, alpha, beta, false).score;

                board.undoMove(move);



                if (eval > maxEval) {

                    maxEval = eval;

                    bestMove = move;

                }

                alpha = Math.max(alpha, eval);

                //cut branches to save time

                if (beta <= alpha) break;

            }

            return new Result(maxEval, bestMove);

        }

        //Player turn

        else {

            int minEval = Integer.MAX_VALUE;

            for (Move move : allMoves) {

                board.simulateMove(move);

                int eval = minimax(board, depth - 1, alpha, beta, true).score;

                board.undoMove(move);



                if (eval < minEval) {

                    minEval = eval;

                    bestMove = move;

                }

                beta = Math.min(beta, eval);



                //cut branches to save time

                if (beta <= alpha) break;

            }

            return new Result(minEval, bestMove);

        }

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