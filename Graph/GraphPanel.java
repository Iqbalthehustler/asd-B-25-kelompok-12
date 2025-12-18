import javax.swing.*;
import java.awt.*;
import java.util.List;

public class GraphPanel extends JPanel {
    private Graph graph;
    private GraphInputHandler inputHandler;
    private GraphSettings settings;

    private final Color BG_COLOR = Color.decode("#0f172a");
    private final Color EDGE_COLOR = Color.decode("#475569");
    private final Color NODE_COLOR = Color.decode("#1e293b");
    private final Color COLOR_START = Color.decode("#3b82f6");
    private final Color COLOR_END = Color.decode("#ef4444");
    private final Color COLOR_SELECTED = Color.decode("#facc15");
    private final Color SELECTION_FILL = new Color(59, 130, 246, 50);
    private final Color SELECTION_BORDER = new Color(59, 130, 246, 200);

    public GraphPanel(Graph graph, GraphSettings settings) {
        this.graph = graph;
        this.settings = settings;
        this.setBackground(BG_COLOR);
    }

    public void setInputHandler(GraphInputHandler handler) {
        this.inputHandler = handler;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // JUDUL
        g2d.setColor(new Color(255, 255, 255, 30));
        g2d.fillRoundRect(10, 10, 300, 50, 10, 10);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Impact", Font.PLAIN, 24));
        g2d.drawString(settings.getProjectTitle(), 20, 40);
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawString("(Double Click to Edit Title)", 20, 53);

        // EDGE
        g2d.setStroke(new BasicStroke(3));
        for (Edge edge : graph.getEdges()) {
            if (edge.getFrom().getId().compareTo(edge.getTo().getId()) < 0) {
                Node n1 = edge.getFrom(); Node n2 = edge.getTo();
                if (edge.getColor() != null) {
                    g2d.setColor(edge.getColor());
                    g2d.setStroke(new BasicStroke(edge.getColor().equals(Color.decode("#4ade80")) ? 5 : 3));
                } else {
                    g2d.setColor(EDGE_COLOR);
                    g2d.setStroke(new BasicStroke(3));
                }
                g2d.drawLine(n1.getX(), n1.getY(), n2.getX(), n2.getY());

                int midX = (n1.getX() + n2.getX()) / 2;
                int midY = (n1.getY() + n2.getY()) / 2;
                g2d.setColor(BG_COLOR);
                g2d.fillRoundRect(midX - 14, midY - 12, 28, 24, 8, 8);
                g2d.setColor(Color.decode("#94a3b8"));
                g2d.setFont(new Font("Consolas", Font.BOLD, 14));
                String w = String.valueOf(edge.getWeight());
                g2d.drawString(w, midX - g2d.getFontMetrics().stringWidth(w)/2, midY + 5);
            }
        }

        // BOX SELEKSI
        if (inputHandler != null && inputHandler.isAreaSelecting()) {
            Rectangle box = inputHandler.getSelectionBox();
            if (box != null) {
                g2d.setColor(SELECTION_FILL); g2d.fill(box);
                g2d.setColor(SELECTION_BORDER);
                g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[]{5}, 0));
                g2d.draw(box);
            }
        }

        // NODE
        for (Node node : graph.getNodes()) {
            String labelText = node.getLabel();
            Font font = new Font("SansSerif", Font.BOLD, 14);
            FontMetrics fm = g2d.getFontMetrics(font);
            int width = Math.max(50, fm.stringWidth(labelText) + 24);
            int height = 50;

            Color fillColor = NODE_COLOR;
            Color borderColor = Color.WHITE;
            float borderThickness = 2.0f;

            if (node.visualColor != null) { fillColor = node.visualColor; borderColor = fillColor.brighter(); }
            if (inputHandler != null) {
                if (node == inputHandler.getStartNode()) { fillColor = COLOR_START; borderColor = Color.WHITE; }
                else if (node == inputHandler.getEndNode()) { fillColor = COLOR_END; borderColor = Color.WHITE; }
                if (inputHandler.getMultiSelectedNodes().contains(node)) {
                    borderColor = COLOR_SELECTED; borderThickness = 4.0f;
                    g2d.setColor(new Color(250, 204, 21, 100));
                    g2d.fillRoundRect(node.getX() - width/2 - 6, node.getY() - height/2 - 6, width + 12, height + 12, 50, 50);
                }
            }

            g2d.setColor(fillColor);
            g2d.fillRoundRect(node.getX() - width/2, node.getY() - height/2, width, height, height, height);

            g2d.setColor(borderColor);
            g2d.setStroke(new BasicStroke(borderThickness));
            g2d.drawRoundRect(node.getX() - width/2, node.getY() - height/2, width, height, height, height);

            g2d.setColor(Color.WHITE);
            if (fillColor == COLOR_SELECTED || fillColor == Color.WHITE) g2d.setColor(Color.BLACK);
            g2d.setFont(font);
            g2d.drawString(labelText, node.getX() - fm.stringWidth(labelText) / 2, node.getY() + 5);

            if (node.distance != Integer.MAX_VALUE && inputHandler.getStartNode() != null) {
                g2d.setColor(Color.decode("#4ade80"));
                g2d.setFont(new Font("Monospaced", Font.BOLD, 12));
                String d = String.valueOf(node.distance);
                g2d.drawString(d, node.getX() - g2d.getFontMetrics().stringWidth(d)/2, node.getY() + height/2 + 15);
            }
        }
    }
}