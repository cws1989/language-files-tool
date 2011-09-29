package langfiles.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import langfiles.Main;
import langfiles.project.Project;
import langfiles.project.ProjectFileListener;
import langfiles.project.ProjectListener;
import langfiles.util.Config;
import langfiles.util.SortedArrayList;
import langfiles.util.SyncFile;
import langfiles.util.SyncFileListener;

/**
 * The project panel.
 * @todo make all UI operation to use swing dispatching thread
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public class ProjectPanel {

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
    private JPanel projectPanel;
    /**
     * Tree GUI related.
     */
    private JScrollPane treeScrollPane;
    private JTree tree;
    private DefaultMutableTreeNode rootNode;
    /**
     * Project list.
     */
    private final List<Project> projectList;
    private final Map<Project, List<String>> projectExpandCollapseRecordList;
    //
    private SyncFileListener syncFileListener;

    /**
     * Constructor
     */
    public ProjectPanel(MainWindow mainWindow) {
        main = Main.getInstance();
        this.mainWindow = mainWindow;
        projectList = Collections.synchronizedList(new SortedArrayList<Project>());
        projectExpandCollapseRecordList = Collections.synchronizedMap(new TreeMap<Project, List<String>>());

        projectPanel = new JPanel();
        projectPanel.setBackground(projectPanel.getBackground().brighter());
        projectPanel.setLayout(new BorderLayout());

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

        //<editor-fold defaultstate="collapsed" desc="tree">
        rootNode = new DefaultMutableTreeNode(new ProjectTreeNode(ProjectTreeNode.Type.PROJECTS, "Projects", ""));

        tree = new JTree(rootNode);
        tree.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        tree.setRootVisible(true);
        tree.setShowsRootHandles(false);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setCellRenderer(new DefaultTreeCellRenderer() {

            private static final long serialVersionUID = 1L;

            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

                DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
                ProjectTreeNode projectTreeNode = (ProjectTreeNode) treeNode.getUserObject();

                setIcon(projectTreeNode.getIcon(expanded));
                setToolTipText(projectTreeNode.getToolTip());

                return this;
            }
        });
        tree.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int x = e.getX();
                    int y = e.getY();

                    JTree tree = (JTree) e.getSource();
                    TreePath path = tree.getPathForLocation(x, y);
                    if (path == null) {
                        return;
                    }

                    tree.setSelectionPath(path);

                    DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) path.getLastPathComponent();

                    ProjectTreeNode projectTreeNode = (ProjectTreeNode) treeNode.getUserObject();

                    JPopupMenu popup = new JPopupMenu();
                    switch (projectTreeNode.getType()) {
                        case PROJECTS:
                            popup.add(new JMenuItem("New Project"));
                            break;
                        case PROJECT:
                            popup.add(new JMenuItem("Add Folder"));
                            popup.add(new JMenuItem("Add File"));
                            popup.addSeparator();
                            popup.add(new JMenuItem("Commit"));
                            popup.addSeparator();
                            popup.add(new JMenuItem("Properties"));
                            break;
                        case SOURCE_FILES:
                            popup.add(new JMenuItem("Add Folder"));
                            popup.add(new JMenuItem("Add File"));
                            popup.addSeparator();
                            popup.add(new JMenuItem("Commit"));
                            break;
                        case FOLDER:
                            popup.add(new JMenuItem("Ignore"));
                            break;
                        case FILE:
                            popup.add(new JMenuItem("Open"));
                            popup.add(new JMenuItem("Ignore"));
                            break;
                        case PROPERTIES:
                            break;
                    }
                    popup.show(tree, x, y);
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    int x = e.getX();
                    int y = e.getY();

                    JTree tree = (JTree) e.getSource();
                    TreePath path = tree.getPathForLocation(x, y);
                    if (path == null) {
                        return;
                    }

                    DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) path.getLastPathComponent();

                    ProjectTreeNode projectTreeNode = (ProjectTreeNode) treeNode.getUserObject();

                    if (projectTreeNode.getType() == ProjectTreeNode.Type.FILE) {
                        ProjectPanel.this.mainWindow.getCodePanel().add((SyncFile) projectTreeNode.getUserObject(), true);
                    }
                }
            }
        });
//        tree.addTreeSelectionListener(new TreeSelectionListener() {
//
//            @Override
//            public void valueChanged(TreeSelectionEvent e) {
//            }
//        });
        tree.addTreeExpansionListener(new TreeExpansionListener() {

            @Override
            public void treeExpanded(TreeExpansionEvent event) {
                TreePath path = event.getPath();
                if (path.getPath().length > 1) {
                    DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) path.getLastPathComponent();
                    ProjectTreeNode projectTreeNode = (ProjectTreeNode) treeNode.getUserObject();
                    Object userObject = projectTreeNode.getUserObject();
                    if (userObject == null) {
                        return;
                    }
                    if (userObject instanceof SyncFile) {
                        SyncFile syncFile = (SyncFile) userObject;
                        Project project = (Project) syncFile.getInheritUserObject("project");
                        String filePath = syncFile.getFile().getAbsolutePath();

                        synchronized (projectExpandCollapseRecordList) {
                            boolean recordExist = false;

                            List<String> projectExpandCollapseRecord = projectExpandCollapseRecordList.get(project);
                            for (String _path : projectExpandCollapseRecord) {
                                if (_path.startsWith(filePath)) {
                                    recordExist = true;
                                    break;
                                }
                            }

                            if (!recordExist) {
                                projectExpandCollapseRecord.add(filePath);
                            }
                        }
                    } else if (userObject instanceof Project) {
                    }
                }
            }

            @Override
            public void treeCollapsed(TreeExpansionEvent event) {
                TreePath path = event.getPath();
                if (path.getPath().length == 1) {
                    tree.expandPath(path);
                } else {
                    DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) path.getLastPathComponent();
                    ProjectTreeNode projectTreeNode = (ProjectTreeNode) treeNode.getUserObject();
                    Object userObject = projectTreeNode.getUserObject();
                    if (userObject == null) {
                        return;
                    }
                    if (userObject instanceof SyncFile) {
                        SyncFile syncFile = (SyncFile) userObject;
                        if (syncFile == null) {
                            return;
                        }
                        Project project = (Project) syncFile.getInheritUserObject("project");
                        String filePath = syncFile.getFile().getAbsolutePath();

                        synchronized (projectExpandCollapseRecordList) {
                            List<String> projectExpandCollapseRecord = projectExpandCollapseRecordList.get(project);
                            Iterator<String> iterator = projectExpandCollapseRecord.iterator();
                            while (iterator.hasNext()) {
                                String _path = iterator.next();
                                if (_path.equals(filePath)) {
                                    iterator.remove();
                                }
                            }
                        }
                    } else if (userObject instanceof Project) {
//                        synchronized (projectExpandCollapseRecordList) {
//                            List<String> projectExpandCollapseRecord = projectExpandCollapseRecordList.get((Project) userObject);
//                            projectExpandCollapseRecord.clear();
//                        }
                    }
                }
            }
        });
        ToolTipManager.sharedInstance().registerComponent(tree);

        treeScrollPane = new JScrollPane();
        treeScrollPane.setBorder(null);
        treeScrollPane.setViewportView(tree);
        //</editor-fold>
        projectPanel.add(treeScrollPane, BorderLayout.CENTER);

        main.addShutdownEvent(100, new Runnable() {

            @Override
            public void run() {
                List<DefaultMutableTreeNode> expandedNodeList = new ArrayList<DefaultMutableTreeNode>();
                getAllExpandedNodes(tree, rootNode, expandedNodeList);

                List<String> expandedFilePathList = new ArrayList<String>();
                for (DefaultMutableTreeNode treeNode : expandedNodeList) {
                    ProjectTreeNode projectTreeNode = (ProjectTreeNode) treeNode.getUserObject();
                    Object userObject = projectTreeNode.getUserObject();
                    if (userObject instanceof SyncFile) {
                        expandedFilePathList.add(((SyncFile) userObject).getFile().getAbsolutePath());
                    }
                }

                Config preference = main.getPreference();
                synchronized (projectExpandCollapseRecordList) {
                    for (Project project : projectExpandCollapseRecordList.keySet()) {
                        StringBuilder sb = new StringBuilder();
                        List<String> _projectExpandCollapseRecordList = projectExpandCollapseRecordList.get(project);
                        for (String _path : _projectExpandCollapseRecordList) {
                            if (expandedFilePathList.indexOf(_path) != -1) {
                                if (sb.length() != 0) {
                                    sb.append("\t");
                                }
                                sb.append(_path);
                            }
                        }
                        preference.setProperty("project_panel/" + project.getName(), sb.toString());
                    }
                }
            }
        });

        syncFileListener = new SyncFileListener() {

            @Override
            public void fileCreated(SyncFile directory, SyncFile fileCreated, String rootPath, String name) {
                DefaultMutableTreeNode fileTreeNode = (DefaultMutableTreeNode) directory.getUserObject("treeNode");
                if (fileTreeNode == null) {
                    return;
                }

                addChildNodes(new ArrayList<String>(), new ArrayList<TreePath>(), fileTreeNode, fileCreated);

                DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
                treeModel.reload(fileTreeNode);
            }

            @Override
            public void fileDeleted(SyncFile fileDeleted, String rootPath, String name) {
                DefaultMutableTreeNode fileTreeNode = (DefaultMutableTreeNode) fileDeleted.getUserObject("treeNode");
                if (fileTreeNode == null) {
                    return;
                }
                DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) fileTreeNode.getParent();

                fileTreeNode.removeFromParent();

                DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
                treeModel.reload(parentNode);
            }

            @Override
            public void fileModified(SyncFile fileModified, String rootPath, String name) {
                // do nothing
            }

            @Override
            public void fileRenamed(SyncFile fileRenamed, String rootPath, String oldName, String newName) {
                DefaultMutableTreeNode fileTreeNode = (DefaultMutableTreeNode) fileRenamed.getUserObject("treeNode");
                if (fileTreeNode == null) {
                    return;
                }

                ProjectTreeNode projectTreeNode = (ProjectTreeNode) fileTreeNode.getUserObject();
                if (projectTreeNode == null) {
                    return;
                }

                projectTreeNode.setTitle(new File(rootPath + "/" + newName).getName());
                DefaultMutableTreeNode parentTreeNode = (DefaultMutableTreeNode) fileTreeNode.getParent();
                sort(parentTreeNode, fileTreeNode, fileRenamed);

                DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
                treeModel.reload(parentTreeNode);
            }
        };
    }

    protected void sort(DefaultMutableTreeNode parentNode, DefaultMutableTreeNode childNode, SyncFile childSyncFile) {
        int childOldPos = parentNode.getIndex(childNode);
        parentNode.remove(childOldPos);

        int index = 0, count = 0;
        if (childSyncFile.isDirectory()) {
            Enumeration<?> enumeration = parentNode.children();
            while (enumeration.hasMoreElements()) {
                DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) enumeration.nextElement();

                ProjectTreeNode projectTreeNode = (ProjectTreeNode) treeNode.getUserObject();
                SyncFile _syncFile = (SyncFile) projectTreeNode.getUserObject();

                if (!_syncFile.isDirectory()) {
                    break;
                }
                if (_syncFile.getAbsolutePath().equals(childSyncFile.getAbsolutePath())) {
                    return;
                }
                int compareResult = childSyncFile.getFileName().compareTo(_syncFile.getFileName());
                if (compareResult > 0) {
                    index = count + 1;
                }

                count++;
            }
        } else {
            Enumeration<?> enumeration = parentNode.children();
            while (enumeration.hasMoreElements()) {
                DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) enumeration.nextElement();

                ProjectTreeNode projectTreeNode = (ProjectTreeNode) treeNode.getUserObject();
                SyncFile _syncFile = (SyncFile) projectTreeNode.getUserObject();

                if (_syncFile.isDirectory()) {
                    index = count + 1;
                } else {
                    if (_syncFile.getAbsolutePath().equals(childSyncFile.getAbsolutePath())) {
                        return;
                    }
                    int compareResult = childSyncFile.getFileName().compareTo(_syncFile.getFileName());
                    if (compareResult > 0) {
                        index = count + 1;
                    }
                }

                count++;
            }
        }

        parentNode.insert(childNode, index);
    }

    public void getAllExpandedNodes(JTree tree, DefaultMutableTreeNode node, List<DefaultMutableTreeNode> expandedNodeList) {
        if (node.getChildCount() >= 0) {
            Enumeration<?> e = node.children();
            while (e.hasMoreElements()) {
                DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) e.nextElement();

                if (tree.isExpanded(new TreePath(((DefaultTreeModel) tree.getModel()).getPathToRoot(treeNode)))) {
                    expandedNodeList.add(treeNode);
                }
                getAllExpandedNodes(tree, treeNode, expandedNodeList);
            }
        }
    }

    /**
     * Get the GUI display panel.
     * @return the GUI component
     */
    public JPanel getGUI() {
        return projectPanel;
    }

    /**
     * Add project to project panel.
     * @param project the project to add
     */
    public void addProject(Project project) {
        // project list
        synchronized (projectList) {
            if (projectList.indexOf(project) != -1) {
                return;
            }
            projectList.add(project);
        }

        // read expand collapse record
        List<String> projectExpandCollapseRecord = new SortedArrayList<String>();
        String projectExpandCollapseRecordString = main.getPreference().getProperty("project_panel/" + project.getName());
        if (projectExpandCollapseRecordString != null) {
            projectExpandCollapseRecord.addAll(Arrays.asList(projectExpandCollapseRecordString.split("\t")));
        }
        projectExpandCollapseRecordList.put(project, projectExpandCollapseRecord);

        // tree nodes
        // project node
        ProjectTreeNode projectProjectTreeNode = new ProjectTreeNode(ProjectTreeNode.Type.PROJECT, project.getName(), null);
        projectProjectTreeNode.setUserObject(project);
        DefaultMutableTreeNode projectNode = new DefaultMutableTreeNode(projectProjectTreeNode);
        rootNode.add(projectNode);

        // source files node
        ProjectTreeNode sourceFilesProjectTreeNode = new ProjectTreeNode(ProjectTreeNode.Type.SOURCE_FILES, "Source Files", null);
        sourceFilesProjectTreeNode.setUserObject(project);
        final DefaultMutableTreeNode sourceFilesNode = new DefaultMutableTreeNode(sourceFilesProjectTreeNode);
        projectNode.add(sourceFilesNode);

        List<TreePath> pathsToExpand = new ArrayList<TreePath>();

        List<SyncFile> files = project.getSyncFileList();
        for (SyncFile syncFile : files) {
            addChildNodes(projectExpandCollapseRecord, pathsToExpand, sourceFilesNode, syncFile);
        }

        // properties node
        projectNode.add(new DefaultMutableTreeNode(new ProjectTreeNode(ProjectTreeNode.Type.PROPERTIES, "Properties", null)));

        // expand the root node
        tree.expandPath(tree.getPathForRow(0));

        for (TreePath treePath : pathsToExpand) {
            if (!tree.isExpanded(treePath)) {
                tree.expandPath(treePath);
            }
        }

        project.addFileListener(new ProjectFileListener() {

            @Override
            public void projectFileAdded(SyncFile syncFile) {
                addChildNodes(new ArrayList<String>(), new ArrayList<TreePath>(), sourceFilesNode, syncFile);
            }

            @Override
            public void projectFileRemoved(SyncFile syncFile) {
                DefaultMutableTreeNode nodeToRemove = null;

                Enumeration<?> enumeration = sourceFilesNode.children();
                while (enumeration.hasMoreElements()) {
                    DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) enumeration.nextElement();
                    ProjectTreeNode projectTreeNode = (ProjectTreeNode) treeNode.getUserObject();
                    if (projectTreeNode.getUserObject().equals(syncFile)) {
                        nodeToRemove = treeNode;
                        break;
                    }
                }

                if (nodeToRemove != null) {
                    rootNode.remove(nodeToRemove);
                }
            }
        });
    }

    public void removeProject(Project project) {
        DefaultMutableTreeNode nodeToRemove = null;

        Enumeration<?> enumeration = rootNode.children();
        while (enumeration.hasMoreElements()) {
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) enumeration.nextElement();
            ProjectTreeNode projectTreeNode = (ProjectTreeNode) treeNode.getUserObject();
            if (project.equals(projectTreeNode.getUserObject())) {
                nodeToRemove = treeNode;
                break;
            }
        }

        if (nodeToRemove != null) {
            rootNode.remove(nodeToRemove);
        }
    }

    /**
     * Add syncFile to parentNode and recurse on syncFile and add sub-folders and files to relative node.
     * @param parentNode the parent tree node to add syncFile to
     * @param syncFile the file to add and recurse on
     */
    public void addChildNodes(List<String> projectExpandCollapseRecord, List<TreePath> pathsToExpand, DefaultMutableTreeNode parentNode, SyncFile syncFile) {
        DefaultMutableTreeNode _childNode = null;
        if (syncFile.isDirectory()) {
            ProjectTreeNode folderProjectTreeNode = new ProjectTreeNode(ProjectTreeNode.Type.FOLDER, syncFile.getFileName(), null);
            folderProjectTreeNode.setUserObject(syncFile);

            _childNode = new DefaultMutableTreeNode(folderProjectTreeNode);
            parentNode.add(_childNode);

            List<SyncFile> fileList = syncFile.getChildSyncFileList();
            for (SyncFile _file : fileList) {
                addChildNodes(projectExpandCollapseRecord, pathsToExpand, _childNode, _file);
            }

            synchronized (projectExpandCollapseRecordList) {
                String directoryPath = syncFile.getAbsolutePath();

                for (String _path : projectExpandCollapseRecord) {
                    if (directoryPath.equals(_path)) {
                        TreePath pathToNode = new TreePath(((DefaultTreeModel) tree.getModel()).getPathToRoot(_childNode));
                        pathsToExpand.add(pathToNode);
                    }
                }
            }
        } else {
            ProjectTreeNode fileProjectTreeNode = new ProjectTreeNode(ProjectTreeNode.Type.FILE, syncFile.getFileName(), null);
            fileProjectTreeNode.setUserObject(syncFile);

            _childNode = new DefaultMutableTreeNode(fileProjectTreeNode);
            parentNode.add(_childNode);
        }
        syncFile.setUserObject("treeNode", _childNode);
        syncFile.addListener(syncFileListener);
        sort(parentNode, _childNode, syncFile);
    }

    /**
     * The tree node 'user object' to use for project panel tree.
     */
    private static class ProjectTreeNode {

        public static enum Type {

            PROJECTS, PROJECT, SOURCE_FILES, FOLDER, FILE, PROPERTIES
        }

        public static final class NodeIcon {

            public static final Icon projects = new ImageIcon(Toolkit.getDefaultToolkit().getImage(ProjectTreeNode.class.getResource("/langfiles/gui/images/ProjectPanel/projects.png")));
            public static final Icon project = new ImageIcon(Toolkit.getDefaultToolkit().getImage(ProjectTreeNode.class.getResource("/langfiles/gui/images/ProjectPanel/project.png")));
            public static final Icon projectOpen = new ImageIcon(Toolkit.getDefaultToolkit().getImage(ProjectTreeNode.class.getResource("/langfiles/gui/images/ProjectPanel/project_open.png")));
            public static final Icon sourceFiles = new ImageIcon(Toolkit.getDefaultToolkit().getImage(ProjectTreeNode.class.getResource("/langfiles/gui/images/ProjectPanel/source_files.png")));
            public static final Icon folder = new ImageIcon(Toolkit.getDefaultToolkit().getImage(ProjectTreeNode.class.getResource("/langfiles/gui/images/ProjectPanel/folder.png")));
            public static final Icon folderOpen = new ImageIcon(Toolkit.getDefaultToolkit().getImage(ProjectTreeNode.class.getResource("/langfiles/gui/images/ProjectPanel/folder_open.png")));
            public static final Icon file = new ImageIcon(Toolkit.getDefaultToolkit().getImage(ProjectTreeNode.class.getResource("/langfiles/gui/images/ProjectPanel/file.png")));
            public static final Icon properties = new ImageIcon(Toolkit.getDefaultToolkit().getImage(ProjectTreeNode.class.getResource("/langfiles/gui/images/ProjectPanel/properties.png")));

            private NodeIcon() {
            }
        }
        protected Type type;
        protected String title;
        protected String toolTip;
        protected Icon icon;
        protected Icon iconExpanded;
        protected Object userObject;

        private ProjectTreeNode(Type type, String title, String toolTip) {
            this.type = type;
            this.title = title;
            this.toolTip = toolTip;
            switch (type) {
                case PROJECTS:
                    icon = NodeIcon.projects;
                    iconExpanded = NodeIcon.projects;
                    break;
                case PROJECT:
                    icon = NodeIcon.project;
                    iconExpanded = NodeIcon.projectOpen;
                    break;
                case SOURCE_FILES:
                    icon = NodeIcon.sourceFiles;
                    iconExpanded = NodeIcon.sourceFiles;
                    break;
                case FOLDER:
                    icon = NodeIcon.folder;
                    iconExpanded = NodeIcon.folderOpen;
                    break;
                case FILE:
                    icon = NodeIcon.file;
                    iconExpanded = NodeIcon.file;
                    break;
                case PROPERTIES:
                    icon = NodeIcon.properties;
                    iconExpanded = NodeIcon.properties;
                    break;
            }
        }

        public Type getType() {
            return type;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Icon getIcon(boolean expanded) {
            return expanded ? iconExpanded : icon;
        }

        public String getToolTip() {
            return toolTip;
        }

        public void setToolTip(String toolTip) {
            this.toolTip = toolTip;
        }

        public Object getUserObject() {
            return userObject;
        }

        public void setUserObject(Object userObject) {
            this.userObject = userObject;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    /**
     * For test/development purpose.
     */
    public static void main(String[] args) {
        ProjectPanel projectPanel = new ProjectPanel(null);

        Project project = new Project("Project Name");
        project.setAllowedExtensions(Arrays.asList(new String[]{".java"}));
        project.add(new File(System.getProperty("user.dir")));
        projectPanel.addProject(project);

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(projectPanel.getGUI());
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }
}
