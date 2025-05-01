package main;

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class MainMenuController {

    @FXML
    private AnchorPane playWithPerson;

    @FXML
    private AnchorPane playWithBot;

    private String player1Name = "Player 1"; // Default name for Player 1
    private String player2Name = "Player 2"; // Default name for Player 2
    private String botName = "Bot"; // Default bot name
    private Game game;

    @FXML
    public void initialize() {
        // Handler for "Play with Person"
        playWithPerson.setOnMouseClicked(event -> {
            startGame(false);
        });

        // Handler for "Play with Bot"
        playWithBot.setOnMouseClicked(event -> {
            startGame(true);
        });
    }

    private void startGame(boolean isPlayingWithBot) {
        if (game == null) {
            throw new IllegalStateException("Game instance is not set in MainMenuController");
        }

        // Get the Stage from the current scene
        Stage stage = (Stage) playWithPerson.getScene().getWindow();
        if (stage == null) {
            throw new IllegalStateException("Cannot retrieve Stage from playWithPerson scene");
        }

        // Get the ChessController to pass player names
        ChessController chessController = game.getChessController();

        // Configure the Board for the game mode
        Board board = chessController.getBoard();
        board.setAIEnabled(isPlayingWithBot);

        // Set player names based on the mode
        if (isPlayingWithBot) {
            chessController.setPlayerNames(player1Name, botName, true);
        } else {
            chessController.setPlayerNames(player1Name, player2Name, false);
        }

        // Switch to the chess scene
        game.switchToChessScene(stage);
    }

    public void setGame(Game game) {
        this.game = game;
    }
}