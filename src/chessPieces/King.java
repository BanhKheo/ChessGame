package chessPieces;

import utilz.LoadImage;

import java.awt.*;

public class King extends Pieces {
    public King( int x , int y){
        super(x, y , LoadImage.GetAtlas(LoadImage.wk));
    }

    @Override
    public void update() {

    }
}
