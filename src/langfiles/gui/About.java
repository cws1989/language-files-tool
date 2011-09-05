package langfiles.gui;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Frame;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
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

        Box titleBox = Box.createHorizontalBox();
        panel.add(titleBox);
        JLabel titleLabel = new JLabel("Language Files Tool");
        titleLabel.setFont(CommonUtil.deriveFont(titleLabel.getFont(), true, 14));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 12, 10));
        titleBox.add(titleLabel);
        titleBox.add(Box.createHorizontalGlue());


        JPanel contentPanel = new JPanel();
        contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 5, 0));
        contentPanel.setLayout(new SpringLayout());
        panel.add(contentPanel);

        JLabel authorTitleLabel = new JLabel("Contributor: ");
        authorTitleLabel.setFont(CommonUtil.deriveFont(authorTitleLabel.getFont(), false, 12));
        contentPanel.add(authorTitleLabel);
        JLabel authorLabel = new JLabel("Chan Wai Shing");
        authorLabel.setFont(CommonUtil.deriveFont(authorLabel.getFont(), false, 12));
        contentPanel.add(authorLabel);

        JLabel contactTitleLabel = new JLabel("Email: ");
        contactTitleLabel.setFont(CommonUtil.deriveFont(contactTitleLabel.getFont(), false, 12));
        contentPanel.add(contactTitleLabel);
        JLabel contactLabel = new JLabel("<html><a href='mailto:cws1989@gmail.com'>cws1989@gmail.com</a></html>");
        contactLabel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.MAIL)) {
                    try {
                        desktop.mail(new URI("mailto:cws1989@gmail.com"));
                    } catch (IOException ex) {
                        Logger.getLogger(About.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (URISyntaxException ex) {
                        Logger.getLogger(About.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        contactLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        contactLabel.setFont(CommonUtil.deriveFont(contactLabel.getFont(), false, 12));
        contentPanel.add(contactLabel);

        JLabel webLinkTitleLabel = new JLabel("Web: ");
        webLinkTitleLabel.setFont(CommonUtil.deriveFont(webLinkTitleLabel.getFont(), false, 12));
        contentPanel.add(webLinkTitleLabel);
        JLabel webLinkLabel = new JLabel("<html><a href='http://language-files-tool.googlecode.com'>http://language-files-tool.googlecode.com</a></html>");
        webLinkLabel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    try {
                        desktop.browse(new URI("http://language-files-tool.googlecode.com"));
                    } catch (IOException ex) {
                        Logger.getLogger(About.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (URISyntaxException ex) {
                        Logger.getLogger(About.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        webLinkLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        webLinkLabel.setFont(CommonUtil.deriveFont(webLinkLabel.getFont(), false, 12));
        contentPanel.add(webLinkLabel);

        SpringUtilities.makeCompactGrid(contentPanel, 3, 2, 0, 0, 10, 6);


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
