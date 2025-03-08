package utilz;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class LoadImage {
    public static String boardBackground = "200.png";
    public static String wr = "wr.png";
    public static String br = "br.png";
    public static String wn = "wn.png";
    public static String bn = "bn.png";
    public static String wb = "wb.png";
    public static String bb = "bb.png";
    public static String wq = "wq.png";
    public static String bq = "bq.png";
    public static String wk = "wk.png";
    public static String bk = "bk.png";
    public static String wp = "wp.png";
    public static String bp = "bp.png";



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
}
