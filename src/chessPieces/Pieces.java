package chessPieces;

import main.Game;

import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class Pieces {
    protected int col;
    protected int row;
    protected int height;
    protected int width;
    protected BufferedImage img;
    public Pieces(int col, int row, BufferedImage img){
        this.col = col;
        this.row = row;
        height = 100;
        width = 100;
        this.img = img;
    }

    public void draw(Graphics g){
        g.drawImage( img , col * Game.GAME_TILES, row * Game.GAME_TILES, width , height , null);
    }


    public abstract void update();

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
}
