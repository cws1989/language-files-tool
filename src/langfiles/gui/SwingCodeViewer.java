package langfiles.gui;

import langfiles.handler.CodeViewer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import langfiles.util.CommonUtil;
import langfiles.handler.DigestedFile;
import langfiles.handler.DigestedFile.Component;
import langfiles.handler.Handler;

/**
 * The code viewer.
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public class SwingCodeViewer implements CodeViewer {

    /**
     * The font of the line number text.
     */
    protected Font lineNumberFont = new Font("Verdana", Font.PLAIN, 10);
    /**
     * The scroll pane that contain the codePanel.
     */
    protected JScrollPane scrollPane;
    /**
     * The GUI panel of the code viewer.
     */
    protected JPanel codePanel;
    /**
     * The list that store only text components.
     */
    protected List<JComponent> textComponentList = new ArrayList<JComponent>();

    /**
     * Constructor.
     */
    public SwingCodeViewer() {
        codePanel = new JPanel();
        codePanel.setLayout(new GridBagLayout());
        codePanel.setBackground(Color.white);

        scrollPane = new JScrollPane();
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(20);
        scrollPane.setViewportView(codePanel);
    }

    /**
     * Get the GUI display panel.
     * @return the GUI component
     */
    public JScrollPane getGUI() {
        return scrollPane;
    }

    /**
     * Set the text for display, lines are separated by \n.
     * @param text the text
     */
    public void setText(String text) {
        codePanel.removeAll();
        textComponentList.clear();

        // split the text into lines
        String[] lines = text.split("\n");

        // the width of the maximum line number
        int lineNumberBoxWidth = CommonUtil.getFontMetrics(lineNumberFont).stringWidth(Integer.toString(lines.length)) + 7;

        // the grid bag constraints
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1.0F;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;

        for (int i = 0; i < lines.length; i++) {
            String lineString = lines[i];

            Box rowContainer = Box.createHorizontalBox();

            rowContainer.add(createLineNumberPanel(i + 1, lineNumberBoxWidth));
            rowContainer.add(Box.createRigidArea(new Dimension(10, 1)));

            Pattern javaPattern = Pattern.compile("\"([^\"]*?(\\\\\")*)*?\"");
            Matcher matcher = javaPattern.matcher(lineString);
            while (matcher.find()) {
                StringBuffer sb = new StringBuffer();
                matcher.appendReplacement(sb, "");
                JTextField beforeMatch = createTextField(sb.toString());
                rowContainer.add(beforeMatch);

                JCheckBox matched = createCheckBox(matcher.group(0));
                rowContainer.add(matched);
                textComponentList.add(matched);
            }
            StringBuffer sb = new StringBuffer();
            matcher.appendTail(sb);
            JTextField tail = createTextField(sb.toString());
            rowContainer.add(tail);

            rowContainer.add(Box.createHorizontalGlue());

            c.gridy = i;
            codePanel.add(rowContainer, c);
        }

        JPanel padPanel = new JPanel();
        padPanel.setMinimumSize(new Dimension(0, 0));
        padPanel.setPreferredSize(new Dimension(0, 0));
        padPanel.setBackground(Color.white);
        c.gridy = lines.length;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1.0F;
        codePanel.add(padPanel, c);
    }

    @Override
    public void setCode(DigestedFile digestedFile) {
        codePanel.removeAll();
        textComponentList.clear();

        // the width of the maximum line number
        int lineNumberBoxWidth = CommonUtil.getFontMetrics(lineNumberFont).stringWidth(Integer.toString(digestedFile.getRowSize())) + 7;

        // the grid bag constraints
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1.0F;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;

        List<List<Component>> dataList = digestedFile.getDataList();
        for (int i = 0, iEnd = dataList.size(); i < iEnd; i++) {
            List<Component> row = dataList.get(i);

            Box rowContainer = Box.createHorizontalBox();

            rowContainer.add(createLineNumberPanel(i + 1, lineNumberBoxWidth));
            rowContainer.add(Box.createRigidArea(new Dimension(10, 1)));

            for (Component component : row) {
                switch (component.getType()) {
                    case CODE:
                        JTextField beforeMatch = createTextField(component.getContent());
                        rowContainer.add(beforeMatch);
                        break;
                    case TEXT:
                        JCheckBox matched = createCheckBox(component.getContent());
                        rowContainer.add(matched);
                        textComponentList.add(matched);
                        break;
                    case LANGUAGE:
                        break;
                }
            }

            rowContainer.add(Box.createHorizontalGlue());

            c.gridy = i;
            codePanel.add(rowContainer, c);
        }

        JPanel padPanel = new JPanel();
        padPanel.setMinimumSize(new Dimension(0, 0));
        padPanel.setPreferredSize(new Dimension(0, 0));
        padPanel.setBackground(Color.white);
        c.gridy = digestedFile.getRowSize();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1.0F;
        codePanel.add(padPanel, c);
    }

    /**
     * Create the line number GUI panel.
     * @param lineNumber the line number to show
     * @param panelWidth the fix width of the panel
     * @return the line number panel
     */
    protected JPanel createLineNumberPanel(int lineNumber, int panelWidth) {
        JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBackground(new Color(233, 232, 226));
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(184, 184, 184)));

        JLabel label = new JLabel();
        label.setText(Integer.toString(lineNumber));
        label.setFont(lineNumberFont);
        label.setOpaque(false);
        label.setHorizontalAlignment(JLabel.RIGHT);
        label.setPreferredSize(new Dimension(panelWidth, (int) label.getPreferredSize().getHeight()));
        label.setMinimumSize(label.getPreferredSize());
        label.setMaximumSize(new Dimension(panelWidth, 65535));
        panel.add(label);

        panel.add(Box.createRigidArea(new Dimension(2, 1)));

        return panel;
    }

    /**
     * Create the checkbox for the text.
     * @param text the text to be shown
     * @return the JCheckBox
     */
    protected JCheckBox createCheckBox(String text) {
        JCheckBox checkBox = new JCheckBox();

        //checkBox.setContentType("text/html");
        //checkBox.setText("<span style='font-family: Tahoma; font-size: 11pt; line-height: 9px; color: red;'>" + text + "</span>");
        checkBox.setText(text);
        checkBox.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
        checkBox.setForeground(Color.red);
        Dimension size = new Dimension((int) checkBox.getPreferredSize().getWidth(), 15);
        checkBox.setPreferredSize(size);
        checkBox.setMinimumSize(size);
        checkBox.setMaximumSize(size);
        checkBox.setOpaque(false);

        return checkBox;
    }

    /**
     * Create the textfield for the text.
     * @param text the text to be shown
     * @return the JTextField
     */
    protected JTextField createTextField(String text) {
        JTextField textField = new JTextField();

        textField.setText(text);
        textField.setBorder(null);
        Dimension size = new Dimension((int) textField.getPreferredSize().getWidth(), 15);
        textField.setPreferredSize(size);
        textField.setMinimumSize(size);
        textField.setMaximumSize(size);
        textField.setOpaque(false);
        textField.setEditable(false);

        return textField;
    }

    /**
     * For test purpose
     */
    public static void main(String[] args) throws IOException {
        CommonUtil.setLookAndFeel();

        Handler handler = new Handler();
        handler.addFile(new File("build.xml"));
        List<DigestedFile> digestedFileLis = handler.getDigestedData();

        SwingCodeViewer codePanel = new SwingCodeViewer();
        codePanel.setText(CommonUtil.readFile(new File("manifest.mf")));
        codePanel.setCode(digestedFileLis.get(0));

        JFrame frame = new JFrame();
        frame.setPreferredSize(new Dimension(800, 600));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(codePanel.getGUI());
        frame.pack();
        CommonUtil.centerWindow(frame);
        frame.setVisible(true);
    }
}
