package langfiles.gui;

import langfiles.gui.component.JWebLinkLabel;
import java.awt.Dimension;
import java.awt.Frame;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import langfiles.util.CommonUtil;
import langfiles.util.SpringUtilities;

/**
 * 'About' panel.
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public class About {

  /**
   * The dialog object of the 'About' panel.
   */
  protected JDialog dialog;

  /**
   * Constructor.
   * @param frame the frame to bind to
   */
  public About(Frame frame) {
    //<editor-fold defaultstate="collapsed" desc="titleBox">
    JLabel titleLabel = new JLabel("Language Files Tool");
    titleLabel.setFont(CommonUtil.deriveFont(titleLabel.getFont(), true, 14));

    JLabel versionLabel = new JLabel("0.9.0 beta");
    versionLabel.setBorder(BorderFactory.createEmptyBorder(titleLabel.getPreferredSize().height - versionLabel.getPreferredSize().height, 0, 0, 0));

    Box titleBox = Box.createHorizontalBox();
    titleBox.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 10));
    titleBox.add(titleLabel);
    titleBox.add(Box.createRigidArea(new Dimension(10, 10)));
    titleBox.add(versionLabel);
    titleBox.add(Box.createHorizontalGlue());
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="contentPanel">
    JLabel authorTitleLabel = new JLabel("Contributor: ");
    authorTitleLabel.setFont(CommonUtil.deriveFont(authorTitleLabel.getFont(), false, 12));

    JLabel authorLabel = new JLabel("Chan Wai Shing");
    authorLabel.setFont(CommonUtil.deriveFont(authorLabel.getFont(), false, 12));

    JLabel contactTitleLabel = new JLabel("Email: ");
    contactTitleLabel.setFont(CommonUtil.deriveFont(contactTitleLabel.getFont(), false, 12));

    JWebLinkLabel contactLabel = null;
    try {
      contactLabel = new JWebLinkLabel("mailto:cws1989@gmail.com", "cws1989@gmail.com");
      contactLabel.setFont(CommonUtil.deriveFont(contactLabel.getFont(), false, 12));
    } catch (URISyntaxException ex) {
      Logger.getLogger(About.class.getName()).log(Level.SEVERE, null, ex);
    }

    JLabel webLinkTitleLabel = new JLabel("Web: ");
    webLinkTitleLabel.setFont(CommonUtil.deriveFont(webLinkTitleLabel.getFont(), false, 12));

    JWebLinkLabel webLinkLabel = null;
    try {
      webLinkLabel = new JWebLinkLabel("http://language-files-tool.googlecode.com", "http://language-files-tool.googlecode.com");
      webLinkLabel.setFont(CommonUtil.deriveFont(webLinkLabel.getFont(), false, 12));
    } catch (URISyntaxException ex) {
      Logger.getLogger(About.class.getName()).log(Level.SEVERE, null, ex);
    }

    JPanel contentPanel = new JPanel();
    contentPanel.setLayout(new SpringLayout());
    contentPanel.add(authorTitleLabel);
    contentPanel.add(authorLabel);
    contentPanel.add(contactTitleLabel);
    // should not be null
    contentPanel.add(contactLabel);
    contentPanel.add(webLinkTitleLabel);
    // should not be null
    contentPanel.add(webLinkLabel);

    SpringUtilities.makeCompactGrid(contentPanel, 3, 2, 10, 5, 10, 5);
    //</editor-fold>

    JPanel mainPanel = new JPanel();
    mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
    mainPanel.add(titleBox);
    mainPanel.add(contentPanel);

    dialog = new JDialog(frame, "About", true);
    dialog.add(mainPanel);
    dialog.setResizable(false);
    dialog.pack();
    dialog.setLocationByPlatform(true);
    dialog.setLocationRelativeTo(frame);
  }

  /**
   * Show the about dialog.
   */
  public void show() {
    dialog.setVisible(true);
  }
}
