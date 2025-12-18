import java.awt.Color;
import java.io.Serializable;

public class Node implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String label;
    private int x, y;

    // Algoritma State
    public int distance = Integer.MAX_VALUE;
    public Node previous = null;
    public boolean visited = false;
    public Color visualColor = null;

    public Node(String id, int x, int y) {
        this.id = id;
        this.label = id;
        this.x = x;
        this.y = y;
    }

    public void reset() {
        this.distance = Integer.MAX_VALUE;
        this.previous = null;
        this.visited = false;
        this.visualColor = null;
    }

    public void setLabel(String label) { this.label = label; }
    public String getLabel() { return label; }
    public String getId() { return id; }
    public int getX() { return x; }
    public int getY() { return y; }
    public void setPosition(int x, int y) { this.x = x; this.y = y; }
}