package langfiles.project;

/**
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public interface ProjectEventListener {

  void projectAdded(Project project);

  void projectDeleted(Project project);
}
