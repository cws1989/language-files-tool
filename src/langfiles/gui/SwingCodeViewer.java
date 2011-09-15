package langfiles.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
import langfiles.project.CodeViewer;
import langfiles.project.DigestedFile;
import langfiles.project.DigestedFile.Component;
import langfiles.project.Project;

/**
 * The code viewer.
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public class SwingCodeViewer implements CodeViewer {

    /**
     * The font of the line number text.
     */
    protected Font lineNumberFont = new Font("Verdana", Font.PLAIN, 10);
    protected JPanel guiPanel;
    /**
     * The scroll pane that contain the codePanel.
     */
    protected JScrollPane scrollPane;
    /**
     * The GUI panel of the code viewer.
     */
    protected JPanel codePanel;
    protected JPanel rowHeaderPanel;
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
        codePanel.setBackground(codePanel.getBackground().brighter());
        codePanel.setBorder(null);

        rowHeaderPanel = new JPanel();
        rowHeaderPanel.setLayout(new GridBagLayout());

        scrollPane = new JScrollPane();
        scrollPane.setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(20);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setViewportView(codePanel);
        scrollPane.setBorder(null);
        scrollPane.setRowHeaderView(rowHeaderPanel);

        guiPanel = new JPanel();
        guiPanel.setLayout(new BorderLayout());
        guiPanel.add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Get the GUI display panel.
     * @return the GUI component
     */
    @Override
    public JPanel getGUI() {
        return guiPanel;
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

            //rowContainer.add(createLineNumberPanel(i + 1, lineNumberBoxWidth));
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
            rowHeaderPanel.add(createLineNumberPanel(i + 1, lineNumberBoxWidth, rowContainer.getPreferredSize().height), c);
            codePanel.add(rowContainer, c);
        }

        c.gridy = digestedFile.getRowSize();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1.0F;

        JPanel padPanel = new JPanel();
        padPanel.setMinimumSize(new Dimension(0, 0));
        padPanel.setPreferredSize(new Dimension(0, 0));
        padPanel.setOpaque(false);
        codePanel.add(padPanel, c);

        padPanel = new JPanel();
        padPanel.setMinimumSize(new Dimension(0, 0));
        padPanel.setPreferredSize(new Dimension(0, 0));
        padPanel.setOpaque(false);
        rowHeaderPanel.add(padPanel, c);
    }

    /**
     * Create the line number GUI panel.
     * @param lineNumber the line number to show
     * @param panelWidth the fix width of the panel
     * @return the line number panel
     */
    protected JPanel createLineNumberPanel(int lineNumber, int panelWidth, int panelHeight) {
        JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBackground(new Color(233, 232, 226));
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(184, 184, 184)));

        JLabel label = new JLabel();
        label.setText(Integer.toString(lineNumber));
        label.setFont(lineNumberFont);
        label.setOpaque(false);
        label.setHorizontalAlignment(JLabel.RIGHT);
        label.setPreferredSize(new Dimension(panelWidth, panelHeight));
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
        Dimension size = new Dimension(checkBox.getPreferredSize().width, 15);
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
        // bad +1
        Dimension size = new Dimension(textField.getPreferredSize().width + 1, 15);
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

        Project handler = new Project("Project Name");
        handler.addFile(new File("build.xml"));
        List<DigestedFile> digestedFileList = handler.getDigestedData();

        SwingCodeViewer codePanel = new SwingCodeViewer();
        codePanel.setCode(digestedFileList.get(0));

        JFrame frame = new JFrame();
        frame.setPreferredSize(new Dimension(800, 600));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(codePanel.getGUI());
        frame.pack();
        CommonUtil.centerWindow(frame);
        frame.setVisible(true);
    }
}
