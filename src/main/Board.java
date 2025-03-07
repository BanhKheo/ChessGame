package main;

import utilz.LoadImage;

import java.awt.*;
import java.awt.image.BufferedImage;
import static main.GamePanel.*;
import static utilz.LoadImage.*;

public class Board {
    BufferedImage img;

    Board(){
        img = LoadImage.GetBackground(Board_background);
    }

    public void update(){

    }
    public void draw(Graphics g){
        g.drawImage(img , WIDTH, HEIGHT , null );
    }
}
