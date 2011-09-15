package langfiles.gui;

import java.awt.BorderLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import langfiles.Main;
import langfiles.project.CodeViewer;
import langfiles.project.DigestedFile;
import langfiles.project.Project;
import langfiles.util.Config;

/**
 * The code panel.
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public class CodePanel {

    /**
     * Main
     */
    private Main main;
    /**
     * MainWindow
     */
    private MainWindow mainWindow;
    /**
     * The project panel.
     */
    private JPanel codePanel;
    /**
     * The tabbed pane that place the code panels.
     */
    private JTabbedPane tabbedPane;
    private List<String> lastOpenedRecordList;
    private String lastSelectedTabFileAbsolutePath;
    protected ExecutorService threadExecutor;

    /**
     * Constructor.
     */
    public CodePanel(MainWindow mainWindow) {
        this.main = Main.getInstance();
        this.mainWindow = mainWindow;
        this.threadExecutor = Executors.newSingleThreadExecutor();

        tabbedPane = new JTabbedPane();
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.setFocusable(false);

        codePanel = new JPanel();
        codePanel.setLayout(new BorderLayout());
        codePanel.setBackground(codePanel.getBackground().brighter());
        codePanel.add(tabbedPane, BorderLayout.CENTER);

        mainWindow.addMainWindowEventListener(new MainWindowEventListener() {

            @Override
            public boolean windowCanCloseNow(ChangeEvent event) {
                return true;
            }

            @Override
            public void windowIsClosing(ChangeEvent event) {
                threadExecutor.shutdownNow();
            }
        });

        main.addShutdownEvent(100, new Runnable() {

            @Override
            public void run() {
                StringBuilder sb = new StringBuilder();
                for (int i = 0, iEnd = tabbedPane.getTabCount(); i < iEnd; i++) {
                    if (sb.length() != 0) {
                        sb.append("\t");
                    }
                    CodePanelTab codePanelTab = (CodePanelTab) tabbedPane.getComponentAt(i);
                    sb.append(codePanelTab.getDigestedFile().getFile().getAbsolutePath());
                }

                String selectedTab = "";
                CodePanelTab codePanelTab = (CodePanelTab) tabbedPane.getSelectedComponent();
                if (codePanelTab != null) {
                    selectedTab = codePanelTab.getDigestedFile().getFile().getAbsolutePath();
                }

                Config config = main.getConfig();
                config.setProperty("code_panel", sb.toString());
                config.setProperty("code_panel_selected", selectedTab);
            }
        });

        Config config = main.getConfig();
        String codePanelRecordString = config.getProperty("code_panel");
        if (codePanelRecordString != null) {
            lastOpenedRecordList = new ArrayList(Arrays.asList(codePanelRecordString.split("\t")));
        } else {
            lastOpenedRecordList = new ArrayList();
        }
        lastSelectedTabFileAbsolutePath = config.getProperty("code_panel_selected");

        // add global listener to listen to ctrl + w to close current selected tab
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {

            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (e.getID() == KeyEvent.KEY_PRESSED && e.isControlDown() && e.getKeyCode() == KeyEvent.VK_W) {
                    closeSelectedTab();
                }
                return false;
            }
        });
    }

    /**
     * Get the GUI display panel.
     * @return the GUI component
     */
    public JPanel getGUI() {
        return codePanel;
    }

    /**
     * Add file to code panel.
     * @param digestedFile the file to add to code panel
     */
    public final void add(final DigestedFile digestedFile, final boolean getFocus) {
        String filePath = digestedFile.getFile().getAbsolutePath();
        boolean fileExistAlready = false;
        for (int i = 0, iEnd = tabbedPane.getTabCount(); i < iEnd; i++) {
            CodePanelTab codePanelTab = (CodePanelTab) tabbedPane.getComponentAt(i);
            if (codePanelTab.getDigestedFile().getFile().getAbsolutePath().equals(filePath)) {
                tabbedPane.setSelectedIndex(i);
                fileExistAlready = true;
                break;
            }
        }
        if (fileExistAlready) {
            return;
        }

        SwingCodeViewer codeViewer = new SwingCodeViewer();
        codeViewer.setCode(digestedFile);

        final CodePanelTab codePanelTab = new CodePanelTab(codeViewer, digestedFile);

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                tabbedPane.addTab(digestedFile.getFile().getName(), codePanelTab);
                tabbedPane.setTabComponentAt(tabbedPane.getTabCount() - 1, new JTabComponent(tabbedPane));
                if (getFocus
                        | (lastSelectedTabFileAbsolutePath != null && lastSelectedTabFileAbsolutePath.equals(digestedFile.getFile().getAbsolutePath()))) {
                    tabbedPane.setSelectedComponent(codePanelTab);
                }
            }
        });
    }

    public final void setSelectedTabByFileAbsolutePath(String path) {
        for (int i = 0, iEnd = tabbedPane.getTabCount(); i < iEnd; i++) {
            CodePanelTab codePanelTab = (CodePanelTab) tabbedPane.getComponentAt(i);
            if (codePanelTab.getDigestedFile().getFile().getAbsolutePath().equals(path)) {
                tabbedPane.setSelectedIndex(i);
                break;
            }
        }
    }

    public void addProject(Project project) {
        threadExecutor.execute(new Runnable() {

            @Override
            public void run() {
                recheckLastOpenedFile();
            }
        });
    }

    public void closeSelectedTab() {
        int selectedTabIndex = tabbedPane.getSelectedIndex();
        if (selectedTabIndex != -1) {
            tabbedPane.removeTabAt(tabbedPane.getSelectedIndex());
        }
    }

    private void recheckLastOpenedFile() {
        Iterator<String> iterator = (lastOpenedRecordList).iterator();
        while (iterator.hasNext()) {
            String codePanelRecord = iterator.next();
            DigestedFile digestedFile = mainWindow.getDigestedFileByAbsolutePath(codePanelRecord);
            if (digestedFile != null) {
                add(digestedFile, false);
                iterator.remove();
            }
        }
    }

    private class CodePanelTab extends JPanel {

        private static final long serialVersionUID = 1L;
        private CodeViewer codeViewer;
        private DigestedFile digestedFile;

        private CodePanelTab(CodeViewer codeViewer, DigestedFile digestedFile) {
            setLayout(new BorderLayout());

            add(codeViewer.getGUI(), BorderLayout.CENTER);

            this.codeViewer = codeViewer;
            this.digestedFile = digestedFile;
        }

        public CodeViewer getCodeViewer() {
            return codeViewer;
        }

        public DigestedFile getDigestedFile() {
            return digestedFile;
        }
    }
}
