package langfiles.gui.component;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 * The tab component with close button.
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public class JTabComponent extends JPanel {

    private static final long serialVersionUID = 1L;
    /**
     * The tabbed pane.
     */
    private JTabbedPane pane;
    private JLabel titleLabel;
    private TabCloseButton tabCloseButton;
    private final List<JTabComponentListener> tabComponentListenerList;

    /**
     * Constructor.
     * @param pane the tabbed pane to bind to
     */
    public JTabComponent(JTabbedPane pane) {
        this(pane, null);
    }

    public JTabComponent(JTabbedPane pane, Icon icon) {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        setLayout(new BorderLayout());

        this.pane = pane;
        tabComponentListenerList = Collections.synchronizedList(new ArrayList<JTabComponentListener>());
        setOpaque(false);

        titleLabel = new JLabel() {

            private static final long serialVersionUID = 1L;
            private String labelTitle;
            private int labelWidth = 0;

            @Override
            public String getText() {
                int tabIndex = JTabComponent.this.pane.indexOfTabComponent(JTabComponent.this);
                if (tabIndex != -1) {
                    String currentTitle = JTabComponent.this.pane.getTitleAt(tabIndex);
                    if (!currentTitle.equals(labelTitle)) {
                        labelTitle = currentTitle;
                        labelWidth = getFontMetrics(getFont()).stringWidth(labelTitle) + 3;
                    }
                    return labelTitle;
                }
                return null;
            }

            @Override
            public Dimension getPreferredSize() {
                if (labelTitle == null) {
                    return super.getPreferredSize();
                } else {
                    Icon imageIcon = getIcon();
                    int iconOffset = imageIcon != null ? imageIcon.getIconWidth() + getIconTextGap() : 0;
                    return new Dimension(labelWidth + iconOffset, (int) super.getPreferredSize().getHeight());
                }
            }

            @Override
            public int getWidth() {
                if (labelTitle == null) {
                    return super.getWidth();
                } else {
                    Icon imageIcon = getIcon();
                    int iconOffset = imageIcon != null ? imageIcon.getIconWidth() + getIconTextGap() : 0;
                    return labelWidth + iconOffset;
                }
            }
        };
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 3));
        if (icon != null) {
            titleLabel.setIcon(icon);
        }
        add(titleLabel, BorderLayout.CENTER);

        add(tabCloseButton = new TabCloseButton(pane, this), BorderLayout.EAST);
    }

    @Override
    public Dimension getPreferredSize() {
        if (titleLabel == null || tabCloseButton == null) {
            return super.getPreferredSize();
        } else {
            return new Dimension(titleLabel.getWidth() + 11, (int) super.getPreferredSize().getHeight());
        }
    }

    public void addTabComponentListener(JTabComponentListener listener) {
        tabComponentListenerList.add(listener);
    }

    public void removeTabComponentListener(JTabComponentListener listener) {
        tabComponentListenerList.remove(listener);
    }

    /**
     * The close button of the tab component.
     */
    private class TabCloseButton extends JButton {

        private static final long serialVersionUID = 1L;
        private JTabbedPane pane;
        private JTabComponent tabComponent;

        /**
         * The constructor.
         */
        private TabCloseButton(JTabbedPane pane, JTabComponent tabComponent) {
            this.pane = pane;
            this.tabComponent = tabComponent;

            setPreferredSize(new Dimension(11, 14));
            setToolTipText("Close");
            setUI(new BasicButtonUI());
            setContentAreaFilled(false);
            setFocusable(false);
            setRolloverEnabled(true);
            setBorder(null);
            addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    int tabIndex = TabCloseButton.this.pane.indexOfTabComponent(TabCloseButton.this.tabComponent);
                    if (tabIndex != -1) {
                        Component tab = TabCloseButton.this.pane.getComponentAt(tabIndex);
                        TabCloseButton.this.pane.removeTabAt(tabIndex);
                        synchronized (tabComponentListenerList) {
                            for (JTabComponentListener listener : tabComponentListenerList) {
                                listener.tabClosed(tab, TabCloseButton.this.tabComponent);
                            }
                        }
                    }
                }
            });
        }

        @Override
        public void updateUI() {
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D graphics2D = (Graphics2D) g.create();

            //shift the image for pressed buttons
            if (getModel().isPressed()) {
                graphics2D.translate(1, 1);
            }

            graphics2D.setStroke(new BasicStroke(2));
            if (getModel().isRollover()) {
                graphics2D.setColor(getBackground().darker().darker().darker());
            } else {
                graphics2D.setColor(getBackground().darker().darker());
            }

            // keep the cross be 4x4 pixels
            int width = getWidth(), height = getHeight();
            int startY = (height - 4) / 2;
            int endY = startY + 4;
            int startX = width - 4 - 2;
            int endX = width - 2;

            graphics2D.drawLine(startX, startY, endX, endY);
            graphics2D.drawLine(startX, endY, endX, startY);

            graphics2D.dispose();
        }
    }
}
