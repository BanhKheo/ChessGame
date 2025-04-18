package main;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;

public class MainMenuController {

    @FXML
    private AnchorPane playWithPerson;
    @FXML
    private AnchorPane playWithBot;

    private Game game;

    public void setGame(Game game) {
        this.game = game;
    }

    @FXML
    public void initialize()  {
        playWithPerson.setOnMouseClicked(e -> {game.switchToChessScene();});
        playWithBot.setOnMouseClicked(e -> {game.switchToChessScene();});
    }
}
