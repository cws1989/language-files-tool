package langfiles.util;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.SplashScreen;

/**
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public class Splash {

    private static Font font = new Font("Tahoma", Font.PLAIN, 12);
    private static Color color = Color.black;
    private static Point position = new Point(15, 10);

    private Splash() {
    }

    public static void setFont(Font font) {
        Splash.font = font;
    }

    public static void setPosition(Point position) {
        Splash.position = position;
    }

    public static void updateMessage(String message) {
        try {
            SplashScreen splash = SplashScreen.getSplashScreen();
            if (splash == null) {
                return;
            }

            Dimension dim = splash.getSize();

            Graphics2D g = splash.createGraphics();
            g.setComposite(AlphaComposite.Clear);
            g.fillRect(0, 0, dim.width, dim.height);
            g.setPaintMode();
            g.setColor(color);
            g.setFont(font);
            g.drawString(message, position.x, position.y);

            splash.update();
        } catch (Exception ex) {
        }
    }

    public static void close() {
        try {
            SplashScreen splash = SplashScreen.getSplashScreen();
            if (splash == null) {
                return;
            }
            splash.close();
        } catch (Exception ex) {
        }
    }
}
