package chessPieces;

import utilz.LoadImage;

import java.awt.*;

public class Queen extends Pieces {
    public Queen( int x , int y){
        super(x, y , LoadImage.GetAtlas(LoadImage.wq));
    }

    @Override
    public void update() {

    }
}
