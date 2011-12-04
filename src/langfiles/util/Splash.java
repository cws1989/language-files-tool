package langfiles.util;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.SplashScreen;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public class Splash {

  private static final Logger LOG = Logger.getLogger(Splash.class.getName());
  protected static Font font = new Font("Tahoma", Font.PLAIN, 12);
  protected static Color color = Color.black;
  protected static Point position = new Point(15, 10);

  protected Splash() {
  }

  public static void setFont(Font font) {
    Splash.font = font;
  }

  public static void setColor(Color color) {
    Splash.color = color;
  }

  public static void setPosition(Point position) {
    Splash.position = position != null ? position : new Point(0, 0);
  }

  public static void updateMessage(String message) {
    String _message = message != null ? message : "";
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
      if (color != null) {
        g.setColor(color);
      }
      if (font != null) {
        g.setFont(font);
      }
      g.drawString(_message, position.x, position.y);

      splash.update();
    } catch (IllegalStateException ex) {
      LOG.log(Level.WARNING, null, ex);
    }
  }

  public static void close() {
    try {
      SplashScreen splash = SplashScreen.getSplashScreen();
      if (splash == null) {
        return;
      }
      splash.close();
    } catch (IllegalStateException ex) {
      LOG.log(Level.WARNING, null, ex);
    }
  }
}
