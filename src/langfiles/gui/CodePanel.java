package langfiles.gui;

import langfiles.gui.component.JTabComponentListener;
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
import langfiles.project.ProjectListener;
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
    private final List<String> lastOpenedRecordList;
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

        mainWindow.addProjectEventListener(new ProjectListener() {

            @Override
            public void projectAdded(Project project) {
                addProject(project);
            }

            @Override
            public void projectRemoved(Project project) {
                removeProject(project);
            }
        });

        mainWindow.addMainWindowEventListener(new MainWindowEventListener() {

            @Override
            public boolean windowCanCloseNow(ChangeEvent event) {
                return true;
            }

            @Override
            public void windowIsClosing(ChangeEvent event) {
                // must be add to window close event but not shutdown hook
                threadExecutor.shutdownNow();
            }
        });

        // add shutdown hook to record opened and selected tab
        // if allow multiple MainWindow, need to change this to MainWindow close hook
        main.addShutdownEvent(100, new Runnable() {

            @Override
            public void run() {
                // record opened tab
                StringBuilder sb = new StringBuilder();
                for (int i = 0, iEnd = tabbedPane.getTabCount(); i < iEnd; i++) {
                    if (sb.length() != 0) {
                        sb.append("\t");
                    }
                    CodePanelTab codePanelTab = (CodePanelTab) tabbedPane.getComponentAt(i);
                    sb.append(codePanelTab.getSyncFile().getAbsolutePath());
                }

                // record selected tab
                String selectedTab = "";
                CodePanelTab codePanelTab = (CodePanelTab) tabbedPane.getSelectedComponent();
                if (codePanelTab != null) {
                    selectedTab = codePanelTab.getSyncFile().getAbsolutePath();
                }

                // save record
                Config preference = main.getPreference();
                preference.setProperty("code_panel/opened", sb.toString());
                preference.setProperty("code_panel/selected", selectedTab);
            }
        });

        // read opened tab and selected tab from record
        Config preference = main.getPreference();
        // opened
        String codePanelRecordString = preference.getProperty("code_panel/opened");
        lastOpenedRecordList = codePanelRecordString != null ? new ArrayList<String>(Arrays.asList(codePanelRecordString.split("\t"))) : new ArrayList<String>();
        // selected
        lastSelectedTabFileAbsolutePath = preference.getProperty("code_panel/selected");

        // add global listener to listen to ctrl + w to close current selected tab
        // if allow multiple MainWindow, need to remove this listener before/after window closed, also determine whether current frame is active frame (may move to MainWindow)
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
    public void add(final SyncFile syncFile, final boolean getFocus) {
        String filePath = syncFile.getAbsolutePath();
        for (int i = 0, iEnd = tabbedPane.getTabCount(); i < iEnd; i++) {
            CodePanelTab codePanelTab = (CodePanelTab) tabbedPane.getComponentAt(i);
            if (codePanelTab.getSyncFile().getAbsolutePath().equals(filePath)) {
                tabbedPane.setSelectedIndex(i);
                return;
            }
        }

        final CodeViewer codeViewer = new SyntaxHighlightedCodeViewer();
        codeViewer.setCode(syncFile);

        //<editor-fold defaultstate="collapsed" desc="syncFileListener">
        SyncFileListener syncFileListener = new SyncFileListener() {

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
                codeViewer.setCode(fileModified);
            }

            @Override
            public void fileRenamed(SyncFile fileRenamed, String rootPath, String oldName, String newName) {
                CodePanelTab codePanelTab = (CodePanelTab) fileRenamed.getUserObject("codePanelTab");
                if (codePanelTab != null) {
                    tabbedPane.setTitleAt(tabbedPane.indexOfComponent(codePanelTab), fileRenamed.getFileName());
                }
            }
        };
        syncFile.addListener(syncFileListener);
        //</editor-fold>

        final CodePanelTab codePanelTab = new CodePanelTab(codeViewer, syncFile, syncFileListener);
        syncFile.setUserObject("codePanelTab", codePanelTab);

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                JTabComponent tabComponent = new JTabComponent(tabbedPane);
                tabComponent.addTabComponentListener(new JTabComponentListener() {

                    @Override
                    public void tabClosed(Component tab, JTabComponent tabComponent) {
                        ((CodePanelTab) tab).close();
                    }
                });

                tabbedPane.addTab(syncFile.getFileName(), codePanelTab);
                tabbedPane.setTabComponentAt(tabbedPane.getTabCount() - 1, tabComponent);
                // check with selected tab record, if matched, switch to it
                // should be used at startup only
                if (getFocus
                        | (lastSelectedTabFileAbsolutePath != null && lastSelectedTabFileAbsolutePath.equals(syncFile.getAbsolutePath()))) {
                    lastSelectedTabFileAbsolutePath = null;
                    tabbedPane.setSelectedComponent(codePanelTab);
                }
            }
        });
    }

//    public void setSelectedTabByFileAbsolutePath(String path) {
//        for (int i = 0, iEnd = tabbedPane.getTabCount(); i < iEnd; i++) {
//            CodePanelTab codePanelTab = (CodePanelTab) tabbedPane.getComponentAt(i);
//            if (codePanelTab.getSyncFile().getAbsolutePath().equals(path)) {
//                tabbedPane.setSelectedIndex(i);
//                break;
//            }
//        }
//    }
    public void addProject(final Project project) {
        threadExecutor.execute(new Runnable() {

            @Override
            public void run() {
                // be used at startup
                synchronized (lastOpenedRecordList) {
                    Iterator<String> iterator = lastOpenedRecordList.iterator();
                    while (iterator.hasNext()) {
                        String codePanelRecord = iterator.next();

                        SyncFile syncFile = project.getSyncFileByAbsolutePath(codePanelRecord);
                        if (syncFile != null && !syncFile.isDirectory()) {
                            add(syncFile, false);
                            iterator.remove();
                        }
                    }
                }
            }
        });
    }

    public void removeProject(Project project) {
        for (int i = 0, iEnd = tabbedPane.getTabCount(); i < iEnd; i++) {
            CodePanelTab codePanelTab = (CodePanelTab) tabbedPane.getSelectedComponent();
            SyncFile syncFile = codePanelTab.getSyncFile();
            if (syncFile.getInheritUserObject("project").equals(project)) {
                codePanelTab.close();
            }
        }
    }

    public void closeSelectedTab() {
        int selectedTabIndex = tabbedPane.getSelectedIndex();
        if (selectedTabIndex != -1) {
            CodePanelTab codePanelTab = (CodePanelTab) tabbedPane.getSelectedComponent();
            codePanelTab.close();
            tabbedPane.removeTabAt(tabbedPane.getSelectedIndex());
        }
    }

    private static class CodePanelTab extends JPanel {

        private static final long serialVersionUID = 1L;
        //
        private CodeViewer codeViewer;
        private SyncFile syncFile;
        private SyncFileListener syncFileListener;

        private CodePanelTab(CodeViewer codeViewer, SyncFile syncFile, SyncFileListener syncFileListener) {
            this.codeViewer = codeViewer;
            this.syncFile = syncFile;
            this.syncFileListener = syncFileListener;
            setLayout(new BorderLayout());
            add(codeViewer.getGUI(), BorderLayout.CENTER);
        }

        public void close() {
            // may invoke this when iterating syncFile's listeners
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    syncFile.removeListener(syncFileListener);
                }
            });
        }

        public CodeViewer getCodeViewer() {
            return codeViewer;
        }

        public SyncFile getSyncFile() {
            return syncFile;
        }

        public SyncFileListener getSyncFileListener() {
            return syncFileListener;
        }
    }
}
