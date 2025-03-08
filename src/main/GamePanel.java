package main;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import inputs.MouseInput;
import utilz.LoadImage;

public class GamePanel extends JPanel {
    private  Game game;
    private BufferedImage img;
    private MouseInput mouseInput;
    private Board board;



    GamePanel( Game game , Board board  ) {
        this.game = game;
        setPreferredSize(new Dimension(Game.GAME_WIDTH, Game.GAME_HEIGHT));
        img = LoadImage.GetAtlas(LoadImage.boardBackground);
        this.board = board;
        mouseInput = new MouseInput(this.board);

        addMouseListener(mouseInput);
        addMouseMotionListener(mouseInput);
        setBackground(Color.BLACK);


    }
    public void update() {


    }
    public void drawBackground(){

    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(img,0,0,Game.GAME_WIDTH, Game.GAME_HEIGHT,this);
        game.render(g);
    }

}
