package main;

import chessPieces.*;
import javafx.application.Platform;
import javafx.scene.layout.AnchorPane;
import utilz.MoveSnapshot;
import ai.ChessAI;
import static utilz.Constants.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The Board class encapsulates the state and logic of the chess game, including interaction with the AI.
 * It is UI-agnostic: it exposes methods for interacting with the game, but does not handle any UI code.
 */
public class Board {

    private final Piece[][] board = new Piece[8][8];
    private final List<Piece> whitePieces = new ArrayList<>();
    private final List<Piece> blackPieces = new ArrayList<>();
    private List<int[]> validMoves = Collections.emptyList();

    private int whiteKingRow = -1, whiteKingCol = -1;
    private int blackKingRow = -1, blackKingCol = -1;

    private Piece selectedPiece;
    private boolean whiteTurn = true;
    private boolean isPlayerMoving = true;
    private ChessController chessController;
    private final ExecutorService aiExecutor = Executors.newSingleThreadExecutor();
    private ChessAI ai;
    private boolean isAIEnabled = false;

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

    /**
     * Handles selection and move logic for a piece based on board coordinates.
     * Should only be called by the UI/controller after confirming it's the user's turn.
     */
    public void handleSelectedPiece(int x, int y) {
        if (isAITurn()) return; // Prevent user actions when it's AI's turn

        int col = x / Game.GAME_TILES;
        int row = y / Game.GAME_TILES;
        if (col < 0 || col >= 8 || row < 0 || row >= 8) return;

        Piece clickedPiece = board[row][col];

        if (selectedPiece == null) {
            if (clickedPiece != null && clickedPiece.isWhite() == whiteTurn) {
                selectPiece(clickedPiece);
            }
        } else {
            if (clickedPiece == selectedPiece) {
                deselectPiece();
            } else if (clickedPiece != null && clickedPiece.isWhite() == whiteTurn) {
                selectPiece(clickedPiece);
            } else if (isValidMove(col, row)) {
                movePiece(selectedPiece, col, row);
                deselectPiece();
            }
        }
    }

    private void selectPiece(Piece piece) {
        selectedPiece = piece;
        validMoves = getValidMoves(selectedPiece);
        if (chessController != null) chessController.redraw();
    }

    private void deselectPiece() {
        selectedPiece = null;
        validMoves = Collections.emptyList();
        if (chessController != null) chessController.redraw();
    }

    private boolean isValidMove(int col, int row) {
        for (int[] move : validMoves) {
            if (move[0] == col && move[1] == row) return true;
        }
        return false;
    }

    /**
     * Actually perform a move for the given piece.
     */
    private void movePiece(Piece piece, int col, int row) {
        if (!legalMove(piece, col, row) || !isPlayerMoving) return;

        isPlayerMoving = false;
        int oldRow = piece.getRow(), oldCol = piece.getCol();
        boolean wasFirstMove = recordFirstMoveStatus(piece);

        MoveSnapshot snapshot = new MoveSnapshot(piece, board[row][col], oldRow, oldCol, row, col, whiteTurn, wasFirstMove);
        if (chessController != null) chessController.addMoveSnapshot(snapshot);

        makeMove(piece, oldRow, oldCol, row, col);

        if (piece instanceof King king) {
            handleCastling(king, oldCol, row, col);
            updateKingPosition(king, row, col);
        }

        if (isCheckmate(!whiteTurn) && chessController != null) chessController.handleCheckmate(whiteTurn);

        whiteTurn = !whiteTurn;
        if (chessController != null) chessController.redraw();

        if (isAITurn()) performAIMove();
        else isPlayerMoving = true;
    }

    private void makeMove(Piece piece, int fromRow, int fromCol, int toRow, int toCol) {
        board[fromRow][fromCol] = null;
        piece.setRow(toRow);
        piece.setCol(toCol);
        board[toRow][toCol] = piece;
        updateMoveStatus(piece);
    }

    private boolean recordFirstMoveStatus(Piece piece) {
        if (piece instanceof Pawn pawn) return !pawn.isMoved();
        if (piece instanceof King king) return !king.isMoved();
        if (piece instanceof Rook rook) return !rook.isMoved();
        return false;
    }

    private void updateMoveStatus(Piece piece) {
        if (piece instanceof Pawn pawn) pawn.setMove(true);
        else if (piece instanceof King king) king.setMove(true);
        else if (piece instanceof Rook rook) rook.setMove(true);
    }

    private void performAIMove() {
        if (chessController != null) chessController.showBotThinking(true);
        aiExecutor.submit(() -> {
            try {
                ChessAI.Move bestMove = ai.getBestMove(this, 3);
                Platform.runLater(() -> {
                    if (bestMove != null) {
                        synchronized (this) {
                            Piece aiPiece = getPieceAt(bestMove.fromRow, bestMove.fromCol);
                            if (aiPiece != null) {
                                makeMove(aiPiece, bestMove.fromRow, bestMove.fromCol, bestMove.toRow, bestMove.toCol);
                                whiteTurn = true;
                                isPlayerMoving = true;
                                if (chessController != null) {
                                    chessController.redraw();
                                    if (isCheckmate(true)) chessController.handleCheckmate(whiteTurn);
                                }
                            }
                        }
                    }
                    if (chessController != null) chessController.showBotThinking(false);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    if (isCheckmate(true) && chessController != null)
                        chessController.handleCheckmate(false);
                    whiteTurn = true;
                    isPlayerMoving = true;
                });
            }
        });
    }

    public void shutdown() {
        aiExecutor.shutdown();
    }

    private void handleCastling(King king, int oldCol, int row, int col) {
        if (col == oldCol + 2) moveRookForCastling(row, 7, col - 1);
        else if (col == oldCol - 2) moveRookForCastling(row, 0, col + 1);
    }

    private void moveRookForCastling(int row, int rookCol, int targetCol) {
        Piece rook = board[row][rookCol];
        if (rook instanceof Rook) {
            board[row][rookCol] = null;
            rook.setCol(targetCol);
            board[row][targetCol] = rook;
            ((Rook) rook).setMove(true);
            List<Piece> pieces = rook.isWhite() ? whitePieces : blackPieces;
            pieces.remove(rook);
            pieces.add(rook);
        }
    }

    public void resetBoard() {
        for (int row = 0; row < 8; row++)
            for (int col = 0; col < 8; col++)
                board[row][col] = null;
        whiteTurn = true;
        selectedPiece = null;
        validMoves = Collections.emptyList();
        whiteKingRow = whiteKingCol = blackKingRow = blackKingCol = -1;
        initializePieces();
        ai = isAIEnabled ? new ChessAI(false) : null;
        if (chessController != null) chessController.redraw();
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

        whitePieces.clear();
        blackPieces.clear();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                boolean isWhite = row >= 6;
                int type = initialBoard[row][col];
                Piece piece = switch (type) {
                    case ROOK -> new Rook(col, row, isWhite);
                    case KNIGHT -> new Knight(col, row, isWhite);
                    case BISHOP -> new Bishop(col, row, isWhite);
                    case QUEEN -> new Queen(col, row, isWhite);
                    case KING -> new King(col, row, isWhite);
                    case PAWN -> new Pawn(col, row, isWhite);
                    default -> null;
                };
                board[row][col] = piece;
                if (piece != null) {
                    (isWhite ? whitePieces : blackPieces).add(piece);
                    if (piece instanceof King) updateKingPosition((King) piece, row, col);
                }
            }
        }
    }

    private boolean legalMove(Piece piece, int col, int row) {
        if (!piece.logicMove(piece.getRow(), piece.getCol(), row, col, board)) return false;
        if (isBlocked(piece, row, col)) return false;
        Piece targetPiece = board[row][col];
        return targetPiece == null || targetPiece.isWhite() != piece.isWhite();
    }

    private boolean isBlocked(Piece piece, int newRow, int newCol) {
        int[] blockedPos = piece.getBlockPieces(this, newRow, newCol);
        if (blockedPos != null) {
            for (int i = 1; i < blockedPos.length; i++) {
                if (board[blockedPos[0]][blockedPos[i]] != null) return true;
            }
            return false;
        }
        if (piece instanceof Rook || piece instanceof Bishop || piece instanceof Queen) {
            int oldRow = piece.getRow(), oldCol = piece.getCol();
            int rowStep = Integer.compare(newRow, oldRow), colStep = Integer.compare(newCol, oldCol);
            for (int r = oldRow + rowStep, c = oldCol + colStep; r != newRow || c != newCol; r += rowStep, c += colStep) {
                if (board[r][c] != null) return true;
            }
        }
        return false;
    }

    private List<int[]> getValidMoves(Piece piece) {
        List<int[]> moves = new ArrayList<>();
        boolean wasMoved = false;
        if (piece instanceof Pawn pawn) wasMoved = pawn.isMoved();
        else if (piece instanceof King king) {
            wasMoved = king.isMoved();
            if (!king.isMoved() && !isKingInCheck(king.isWhite())) {
                int row = king.getRow(), col = king.getCol();
                if (canCastle(king, true)) moves.add(new int[]{col + 2, row});
                if (canCastle(king, false)) moves.add(new int[]{col - 2, row});
            }
        }
        int originalRow = piece.getRow(), originalCol = piece.getCol();
        Piece originalTarget;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (!legalMove(piece, col, row) || isBlocked(piece, row, col)) continue;
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
                if (kingSafe) moves.add(new int[]{col, row});
            }
        }
        if (piece instanceof Pawn pawn) pawn.setMove(wasMoved);
        else if (piece instanceof King king) king.setMove(wasMoved);
        return moves;
    }

    private boolean canCastle(King king, boolean kingSide) {
        int row = king.getRow(), col = king.getCol();
        int rookCol = kingSide ? 7 : 0;
        Piece rook = board[row][rookCol];
        if (!(rook instanceof Rook) || ((Rook) rook).isMoved()) return false;
        int step = kingSide ? 1 : -1;
        int clearTill = kingSide ? 7 : 0;
        for (int c = col + step; kingSide ? c < clearTill : c > clearTill; c += step) {
            if (board[row][c] != null) return false;
        }
        for (int c = col; kingSide ? c <= col + 2 : c >= col - 2; c += step) {
            if (isSquareUnderAttack(row, c, !king.isWhite())) return false;
        }
        return true;
    }

    public List<ChessAI.Move> getAllLegalMoves(boolean isWhite) {
        List<ChessAI.Move> legalMoves = new ArrayList<>();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board[row][col];
                if (piece != null && piece.isWhite() == isWhite) {
                    for (int[] move : getValidMoves(piece)) {
                        legalMoves.add(new ChessAI.Move(piece, move[1], move[0]));
                    }
                }
            }
        }
        return legalMoves;
    }

    public boolean isCheckmate(boolean white) {
        if (!isKingInCheck(white)) return false;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board[row][col];
                if (piece != null && piece.isWhite() == white) {
                    for (int[] move : getValidMoves(piece)) {
                        Piece backup = board[move[1]][move[0]];
                        int oldRow = piece.getRow(), oldCol = piece.getCol();
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
        for (int r = 0; r < 8 && kingRow == -1; r++) {
            for (int c = 0; c < 8; c++) {
                Piece piece = board[r][c];
                if (piece instanceof King && piece.isWhite() == isWhitePlayer) {
                    kingRow = r;
                    kingCol = c;
                    break;
                }
            }
        }
        // ADD THIS CHECK:
        if (kingRow == -1 || kingCol == -1) return false; // or throw new IllegalStateException("King not found!");

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

    public MoveSnapshot simulateMove(Piece piece, int toRow, int toCol) {
        int fromRow = piece.getRow(), fromCol = piece.getCol();
        Piece captured = board[toRow][toCol];
        MoveSnapshot snapshot = new MoveSnapshot(piece, captured, fromRow, fromCol, toRow, toCol, false, false);
        board[toRow][toCol] = piece;
        board[fromRow][fromCol] = null;
        piece.setRow(toRow);
        piece.setCol(toCol);
        return snapshot;
    }

    public void undoMove(MoveSnapshot snapshot) {
        if (snapshot == null || snapshot.movedPiece == null) return;
        Piece movingPiece = snapshot.movedPiece, capturedPiece = snapshot.capturedPiece;
        int fromRow = snapshot.fromRow, fromCol = snapshot.fromCol;
        int toRow = snapshot.toRow, toCol = snapshot.toCol;

        board[fromRow][fromCol] = movingPiece;
        board[toRow][toCol] = capturedPiece;
        movingPiece.setRow(fromRow);
        movingPiece.setCol(fromCol);

        if (movingPiece instanceof King) updateKingPosition((King) movingPiece, fromRow, fromCol);
        if (capturedPiece != null) {
            List<Piece> opponentPiece = snapshot.whiteTurnBeforeMove ? blackPieces : whitePieces;
            opponentPiece.add(capturedPiece);
        }

        if (snapshot.movedPieceFirstMove) {
            if (movingPiece instanceof Pawn pawn) pawn.setMove(false);
            else if (movingPiece instanceof King king) king.setMove(false);
            else if (movingPiece instanceof Rook rook) rook.setMove(false);
        }

        if (movingPiece instanceof King && Math.abs(toCol - fromCol) == 2) undoCastling(fromRow, fromCol, toRow);
    }

    private void undoCastling(int row, int kingFromCol, int kingToCol) {
        int rookFromCol = kingToCol > kingFromCol ? 7 : 0;
        int rookToCol = kingToCol > kingFromCol ? kingToCol - 1 : kingToCol + 1;
        Piece rook = board[row][rookToCol];
        if (rook instanceof Rook) {
            board[row][rookToCol] = null;
            board[row][rookFromCol] = rook;
            rook.setCol(rookFromCol);
            ((Rook) rook).setMove(false);
            if (rook.isWhite()) whitePieces.add(rook);
            else blackPieces.add(rook);
        }
    }

    private void updateKingPosition(King king, int row, int col) {
        if (king.isWhite()) {
            whiteKingRow = row;
            whiteKingCol = col;
        } else {
            blackKingRow = row;
            blackKingCol = col;
        }
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

    public Piece getPieceAt(int row, int col) { return board[row][col]; }
    public Piece getSelectedPiece() { return selectedPiece; }
    public void setSelectedPiece(Piece piece) { this.selectedPiece = piece; }
    public List<int[]> getValidMoves() { return validMoves; }
    public boolean isWhiteTurn() { return whiteTurn; }
    public void setWhiteTurn(boolean whiteTurn) { this.whiteTurn = whiteTurn; }
    public boolean isAITurn() { return isAIEnabled && !whiteTurn && ai != null; }
    public ChessController getChessController() { return chessController; }
    public boolean isGameEnded() { return chessController != null && chessController.isGameEnded(); }
    public Piece[][] getBoard() { return board; }

    public void draw(AnchorPane boardGame) {
        boardGame.getChildren().clear();
        for (int row = 0; row < 8; row++)
            for (int col = 0; col < 8; col++)
                if (board[row][col] != null)
                    board[row][col].draw(boardGame);

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
}