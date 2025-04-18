package main;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;

public class Game extends Application {
   private Board board;
   public static final int GAME_TILES = 88;

   private MainMenuController mainController;

   private Scene mainScene;
   private Scene chessScene;

   private ChessController chessController;

   @Override
   public void start(Stage stage) throws IOException {
      board = new Board();

      // Load main menu FXML
      FXMLLoader mainLoader = new FXMLLoader(Game.class.getResource("/MainPage.fxml"));
      mainScene = new Scene(mainLoader.load(), 774, 489);
      mainController = mainLoader.getController();

      if (mainController == null) {
         throw new IllegalStateException("Failed to load MainMenuController from FXML");
      }

      mainController.setGame(this);

      // Load chess game FXML
      FXMLLoader chessLoader = new FXMLLoader(Game.class.getResource("/GameBoard.fxml"));
      chessScene = new Scene(chessLoader.load(), 1170, 792);
      chessController = chessLoader.getController();

      if (chessController == null) {
         throw new IllegalStateException("ChessController was not injected properly");
      }
      chessController.setBoard(board);

      // Game loop
      new AnimationTimer() {
         @Override
         public void handle(long now)  {
            if(stage.getScene() == chessScene) {
               chessController.redraw();
            }
         }
      }.start();

      // Set up and show the initial stage
      stage.setTitle("Chess Game");
      stage.setScene(mainScene);
      stage.setResizable(false);
      stage.show();
   }



   public void switchToChessScene() {
      Stage stage = (Stage) mainScene.getWindow();
      stage.setScene(chessScene);
      stage.centerOnScreen();
   }

   public static void main(String[] args) {
      launch(args);
   }

}
