package main;

import chessPieces.*;

import static utilz.LoadImage.*;
import static utilz.Constants.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Board {
    private final int height = 800;
    private final int width = 800;
    private List<Rook> rooks;
    private List<Knight> knights;
    private List<Bishop> bishops;
    private List<Pawn> pawns;
    private List<King> kings;
    private List<Queen> queens;
    private List<Pieces> pieces;

    private Pieces selectedPiece;

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
        initailizePieces();
    }



    public void update(){


    }

    //Handle the selected pieces when on click
    public void handleSelectedPiece( int x , int y ){
        int col = x / Game.GAME_TILES;
        int row = y / Game.GAME_TILES;

        if( selectedPiece == null ){
            selectedPiece = getPieceAt( col , row);
        }
        else
        {
            movePiece(selectedPiece, col, row);
            selectedPiece = null;
        }
    }
    private void movePiece( Pieces piece, int col, int row)
    {
        piece.setCol(col);
        piece.setRow(row);
    }


    private Pieces getPieceAt(int col, int row) {
        for( Pieces p : pieces)
            if( p.getCol() == col && p.getRow() == row )
                return p;
        return null;
    }

    //Generate the initial position of pieces
    private void initailizePieces(){
        rooks = new ArrayList<>();
        knights = new ArrayList<>();
        bishops = new ArrayList<>();
        queens = new ArrayList<>();
        kings = new ArrayList<>();
        pawns = new ArrayList<>();
        pieces = new ArrayList<>();

        for(int i = 0; i < board.length ; i++){
            for(int j = 0; j < board[i].length ; j++){
                switch ( board[i][j]){
                    case ROOk -> pieces.add( new Rook( j , i  ) );
                    case KNIGHT -> pieces.add( new Knight( j  , i  ) );
                    case BISHOP -> pieces.add( new Bishop( j , i ) );
                    case QUEEN -> pieces.add( new Queen( j  , i ) );
                    case KING -> pieces.add( new King( j , i ) );
                    case PAWN -> pieces.add( new Pawn ( j  , i ) );

                    default -> {}
                }
            }
        }

    }
    public void draw(Graphics g){

        for( Pieces p : pieces)
            p.draw(g);



    }
}
