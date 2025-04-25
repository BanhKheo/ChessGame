package main;

import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.Arrays;
import java.util.List;

public class ChessController {

    @FXML
    private Text player1Name;

    @FXML
    private Text player2Name;

    @FXML
    private Text botName;

    @FXML
    private AnchorPane gameStatusPage;

    @FXML
    private AnchorPane newGamePage;

    @FXML
    private AnchorPane StatusPageOn;

    @FXML
    private AnchorPane StatusPageOff;

    @FXML
    private AnchorPane newGamePageOff;

    @FXML
    private AnchorPane newgamePageOn;

    @FXML
    private Rectangle opt10Mins;

    @FXML
    private Rectangle opt5Mins;

    @FXML
    private Rectangle opt3Mins;

    @FXML
    private AnchorPane playGame;

    @FXML
    private Text blackTime;

    @FXML
    private Text whiteTime;

    @FXML
    private AnchorPane blackTurn;

    @FXML
    private AnchorPane whiteTurn;

    @FXML
    private AnchorPane restart;

    private Board board;

    private int selectedTimeSeconds = 600; // Default to 10 minutes (600 seconds)

    private Timeline blackTimer;
    private Timeline whiteTimer;
    private int blackTimeSeconds;
    private int whiteTimeSeconds;
    private boolean isWhiteTurn = true; // Start with white's turn
    private boolean isFirstMove = true;

    private Game game; // Reference to the Game instance

    @FXML
    private AnchorPane boardGameChess;

    public void setGame(Game game) {
        this.game = game;
    }

    public void setBoard(Board board) {
        this.board = board;
        initializeTabTransitions();
        initializeTimeOptions();
        initializePlayGame();
        initializeBoardInteraction();
        // Redraw the board initially to show the pieces
        board.draw(boardGameChess);
        updateTurnIndicators();
        initializeRestartButton();
    }

    public void redraw() {
        // Redraw the board
        board.draw(boardGameChess);
        // Update timers based on the current turn (synchronized with Board)
        if (board.isWhiteTurn()) {
            blackTimer.pause();
            whiteTimer.play();
        } else {
            whiteTimer.pause();
            blackTimer.play();
        }
        updateTurnIndicators();
    }

    public void setPlayerNames(String player1, String player2, boolean isPlayingWithBot) {
        if (player1Name != null) {
            player1Name.setText(player1);
        }
        if (isPlayingWithBot) {
            player2Name.setVisible(false);
            botName.setVisible(true);
            botName.setText(player2);
        } else {
            player2Name.setVisible(true);
            botName.setVisible(false);
            player2Name.setText(player2);
        }
    }

    public void setTimer(int timeSeconds) {
        this.selectedTimeSeconds = timeSeconds;
        blackTimeSeconds = timeSeconds;
        whiteTimeSeconds = timeSeconds;
        updateTimerDisplay();
        initializeTimers();
    }

    private void initializeTabTransitions() {
        gameStatusPage.setVisible(false);
        newGamePage.setVisible(true);

        StatusPageOn.setOnMouseClicked(event -> {
            if (!gameStatusPage.isVisible()) {
                transitionPages(newGamePage, gameStatusPage);
                updateTabStyles(true);
            } else {
                updateTabStyles(true);
            }
        });

        StatusPageOff.setOnMouseClicked(event -> {
            if (!gameStatusPage.isVisible()) {
                transitionPages(newGamePage, gameStatusPage);
                updateTabStyles(true);
            } else {
                updateTabStyles(true);
            }
        });

        newGamePageOff.setOnMouseClicked(event -> {
            if (!newGamePage.isVisible()) {
                transitionPages(gameStatusPage, newGamePage);
                updateTabStyles(false);
            } else {
                updateTabStyles(false);
            }
        });

        newgamePageOn.setOnMouseClicked(event -> {
            if (!newGamePage.isVisible()) {
                transitionPages(gameStatusPage, newGamePage);
                updateTabStyles(false);
            } else {
                updateTabStyles(false);
            }
        });
    }

    private void transitionPages(AnchorPane fromPage, AnchorPane toPage) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(1), fromPage);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(event -> {
            fromPage.setVisible(false);
            toPage.setVisible(true);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(1), toPage);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();
    }

    private void updateTabStyles(boolean isGameStatusActive) {
        if (isGameStatusActive) {
            StatusPageOn.getStyleClass().removeAll("tab-active", "tab-inactive");
            StatusPageOn.getStyleClass().add("tab-inactive");

            StatusPageOff.getStyleClass().removeAll("tab-active", "tab-inactive");
            StatusPageOff.getStyleClass().add("tab-inactive");

            newGamePageOff.getStyleClass().removeAll("tab-new-game-active", "tab-new-game-inactive");
            newGamePageOff.getStyleClass().add("tab-new-game-active");

            newgamePageOn.getStyleClass().removeAll("tab-new-game-active", "tab-new-game-inactive");
            newgamePageOn.getStyleClass().add("tab-new-game-active");
        } else {
            StatusPageOff.getStyleClass().removeAll("tab-active", "tab-inactive");
            StatusPageOff.getStyleClass().add("tab-active");

            StatusPageOn.getStyleClass().removeAll("tab-active", "tab-inactive");
            StatusPageOn.getStyleClass().add("tab-active");

            newGamePageOff.getStyleClass().removeAll("tab-new-game-active", "tab-new-game-inactive");
            newGamePageOff.getStyleClass().add("tab-new-game-inactive");

            newgamePageOn.getStyleClass().removeAll("tab-new-game-active", "tab-new-game-inactive");
            newgamePageOn.getStyleClass().add("tab-new-game-inactive");
        }
    }

    private void initializeTimeOptions() {
        List<Rectangle> timeOptions = Arrays.asList(opt10Mins, opt5Mins, opt3Mins);
        timeOptions.forEach(option -> option.setOnMouseClicked(event -> handleTimeOptionClick(option, timeOptions)));
    }

    private void handleTimeOptionClick(Rectangle clickedOption, List<Rectangle> timeOptions) {
        timeOptions.forEach(option -> option.getStyleClass().remove("rectangle-selected"));
        clickedOption.getStyleClass().add("rectangle-selected");

        if (clickedOption == opt10Mins) {
            selectedTimeSeconds = 600;
        } else if (clickedOption == opt5Mins) {
            selectedTimeSeconds = 300;
        } else if (clickedOption == opt3Mins) {
            selectedTimeSeconds = 180;
        }
    }

    private void initializePlayGame() {
        playGame.setOnMouseClicked(event -> {
            if (game == null) {
                throw new IllegalStateException("Game instance is not set in ChessController");
            }
            game.switchToMainScene();
        });
    }

    private void initializeBoardInteraction() {
        boardGameChess.setOnMouseClicked(event -> {
            int x = (int) event.getX();
            int y = (int) event.getY();
            boolean wasWhiteTurn = board.isWhiteTurn();
            board.handleSelectedPiece(x, y);
            // Redraw after selecting a piece to show valid moves
            if (board.getSelectedPiece() != null && wasWhiteTurn == board.isWhiteTurn()) {
                redraw();
            }
            // Check if the turn changed (i.e., a valid move was made)
            if (wasWhiteTurn != board.isWhiteTurn()) {
                if (isFirstMove) {
                    // Start the timer for the first move
                    if (board.isWhiteTurn()) {
                        whiteTimer.play();
                    } else {
                        blackTimer.play();
                    }
                    isFirstMove = false;
                }
                redraw();
            }
        });
    }

    private void initializeTimers() {
        blackTimer = new Timeline(new javafx.animation.KeyFrame(Duration.seconds(1), event -> {
            if (blackTimeSeconds > 0) {
                blackTimeSeconds--;
                updateTimerDisplay();
            } else {
                blackTimer.stop();
                // Handle game over (black ran out of time)
            }
        }));
        blackTimer.setCycleCount(Timeline.INDEFINITE);

        whiteTimer = new Timeline(new javafx.animation.KeyFrame(Duration.seconds(1), event -> {
            if (whiteTimeSeconds > 0) {
                whiteTimeSeconds--;
                updateTimerDisplay();
            } else {
                whiteTimer.stop();
                // Handle game over (white ran out of time)
            }
        }));
        whiteTimer.setCycleCount(Timeline.INDEFINITE);
    }

    // Add this method to the existing ChessController.java
    public void resetGame() {
        isFirstMove = true;
        // Stop timers
        if (blackTimer != null) {
            blackTimer.stop();
        }
        if (whiteTimer != null) {
            whiteTimer.stop();
        }

        // Reset timers
        blackTimeSeconds = selectedTimeSeconds;
        whiteTimeSeconds = selectedTimeSeconds;

        // Update timers
        updateTimerDisplay();

        // Reset the board
        board.resetBoard();
        // Redraw the board to reflect the reset state
        board.draw(boardGameChess);
        updateTurnIndicators();
    }

    private void updateTimerDisplay() {
        blackTime.setText(formatTime(blackTimeSeconds));
        whiteTime.setText(formatTime(whiteTimeSeconds));
    }

    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }

    private void updateTurnIndicators() {
        blackTurn.setVisible(!isWhiteTurn);
        whiteTurn.setVisible(isWhiteTurn);
    }

    private void initializeRestartButton() {
        if (restart != null) {
            restart.setOnMouseClicked(event -> resetGame());
        }
    }

    public int getSelectedTimeSeconds() {
        return selectedTimeSeconds;
    }
}