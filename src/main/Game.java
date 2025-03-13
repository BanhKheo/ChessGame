package main;

import java.awt.*;

public class Game implements Runnable {
    private GameWindow window;
    private GamePanel panel;
    private Board board;

    //Global variable
    public static final int GAME_WIDTH = 720;
    public static final int GAME_HEIGHT = 720;

    public static final int GAME_TILES = 90;


    //Game loop variable
    private Thread gameThread;
    private final int FPS_SET = 120;
    private final int UPS_SET = 200;

    Game(){
        initialClasses();
        panel = new GamePanel(this , board);
        window = new GameWindow(panel);
        startGameLoop();


    }
    private void initialClasses()
    {
        board = new Board();

    }

    private void startGameLoop() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    private void update() {

    }

    public void render(Graphics g)
    {
        board.draw(g);
    }
    @Override
    public void run() {


        double timePerFrame = 1000000000.0 / FPS_SET;
        double timePerUpdate = 1000000000.0 / UPS_SET;

        long previousTime = System.nanoTime();

        int frames = 0;
        int updates = 0;
        long lastCheck = System.currentTimeMillis();

        double deltaU = 0;
        double deltaF = 0;

        while (true) {
            long currentTime = System.nanoTime();

            deltaU += (currentTime - previousTime) / timePerUpdate;
            deltaF += (currentTime - previousTime) / timePerFrame;
            previousTime = currentTime;

            if (deltaU >= 1) {
                update();
                updates++;
                deltaU--;
            }

            if (deltaF >= 1) {
                panel.repaint();
                frames++;
                deltaF--;
            }

            if (System.currentTimeMillis() - lastCheck >= 1000) {
                lastCheck = System.currentTimeMillis();
                System.out.println("FPS: " + frames + " | UPS: " + updates);
                frames = 0;
                updates = 0;

            }
        }

    }


}
