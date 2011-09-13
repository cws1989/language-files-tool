package langfiles.gui;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
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

    /**
     * Constructor.
     * @param pane the tabbed pane to bind to
     */
    public JTabComponent(JTabbedPane pane) {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));

        this.pane = pane;
        setOpaque(false);

        JLabel titleLabel = new JLabel() {

            private static final long serialVersionUID = 1L;

            @Override
            public String getText() {
                int tabIndex = JTabComponent.this.pane.indexOfTabComponent(JTabComponent.this);
                if (tabIndex != -1) {
                    return JTabComponent.this.pane.getTitleAt(tabIndex);
                }
                return null;
            }
        };
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 3));
        add(titleLabel);

        add(new TabCloseButton());
    }

    /**
     * The close button of the tab component.
     */
    private class TabCloseButton extends JButton {

        private static final long serialVersionUID = 1L;
        /**
         * Default dimension of the button.
         */
        private int size = 14;

        /**
         * The constructor.
         */
        private TabCloseButton() {
            setPreferredSize(new Dimension(size, size));
            setToolTipText("Close");
            setUI(new BasicButtonUI());
            setContentAreaFilled(false);
            setFocusable(false);
            setRolloverEnabled(true);
            addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    int tabIndex = pane.indexOfTabComponent(JTabComponent.this);
                    if (tabIndex != -1) {
                        pane.remove(tabIndex);
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
            int borderWidth = (size - 4) / 2;
            graphics2D.drawLine(borderWidth, borderWidth, getWidth() - borderWidth, getHeight() - borderWidth);
            graphics2D.drawLine(borderWidth, getHeight() - borderWidth, getWidth() - borderWidth, borderWidth);

            graphics2D.dispose();
        }
    }
}
