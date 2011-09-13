package langfiles.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Arrays;
import java.util.List;
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
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import langfiles.project.DigestedFile;
import langfiles.project.Project;

/**
 * The project panel.
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public class ProjectPanel {

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
     * Constructor
     */
    public ProjectPanel(MainWindow mainWindow) {
        this.mainWindow = mainWindow;

        projectPanel = new JPanel();
        projectPanel.setBackground(projectPanel.getBackground().brighter());
        projectPanel.setLayout(new BorderLayout());

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
                        ProjectPanel.this.mainWindow.getCodePanel().add((DigestedFile) projectTreeNode.getUserObject());
                    }
                }
            }
        });
        tree.addTreeSelectionListener(new TreeSelectionListener() {

            @Override
            public void valueChanged(TreeSelectionEvent e) {
            }
        });
        tree.addTreeExpansionListener(new TreeExpansionListener() {

            @Override
            public void treeExpanded(TreeExpansionEvent event) {
            }

            @Override
            public void treeCollapsed(TreeExpansionEvent event) {
                TreePath path = event.getPath();
                if (path.getPath().length == 1) {
                    tree.expandPath(path);
                }
            }
        });
        ToolTipManager.sharedInstance().registerComponent(tree);

        treeScrollPane = new JScrollPane();
        treeScrollPane.setBorder(null);
        treeScrollPane.setViewportView(tree);
        projectPanel.add(treeScrollPane, BorderLayout.CENTER);
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
        DefaultMutableTreeNode projectNode = new DefaultMutableTreeNode(new ProjectTreeNode(ProjectTreeNode.Type.PROJECT, "Project", null));
        rootNode.add(projectNode);

        DefaultMutableTreeNode sourceFilesNode = new DefaultMutableTreeNode(new ProjectTreeNode(ProjectTreeNode.Type.SOURCE_FILES, "Source Files", null));
        projectNode.add(sourceFilesNode);

        List<DigestedFile> files = project.getDigestedData();
        for (DigestedFile digestedFile : files) {
            addChildNodes(sourceFilesNode, digestedFile);
        }

        projectNode.add(new DefaultMutableTreeNode(new ProjectTreeNode(ProjectTreeNode.Type.PROPERTIES, "Properties", null)));

        // expand the root node
        tree.expandPath(tree.getPathForRow(0));
    }

    /**
     * Add digestedFile to parentNode and recurse on digestedFile and add sub-folders and files to relative node.
     * @param parentNode the parent tree node to add digestedFile to
     * @param digestedFile the file to add and recurse on
     */
    public void addChildNodes(DefaultMutableTreeNode parentNode, DigestedFile digestedFile) {
        if (digestedFile.isDirectory()) {
            ProjectTreeNode folderProjectTreeNode = new ProjectTreeNode(ProjectTreeNode.Type.FOLDER, digestedFile.getFile().getName(), null);
            folderProjectTreeNode.setUserObject(digestedFile);

            DefaultMutableTreeNode folderNode = new DefaultMutableTreeNode(folderProjectTreeNode);
            parentNode.add(folderNode);

            List<DigestedFile> fileList = digestedFile.getFiles();
            for (DigestedFile _file : fileList) {
                addChildNodes(folderNode, _file);
            }
        } else {
            ProjectTreeNode fileProjectTreeNode = new ProjectTreeNode(ProjectTreeNode.Type.FILE, digestedFile.getFile().getName(), null);
            fileProjectTreeNode.setUserObject(digestedFile);
            parentNode.add(new DefaultMutableTreeNode(fileProjectTreeNode));
        }
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

        Project project = new Project();
        project.setAllowedExtensions(Arrays.asList(new String[]{".java"}));
        project.addFolder(new File(System.getProperty("user.dir")));
        projectPanel.addProject(project);

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(projectPanel.getGUI());
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }
}
