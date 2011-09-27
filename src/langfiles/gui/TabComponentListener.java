package langfiles.gui;

import langfiles.gui.component.JTabComponent;
import java.awt.Component;

/**
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public interface TabComponentListener {

    void tabClosed(Component tab, JTabComponent tabComponent);
}
