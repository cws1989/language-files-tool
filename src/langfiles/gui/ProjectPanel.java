package langfiles.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
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

/**
 * The project panel.
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public class ProjectPanel {

    /**
     * The project panel.
     */
    private JPanel projectPanel;
    private JScrollPane treeScrollPane;
    private JTree tree;

    /**
     * Constructor
     */
    public ProjectPanel() {
        projectPanel = new JPanel();
        projectPanel.setBackground(projectPanel.getBackground().brighter());
        projectPanel.setLayout(new BorderLayout());

        DefaultMutableTreeNode top = new DefaultMutableTreeNode("Projects");
        createNodes(top);

        tree = new JTree(top);
        tree.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        tree.setRootVisible(true);
        tree.setShowsRootHandles(false);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setCellRenderer(new DefaultTreeCellRenderer() {

            private static final long serialVersionUID = 19890211167598L;

            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                switch (row) {
                    case 0:
                        setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/langfiles/gui/images/ProjectPanel/projects.png"))));
                        break;
                    case 1:
                        setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/langfiles/gui/images/ProjectPanel/project" + (expanded ? "_open" : "") + ".png"))));
                        break;
                    case 2:
                        setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/langfiles/gui/images/ProjectPanel/source_files.png"))));
                        break;
                    case 3:
                        setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/langfiles/gui/images/ProjectPanel/folder" + (expanded ? "_open" : "") + ".png"))));
                        break;
                    case 4:
                        setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/langfiles/gui/images/ProjectPanel/file.png"))));
                        break;
                    case 5:
                        setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/langfiles/gui/images/ProjectPanel/file.png"))));
                        break;
                    case 6:
                        setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/langfiles/gui/images/ProjectPanel/file.png"))));
                        break;
                    case 7:
                        setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/langfiles/gui/images/ProjectPanel/properties.png"))));
                        break;
                    case 8:
                        setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/langfiles/gui/images/ProjectPanel/project" + (expanded ? "_open" : "") + ".png"))));
                        break;
                    case 9:
                        setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/langfiles/gui/images/ProjectPanel/source_files.png"))));
                        break;
                    case 10:
                        setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/langfiles/gui/images/ProjectPanel/properties.png"))));
                        break;
                }
                setToolTipText("This book is in the Tutorial series.");

                return this;
            }
        });
        tree.addMouseListener(new MouseAdapter() {

            private void myPopupEvent(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                JTree tree = (JTree) e.getSource();
                TreePath path = tree.getPathForLocation(x, y);
                if (path == null) {
                    return;
                }

                tree.setSelectionPath(path);

                JPopupMenu popup = new JPopupMenu();
                popup.add(new JMenuItem("test"));
                popup.show(tree, x, y);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    myPopupEvent(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    myPopupEvent(e);
                }
            }
        });
        tree.addTreeSelectionListener(new TreeSelectionListener() {

            @Override
            public void valueChanged(TreeSelectionEvent e) {
                System.out.println(e.getOldLeadSelectionPath());
                System.out.println(e.getNewLeadSelectionPath());
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

    private void createNodes(DefaultMutableTreeNode top) {
        DefaultMutableTreeNode category = null;
        DefaultMutableTreeNode book = null;
        DefaultMutableTreeNode sourceFile = null;
        DefaultMutableTreeNode sourceFolder = null;

        category = new DefaultMutableTreeNode("Project 1");
        top.add(category);

        book = new DefaultMutableTreeNode("Source Files");
        category.add(book);

        sourceFolder = new DefaultMutableTreeNode("gui");
        book.add(sourceFolder);

        sourceFile = new DefaultMutableTreeNode("Main.java");
        sourceFolder.add(sourceFile);

        sourceFile = new DefaultMutableTreeNode("build.xml");
        book.add(sourceFile);

        sourceFile = new DefaultMutableTreeNode("manifest.mf");
        book.add(sourceFile);

        book = new DefaultMutableTreeNode("Properties");
        category.add(book);

        category = new DefaultMutableTreeNode("Project 2");
        top.add(category);

        book = new DefaultMutableTreeNode("Source Files");
        category.add(book);

        book = new DefaultMutableTreeNode("Properties");
        category.add(book);
    }

    /**
     * Get the GUI display panel.
     * @return the GUI component
     */
    public JPanel getGUI() {
        return projectPanel;
    }
}
