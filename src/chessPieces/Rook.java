package chessPieces;

import utilz.LoadImage;

import java.awt.*;

public class Rook extends Pieces {
    public Rook( int x , int y){
        super(x, y , LoadImage.GetAtlas(LoadImage.wr));
    }



    @Override
    public void update() {

    }
}
