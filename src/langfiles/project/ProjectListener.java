package langfiles.project;

/**
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public interface ProjectListener {

  void projectAdded(Project project);

  void projectRemoved(Project project);
}
