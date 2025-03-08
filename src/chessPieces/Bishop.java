package chessPieces;

import utilz.LoadImage;

import java.awt.*;

public class Bishop extends Pieces {
    public Bishop( int x , int y){
        super(x, y , LoadImage.GetAtlas(LoadImage.wb));
    }


    @Override
    public void update() {

    }
}
