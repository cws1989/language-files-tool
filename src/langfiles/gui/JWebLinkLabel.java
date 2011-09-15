package langfiles.gui;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.HeadlessException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 * Web link label. Support email and web site link. Web site link should start with 'http:'; email link should start with 'mailto:'.
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public class JWebLinkLabel extends JLabel {

    private static final long serialVersionUID = 1L;
    /**
     * Record the type of the link, either {@link java.awt.Desktop.Action.MAIL} or {@link java.awt.Desktop.Action.BROWSE}.
     */
    protected Desktop.Action action;
    /**
     * The URI of the link
     */
    protected URI webLinkURI;
    /**
     * The {@link java.awt.Desktop} instance.
     */
    protected Desktop desktop;

    /**
     * Constructor.
     */
    public JWebLinkLabel() {
        super();
        webLinkInitialize();
    }

    /**
     * Constructor.
     * @param link the link
     * @throws URISyntaxException 
     */
    public JWebLinkLabel(String link) throws URISyntaxException {
        super();
        setLink(link);
        webLinkInitialize();
    }

    /**
     * Constructor. &lt;a href='link'&gt;description&lt;/a&gt;
     * @param link the link
     * @param description the link description
     * @throws URISyntaxException 
     */
    public JWebLinkLabel(String link, String description) throws URISyntaxException {
        super();
        setLink(link, description);
        webLinkInitialize();
    }

    /**
     * Set the link.
     * @param link the link
     * @throws URISyntaxException 
     */
    public void setLink(String link) throws URISyntaxException {
        setLink(link, link);
    }

    /**
     * Set the link.
     * @param link the link
     * @param description the link description
     * @throws URISyntaxException 
     */
    public void setLink(String link, String description) throws URISyntaxException {
        String URLLowerCase = link.toLowerCase();
        if (URLLowerCase.startsWith("mailto:")) {
            action = Desktop.Action.MAIL;
        } else if (URLLowerCase.startsWith("http:")) {
            action = Desktop.Action.BROWSE;
        } else {
            action = null;
        }

        if (action != null) {
            webLinkURI = new URI(link);
            super.setText("<html><a href='" + link + "'>" + description + "</a></html>");
        } else {
            super.setText("");
        }
    }

    /**
     * Initialization function.
     */
    protected final void webLinkInitialize() {
        try {
            desktop = Desktop.getDesktop();
        } catch (HeadlessException ex) {
            desktop = null;
            Logger.getLogger(JWebLinkLabel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedOperationException ex) {
            desktop = null;
            Logger.getLogger(JWebLinkLabel.class.getName()).log(Level.SEVERE, null, ex);
        }

        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (action != null && desktop != null) {
                    try {
                        switch (action) {
                            case MAIL:
                                if (desktop.isSupported(Desktop.Action.MAIL)) {
                                    desktop.mail(webLinkURI);
                                } else {
                                    JOptionPane.showMessageDialog(null, "Failed to open link: " + webLinkURI);
                                }
                                break;
                            case BROWSE:
                                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                                    desktop.browse(webLinkURI);
                                } else {
                                    JOptionPane.showMessageDialog(null, "Failed to open link: " + webLinkURI);
                                }
                                break;
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(JWebLinkLabel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });

        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /**
     * For test purpose.
     */
    public static void main(String[] args) throws URISyntaxException {
        javax.swing.JFrame frame = new javax.swing.JFrame();
        frame.getContentPane().add(new JWebLinkLabel("http:hk.yahoo.com", "Yahoo!"));
        frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }
}
