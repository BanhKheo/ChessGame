package chessPieces;

import main.Board;
import main.Game;

import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class Piece {
    protected int col;
    protected int row;
    protected int height;
    protected int width;
    protected BufferedImage img;
    protected boolean isWhite;
    public Piece(int col, int row, BufferedImage img , boolean isWhite){
        this.col = col;
        this.row = row;
        height = 90;
        width = 90;
        this.img = img;
        this.isWhite = isWhite;
    }

    public void draw(Graphics g){
        g.drawImage( img , col * Game.GAME_TILES, row * Game.GAME_TILES, width , height , null);
    }


    public abstract boolean logicMove(int oldRow , int oldCol , int newRow , int newCol);

    public int[] getBlockPieces(Board board, int newRow, int newCol){
        return null;
    }

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public void setRow(int row) {
        this.row = row;
    }


    public boolean isWhite() {
        return isWhite;
    }
}
