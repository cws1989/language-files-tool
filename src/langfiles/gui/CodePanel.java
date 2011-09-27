package langfiles.gui;

import langfiles.gui.component.JTabComponent;
import java.awt.BorderLayout;
import java.awt.Component;
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
import langfiles.project.Project;
import langfiles.util.Config;
import langfiles.util.SyncFile;
import langfiles.util.SyncFileListener;

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
        main = Main.getInstance();

        this.mainWindow = mainWindow;
        threadExecutor = Executors.newSingleThreadExecutor();

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
                    sb.append(codePanelTab.getSyncFile().getFile().getAbsolutePath());
                }

                String selectedTab = "";
                CodePanelTab codePanelTab = (CodePanelTab) tabbedPane.getSelectedComponent();
                if (codePanelTab != null) {
                    selectedTab = codePanelTab.getSyncFile().getFile().getAbsolutePath();
                }

                Config preference = main.getPreference();
                preference.setProperty("code_panel/opened", sb.toString());
                preference.setProperty("code_panel/selected", selectedTab);
            }
        });

        Config preference = main.getPreference();
        String codePanelRecordString = preference.getProperty("code_panel/opened");
        lastOpenedRecordList = codePanelRecordString != null ? new ArrayList<String>(Arrays.asList(codePanelRecordString.split("\t"))) : new ArrayList<String>();
        lastSelectedTabFileAbsolutePath = preference.getProperty("code_panel/selected");

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
     * @param syncFile the file to add to code panel
     */
    public final void add(final SyncFile syncFile, final boolean getFocus) {
        String filePath = syncFile.getFile().getAbsolutePath();
        boolean fileExistAlready = false;
        for (int i = 0, iEnd = tabbedPane.getTabCount(); i < iEnd; i++) {
            CodePanelTab codePanelTab = (CodePanelTab) tabbedPane.getComponentAt(i);
            if (codePanelTab.getSyncFile().getFile().getAbsolutePath().equals(filePath)) {
                tabbedPane.setSelectedIndex(i);
                fileExistAlready = true;
                break;
            }
        }
        if (fileExistAlready) {
            return;
        }

        final CodeViewer codeViewer = new SyntaxHighlightedCodeViewer();
        codeViewer.setCode(syncFile);

        SyncFileListener digestedFileListener = new SyncFileListener() {

            @Override
            public void fileCreated(SyncFile directory, SyncFile fileCreated, String rootPath, String name) {
                // should not have this event
            }

            @Override
            public void fileDeleted(SyncFile fileDeleted, String rootPath, String name) {
                CodePanelTab codePanelTab = (CodePanelTab) fileDeleted.getUserObject("codePanelTab");
                if (codePanelTab != null) {
                    tabbedPane.remove(codePanelTab);
                    codePanelTab.close();
                }
            }

            @Override
            public void fileModified(SyncFile fileModified, String rootPath, String name) {
//                codeViewer.setCode(fileModified);
            }

            @Override
            public void fileRenamed(SyncFile fileRenamed, String rootPath, String oldName, String newName) {
                CodePanelTab codePanelTab = (CodePanelTab) fileRenamed.getUserObject("codePanelTab");
                if (codePanelTab != null) {
                    tabbedPane.setTitleAt(tabbedPane.indexOfComponent(codePanelTab), fileRenamed.getFile().getName());
                }
            }
        };
        syncFile.addListener(digestedFileListener);

        final CodePanelTab codePanelTab = new CodePanelTab(codeViewer, syncFile, digestedFileListener);
        syncFile.setUserObject("codePanelTab", codePanelTab);

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                tabbedPane.addTab(syncFile.getFile().getName(), codePanelTab);
                JTabComponent tabComponent = new JTabComponent(tabbedPane);
                tabComponent.addTabComponentListener(new TabComponentListener() {

                    @Override
                    public void tabClosed(Component tab, JTabComponent tabComponent) {
                        CodePanelTab codePanelTab = (CodePanelTab) tab;
                        codePanelTab.close();
                    }
                });
                tabbedPane.setTabComponentAt(tabbedPane.getTabCount() - 1, tabComponent);
                if (getFocus
                        | (lastSelectedTabFileAbsolutePath != null && lastSelectedTabFileAbsolutePath.equals(syncFile.getFile().getAbsolutePath()))) {
                    tabbedPane.setSelectedComponent(codePanelTab);
                }
            }
        });
    }

    public final void setSelectedTabByFileAbsolutePath(String path) {
        for (int i = 0, iEnd = tabbedPane.getTabCount(); i < iEnd; i++) {
            CodePanelTab codePanelTab = (CodePanelTab) tabbedPane.getComponentAt(i);
            if (codePanelTab.getSyncFile().getFile().getAbsolutePath().equals(path)) {
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
            CodePanelTab codePanelTab = (CodePanelTab) tabbedPane.getSelectedComponent();
            codePanelTab.close();
            tabbedPane.removeTabAt(tabbedPane.getSelectedIndex());
        }
    }

    private void recheckLastOpenedFile() {
        Iterator<String> iterator = (lastOpenedRecordList).iterator();
//        long t1, t2;
//        t1 = System.currentTimeMillis();
        while (iterator.hasNext()) {
            String codePanelRecord = iterator.next();
            SyncFile syncFile = mainWindow.getSyncFileByAbsolutePath(codePanelRecord);
            if (syncFile != null && !syncFile.isDirectory()) {
                add(syncFile, false);
                iterator.remove();
            }
        }
//        t2 = System.currentTimeMillis();
//        System.out.println((t2 - t1));
    }

    private static class CodePanelTab extends JPanel {

        private static final long serialVersionUID = 1L;
        private CodeViewer codeViewer;
        private SyncFile syncFile;
        private SyncFileListener digestedFileListener;

        private CodePanelTab(CodeViewer codeViewer, SyncFile syncFile, SyncFileListener syncFileListener) {
            this.codeViewer = codeViewer;
            this.syncFile = syncFile;
            this.digestedFileListener = syncFileListener;
            setLayout(new BorderLayout());
            add(codeViewer.getGUI(), BorderLayout.CENTER);
        }

        public void close() {
            // may invoke this when iterating digestedFile's listeners, cannot remove listener
//            digestedFile.removeListener(digestedFileListener);
        }

        public CodeViewer getCodeViewer() {
            return codeViewer;
        }

        public SyncFile getSyncFile() {
            return syncFile;
        }

        public SyncFileListener getSyncFileListener() {
            return digestedFileListener;
        }
    }
}
