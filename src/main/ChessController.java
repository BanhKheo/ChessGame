package main;

import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.scene.input.MouseEvent;
import javafx.scene.Scene;
import javafx.scene.Node;
import utilz.MoveSnapshot;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class ChessController {

    @FXML
    private Text player1Name, player2Name, botName;

    @FXML
    private AnchorPane gameStatusPage, newGamePage;

    @FXML
    private AnchorPane StatusPageOn, StatusPageOff, newGamePageOff, newgamePageOn;

    @FXML
    private Rectangle opt10Mins, opt5Mins, opt3Mins;

    @FXML
    private AnchorPane playGame, surrender;

    @FXML
    private Text blackTime, whiteTime;

    @FXML
    private AnchorPane blackTurn, whiteTurn;

    @FXML
    private Text blackTextTurn, whiteTextTurn;

    @FXML
    private AnchorPane notificationWinPage;

    @FXML
    private AnchorPane undo, restart;

    @FXML
    private Text winner1, winner2, botWin;

    @FXML
    private AnchorPane blackPieceWin, blackPieceLoose, whitePieceWin, whitePieceLoose;

    @FXML
    private Text checkMateBlack, checkMateWhite;

    @FXML
    private AnchorPane boardGameChess;

    private Board board;
    private int selectedTimeSeconds = 600;
    private Timeline blackTimer, whiteTimer;
    private int blackTimeSeconds, whiteTimeSeconds;
    private boolean isFirstMove = true;
    private Game game;
    private boolean gameEnded = false;
    private double dragOffsetX, dragOffsetY;
    private boolean isAIEnabled = false;
    private boolean isPlayerMoving = true;

    // Stack to store move history for undoing
    private Stack<MoveSnapshot> moveHistory = new Stack<>();

    // Track Timer state before each move
    private Stack<Integer> blackTimeHistory = new Stack<>();
    private Stack<Integer> whiteTimeHistory = new Stack<>();

    public void setGame(Game game) {
        this.game = game;
    }

    public void setBoard(Board board) {
        this.board = board;
        board.setChessController(this);
        initializeTabTransitions();
        initializeTimeOptions();
        initializePlayGame();
        initializeBoardInteraction();
        initializeNotificationWinPage();
        initializeUndoButton();
        initializeRestartButton();
        initializeSurrenderButton();
        board.draw(boardGameChess);
        updateTurnIndicators();

        Scene scene = boardGameChess.getScene();
        if (scene != null) {
            scene.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
                if (notificationWinPage.isVisible() && !isChildOf((Node) event.getTarget(), notificationWinPage)) {
                    event.consume();
                }
            });
        }
    }

    public void addMoveSnapshot(MoveSnapshot snapshot) {
        moveHistory.push(snapshot);
        blackTimeHistory.push(blackTimeSeconds);
        whiteTimeHistory.push(whiteTimeSeconds);
        if (undo != null) {
            undo.setDisable(false);
        }
    }

    // New method to initialize undo button
    private void initializeUndoButton() {
        if (undo != null) {
            undo.setOnMouseClicked(event -> handleUndo());
            undo.setDisable(true); // Disable until a move is made
        } else {
            System.out.println("Undo button not found in FXML");
        }
    }



    private void handleUndo() {
        if (gameEnded || moveHistory.isEmpty()) {
            return; // No moves to undo or game has ended
        }

        // Pause timers
        stopTimers();

        // Undo the last move
        MoveSnapshot snapshot = moveHistory.pop();
        board.undoMove(snapshot);

        // Restore timer state
        blackTimeSeconds = blackTimeHistory.pop();
        whiteTimeSeconds = whiteTimeHistory.pop();
        updateTimerDisplay();

        // Restore turn and player moving state
        isPlayerMoving = true;

        // Redraw board and update UI
        redraw();

        // Disable undo button if no moves remain
        undo.setDisable(moveHistory.isEmpty());

        // If AI is enabled and it's AI's turn, undo AI's move as well
        if (isAIEnabled && !board.isWhiteTurn()) {
            if (!moveHistory.isEmpty()) {
                snapshot = moveHistory.pop();
                board.undoMove(snapshot);
                blackTimeSeconds = blackTimeHistory.pop();
                whiteTimeSeconds = whiteTimeHistory.pop();
                updateTimerDisplay();
                isPlayerMoving = true;
                redraw();
                undo.setDisable(moveHistory.isEmpty());
            }
        }
    }


    public Board getBoard() {
        return board;
    }

    public void showBotThinking(boolean show) {
        Text botThinking = (Text) boardGameChess.getScene().lookup("#botThinking");
        if (botThinking != null) {
            botThinking.setVisible(show);
        }
    }

    public void redraw() {
        board.draw(boardGameChess);
        if (!gameEnded) {
            if (board.isWhiteTurn()) {
                if (blackTimer != null) blackTimer.pause();
                if (whiteTimer != null) whiteTimer.play();
            } else {
                if (whiteTimer != null) whiteTimer.pause();
                if (blackTimer != null) blackTimer.play();
            }
        }
        updateTurnIndicators();
    }

    public void setPlayerNames(String player1, String player2, boolean isPlayingWithBot) {
        if (player1Name != null) player1Name.setText(player1);
        player2Name.setVisible(!isPlayingWithBot);
        botName.setVisible(isPlayingWithBot);
        (isPlayingWithBot ? botName : player2Name).setText(player2);
    }

    public void setTimer(int timeSeconds) {
        selectedTimeSeconds = timeSeconds;
        blackTimeSeconds = whiteTimeSeconds = timeSeconds;
        updateTimerDisplay();
        initializeTimers();
    }

    private void initializeTabTransitions() {
        gameStatusPage.setVisible(true);
        newGamePage.setVisible(false);
        updateTabStyles(true);

        StatusPageOn.setOnMouseClicked(event -> {
            if (!gameStatusPage.isVisible()) {
                transitionPages(newGamePage, gameStatusPage);
                updateTabStyles(true);
            }
        });

        StatusPageOff.setOnMouseClicked(event -> {
            if (!gameStatusPage.isVisible()) {
                transitionPages(newGamePage, gameStatusPage);
                updateTabStyles(true);
            }
        });

        newGamePageOff.setOnMouseClicked(event -> {
            if (!newGamePage.isVisible()) {
                transitionPages(gameStatusPage, newGamePage);
                updateTabStyles(false);
            }
        });

        newgamePageOn.setOnMouseClicked(event -> {
            if (!newGamePage.isVisible()) {
                transitionPages(gameStatusPage, newGamePage);
                updateTabStyles(false);
            }
        });
    }

    private void transitionPages(AnchorPane fromPage, AnchorPane toPage) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(3), fromPage);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(event -> {
            fromPage.setVisible(false);
            toPage.setVisible(true);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(3), toPage);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();
    }

    private void updateTabStyles(boolean isGameStatusActive) {
        updateStyleClass(StatusPageOn, isGameStatusActive, "tab-active", "tab-inactive");
        updateStyleClass(StatusPageOff, isGameStatusActive, "tab-active", "tab-inactive");
        updateStyleClass(newGamePageOff, !isGameStatusActive, "tab-new-game-active", "tab-new-game-inactive");
        updateStyleClass(newgamePageOn, !isGameStatusActive, "tab-new-game-active", "tab-new-game-inactive");
    }

    private void updateStyleClass(Node node, boolean condition, String activeClass, String inactiveClass) {
        node.getStyleClass().removeAll(activeClass, inactiveClass);
        node.getStyleClass().add(condition ? activeClass : inactiveClass);
    }

    private void initializeTimeOptions() {
        List<Rectangle> timeOptions = Arrays.asList(opt10Mins, opt5Mins, opt3Mins);
        timeOptions.forEach(option -> option.setOnMouseClicked(event -> handleTimeOptionClick(option, timeOptions)));
    }

    private void handleTimeOptionClick(Rectangle clickedOption, List<Rectangle> timeOptions) {
        timeOptions.forEach(option -> option.getStyleClass().remove("rectangle-selected"));
        clickedOption.getStyleClass().add("rectangle-selected");
        selectedTimeSeconds = clickedOption == opt10Mins ? 600 : clickedOption == opt5Mins ? 300 : 180;
    }

    private void initializePlayGame() {
        if (playGame != null) {
            playGame.setOnMouseClicked(event -> {
                if (game == null) throw new IllegalStateException("Game instance is not set in ChessController");
                game.switchToMainScene();
            });
        }
    }

    private void initializeBoardInteraction() {
        boardGameChess.setOnMouseClicked(event -> {
            if (gameEnded) return;
            int x = (int) event.getX();
            int y = (int) event.getY();
            boolean wasWhiteTurn = board.isWhiteTurn();
            board.handleSelectedPiece(x, y);
            if (board.getSelectedPiece() != null || wasWhiteTurn != board.isWhiteTurn()) {
                if (isFirstMove && wasWhiteTurn != board.isWhiteTurn()) {
                    if (board.isWhiteTurn()) whiteTimer.play();
                    else blackTimer.play();
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
                stopTimers();
                endGame(true, "White wins by timeout! Black ran out of time.");
            }
        }));
        blackTimer.setCycleCount(Timeline.INDEFINITE);

        whiteTimer = new Timeline(new javafx.animation.KeyFrame(Duration.seconds(1), event -> {
            if (whiteTimeSeconds > 0) {
                whiteTimeSeconds--;
                updateTimerDisplay();
            } else {
                stopTimers();
                endGame(false, "Black wins by timeout! White ran out of time.");
            }
        }));
        whiteTimer.setCycleCount(Timeline.INDEFINITE);

        System.out.println("Timers initialized: blackTimer=" + (blackTimer != null) + ", whiteTimer=" + (whiteTimer != null));
    }

    private void initializeNotificationWinPage() {
        AnchorPane turnOffWinPage = (AnchorPane) notificationWinPage.lookup("#turnOffWinPage");
        if (turnOffWinPage != null) {
            turnOffWinPage.setOnMouseClicked(event -> {
                notificationWinPage.setVisible(false);
                game.switchToMainScene();
                System.out.println("turnOffWinPage clicked, returning to main scene");
            });
        }

        AnchorPane rematchGame = (AnchorPane) notificationWinPage.lookup("#rematchGame");
        if (rematchGame != null) {
            rematchGame.setOnMouseClicked(event -> {
                resetGame();
                notificationWinPage.setVisible(false);
                System.out.println("rematchGame clicked, game reset and notificationWinPage hidden");
            });
        } else {
            System.out.println("rematchGame not found in notificationWinPage");
        }

        notificationWinPage.setOnMousePressed(event -> {
            dragOffsetX = event.getX();
            dragOffsetY = event.getY();
            System.out.println("Mouse pressed on notificationWinPage at: " + dragOffsetX + ", " + dragOffsetY);
            event.consume();
        });

        notificationWinPage.setOnMouseDragged(event -> {
            Scene scene = notificationWinPage.getScene();
            if (scene == null) {
                System.out.println("Scene not available during drag");
                return;
            }

            double newX = event.getSceneX() - dragOffsetX;
            double newY = event.getSceneY() - dragOffsetY;

            double sceneWidth = scene.getWidth();
            double sceneHeight = scene.getHeight();
            double paneWidth = notificationWinPage.getWidth();
            double paneHeight = notificationWinPage.getHeight();

            newX = Math.max(0, Math.min(newX, sceneWidth - paneWidth));
            newY = Math.max(0, Math.min(newY, sceneHeight - paneHeight));

            notificationWinPage.setLayoutX(newX);
            notificationWinPage.setLayoutY(newY);
            System.out.println("Dragging notificationWinPage to: " + newX + ", " + newY);
            event.consume();
        });
    }

    private void initializeSurrenderButton() {
        if (surrender != null) {
            surrender.setOnMouseClicked(event -> {
                if (!gameEnded) surrender();
            });
        } else {
            System.out.println("Surrender button not found in FXML");
        }
    }

    private void initializeRestartButton() {
        if (restart != null) {
            restart.setOnMouseClicked(event -> resetGame());
        }

    }

    public void surrender() {
        boolean isWhiteSurrendering = board.isWhiteTurn();
        String message = isWhiteSurrendering ? "White surrenders! Black wins." : "Black surrenders! White wins.";
        endGame(!isWhiteSurrendering, message);
    }

    public void handleCheckmate(boolean isWhiteTurn) {
        String message = isWhiteTurn ? "Black wins! White is checkmated." : "White wins! Black is checkmated.";
        endGame(!isWhiteTurn, message);
    }

    private void endGame(boolean whiteWins, String message) {
        stopTimers();
        gameEnded = true;

        boolean isPlayingWithBot = botName.isVisible();
        String winnerName = whiteWins ? player1Name.getText() : isPlayingWithBot ? botName.getText() : player2Name.getText();

        whitePieceWin.setVisible(whiteWins);
        blackPieceLoose.setVisible(whiteWins);
        blackPieceWin.setVisible(!whiteWins);
        whitePieceLoose.setVisible(!whiteWins);
        checkMateWhite.setVisible(whiteWins);
        checkMateBlack.setVisible(!whiteWins);

        winner1.setVisible(false);
        winner2.setVisible(false);
        botWin.setVisible(false);
        if (whiteWins) {
            winner1.setText(winnerName);
            winner1.setVisible(true);
        } else if (isPlayingWithBot) {
            botWin.setText(winnerName);
            botWin.setVisible(true);
        } else {
            winner2.setText(winnerName);
            winner2.setVisible(true);
        }

        notificationWinPage.setVisible(true);
        System.out.println(message + " Winner: " + winnerName + ", isPlayingWithBot: " + isPlayingWithBot);
    }

    private void stopTimers() {
        if (blackTimer != null) {
            blackTimer.stop();
            System.out.println("blackTimer stopped");
        }
        if (whiteTimer != null) {
            whiteTimer.stop();
            System.out.println("whiteTimer stopped");
        }
    }

    public void resetGame() {
        isFirstMove = true;
        stopTimers();
        board.resetBoard();
        blackTimeSeconds = whiteTimeSeconds = selectedTimeSeconds;
        updateTimerDisplay();
        blackPieceWin.setVisible(false);
        whitePieceWin.setVisible(false);
        blackPieceLoose.setVisible(false);
        whitePieceLoose.setVisible(false);
        checkMateBlack.setVisible(false);
        checkMateWhite.setVisible(false);
        notificationWinPage.setVisible(false);
        notificationWinPage.setLayoutX(260);
        notificationWinPage.setLayoutY(100);
        moveHistory.clear();
        undo.setDisable(true);
        gameEnded = false;
        board.draw(boardGameChess);
        updateTurnIndicators();
    }

    private void updateTimerDisplay() {
        blackTime.setText(formatTime(blackTimeSeconds));
        whiteTime.setText(formatTime(whiteTimeSeconds));
    }

    private String formatTime(int seconds) {
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    private void updateTurnIndicators() {
        boolean isWhiteTurn = board.isWhiteTurn();
        blackTurn.setVisible(!isWhiteTurn);
        whiteTurn.setVisible(isWhiteTurn);
        blackTextTurn.setVisible(!isWhiteTurn);
        whiteTextTurn.setVisible(isWhiteTurn);

        whiteTurn.getStyleClass().setAll(isWhiteTurn ? "white-turn" : "white-turn-inactive");
        blackTurn.getStyleClass().setAll(isWhiteTurn ? "black-turn-inactive" : "black-turn");
        whiteTextTurn.setFill(isWhiteTurn ? javafx.scene.paint.Color.BLACK : javafx.scene.paint.Color.WHITE);
        blackTextTurn.setFill(isWhiteTurn ? javafx.scene.paint.Color.WHITE : javafx.scene.paint.Color.BLACK);
    }

    public int getSelectedTimeSeconds() {
        return selectedTimeSeconds;
    }

    private boolean isChildOf(Node node, Node parent) {
        while (node != null) {
            if (node == parent) return true;
            node = node.getParent();
        }
        return false;
    }

    public boolean isGameEnded() {
        return gameEnded;
    }
}