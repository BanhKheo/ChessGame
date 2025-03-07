package main;

import javax.swing.*;

public class GameWindow extends JFrame {
    private GamePanel gamePanel;
    // Game window size
    GameWindow( GamePanel gamePanel){
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("Chess");
        this.gamePanel = gamePanel;

        add(gamePanel);
        pack();

        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
