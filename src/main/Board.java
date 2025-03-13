package main;

import chessPieces.*;

import static utilz.LoadImage.*;
import static utilz.Constants.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Board {
    private List<Piece> pieces;

    private List<int[]> validMoves = new ArrayList<>();

    private Piece selectedPiece;

    BufferedImage img;
    private int[][] board = {
            {1, 2, 3, 4, 5, 3, 2, 1},  // Black pieces
            {6, 6, 6, 6, 6, 6, 6, 6},  // Black pawns
            {0, 0, 0, 0, 0, 0, 0, 0},  // Empty
            {0, 0, 0, 0, 0, 0, 0, 0},  // Empty
            {0, 0, 0, 0, 0, 0, 0, 0},  // Empty
            {0, 0, 0, 0, 0, 0, 0, 0},  // Empty
            {6, 6, 6, 6, 6, 6, 6, 6},  // White pawns
            {1, 2, 3, 4, 5, 3, 2, 1}   // White pieces

    };


    Board(){
        img = GetAtlas(boardBackground);
        initializePieces();
    }



    public void update(){


    }

    //Handle the selected pieces when on click
    public void handleSelectedPiece( int x , int y ){
        int col = x / Game.GAME_TILES;
        int row = y / Game.GAME_TILES;
        if( selectedPiece == null ){
            selectedPiece = getPieceAt( col , row);
            if (selectedPiece != null) {
                validMoves = getValidMoves(selectedPiece);
            }
            System.out.println(selectedPiece);
        }
        else
        {
            movePiece(selectedPiece, col, row);
            selectedPiece = null;
        }
    }
    private void movePiece(Piece piece, int col, int row)
    {
        if( legalMove(piece , col , row)){
            board[piece.getRow()][piece.getCol()] = 0;
            piece.setCol(col);
            piece.setRow(row);
            board[row][col] = getPiecesState(piece);

            for(int i = 0 ; i < 8 ; i++){
                for ( int j = 0 ; j < 8 ; j++){
                    System.out.print(board[i][j] + " ");
                }
                System.out.println();
            }


        }
    }
    private List<int[]> getValidMoves(Piece piece) {
        List<int[]> moves = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (legalMove(piece, j, i)) {
                    moves.add(new int[]{j, i});
                }
            }
        }
        return moves;
    }


    private boolean legalMove( Piece piece , int col , int row ){
        for(int i = 0 ; i < 8 ; i++)
            for ( int j = 0 ; j < 8 ; j++){
                if( board[row][col] == 0 && piece.logicMove(piece.getRow() , piece.getCol() , row , col))
                    return true;
            }
        return false;
    }


    private Piece getPieceAt(int col, int row) {
        for( Piece p : pieces)
            if( p.getCol() == col && p.getRow() == row )
                return p;
        return null;
    }

    //Generate the initial position of pieces
    private void initializePieces() {
        pieces = new ArrayList<>();
        for (int i = 0; i < board.length; i++) {
            boolean isWhite = (i >= 6); // White pieces are on rows 6 and 7 (ranks 2 and 1)
            for (int j = 0; j < board[i].length; j++) {
                switch (board[i][j]) {
                    case ROOK -> pieces.add(new Rook(j, i, isWhite));
                    case KNIGHT -> pieces.add(new Knight(j, i, isWhite));
                    case BISHOP -> pieces.add(new Bishop(j, i, isWhite));
                    case QUEEN -> pieces.add(new Queen(j, i, isWhite));
                    case KING -> pieces.add(new King(j, i, isWhite));
                    case PAWN -> pieces.add(new Pawn(j, i, isWhite));
                    default -> {}
                }
            }
        }
    }
    public void draw(Graphics g){

        for( Piece p : pieces)
            p.draw(g);

        // Draw valid move indicators
        if (selectedPiece != null) {
            g.setColor(new Color(169, 169, 169, 150));

            // Adjusted greenish-gray color with transparency
            for (int[] move : validMoves) {
                int x = move[0] * Game.GAME_TILES;
                int y = move[1] * Game.GAME_TILES;
                g.fillOval(x + 28, y + 28, 32, 32); // Draw circle at valid move positions
            }
        }

    }
}
