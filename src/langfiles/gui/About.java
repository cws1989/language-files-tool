package langfiles.gui;

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
     * The dialog object.
     */
    private JDialog dialog;

    /**
     * Constructor.
     * @param frame the frame to bind to
     */
    public About(Frame frame) {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        //<editor-fold defaultstate="collapsed" desc="titleBox">
        Box titleBox = Box.createHorizontalBox();
        titleBox.setBorder(BorderFactory.createEmptyBorder(0, 10, 12, 10));

        JLabel titleLabel = new JLabel("Language Files Tool");
        titleLabel.setFont(CommonUtil.deriveFont(titleLabel.getFont(), true, 14));
        titleBox.add(titleLabel);

        titleBox.add(Box.createRigidArea(new Dimension(10, 10)));

        JLabel versionLabel = new JLabel("v1.0");
        versionLabel.setBorder(BorderFactory.createEmptyBorder(titleLabel.getPreferredSize().height - versionLabel.getPreferredSize().height, 0, 0, 0));
        titleBox.add(versionLabel);

        titleBox.add(Box.createHorizontalGlue());
        //</editor-fold>
        panel.add(titleBox);

        //<editor-fold defaultstate="collapsed" desc="contentPanel">
        JPanel contentPanel = new JPanel();
        contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 5, 0));
        contentPanel.setLayout(new SpringLayout());

        JLabel authorTitleLabel = new JLabel("Contributor: ");
        authorTitleLabel.setFont(CommonUtil.deriveFont(authorTitleLabel.getFont(), false, 12));
        contentPanel.add(authorTitleLabel);
        JLabel authorLabel = new JLabel("Chan Wai Shing");
        authorLabel.setFont(CommonUtil.deriveFont(authorLabel.getFont(), false, 12));
        contentPanel.add(authorLabel);

        JLabel contactTitleLabel = new JLabel("Email: ");
        contactTitleLabel.setFont(CommonUtil.deriveFont(contactTitleLabel.getFont(), false, 12));
        contentPanel.add(contactTitleLabel);
        try {
            JWebLinkLabel contactLabel = new JWebLinkLabel("mailto:cws1989@gmail.com", "cws1989@gmail.com");
            contactLabel.setFont(CommonUtil.deriveFont(contactLabel.getFont(), false, 12));
            contentPanel.add(contactLabel);
        } catch (URISyntaxException ex) {
            Logger.getLogger(About.class.getName()).log(Level.SEVERE, null, ex);
        }

        JLabel webLinkTitleLabel = new JLabel("Web: ");
        webLinkTitleLabel.setFont(CommonUtil.deriveFont(webLinkTitleLabel.getFont(), false, 12));
        contentPanel.add(webLinkTitleLabel);
        try {
            JWebLinkLabel webLinkLabel = new JWebLinkLabel("http://language-files-tool.googlecode.com", "http://language-files-tool.googlecode.com");
            webLinkLabel.setFont(CommonUtil.deriveFont(webLinkLabel.getFont(), false, 12));
            contentPanel.add(webLinkLabel);
        } catch (URISyntaxException ex) {
            Logger.getLogger(About.class.getName()).log(Level.SEVERE, null, ex);
        }

        SpringUtilities.makeCompactGrid(contentPanel, 3, 2, 0, 0, 10, 6);
        //</editor-fold>
        panel.add(contentPanel);

        dialog = new JDialog(frame, "About", true);
        dialog.add(panel);
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
