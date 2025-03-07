package main;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import inputs.MouseInput;
import utilz.LoadImage;
import utilz.LoadImage.*;

public class GamePanel extends JPanel {
    private  Game game;
    private BufferedImage img;
    private MouseInput mouseInput;


    public static final  int WIDTH = 800;
    public static final  int HEIGHT = 800;
    GamePanel( Game game){
        this.game = game;
        setPreferredSize(new Dimension(WIDTH,HEIGHT));
        img = LoadImage.GetBackground(LoadImage.Board_background);
        mouseInput = new MouseInput();

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
        g.drawImage(img,0,0,WIDTH, HEIGHT,this);

    }

}
