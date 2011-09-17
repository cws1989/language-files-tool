package langfiles.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import langfiles.util.CommonUtil;
import langfiles.util.InterruptibleCharSequence;

/**
 * The regular expression tester panel.
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public class RegularExpressionTester {

    protected JTitledPanel panel;
    //
    protected PatternPanel patternPanel;
    protected PatternFlagsPanel patternFlagsPanel;
    protected JTextField replaceByInput;
    protected JTextArea contentInput;
    protected JScrollPane resultScrollPane;
    protected JPanel resultPanel;
    protected ButtonPanel buttonPanel;
    //
    protected ExecutorService threadExecutor;
    protected Future<?> currentTask;
    protected final Object lock = new Object();

    public RegularExpressionTester() {
        JTabbedPane tabbedPane = new JTabbedPane();

        //<editor-fold defaultstate="collapsed" desc="split pane">
        JSplitPane splitPane = new JSplitPane();
        splitPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        splitPane.setDividerSize(3);
        splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.5F);
        splitPane.setBackground(splitPane.getBackground().brighter());

        //<editor-fold defaultstate="collapsed" desc="tester content panel">
        JPanel testerContentPanel = new JPanel();
        testerContentPanel.setOpaque(false);
        testerContentPanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1.0F;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(3, 5, 0, 5);

        c.gridy = 0;

        //<editor-fold defaultstate="collapsed" desc="pattern">
        c.gridx = 0;
        c.weightx = 0.0F;
        testerContentPanel.add(new JLabel("Pattern: "), c);
        c.gridx = 1;
        c.weightx = 1.0F;
        testerContentPanel.add(patternPanel = new PatternPanel(), c);
        //</editor-fold>

        c.insets = new Insets(5, 5, 0, 5);
        c.gridy++;

        //<editor-fold defaultstate="collapsed" desc="pattern flag">
        c.gridx = 0;
        c.weightx = 0.0F;
        testerContentPanel.add(new JLabel(), c);
        c.gridx = 1;
        c.weightx = 1.0F;
        testerContentPanel.add(patternFlagsPanel = new PatternFlagsPanel(), c);
        //</editor-fold>

        c.gridy++;

        //<editor-fold defaultstate="collapsed" desc="replace by">
        c.gridx = 0;
        c.weightx = 0.0F;
        testerContentPanel.add(new JLabel("Replace By: "), c);
        c.gridx = 1;
        c.weightx = 1.0F;
        testerContentPanel.add(replaceByInput = new JTextField(), c);
        CommonUtil.setUndoManager(replaceByInput);
        //</editor-fold>

        c.gridy++;

        //<editor-fold defaultstate="collapsed" desc="replace by description">
        c.gridx = 0;
        c.weightx = 0.0F;
        testerContentPanel.add(new JLabel(), c);
        c.gridx = 1;
        c.weightx = 1.0F;
        JLabel leaveBlankDescriptionLabel = new JLabel("Leave blank will show all matched results");
        leaveBlankDescriptionLabel.setFont(CommonUtil.changeFontSize(leaveBlankDescriptionLabel.getFont(), leaveBlankDescriptionLabel.getFont().getSize() - 1));
        testerContentPanel.add(leaveBlankDescriptionLabel, c);
        //</editor-fold>

        c.gridy++;

        //<editor-fold defaultstate="collapsed" desc="replace by description">
        c.gridx = 0;
        c.weightx = 0.0F;
        testerContentPanel.add(new JLabel(), c);
        c.gridx = 1;
        c.weightx = 1.0F;
        JLabel replaceDescriptionLabel = new JLabel("\\0: whole pattern matched, \\1: first captured group, \\2 ...");
        replaceDescriptionLabel.setFont(CommonUtil.changeFontSize(replaceDescriptionLabel.getFont(), replaceDescriptionLabel.getFont().getSize() - 1));
        testerContentPanel.add(replaceDescriptionLabel, c);
        //</editor-fold>

        c.gridy++;

        //<editor-fold defaultstate="collapsed" desc="content">
        c.gridx = 0;
        c.weightx = 0.0F;
        c.anchor = GridBagConstraints.NORTH;
        testerContentPanel.add(new JLabel("Content: "), c);
        c.anchor = GridBagConstraints.CENTER;

        c.gridx = 1;
        c.weightx = 1.0F;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1.0F;
        contentInput = new JTextArea();
        contentInput.setFont(replaceByInput.getFont());
        CommonUtil.setUndoManager(contentInput);
        JScrollPane textScrollPane = new JScrollPane();
        textScrollPane.setViewportView(contentInput);
        testerContentPanel.add(textScrollPane, c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weighty = 0.0F;
        //</editor-fold>

        c.insets = new Insets(5, 5, 5, 5);
        c.gridy++;

        //<editor-fold defaultstate="collapsed" desc="button panel">
        c.gridx = 0;
        c.weightx = 1.0F;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.BOTH;
        testerContentPanel.add(buttonPanel = new ButtonPanel(), c);
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        //</editor-fold>

        testerContentPanel.setMinimumSize(new Dimension(testerContentPanel.getPreferredSize().width, 270));
        //</editor-fold>
        splitPane.setTopComponent(testerContentPanel);

        //<editor-fold defaultstate="collapsed" desc="tester result panel">
        JPanel testerResultPanel = new JPanel();
        testerResultPanel.setOpaque(false);
        testerResultPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 3, 5));
        testerResultPanel.setPreferredSize(new Dimension(100, 210));
        testerResultPanel.setLayout(new BoxLayout(testerResultPanel, BoxLayout.Y_AXIS));

        Box labelBox = Box.createHorizontalBox();
        JLabel resultLabel = new JLabel("Result: ");
        resultLabel.setAlignmentX(JLabel.LEADING);
        labelBox.add(resultLabel);
        labelBox.add(Box.createHorizontalGlue());
        testerResultPanel.add(labelBox);

        testerResultPanel.add(Box.createRigidArea(new Dimension(5, 5)));

        resultScrollPane = new JScrollPane();
        resultPanel = new JPanel();
        resultPanel.setLayout(new BorderLayout());
        resultScrollPane.setViewportView(resultPanel);
        testerResultPanel.add(resultScrollPane);
        //</editor-fold>
        splitPane.setBottomComponent(testerResultPanel);
        //</editor-fold>
        tabbedPane.addTab("Basic", splitPane);

        panel = new JTitledPanel();
        panel.setTitle("Regular Expression Tester", new ImageIcon(Toolkit.getDefaultToolkit().getImage(JTitledPanel.class.getResource("/langfiles/gui/images/RegularExpressionTester/logo.png"))));
        panel.setFooterPanelVisibility(false);
        panel.getContentPanel().setLayout(new BorderLayout());
        panel.getContentPanel().add(tabbedPane);
        panel.getContentPanel().setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 10));
    }

    public JTitledPanel getGUI() {
        return panel;
    }

    public void close() {
        synchronized (lock) {
            if (threadExecutor != null) {
                threadExecutor.shutdownNow();
                threadExecutor = null;
            }
        }
    }

    protected Window getParent() {
        Container target = panel;
        while ((target = target.getParent()) != null) {
            if (target instanceof Window) {
                return (Window) target;
            }
        }
        return null;
    }

    protected boolean runTask() {
        String patternString = patternPanel.getPattern();
        if (patternString.isEmpty()) {
            JOptionPane.showMessageDialog(getParent(), "Please input a pattern first");
            patternPanel.grabFocus();
            return false;
        }

        Pattern pattern = null;
        try {
            pattern = Pattern.compile(patternString, patternFlagsPanel.getPattern());
        } catch (PatternSyntaxException ex) {
            showPatternSyntaxError(ex.getMessage());
            patternPanel.grabFocus();
            return false;
        }
        final Pattern finalPattern = pattern;

        String contentString = contentInput.getText();
        if (contentString.isEmpty()) {
            JOptionPane.showMessageDialog(getParent(), "Please input content first");
            contentInput.grabFocus();
            return false;
        }

        execute(new Runnable() {

            @Override
            public void run() {
                String replaceByString = replaceByInput.getText();
                if (replaceByString.isEmpty()) {
                    JEditorPane resultPane = new JEditorPane();
                    resultPane.setEditable(false);
                    resultPane.setContentType("text/html");

                    StringBuilder resultString = new StringBuilder();
                    resultString.append("<html><div style='font-size: 12pt;'>");

                    Matcher matcher = finalPattern.matcher(new InterruptibleCharSequence(contentInput.getText()));
                    while (matcher.find()) {
                        resultString.append("<b>Matched:</b>");
                        resultString.append("<div>");
                        resultString.append(htmlTreatment(matcher.group(0)));
                        resultString.append("</div>");
                        if (matcher.groupCount() > 0) {
                            resultString.append("<div style='padding-left: 10px;'>");
                            for (int i = 1, iEnd = matcher.groupCount(); i <= iEnd; i++) {
                                resultString.append("<b>Match ");
                                resultString.append(i);
                                resultString.append(":</b>");

                                resultString.append("<div>");
                                resultString.append(htmlTreatment(matcher.group(i)));
                                resultString.append("</div>");
                            }
                            resultString.append("</div>");
                        }
                    }

                    resultPane.setText(resultString.toString());
                    resultPanel.removeAll();
                    resultPanel.add(resultPane, BorderLayout.CENTER);
                } else {
                    JTextArea resultArea = new JTextArea();
                    resultArea.setFont(replaceByInput.getFont());
                    resultArea.setEditable(false);

                    JScrollPane resultResultPane = new JScrollPane();
                    resultResultPane.setBorder(null);
                    resultResultPane.setViewportView(resultArea);

                    StringBuffer resultString = new StringBuffer();

                    Matcher matcher = finalPattern.matcher(new InterruptibleCharSequence(contentInput.getText()));
                    while (matcher.find()) {
                        String replacement = replaceByString;
                        for (int i = 0, iEnd = matcher.groupCount(); i <= iEnd; i++) {
                            replacement = replacement.replace("\\" + i, matcher.group(i).replace("\\", "\\\\"));
                        }
                        matcher.appendReplacement(resultString, replacement);
                    }
                    matcher.appendTail(resultString);

                    resultArea.setText(resultString.toString());
                    resultPanel.removeAll();
                    resultPanel.add(resultResultPane, BorderLayout.CENTER);
                }

                buttonPanel.taskFinished();
                JOptionPane.showMessageDialog(getParent(), "Execution Complete");
            }
        });

        return true;
    }

    protected String htmlTreatment(String data) {
        return data == null || data.isEmpty() ? "" : data.replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br />");
    }

    protected void showPatternSyntaxError(String errorMessage) {
        final JDialog dialog = new JDialog(getParent(), "Pattern Syntax Error", Dialog.ModalityType.APPLICATION_MODAL);

        JPanel messagePanel = new JPanel();
        messagePanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 5, 15));
        messagePanel.setLayout(new BorderLayout());

        //<editor-fold defaultstate="collapsed" desc="title panel">
        JPanel titlePanel = new JPanel();
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 7, 0));
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.X_AXIS));

        JLabel titleLabel = new JLabel("Pattern Syntax Error:");
        titleLabel.setFont(CommonUtil.deriveFont(titleLabel.getFont(), true, 14));

        titlePanel.add(titleLabel);
        titlePanel.add(Box.createHorizontalGlue());
        //</editor-fold>
        messagePanel.add(titlePanel, BorderLayout.NORTH);

        //<editor-fold defaultstate="collapsed" desc="message">
        JScrollPane messageScrollPane = new JScrollPane();
        messageScrollPane.setBorder(null);
        messageScrollPane.setOpaque(false);
        messageScrollPane.getViewport().setOpaque(false);

        JTextArea messageArea = new JTextArea();
        messageArea.setText(errorMessage);
        messageArea.setOpaque(false);
        messageArea.setEditable(false);
        messageArea.setCursor(Cursor.getDefaultCursor());
        messageArea.setFont(new Font("Courier New", Font.PLAIN, 12));

        messageScrollPane.setViewportView(messageArea);
        //</editor-fold>
        messagePanel.add(messageScrollPane, BorderLayout.CENTER);

        //<editor-fold defaultstate="collapsed" desc="button panel">
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });

        JPanel confirmButtonPanel = new JPanel();
        confirmButtonPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        confirmButtonPanel.setOpaque(false);
        confirmButtonPanel.setLayout(new BoxLayout(confirmButtonPanel, BoxLayout.X_AXIS));
        confirmButtonPanel.add(Box.createHorizontalGlue());
        confirmButtonPanel.add(okButton);
        confirmButtonPanel.add(Box.createHorizontalGlue());
        //</editor-fold>
        messagePanel.add(confirmButtonPanel, BorderLayout.SOUTH);

        dialog.add(messagePanel);
        dialog.setResizable(false);
        dialog.pack();
        dialog.setLocationByPlatform(true);
        dialog.setVisible(true);
    }

    protected void reset() {
        cancelTask();
        patternPanel.reset();
        patternFlagsPanel.reset();
        replaceByInput.setText("");
        contentInput.setText("");
        patternPanel.grabFocus();
        resultPanel.removeAll();
        resultPanel.repaint();
    }

    protected void cancelTask() {
        synchronized (lock) {
            if (currentTask != null) {
                currentTask.cancel(true);
                currentTask = null;
            }
        }
    }

    protected void execute(Runnable task) {
        synchronized (lock) {
            if (threadExecutor == null) {
                threadExecutor = Executors.newSingleThreadExecutor();
            }
            if (currentTask != null) {
                currentTask.cancel(true);
                currentTask = null;
            }
            currentTask = threadExecutor.submit(task);
        }
    }

    protected class PatternPanel extends JPanel {

        private static final long serialVersionUID = 1L;
        protected JTextField patternInput;

        protected PatternPanel() {
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

            add(patternInput = new JTextField());
            CommonUtil.setUndoManager(patternInput);
            add(Box.createRigidArea(new Dimension(2, 1)));

            JButton referenceButton = new JButton(new ImageIcon(Toolkit.getDefaultToolkit().getImage(JTitledPanel.class.getResource("/langfiles/gui/images/RegularExpressionTester/pattern_reference.png"))));
            referenceButton.setOpaque(false);
            referenceButton.setMargin(new Insets(0, 1, 0, 1));
            referenceButton.setToolTipText("Open reference website");
            referenceButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    String referenceWebsite = "http://download.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html";
                    Desktop desktop = Desktop.getDesktop();
                    if (desktop.isSupported(Desktop.Action.BROWSE)) {
                        try {
                            desktop.browse(new URI(referenceWebsite));
                        } catch (IOException ex) {
                            Logger.getLogger(RegularExpressionTester.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (URISyntaxException ex) {
                            Logger.getLogger(RegularExpressionTester.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        JOptionPane.showMessageDialog(getParent(), "Failed to open the reference website: " + referenceWebsite);
                    }
                }
            });
            add(referenceButton);
        }

        public String getPattern() {
            return patternInput.getText();
        }

        public void reset() {
            patternInput.setText("");
        }

        @Override
        public void grabFocus() {
            patternInput.grabFocus();
        }
    }

    protected class PatternFlagsPanel extends JPanel {

        private static final long serialVersionUID = 1L;
        protected JCheckBox caseInsensitive;
        protected JCheckBox multiline;
        protected JCheckBox dotall;
        protected JCheckBox unicodeCast;
        protected JCheckBox canonEq;
        protected JCheckBox unixLines;
        protected JCheckBox literal;
        protected JCheckBox comments;

        protected PatternFlagsPanel() {
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            Box firstLine = Box.createHorizontalBox();
            Box secondLine = Box.createHorizontalBox();

            // explanation copied from http://download.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html
            caseInsensitive = new JCheckBox("Case Insensitive");
            caseInsensitive.setOpaque(false);
            CommonUtil.setTooltipDismissDelay(caseInsensitive, 999999);
            caseInsensitive.setToolTipText("<html><p>Enables case-insensitive matching.</p>"
                    + "<p>By default, case-insensitive matching assumes that only characters in the US-ASCII charset are being matched.<br />Unicode-aware case-insensitive matching can be enabled by specifying the UNICODE_CASE flag in conjunction with this flag.</p>"
                    + "<p>Case-insensitive matching can also be enabled via the embedded flag expression (?i).</p></html>");
            multiline = new JCheckBox("Multiline");
            multiline.setOpaque(false);
            CommonUtil.setTooltipDismissDelay(multiline, 999999);
            multiline.setToolTipText("<html><p>Enables multiline mode.</p>"
                    + "<p>In multiline mode the expressions ^ and $ match just after or just before, respectively, a line terminator or the end of the input sequence.<br />By default these expressions only match at the beginning and the end of the entire input sequence.</p>"
                    + "<p>Multiline mode can also be enabled via the embedded flag expression (?m).</p></html>");
            dotall = new JCheckBox("Dotall");
            dotall.setOpaque(false);
            CommonUtil.setTooltipDismissDelay(dotall, 999999);
            dotall.setToolTipText("<html><p>Enables dotall mode.</p>"
                    + "<p>In dotall mode, the expression . matches any character, including a line terminator. By default this expression does not match line terminators.</p>"
                    + "<p>Dotall mode can also be enabled via the embedded flag expression (?s). (The s is a mnemonic for \"single-line\" mode, which is what this is called in Perl.)</p></html>");
            unicodeCast = new JCheckBox("Unicode Cast");
            unicodeCast.setOpaque(false);
            CommonUtil.setTooltipDismissDelay(unicodeCast, 999999);
            unicodeCast.setToolTipText("<html><p>Enables Unicode-aware case folding.</p>"
                    + "<p>When this flag is specified then case-insensitive matching, when enabled by the CASE_INSENSITIVE flag, is done in a manner consistent with the Unicode Standard.<br />By default, case-insensitive matching assumes that only characters in the US-ASCII charset are being matched.</p>"
                    + "<p>Unicode-aware case folding can also be enabled via the embedded flag expression (?u).</p></html>");
            canonEq = new JCheckBox("Canon. Eq.");
            canonEq.setOpaque(false);
            CommonUtil.setTooltipDismissDelay(canonEq, 999999);
            canonEq.setToolTipText("<html><p>Enables canonical equivalence.</p>"
                    + "<p>When this flag is specified then two characters will be considered to match if, and only if, their full canonical decompositions match.<br />The expression \"a\u030A\", for example, will match the string \"å\" when this flag is specified. By default, matching does not take canonical equivalence into account.</p>"
                    + "<p>There is no embedded flag character for enabling canonical equivalence.</p></html>");
            unixLines = new JCheckBox("Unix Lines.");
            unixLines.setOpaque(false);
            CommonUtil.setTooltipDismissDelay(unixLines, 999999);
            unixLines.setToolTipText("<html><p>Enables Unix lines mode.</p>"
                    + "<p>In this mode, only the '\n' line terminator is recognized in the behavior of ., ^, and $.</p>"
                    + "<p>Unix lines mode can also be enabled via the embedded flag expression (?d).</p></html>");
            literal = new JCheckBox("Literal");
            literal.setOpaque(false);
            CommonUtil.setTooltipDismissDelay(literal, 999999);
            literal.setToolTipText("<html><p>Enables literal parsing of the pattern.</p>"
                    + "<p>When this flag is specified then the input string that specifies the pattern is treated as a sequence of literal characters.<br />Metacharacters or escape sequences in the input sequence will be given no special meaning.</p>"
                    + "<p>The flags CASE_INSENSITIVE and UNICODE_CASE retain their impact on matching when used in conjunction with this flag. The other flags become superfluous.</p></html>");
            comments = new JCheckBox("Comments");
            comments.setOpaque(false);
            CommonUtil.setTooltipDismissDelay(comments, 999999);
            comments.setToolTipText("<html><p>Permits whitespace and comments in pattern.</p>"
                    + "<p>In this mode, whitespace is ignored, and embedded comments starting with # are ignored until the end of a line.</p>"
                    + "<p>Comments mode can also be enabled via the embedded flag expression (?x).</p></html>");

            firstLine.add(caseInsensitive);
            firstLine.add(multiline);
            firstLine.add(dotall);
            firstLine.add(unicodeCast);
            firstLine.add(Box.createHorizontalGlue());
            secondLine.add(canonEq);
            secondLine.add(unixLines);
            secondLine.add(literal);
            secondLine.add(comments);
            secondLine.add(Box.createHorizontalGlue());

            add(firstLine);
            add(secondLine);
        }

        public int getPattern() {
            return (caseInsensitive.isSelected() ? Pattern.CASE_INSENSITIVE & 0xffff : 0)
                    | (multiline.isSelected() ? Pattern.MULTILINE : 0)
                    | (dotall.isSelected() ? Pattern.DOTALL : 0)
                    | (unicodeCast.isSelected() ? Pattern.UNICODE_CASE : 0)
                    | (canonEq.isSelected() ? Pattern.CANON_EQ : 0)
                    | (unixLines.isSelected() ? Pattern.UNIX_LINES : 0)
                    | (literal.isSelected() ? Pattern.LITERAL : 0)
                    | (comments.isSelected() ? Pattern.COMMENTS : 0);
        }

        public void reset() {
            caseInsensitive.setSelected(false);
            multiline.setSelected(false);
            dotall.setSelected(false);
            unicodeCast.setSelected(false);
            canonEq.setSelected(false);
            unixLines.setSelected(false);
            literal.setSelected(false);
            comments.setSelected(false);
        }
    }

    protected class ButtonPanel extends JPanel {

        private static final long serialVersionUID = 1L;
        protected JButton testButton;
        protected JButton resetButton;
        protected ActionListener actionListener;
        protected boolean taskRunning = false;

        protected ButtonPanel() {
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

            actionListener = new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    String cmd = e.getActionCommand();
                    if (cmd.equals("test")) {
                        if (taskRunning) {
                            cancelTask();
                            testButton.setText("Test");
                            taskRunning = false;
                        } else {
                            boolean taskStarted = runTask();
                            if (taskStarted) {
                                testButton.setText("Cancel");
                                taskRunning = true;
                            }
                        }
                    } else if (cmd.equals("reset")) {
                        reset();
                        testButton.setText("Test");
                    }
                }
            };

            add(Box.createHorizontalGlue());
            testButton = new JButton("Test");
            testButton.setActionCommand("test");
            testButton.addActionListener(actionListener);
            add(testButton);

            add(Box.createRigidArea(new Dimension(5, 5)));
            resetButton = new JButton("Reset");
            resetButton.setActionCommand("reset");
            resetButton.addActionListener(actionListener);
            add(resetButton);
        }

        public void taskFinished() {
            testButton.setText("Test");
            taskRunning = false;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (threadExecutor != null) {
            threadExecutor.shutdownNow();
            Logger.getLogger(RegularExpressionTester.class.getName()).log(Level.SEVERE, "Regular Expression Tester not closed (invoke close()) properly.");
        }
    }

    /**
     * For test/development purpose.
     */
    public static void main(String[] args) {
        CommonUtil.setLookAndFeel();

        RegularExpressionTester tester = new RegularExpressionTester();

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(tester.getGUI());
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }
}
