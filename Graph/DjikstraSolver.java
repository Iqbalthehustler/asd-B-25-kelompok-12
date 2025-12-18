import java.awt.Color;
import java.util.*;

public class DijkstraSolver {
    private Graph graph;
    private GraphPanel panel;
    private LoggerPanel logger;

    public DijkstraSolver(Graph graph, GraphPanel panel, LoggerPanel logger) {
        this.graph = graph;
        this.panel = panel;
        this.logger = logger;
    }

    public void run(Node start, Node end, int delay) throws InterruptedException {
        graph.resetVisuals();
        logger.clear();
        logger.addLog("▶ START: " + start.getLabel() + " -> " + end.getLabel(), "#ffffff", true);
        logger.addSeparator();

        start.distance = 0;
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.distance));
        pq.add(start);

        while (!pq.isEmpty()) {
            Node current = pq.poll();
            if (current.visited) continue;
            current.visited = true;

            if (current != start && current != end) current.visualColor = Color.decode("#38bdf8");
            logger.addLog("👀 Visit: " + current.getLabel() + " (Dist: " + current.distance + ")", "#38bdf8", false);
            panel.repaint();

            if (current == end) {
                logger.addLog("🎯 TARGET REACHED!", "#ef4444", true);
                break;
            }
            Thread.sleep(delay);

            for (Edge edge : graph.getEdges()) {
                if (edge.getFrom().equals(current)) {
                    Node neighbor = edge.getTo();
                    if (!neighbor.visited) {
                        edge.setColor(Color.YELLOW);
                        Edge r = graph.getEdge(neighbor, current);
                        if(r!=null) r.setColor(Color.YELLOW);
                        panel.repaint();
                        Thread.sleep(delay / 3);

                        int newDist = current.distance + edge.getWeight();
                        if (newDist < neighbor.distance) {
                            logger.addLog("   ⚡ Update " + neighbor.getLabel() + ": " + newDist, "#4ade80", false);
                            neighbor.distance = newDist;
                            neighbor.previous = current;
                            pq.add(neighbor);
                            if (neighbor != start && neighbor != end) neighbor.visualColor = Color.decode("#e879f9");
                        }
                    }
                }
            }
            if (current != start && current != end) current.visualColor = Color.decode("#1e40af");
        }

        logger.addSeparator();
        logger.addLog("🏁 FINAL PATH:", "#ffffff", true);
        Node step = end;
        List<String> pathList = new ArrayList<>();

        if (end.distance != Integer.MAX_VALUE) {
            while (step != null) {
                pathList.add(0, step.getLabel());
                if (step.previous != null) {
                    Edge e1 = graph.getEdge(step.previous, step);
                    Edge e2 = graph.getEdge(step, step.previous);
                    if (e1 != null) e1.setColor(Color.decode("#4ade80"));
                    if (e2 != null) e2.setColor(Color.decode("#4ade80"));
                }
                step.visualColor = Color.WHITE;
                step = step.previous;
            }
            if(start != null) start.visualColor = Color.WHITE;
            logger.addLog(String.join(" → ", pathList), "#facc15", true);
            logger.addLog("📏 Total Distance: " + end.distance, "#ffffff", true);
        } else {
            logger.addLog("❌ NO PATH FOUND", "#ef4444", true);
        }
        panel.repaint();
    }
}