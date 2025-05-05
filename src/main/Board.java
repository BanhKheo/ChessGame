package main;

import chessPieces.*;
import javafx.application.Platform;
import javafx.scene.layout.AnchorPane;
import utilz.MoveSnapshot;
import ai.ChessAI;
import static utilz.Constants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Board {

    private Piece[][] board = new Piece[8][8];
    private List<int[]> validMoves = new ArrayList<>();
    private final List<Piece> whitePieces = new ArrayList<>();
    private final List<Piece> blackPieces = new ArrayList<>();
    private int whiteKingRow = -1, whiteKingCol = -1;
    private int blackKingRow = -1, blackKingCol = -1;
    private Piece selectedPiece;
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

        if (col < 0 || col >= 8 ||  row < 0 || row >= 8) {
            return;
        }

        Piece clickedPiece = board[row][col];

        if (selectedPiece == null) {
            // Select a piece if it's the player's turn and the piece belongs to them
            if (clickedPiece != null && clickedPiece.isWhite() == whiteTurn) {
                selectedPiece = clickedPiece;
                validMoves = getValidMoves(selectedPiece);
                chessController.redraw();
            }
        }

        else {
            if (clickedPiece == selectedPiece) {
                // Deselect the piece if clicked again
                selectedPiece = null;
                validMoves.clear();
                chessController.redraw();
            } else if (clickedPiece != null && clickedPiece.isWhite() == whiteTurn) {
                // Select a new piece if it's the player's piece
                selectedPiece = clickedPiece;
                validMoves = getValidMoves(selectedPiece);
                chessController.redraw();
            } else {
                // Attempt to move to the clicked square if it's a valid move
                for (int[] move : validMoves) {
                    if (move[0] == col && move[1] == row) {
                        movePiece(selectedPiece, col, row);
                        selectedPiece = null;
                        validMoves.clear();
                        chessController.redraw();
                        break;
                    }
                }
                // If not a valid move, keep the current selection (don't deselect)
            }
        }
    }



    private void movePiece(Piece piece, int col, int row) {
        synchronized (this) {
            if (!legalMove(piece, col, row) || !isPlayerMoving) return;

            isPlayerMoving = false;

            int oldRow = piece.getRow();
            int oldCol = piece.getCol();

            // Record first-move status
            boolean wasFirstMove = recordFirstMoveStatus(piece);
            MoveSnapshot snapshot = new MoveSnapshot(piece, board[row][col], oldRow, oldCol, row, col, whiteTurn, wasFirstMove);
            chessController.addMoveSnapshot(snapshot);

            board[oldRow][oldCol] = null;
            piece.setRow(row);
            piece.setCol(col);
            board[row][col] = piece;

            // Update move status and special move
            updateMoveStatus(piece);
            if (piece instanceof King king) {
                handleCastling(king, oldCol, row, col);
                updateKingPosition(king, row, col);
                board[row][col] = piece;
            }


            // Check for checkmate
            boolean opponentInCheckmate = isCheckmate(!whiteTurn);
            if (opponentInCheckmate) {
                if (chessController != null) {
                    chessController.handleCheckmate(whiteTurn);
                }
            }

            // Switch turn
            whiteTurn = !whiteTurn;
            chessController.redraw();

            // Trigger AI move
            if (isAITurn()) {
                performAIMove();
            }
            else {
                isPlayerMoving = true; // Allow next player move if not AI's turn
            }
        }
    }



    private boolean recordFirstMoveStatus(Piece piece) {
        if (piece instanceof Pawn pawn) { return !pawn.isMoved(); }
        if (piece instanceof King king) { return !king.isMoved(); }
        if (piece instanceof Rook rook) { return !rook.isMoved(); }
        return false;
    }



    private void updateMoveStatus(Piece piece) {
        if (piece instanceof Pawn pawn) pawn.setMove(true);
        else if (piece instanceof King king) king.setMove(true);
        else if (piece instanceof Rook rook) rook.setMove(true);
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
                    if (isCheckmate(true)) {
                        chessController.handleCheckmate(false); // AI vừa thắng
                        return;
                    }
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
            List<Piece> pieces = rook.isWhite() ? whitePieces : blackPieces;
            pieces.remove(rook);
            pieces.add(rook);
        }
    }



    public void resetBoard() {
        board = new Piece[8][8];
        whiteTurn = true;
        selectedPiece = null;
        validMoves.clear();
        whiteKingRow = whiteKingCol = blackKingRow = blackKingCol = -1;
        initializePieces();
        if (isAIEnabled) {
            ai = new ChessAI(false);
        } else {
            ai = null;
        }
        chessController.redraw();
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



    // Check if game is ended
    public boolean isCheckmate(boolean white) {
        if (!isKingInCheck(white)) return false; // Check King is attacking by enemies

        // Find which pieces can protect king
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
            if (kingRow != -1) break;
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

        whitePieces.clear();
        blackPieces.clear();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                boolean isWhite = row >= 6; // White pieces on rows 6 and 7
                int type = initialBoard[row][col];

                Piece piece = switch (type) {
                    case ROOK -> board[row][col] = new Rook(col, row, isWhite);
                    case KNIGHT -> board[row][col] = new Knight(col, row, isWhite);
                    case BISHOP -> board[row][col] = new Bishop(col, row, isWhite);
                    case QUEEN -> board[row][col] = new Queen(col, row, isWhite);
                    case KING -> board[row][col] = new King(col, row, isWhite);
                    case PAWN -> board[row][col] = new Pawn(col, row, isWhite);
                    default -> board[row][col] = null;
                };

                board[row][col] = piece;
                if (piece != null) {
                    if (isWhite) whitePieces.add(piece);
                    else blackPieces.add(piece);

                    if (piece instanceof King) updateKingPosition((King) piece, row, col);
                }
            }
        }
    }



    private List<int[]> getValidMoves(Piece piece) {
        List<int[]> moves = new ArrayList<>();
        boolean wasMoved = false;
        if (piece instanceof Pawn pawn) {
            wasMoved = pawn.isMoved();
        }

        else if (piece instanceof King king) {
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
        }

        else if (piece instanceof King king) {
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

    public MoveSnapshot simulateMove(Piece piece, int toRow, int toCol) {
        int fromRow = piece.getRow();
        int fromCol = piece.getCol();
        Piece captured = board[toRow][toCol];

        // Save current state
        MoveSnapshot snapshot = new MoveSnapshot(piece, captured, fromRow, fromCol, toRow, toCol, false, false);

        // Perform simulated move
        board[toRow][toCol] = piece;
        board[fromRow][fromCol] = null;
        piece.setRow(toRow);
        piece.setCol(toCol);

        return snapshot;
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



    public void undoMove(MoveSnapshot snapshot) {
        if (snapshot == null || snapshot.movedPiece == null) {
            System.out.println("Invalid snapshot or moved piece");
            return;
        }

        Piece movingPiece = snapshot.movedPiece;
        Piece capturedPiece = snapshot.capturedPiece;
        int fromRow = snapshot.fromRow;
        int fromCol = snapshot.fromCol;
        int toRow = snapshot.toRow;
        int toCol = snapshot.toCol;

        // Restore board state
        board[fromRow][fromCol] = movingPiece;
        board[toRow][toCol] = capturedPiece;
        movingPiece.setRow(fromRow);
        movingPiece.setCol(fromCol);

        // Restore King position if moved
        if (movingPiece instanceof King) {
            updateKingPosition((King) movingPiece, fromRow, fromCol);
        }

        // Restore piece List for captured piece
        if (capturedPiece != null) {
            List<Piece> opponentPiece = snapshot.whiteTurnBeforeMove ? blackPieces : whitePieces;
            opponentPiece.add(capturedPiece);
        }

        // Restore first-move status
        if (snapshot.movedPieceFirstMove) {
            switch (movingPiece) {
                case Pawn pawn -> pawn.setMove(false);
                case King king -> king.setMove(false);
                case Rook rook -> rook.setMove(false);
                default -> {
                }
            }
        }

        // Handle special move
        if (movingPiece instanceof King && Math.abs(toCol - fromCol) == 2) {
            undoCastling(fromRow, fromCol, toRow);
        }
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
        }

        else {
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

    public void setWhiteTurn(boolean whiteTurn) {
        this.whiteTurn = whiteTurn;
    }

    public boolean isAITurn() {
        return isAIEnabled && !whiteTurn && ai != null;
    }

    public ChessController getChessController() {
        return chessController;
    }

    public boolean isGameEnded(){
        return chessController.isGameEnded();
    }

    public Piece[][] getBoard() {
        return board;
    }
}