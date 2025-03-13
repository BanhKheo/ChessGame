package utilz;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class LoadImage {
    public static String boardBackground = "200.png";
    public static String r = ".png";
    public static String n = ".png";
    public static String b = ".png";
    public static String q = "q.png";
    public static String k = "k.png";
    public static String p = "p.png";



    public static BufferedImage GetAtlas(String fileName) {
        BufferedImage img = null;
        InputStream is = LoadImage.class.getResourceAsStream("/res/" + fileName);
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
        return img;
    }

    public static BufferedImage GetPieceImage(boolean isWhite , String p) {
        BufferedImage img = null;
        InputStream is = LoadImage.class.getResourceAsStream("/res/" + getFileName( isWhite , p)+".png");
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
        return img;
    }

    public static String getFileName( boolean isWhite , String p){
        return isWhite ? "w" + p : "b" + p;
    }
}
