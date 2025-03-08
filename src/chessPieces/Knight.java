package chessPieces;

import utilz.LoadImage;

import java.awt.*;

public class Knight extends Pieces {
    public Knight( int x , int y){
        super(x, y , LoadImage.GetAtlas(LoadImage.wn));
    }

    @Override
    public void update() {

    }
}
