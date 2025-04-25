package main;

import chessPieces.*;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import static utilz.Constants.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

public class Board {


    private List<int[]> validMoves = new ArrayList<>();

    private Piece selectedPiece;


    private Piece[][] board = new Piece[8][8];

    private boolean whiteTurn = true;

    Board(){
        initializePieces();
    }




    //Handle the selected pieces when on click
    public void handleSelectedPiece(int x, int y) {
        int col = x / Game.GAME_TILES;
        int row = y / Game.GAME_TILES;

        if (col < 0 || col >= 8 || row < 0 || row >= 8) {
            return;
        }

        Piece clickedPiece = board[row][col];

        if (selectedPiece == null) {
            //Check whether accuracy turn
            if (clickedPiece != null   && clickedPiece.isWhite() == whiteTurn) {
                selectedPiece = clickedPiece;
                validMoves = getValidMoves(selectedPiece);
            }
        } else {
            //Move piece
            movePiece(selectedPiece, col, row);
            selectedPiece = null;
            validMoves.clear();
        }
    }



    private void movePiece(Piece piece, int col, int row)
    {
        if( legalMove(piece , col , row)){
            int oldCol = piece.getCol();

            board[piece.getRow()][piece.getCol()] = null;

            if (piece instanceof Pawn pawn) {
                pawn.setMove(true);
            }

            if (piece instanceof King king) {
                king.setMove(true);
                if (col == oldCol + 2) {
                    Piece rook = board[row][7];
                    if (rook instanceof Rook) {
                        board[row][7] = null;
                        board[row][col - 1] = rook;
                        rook.setCol(col - 1);
                        ((Rook) rook).setMove(true);
                    }
                }

                if (col == oldCol - 2) {
                    Piece rook = board[row][0];
                    if (rook instanceof Rook) {
                        board[row][0] = null;
                        board[row][col + 1] = rook;
                        rook.setCol(col + 1);
                        ((Rook) rook).setMove(true);
                    }
                }
            }

            if (piece instanceof Rook rook) {
                rook.setMove(true);
            }

            piece.setRow(row);
            piece.setCol(col);
            board[row][col] = piece;

            boolean opponentInCheckmate = isCheckmate(!whiteTurn);
            whiteTurn = !whiteTurn;

            if(opponentInCheckmate){
                if(whiteTurn){
                    System.out.println("white is lose");
                }
                else {
                    System.out.println("black is lose");
                }
            }
        }
    }

    public void resetBoard() {
        // Clear the current board state
        board = new Piece[8][8];
        whiteTurn = true;
        selectedPiece = null;
        validMoves.clear();
        // Reinitialize pieces
        initializePieces();
    }
    private boolean legalMove(Piece piece, int col, int row) {

        //Handle piece satisfy logic move
        if (!piece.logicMove(piece.getRow(), piece.getCol(), row, col, board)) {
            return false;
        }

        if (isBlocked(piece, row, col)) return false;

        //prevent capturing own piece
        Piece targetPiece = board[row][col];
        if (targetPiece != null && targetPiece.isWhite() == piece.isWhite()) {
            return false;
        }
        return true;
    }


    public boolean isCheckmate(boolean white) {
        //The king is not mated
        if (!isKingInCheck(white)) return false;

        //The king is mated. Must move king or piece to avoid checkmate
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

        //find the king
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


        //Any piece attack the king
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


    //Generate the initial position of pieces
    private void initializePieces() {

        int [][] initialBoard = {
                {1, 2, 3, 4, 5, 3, 2, 1},  // Black pieces
                {6, 6, 6, 6, 6, 6, 6, 6},  // Black pawns
                {0, 0, 0, 0, 0, 0, 0, 0},  // Empty
                {0, 0, 0, 0, 0, 0, 0, 0},  // Empty
                {0, 0, 0, 0, 0, 0, 0, 0},  // Empty
                {0, 0, 0, 0, 0, 0, 0, 0},  // Empty
                {6, 6, 6, 6, 6, 6, 6, 6},  // White pawns
                {1, 2, 3, 4, 5, 3, 2, 1}   // White pieces

        };

        for (int row = 0; row < 8; row++) {
            boolean isWhite = (row >= 6); // White pieces are on rows 6 and 7 (ranks 2 and 1)
            for (int col = 0; col < 8; col++) {
                switch (initialBoard[row][col]) {
                    case ROOK -> board[row][col] = new Rook( col , row , isWhite);
                    case KNIGHT -> board[row][col] = new Knight( col , row , isWhite);
                    case BISHOP -> board[row][col] = new Bishop( col , row , isWhite);
                    case QUEEN -> board[row][col] = new Queen( col , row , isWhite);
                    case KING -> board[row][col] = new King( col , row , isWhite);
                    case PAWN -> board[row][col] = new Pawn( col , row , isWhite);
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

            // Handle castling separately
            if (!king.isMoved() && !isKingInCheck(king.isWhite())) {
                int row = king.getRow();
                int col = king.getCol();

                // King-side castling
                if (canCastle(king, true)) {
                    moves.add(new int[]{col + 2, row});
                }

                // Queen-side castling
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

            // Check path is clear
            for (int c = col + 1; c < 7; c++) {
                if (board[row][c] != null) return false;
            }

            // Ensure no check on the path
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
//        return blockedPos != null;  // If there's a blocking piece, return true
    }

    public Piece getPieceAt( int row , int col){
        return board[row][col];
    }
    public void draw(AnchorPane boardGame) {

        boardGame.getChildren().clear();
        // Draw pieces using JavaFX ImageViews
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece p = board[row][col];
                if (p != null) {
                    p.draw(boardGame); // Use your existing JavaFX draw method
                }
            }
        }

        // Draw legal move indicators as transparent circles
        if (selectedPiece != null && !validMoves.isEmpty()) {
            for (int[] move : validMoves) {
                javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle();
                circle.setRadius(16);
                circle.setCenterX(move[0] * Game.GAME_TILES + Game.GAME_TILES / 2);
                circle.setCenterY(move[1] * Game.GAME_TILES + Game.GAME_TILES / 2);
                circle.setFill(javafx.scene.paint.Color.rgb(169, 169, 169, 0.6));
                boardGame.getChildren().add(circle);
                // Debug: Print circle position
                System.out.println("Drawing circle at: (" + circle.getCenterX() + ", " + circle.getCenterY() + ")");
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


}
