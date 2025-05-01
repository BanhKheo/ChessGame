package main;

import chessPieces.*;
import javafx.application.Platform;
import javafx.scene.layout.AnchorPane;
import ai.ChessAI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Board {

    private List<int[]> validMoves = new ArrayList<>();
    private Piece selectedPiece;
    private Piece[][] board = new Piece[8][8];
    private boolean whiteTurn = true;
    private boolean isPlayerMoving = true;
    private ChessController chessController;
    private final ExecutorService aiExecutor = Executors.newSingleThreadExecutor();
    private ChessAI ai;
    private boolean isAIEnabled = false; // Flag to control AI

    public Board() {
        initializePieces();
    }

    public void setChessController(ChessController controller) {
        this.chessController = controller;
    }

    public void setAIEnabled(boolean enabled) {
        this.isAIEnabled = enabled;
        this.ai = enabled ? new ChessAI(false) : null; // AI plays as black if enabled
    }

    public void handleSelectedPiece(int x, int y) {
        int col = x / Game.GAME_TILES;
        int row = y / Game.GAME_TILES;

        if (col < 0 || col >= 8 || row < 0 || row >= 8) {
            return;
        }

        Piece clickedPiece = board[row][col];

        if (selectedPiece == null) {
            if (clickedPiece != null && clickedPiece.isWhite() == whiteTurn) {
                selectedPiece = clickedPiece;
                validMoves = getValidMoves(selectedPiece);
            }
        } else {
            if (clickedPiece == selectedPiece) {
                // Deselect the piece if clicked again
                selectedPiece = null;
                validMoves.clear();
            } else {
                // Attempt to move or select a new piece
                movePiece(selectedPiece, col, row);
                selectedPiece = null;
                validMoves.clear();
                // If no move was made and a new piece is clicked, select it
                if (clickedPiece != null && clickedPiece.isWhite() == whiteTurn) {
                    selectedPiece = clickedPiece;
                    validMoves = getValidMoves(selectedPiece);
                }
            }
        }
        chessController.redraw(); // Redraw to update circles
    }

    private void movePiece(Piece piece, int col, int row) {
        synchronized (this) {
            if (legalMove(piece, col, row) && isPlayerMoving) {
                isPlayerMoving = false;

                int oldRow = piece.getRow();
                int oldCol = piece.getCol();

                board[oldRow][oldCol] = null;

                if (piece instanceof Pawn pawn) {
                    pawn.setMove(true);
                }
                if (piece instanceof Rook rook) {
                    rook.setMove(true);
                }
                if (piece instanceof King king) {
                    king.setMove(true);
                    handleCastling(king, oldCol, row, col);
                }

                piece.setRow(row);
                piece.setCol(col);
                board[row][col] = piece;

                boolean opponentInCheckmate = isCheckmate(!whiteTurn);

                if (opponentInCheckmate) {
                    if (chessController != null) {
                        chessController.handleCheckmate(whiteTurn);
                    }
                }

                whiteTurn = !whiteTurn;

                chessController.redraw();

                if (isAITurn()) {
                    performAIMove();
                } else {
                    isPlayerMoving = true; // Allow next player move if not AI's turn
                }
            }
        }
    }

    private void performAIMove() {
        chessController.showBotThinking(true);
        aiExecutor.submit(() -> {
            try {
                ChessAI.Move bestMove = ai.getBestMove(this, 3);
                Platform.runLater(() -> {
                    if (bestMove != null) {
                        synchronized (this) {
                            Piece aiPiece = getPieceAt(bestMove.fromRow, bestMove.fromCol);
                            if (aiPiece != null) {
                                board[bestMove.fromRow][bestMove.fromCol] = null;
                                aiPiece.setRow(bestMove.toRow);
                                aiPiece.setCol(bestMove.toCol);
                                board[bestMove.toRow][bestMove.toCol] = aiPiece;
                                whiteTurn = true;
                                isPlayerMoving = true;
                                chessController.redraw();
                                boolean opponentInCheckmate = isCheckmate(true);
                                if (opponentInCheckmate) {
                                    chessController.handleCheckmate(whiteTurn);
                                }
                            }
                        }
                    }
                    chessController.showBotThinking(false);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    isPlayerMoving = true;
                    chessController.showBotThinking(false);
                });
            }
        });
    }

    public void shutdown() {
        aiExecutor.shutdown();
    }

    private void handleCastling(King king, int oldCol, int row, int col) {
        if (col == oldCol + 2) {
            moveRookForCastling(row, 7, col - 1);
        } else if (col == oldCol - 2) {
            moveRookForCastling(row, 0, col + 1);
        }
    }

    private void moveRookForCastling(int row, int rookCol, int targetCol) {
        Piece rook = board[row][rookCol];
        if (rook instanceof Rook) {
            board[row][rookCol] = null;
            rook.setCol(targetCol);
            board[row][targetCol] = rook;
            ((Rook) rook).setMove(true);
        }
    }

    public void resetBoard() {
        board = new Piece[8][8];
        whiteTurn = true;
        selectedPiece = null;
        validMoves.clear();
        initializePieces();
        if (isAIEnabled) {
            ai = new ChessAI(false);
        } else {
            ai = null;
        }
    }

    private boolean legalMove(Piece piece, int col, int row) {
        if (!piece.logicMove(piece.getRow(), piece.getCol(), row, col, board)) {
            return false;
        }
        if (isBlocked(piece, row, col)) return false;
        Piece targetPiece = board[row][col];
        if (targetPiece != null && targetPiece.isWhite() == piece.isWhite()) {
            return false;
        }
        return true;
    }

    public boolean isCheckmate(boolean white) {
        if (!isKingInCheck(white)) return false;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board[row][col];
                if (piece != null && piece.isWhite() == white) {
                    List<int[]> moves = getValidMoves(piece);
                    for (int[] move : moves) {
                        Piece backup = board[move[1]][move[0]];
                        int oldRow = piece.getRow();
                        int oldCol = piece.getCol();

                        board[oldRow][oldCol] = null;
                        board[move[1]][move[0]] = piece;
                        piece.setRow(move[1]);
                        piece.setCol(move[0]);

                        boolean stillInCheck = isKingInCheck(white);

                        board[oldRow][oldCol] = piece;
                        board[move[1]][move[0]] = backup;
                        piece.setRow(oldRow);
                        piece.setCol(oldCol);

                        if (!stillInCheck) return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean isKingInCheck(boolean isWhitePlayer) {
        int kingRow = -1, kingCol = -1;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece piece = board[r][c];
                if (piece instanceof King && piece.isWhite() == isWhitePlayer) {
                    kingRow = r;
                    kingCol = c;
                    break;
                }
            }
        }
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece enemy = board[r][c];
                if (enemy != null && enemy.isWhite() != isWhitePlayer) {
                    if (enemy.logicMove(enemy.getRow(), enemy.getCol(), kingRow, kingCol, board)
                            && enemy.getBlockPieces(this, kingRow, kingCol) == null) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void initializePieces() {
        int[][] initialBoard = {
                {1, 2, 3, 4, 5, 3, 2, 1},
                {6, 6, 6, 6, 6, 6, 6, 6},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {6, 6, 6, 6, 6, 6, 6, 6},
                {1, 2, 3, 4, 5, 3, 2, 1}
        };
        for (int row = 0; row < 8; row++) {
            boolean isWhite = (row >= 6);
            for (int col = 0; col < 8; col++) {
                switch (initialBoard[row][col]) {
                    case 1 -> board[row][col] = new Rook(col, row, isWhite);
                    case 2 -> board[row][col] = new Knight(col, row, isWhite);
                    case 3 -> board[row][col] = new Bishop(col, row, isWhite);
                    case 4 -> board[row][col] = new Queen(col, row, isWhite);
                    case 5 -> board[row][col] = new King(col, row, isWhite);
                    case 6 -> board[row][col] = new Pawn(col, row, isWhite);
                    default -> {}
                }
            }
        }
    }

    private List<int[]> getValidMoves(Piece piece) {
        List<int[]> moves = new ArrayList<>();
        boolean wasMoved = false;
        if (piece instanceof Pawn pawn) {
            wasMoved = pawn.isMoved();
        } else if (piece instanceof King king) {
            wasMoved = king.isMoved();
            if (!king.isMoved() && !isKingInCheck(king.isWhite())) {
                int row = king.getRow();
                int col = king.getCol();
                if (canCastle(king, true)) {
                    moves.add(new int[]{col + 2, row});
                }
                if (canCastle(king, false)) {
                    moves.add(new int[]{col - 2, row});
                }
            }
        }
        int originalRow = piece.getRow();
        int originalCol = piece.getCol();
        Piece originalTarget;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (!legalMove(piece, col, row)) continue;
                if (isBlocked(piece, row, col)) continue;
                originalTarget = board[row][col];
                board[originalRow][originalCol] = null;
                board[row][col] = piece;
                piece.setRow(row);
                piece.setCol(col);
                boolean kingSafe = !isKingInCheck(piece.isWhite());
                board[originalRow][originalCol] = piece;
                board[row][col] = originalTarget;
                piece.setRow(originalRow);
                piece.setCol(originalCol);
                if (kingSafe) {
                    moves.add(new int[]{col, row});
                }
            }
        }
        if (piece instanceof Pawn pawn) {
            pawn.setMove(wasMoved);
        } else if (piece instanceof King king) {
            king.setMove(wasMoved);
        }
        return moves;
    }

    private boolean canCastle(King king, boolean kingSide) {
        int row = king.getRow();
        int col = king.getCol();
        if (kingSide) {
            Piece rook = board[row][7];
            if (!(rook instanceof Rook) || ((Rook) rook).isMoved()) return false;
            for (int c = col + 1; c < 7; c++) {
                if (board[row][c] != null) return false;
            }
            for (int c = col; c <= col + 2; c++) {
                if (isSquareUnderAttack(row, c, !king.isWhite())) return false;
            }
            return true;
        } else {
            Piece rook = board[row][0];
            if (!(rook instanceof Rook) || ((Rook) rook).isMoved()) return false;
            for (int c = col - 1; c > 0; c--) {
                if (board[row][c] != null) return false;
            }
            for (int c = col; c >= col - 2; c--) {
                if (isSquareUnderAttack(row, c, !king.isWhite())) return false;
            }
            return true;
        }
    }

    public void simulateMove(ChessAI.Move move) {
        move.capturedPiece = board[move.toRow][move.toCol];
        board[move.fromRow][move.fromCol] = null;
        board[move.toRow][move.toCol] = move.piece;
        move.piece.setRow(move.toRow);
        move.piece.setCol(move.toCol);
    }

    public List<ChessAI.Move> getAllLegalMoves(boolean isWhite) {
        List<ChessAI.Move> legalMoves = new ArrayList<>();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board[row][col];
                if (piece != null && piece.isWhite() == isWhite) {
                    List<int[]> validMoves = getValidMoves(piece);
                    for (int[] move : validMoves) {
                        legalMoves.add(new ChessAI.Move(piece, move[1], move[0]));
                    }
                }
            }
        }
        return legalMoves;
    }

    public void undoMove(ChessAI.Move move) {
        board[move.toRow][move.toCol] = move.capturedPiece;
        board[move.fromRow][move.fromCol] = move.piece;
        move.piece.setRow(move.fromRow);
        move.piece.setCol(move.fromCol);
    }

    private boolean isSquareUnderAttack(int row, int col, boolean byWhite) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece piece = board[r][c];
                if (piece != null && piece.isWhite() == byWhite) {
                    if (piece.logicMove(piece.getRow(), piece.getCol(), row, col, board)
                            && !isBlocked(piece, row, col)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isBlocked(Piece piece, int newRow, int newCol) {
        int[] blockedPos = piece.getBlockPieces(this, newRow, newCol);
        if (blockedPos != null) {
            for (int i = 1; i < blockedPos.length; i++) {
                int col = blockedPos[i];
                if (board[blockedPos[0]][col] != null) {
                    return true;
                }
            }
            return false;
        }
        if (piece instanceof Rook || piece instanceof Bishop || piece instanceof Queen) {
            int oldRow = piece.getRow();
            int oldCol = piece.getCol();
            int rowStep = Integer.compare(newRow, oldRow);
            int colStep = Integer.compare(newCol, oldCol);
            int r = oldRow + rowStep;
            int c = oldCol + colStep;
            while (r != newRow || c != newCol) {
                if (board[r][c] != null) {
                    return true;
                }
                r += rowStep;
                c += colStep;
            }
        }
        return false;
    }

    public Piece getPieceAt(int row, int col) {
        return board[row][col];
    }

    public void draw(AnchorPane boardGame) {
        boardGame.getChildren().clear();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece p = board[row][col];
                if (p != null) {
                    p.draw(boardGame);
                }
            }
        }
        if (selectedPiece != null && !validMoves.isEmpty()) {
            for (int[] move : validMoves) {
                javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle();
                circle.setRadius(16);
                circle.setCenterX(move[0] * Game.GAME_TILES + Game.GAME_TILES / 2);
                circle.setCenterY(move[1] * Game.GAME_TILES + Game.GAME_TILES / 2);
                circle.setFill(javafx.scene.paint.Color.rgb(169, 169, 169, 0.6));
                boardGame.getChildren().add(circle);
            }
        }
    }

    public Piece getSelectedPiece() {
        return selectedPiece;
    }

    public void setSelectedPiece(Piece piece) {
        this.selectedPiece = piece;
    }

    public List<int[]> getValidMoves() {
        return validMoves;
    }

    public boolean isWhiteTurn() {
        return whiteTurn;
    }

    public boolean isAITurn() {
        return isAIEnabled && !whiteTurn && ai != null;
    }

    public ChessController getChessController() {
        return chessController;
    }
}