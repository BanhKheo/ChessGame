package main;

import chessPieces.*;
import javafx.application.Platform;
import javafx.scene.layout.AnchorPane;
import utilz.MoveSnapshot;
import ai.ChessAI;
import static utilz.Constants.*;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    private Board(boolean skipInit) {}

    // --- SETTERS / GETTERS ---

    public void setChessController(ChessController controller) {
        this.chessController = controller;
    }

    public void setAIEnabled(boolean enabled) {
        this.isAIEnabled = enabled;
        this.ai = enabled ? new ChessAI(false) : null;
    }

    // --- UI AND MOVE HANDLING ---

    public void handleSelectedPiece(int x, int y) {
        if (isAITurn()) return;
        int col = x / Game.GAME_TILES, row = y / Game.GAME_TILES;
        if (!isOnBoard(row, col)) return;
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

    private boolean isOnBoard(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
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
        return validMoves.stream().anyMatch(move -> move[0] == col && move[1] == row);
    }

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
        if (piece instanceof Pawn) checkPawnPromotion((Pawn) piece);

        if (isCheckmate(!whiteTurn) && chessController != null) chessController.handleCheckmate(whiteTurn);

        whiteTurn = !whiteTurn;
        if (chessController != null) chessController.redraw();

        if (isAITurn()) performAIMove();
        else isPlayerMoving = true;
    }

    private void checkPawnPromotion(Pawn pawn) {
        int row = pawn.getRow();
        boolean isWhite = pawn.isWhite();
        if ((isWhite && row == 0) || (!isWhite && row == 7)) {
            if (chessController != null) {
                chessController.showPromotionDialog(pawn);
            } else {
                Piece promotedPiece = new Queen(pawn.getCol(), row, isWhite);
                board[row][pawn.getCol()] = promotedPiece;
            }
        }
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

    // --- AI MOVE LOGIC (OPTIMIZED) ---

    private void performAIMove() {
        if (chessController != null) chessController.showBotThinking(true);
        aiExecutor.submit(() -> {
            try {
                Board aiBoard = this.deepCopy();
                ChessAI.Move bestMove = ai.getBestMove(aiBoard, 3);
                Platform.runLater(() -> {
                    if (bestMove != null) {
                        // Validate move on current board state, not on aiBoard
                        if (!isOnBoard(bestMove.fromRow, bestMove.fromCol) || !isOnBoard(bestMove.toRow, bestMove.toCol)) {
                            if (chessController != null) chessController.showBotThinking(false);
                            return;
                        }
                        Piece aiPiece = getPieceAt(bestMove.fromRow, bestMove.fromCol);
                        if (aiPiece != null && !aiPiece.isWhite()) { // Black AI only
                            makeMove(aiPiece, bestMove.fromRow, bestMove.fromCol, bestMove.toRow, bestMove.toCol);
                            whiteTurn = true;
                            isPlayerMoving = true;
                            if (chessController != null) {
                                chessController.redraw();
                                if (isCheckmate(true)) chessController.handleCheckmate(whiteTurn);
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

    // --- CASTLING ---

    private void handleCastling(King king, int oldCol, int row, int col) {
        if (col == oldCol + 2) moveRookForCastling(row, 7, col - 1);
        else if (col == oldCol - 2) moveRookForCastling(row, 0, col + 1);
    }

    private void moveRookForCastling(int row, int rookCol, int targetCol) {
        if (!isOnBoard(row, rookCol) || !isOnBoard(row, targetCol)) return;
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

    // --- PIECE INITIALIZATION ---

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

    // --- MOVE VALIDATION ---



    private boolean legalMove(Piece piece, int col, int row) {
        if (!isOnBoard(row, col)) return false;
        if (!piece.logicMove(piece.getRow(), piece.getCol(), row, col, board)) return false;
        if (isBlocked(piece, row, col)) return false;
        Piece targetPiece = board[row][col];
        return targetPiece == null || targetPiece.isWhite() != piece.isWhite();
    }

    private boolean isBlocked(Piece piece, int newRow, int newCol) {
        if (!isOnBoard(newRow, newCol)) return true;
        int[] blockedPos = piece.getBlockPieces(board, newRow, newCol);
        if (blockedPos != null) {
            for (int i = 1; i < blockedPos.length; i++) {
                if (!isOnBoard(blockedPos[0], blockedPos[i])) continue;
                if (board[blockedPos[0]][blockedPos[i]] != null) return true;
            }
            return false;
        }
        return false;
    }

    private List<int[]> getValidMoves(Piece piece) {
        List<int[]> moves = new ArrayList<>();
        int originalRow = piece.getRow(), originalCol = piece.getCol();

        boolean wasMoved = false;
        if (piece instanceof Pawn pawn) wasMoved = pawn.isMoved();
        else if (piece instanceof King king) wasMoved = king.isMoved();
        else if (piece instanceof Rook rook) wasMoved = rook.isMoved();

        if (piece instanceof King king && !king.isMoved() && !isKingInCheck(king.isWhite())) {
            int row = king.getRow(), col = king.getCol();
            if (canCastle(king, true)) moves.add(new int[]{col + 2, row});
            if (canCastle(king, false)) moves.add(new int[]{col - 2, row});
        }

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (!legalMove(piece, col, row) || isBlocked(piece, row, col)) continue;

                Piece originalTarget = board[row][col];
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
        else if (piece instanceof Rook rook) rook.setMove(wasMoved);

        return moves;
    }

    private boolean canCastle(King king, boolean kingSide) {
        if (king.isMoved() || isKingInCheck(king.isWhite())) return false;
        int row = king.getRow(), col = king.getCol();
        int rookCol = kingSide ? 7 : 0;
        int kingTargetCol = kingSide ? 6 : 2;

        Piece rook = isOnBoard(row, rookCol) ? board[row][rookCol] : null;
        if (!(rook instanceof Rook) || ((Rook) rook).isMoved()) return false;
        int step = kingSide ? 1 : -1;
        for (int c = col + step; kingSide ? c < rookCol : c > rookCol; c += step) {
            if (board[row][c] != null) return false;
        }
        for (int c = col; kingSide ? c <= kingTargetCol : c >= kingTargetCol; c += step) {
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
        if (!isOnBoard(kingRow, kingCol)) return false;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece enemy = board[r][c];
                if (enemy != null && enemy.isWhite() != isWhitePlayer) {
                    if (enemy.logicMove(enemy.getRow(), enemy.getCol(), kingRow, kingCol, board)
                            && enemy.getBlockPieces(board, kingRow, kingCol) == null) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // --- MOVE SIMULATION FOR AI ---

    public MoveSnapshot simulateMove(Piece piece, int toRow, int toCol) {
        int fromRow = piece.getRow(), fromCol = piece.getCol();
        if (!isOnBoard(fromRow, fromCol) || !isOnBoard(toRow, toCol)) return null;
        boolean movedPieceFirstMove = false;
        if (piece instanceof Pawn pawn) movedPieceFirstMove = !pawn.isMoved();
        if (piece instanceof King king) movedPieceFirstMove = !king.isMoved();
        if (piece instanceof Rook rook) movedPieceFirstMove = !rook.isMoved();

        Piece captured = board[toRow][toCol];
        boolean whiteTurnBeforeMove = isWhiteTurn();

        int castlingRookFromCol = -1, castlingRookToCol = -1;
        if (piece instanceof King && Math.abs(toCol - fromCol) == 2) {
            int rookRow = fromRow;
            if (toCol == 6) {
                castlingRookFromCol = 7;
                castlingRookToCol = 5;
                Piece rook = board[rookRow][7];
                board[rookRow][5] = rook;
                board[rookRow][7] = null;
                if (rook != null) rook.setCol(5);
            } else if (toCol == 2) {
                castlingRookFromCol = 0;
                castlingRookToCol = 3;
                Piece rook = board[rookRow][0];
                board[rookRow][3] = rook;
                board[rookRow][0] = null;
                if (rook != null) rook.setCol(3);
            }
        }
        board[toRow][toCol] = piece;
        board[fromRow][fromCol] = null;
        piece.setRow(toRow);
        piece.setCol(toCol);

        if (captured != null) {
            List<Piece> opponentPieces = piece.isWhite() ? blackPieces : whitePieces;
            opponentPieces.remove(captured);
        }

        return new MoveSnapshot(
                piece, captured,
                fromRow, fromCol, toRow, toCol,
                whiteTurnBeforeMove, movedPieceFirstMove,
                false,
                castlingRookFromCol, castlingRookToCol
        );
    }

    public void undoMove(MoveSnapshot snapshot) {
        if (snapshot == null || snapshot.movedPiece == null) return;
        int fromRow = snapshot.fromRow, fromCol = snapshot.fromCol;
        int toRow = snapshot.toRow, toCol = snapshot.toCol;

        if (!isOnBoard(fromRow, fromCol) || !isOnBoard(toRow, toCol)) return;

        Piece movingPiece = snapshot.movedPiece, capturedPiece = snapshot.capturedPiece;

        if (snapshot.isCastlingMove()) {
            int rookRow = fromRow;
            Piece rook = board[rookRow][snapshot.castlingRookToCol];
            board[rookRow][snapshot.castlingRookFromCol] = rook;
            if (rook != null) rook.setCol(snapshot.castlingRookFromCol);
            board[rookRow][snapshot.castlingRookToCol] = null;
        }

        board[fromRow][fromCol] = movingPiece;
        board[toRow][toCol] = capturedPiece;
        movingPiece.setRow(fromRow);
        movingPiece.setCol(fromCol);

        if (snapshot.movedPieceFirstMove) {
            if (movingPiece instanceof Pawn pawn) pawn.setMove(false);
            if (movingPiece instanceof King king) king.setMove(false);
            if (movingPiece instanceof Rook rook) rook.setMove(false);
        }

        if (capturedPiece != null) {
            List<Piece> opponentPieces = movingPiece.isWhite() ? blackPieces : whitePieces;
            opponentPieces.add(capturedPiece);
        }
    }

    private void updateKingPosition(King king, int row, int col) {
        if (!isOnBoard(row, col)) return;
        if (king.isWhite()) {
            whiteKingRow = row;
            whiteKingCol = col;
        } else {
            blackKingRow = row;
            blackKingCol = col;
        }
    }

    private boolean isSquareUnderAttack(int row, int col, boolean byWhite) {
        if (!isOnBoard(row, col)) return false;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece piece = board[r][c];
                if (piece != null && piece.isWhite() == byWhite) {
                    if (legalMove(piece, row, col)) return true;
                }
            }
        }
        return false;
    }

    // --- GETTERS FOR UI/AI ---

    public Piece getPieceAt(int row, int col) {
        return isOnBoard(row, col) ? board[row][col] : null;
    }
    public Piece getSelectedPiece() { return selectedPiece; }
    public void setSelectedPiece(Piece piece) { this.selectedPiece = piece; }
    public List<int[]> getValidMoves() { return validMoves; }
    public boolean isWhiteTurn() { return whiteTurn; }
    public void setWhiteTurn(boolean whiteTurn) { this.whiteTurn = whiteTurn; }
    public boolean isAITurn() { return isAIEnabled && !whiteTurn && ai != null; }
    public ChessController getChessController() { return chessController; }
    public boolean isGameEnded() { return chessController != null && chessController.isGameEnded(); }
    public Piece[][] getBoard() { return board; }

    // --- DEEP COPY ---

    public Board deepCopy() {
        Board copy = new Board(true);
        // Copy pieces
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = this.board[r][c];
                if (p != null) {
                    Piece pCopy = p.copy();
                    copy.board[r][c] = pCopy;
                    if (p.isWhite()) copy.whitePieces.add(pCopy);
                    else copy.blackPieces.add(pCopy);
                    if (pCopy instanceof King) copy.updateKingPosition((King) pCopy, r, c);
                }
            }
        }
        copy.whiteTurn = this.whiteTurn;
        copy.isPlayerMoving = this.isPlayerMoving;
        copy.selectedPiece = null;
        copy.validMoves = Collections.emptyList();
        copy.isAIEnabled = false;
        return copy;
    }

    // --- DRAW UI ---

    public void draw(AnchorPane boardGame) {
        boardGame.getChildren().clear();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board[row][col];
                if (piece != null) piece.draw(boardGame);
            }
        }
        if (selectedPiece != null && !validMoves.isEmpty()) {
            for (int[] move : validMoves) {
                if (move[0] < 0 || move[0] >= 8 || move[1] < 0 || move[1] >= 8) continue;
                javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(16);
                circle.setCenterX(move[0] * Game.GAME_TILES + Game.GAME_TILES / 2);
                circle.setCenterY(move[1] * Game.GAME_TILES + Game.GAME_TILES / 2);
                circle.setFill(javafx.scene.paint.Color.rgb(169, 169, 169, 0.6));
                boardGame.getChildren().add(circle);
            }
        }
    }
}