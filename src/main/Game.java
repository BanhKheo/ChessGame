package main;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Game extends Application {
   private Board board;
   public static final int GAME_TILES = 88;

   private MainMenuController mainController;

   private Scene mainScene;
   private Scene chessScene;

   private ChessController chessController;

   private int selectedTimeSeconds = 600; // Default to 10 minutes

   @Override
   public void start(Stage stage) throws IOException {
      board = new Board();

      // Load main menu FXML
      FXMLLoader mainLoader = new FXMLLoader(Game.class.getResource("/MainPage.fxml"));
      if (mainLoader.getLocation() == null) {
         throw new IOException("Cannot find MainPage.fxml at /MainPage.fxml");
      }
      mainScene = new Scene(mainLoader.load(), 774, 489);
      mainController = mainLoader.getController();

      if (mainController == null) {
         throw new IllegalStateException("Failed to load MainMenuController from FXML");
      }

      mainController.setGame(this);

      // Load chess game FXML
      FXMLLoader chessLoader = new FXMLLoader(Game.class.getResource("/GameBoard.fxml"));
      if (chessLoader.getLocation() == null) {
         throw new IOException("Cannot find GameBoard.fxml at /GameBoard.fxml");
      }
      chessScene = new Scene(chessLoader.load(), 1170, 792);
      chessController = chessLoader.getController();

      if (chessController == null) {
         throw new IllegalStateException("ChessController was not injected properly");
      }
      chessController.setBoard(board);
      chessController.setGame(this);

      /*new AnimationTimer() {
         @Override
         public void handle(long now)  {
            if(stage.getScene() == chessScene) {
               chessController.redraw();
            }
         }
      }.start(); */

      // Set up and show the initial stage
      stage.setTitle("Chess Game");
      stage.setScene(mainScene);
      stage.setResizable(false);
      stage.show();
   }

   public void switchToChessScene(Stage stage) {
      if (stage == null) {
         throw new IllegalArgumentException("Stage cannot be null in switchToChessScene");
      }
      if (chessScene == null) {
         throw new IllegalStateException("Chess scene is not initialized");
      }
      stage.setScene(chessScene);
      stage.centerOnScreen();
      // Set the timer after switching to the chess scene
      chessController.resetGame();
      chessController.setTimer(selectedTimeSeconds);
   }

   public void switchToMainScene() {
      if (mainScene == null) {
         throw new IllegalStateException("Main scene is not initialized");
      }
      // Get the selected time from ChessController
      this.selectedTimeSeconds = chessController.getSelectedTimeSeconds();
      Stage stage = (Stage) chessScene.getWindow();
      if (stage == null) {
         throw new IllegalStateException("Cannot retrieve Stage from chessScene");
      }
      stage.setScene(mainScene);
      stage.centerOnScreen();
   }

   // Getter for ChessController
   public ChessController getChessController() {
      return chessController;
   }

   public static void main(String[] args) {
      launch(args);
   }
}