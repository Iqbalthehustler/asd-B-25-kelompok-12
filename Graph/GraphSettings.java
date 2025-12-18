import java.io.Serializable;

public class GraphSettings implements Serializable {
    private static final long serialVersionUID = 1L;
    private String projectTitle = "UNTITLED GRAPH";

    public String getProjectTitle() {
        return projectTitle;
    }

    public void setProjectTitle(String projectTitle) {
        this.projectTitle = projectTitle;
    }
}