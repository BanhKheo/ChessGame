package main;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

public class ChessController {

    @FXML
    private AnchorPane boardGameChess;

    private Board board;

    public void setBoard(Board board) {
        this.board = board;
        initializeMouseEvents();
        redraw(); // draw initially
    }

    private void initializeMouseEvents() {
        boardGameChess.setOnMouseClicked(this::handleMouseClick);
    }


    private void handleMouseClick(MouseEvent event) {
        int mouseX = (int) event.getX();
        int mouseY = (int) event.getY();
        board.handleSelectedPiece(mouseX, mouseY); // for actual move
    }

    public void redraw() {
        board.draw(boardGameChess);
    }
}
