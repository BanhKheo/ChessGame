package utilz;


import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class LoadImage {

    public static Image GetPieceImage(boolean isWhite , String p) {
        BufferedImage img = null;
        InputStream is = LoadImage.class.getResourceAsStream("/res/img/" + getFileName( isWhite , p)+".png");
        try {
            img = ImageIO.read(is);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return img != null ? SwingFXUtils.toFXImage(img, null) : null;
    }

    public static String getFileName( boolean isWhite , String p){
        return isWhite ? "w" + p : "b" + p;
    }
}
