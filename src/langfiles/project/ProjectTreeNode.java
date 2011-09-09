package langfiles.project;

import java.util.List;
import javax.swing.Icon;

/**
 *
 * @author cws1989
 */
public interface ProjectTreeNode {

    public static enum Type {

        PROJECT, SOURCE_FILES, PROPERTIES, FOLDER, FILE
    }

    Type getType();

    List<ProjectTreeNode> getChildren();

    Icon getIcon();

    String getToolTip();

    Object getObject();

    @Override
    String toString();
}
