package main;

import chessPieces.*;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import static utilz.Constants.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

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
            if (clickedPiece != null && clickedPiece.isWhite() == whiteTurn) {
                selectedPiece = clickedPiece;
                validMoves = getValidMoves(selectedPiece);
            }
        } else {
            //Move piece
            movePiece(selectedPiece, col, row);
            selectedPiece = null;
        }
    }



    private void movePiece(Piece piece, int col, int row)
    {
        if( legalMove(piece , col , row)){
            int oldCol = piece.getCol();
            int oldRow = piece.getRow();

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

            whiteTurn = !whiteTurn;
        }
    }


    private boolean legalMove(Piece piece, int col, int row) {

        //Handle piece satisfy logic move
        if (!piece.logicMove(piece.getRow(), piece.getCol(), row, col, board)) {
            return false;
        }


        // Check if there are blocking pieces (for pieces that move in straight lines)
//        if (piece.getBlockPieces(this, row, col) != null) {
//            return false;  // A piece is blocking the way
//        }

//        int[] blockedPos = piece.getBlockPieces(this, row, col);
//        if (blockedPos != null) {
//            // Check if any of the specified squares are occupied
//            for (int i = 1; i < blockedPos.length; i++) {
//                int c = blockedPos[i];
//                if (board[blockedPos[0]][c] != null) {
//                    return false; // A piece is blocking the way
//                }
//            }
//        }

        if (isBlocked(piece, row, col)) return false;

        //prevent capturing own piece
        Piece targetPiece = board[row][col];
        if (targetPiece != null && targetPiece.isWhite() == piece.isWhite()) {
            return false;
        }
        return true;
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
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (piece instanceof Pawn pawn) {
                    boolean wasMoved = pawn.isMoved();
                    if ( legalMove(piece, col, row) && !isBlocked(piece, row, col)) {
                        moves.add(new int[] {col, row});
                    }
                    pawn.setMove(wasMoved);
                }

                else if(piece instanceof King king) {
                    boolean wasMoved = king.isMoved();
                    if (legalMove(piece, col, row) && !isBlocked(piece, row, col)) {
                        moves.add(new int[] {col, row});
                    }
                    king.setMove(wasMoved);
                }

                else {
                    if (legalMove(piece, col, row) && !isBlocked(piece, row, col)) {
                        moves.add(new int[]{col, row}); // Fix order
                    }
                }
            }
        }
        return moves;
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
        if (selectedPiece != null) {
            for (int[] move : validMoves) {
                if (!isBlocked(selectedPiece, move[1], move[0])) {
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
