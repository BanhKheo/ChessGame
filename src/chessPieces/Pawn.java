package chessPieces;

import utilz.LoadImage;

import java.awt.*;

public class Pawn extends Pieces {
    public Pawn( int x , int y){
        super(x, y , LoadImage.GetAtlas(LoadImage.wp));
    }

    @Override
    public void update() {

    }
}
