package chessPieces;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import main.Board;
import main.Game;

import java.awt.image.BufferedImage;


public abstract class Piece {
    protected int col;
    protected int row;
    protected boolean isWhite;

    protected int height;
    protected int width;

    protected Image img;

    public Piece(int col, int row, Image img , boolean isWhite){
        this.col = col;
        this.row = row;
        height = 80;
        width = 80;
        this.img = img;
        this.isWhite = isWhite;
    }

    public void draw( AnchorPane pane){
        ImageView imageView = new ImageView(img);

        // Set position and size
        imageView.setX(col * Game.GAME_TILES);
        imageView.setY(row * Game.GAME_TILES);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);

        imageView.setSmooth(false); // This disables smoothing (blurring)
        imageView.setPreserveRatio(true); // Optional, keeps aspect ratio


        // Add to scene or pane
        pane.getChildren().add(imageView);

    }


    public abstract boolean logicMove(int oldRow , int oldCol , int newRow , int newCol, Piece[][] board);

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
