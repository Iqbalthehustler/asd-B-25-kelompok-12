import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class GraphInputHandler extends MouseAdapter {
    private Graph graph;
    private GraphPanel panel;
    private StateManager stateManager;
    private GraphSettings settings;

    private List<Node> multiSelectedNodes = new ArrayList<>();
    private Point dragStartPoint = null;
    private Rectangle selectionBox = null;
    private boolean isAreaSelecting = false;
    private boolean hasMovedSincePress = false;
    private Node startNode = null;
    private Node endNode = null;

    public GraphInputHandler(Graph graph, GraphPanel panel, StateManager stateManager, GraphSettings settings) {
        this.graph = graph;
        this.panel = panel;
        this.stateManager = stateManager;
        this.settings = settings;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
            // Edit Judul
            if (e.getX() < 320 && e.getY() < 60) {
                String input = JOptionPane.showInputDialog("Project Title:", settings.getProjectTitle());
                if (input != null && !input.trim().isEmpty()) {
                    stateManager.saveState(graph, settings);
                    settings.setProjectTitle(input.toUpperCase());
                    panel.repaint();
                }
                return;
            }
            // Edit Node Label
            Node hitNode = findNodeAt(e.getX(), e.getY());
            if (hitNode != null) {
                String input = JOptionPane.showInputDialog("Rename Node:", hitNode.getLabel());
                if (input != null && !input.trim().isEmpty()) {
                    stateManager.saveState(graph, settings);
                    hitNode.setLabel(input);
                    panel.repaint();
                }
                return;
            }
            // Edit Bobot
            Edge hitEdge = graph.findEdgeAt(e.getX(), e.getY());
            if (hitEdge != null) {
                String input = JOptionPane.showInputDialog("Edit Weight:", hitEdge.getWeight());
                try {
                    int newWeight = Integer.parseInt(input);
                    stateManager.saveState(graph, settings);
                    hitEdge.setWeight(newWeight);
                    Edge rev = graph.getEdge(hitEdge.getTo(), hitEdge.getFrom());
                    if (rev != null) rev.setWeight(newWeight);
                    panel.repaint();
                } catch (Exception ex) {}
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        dragStartPoint = e.getPoint();
        hasMovedSincePress = false;
        Node clickedNode = findNodeAt(e.getX(), e.getY());

        if (SwingUtilities.isRightMouseButton(e)) {
            if (clickedNode != null) showPopupMenu(e, clickedNode);
            return;
        }
        if (clickedNode != null) {
            if (e.isControlDown()) {
                if (!multiSelectedNodes.isEmpty()) {
                    Node source = multiSelectedNodes.get(0);
                    if (source != clickedNode) {
                        stateManager.saveState(graph, settings);
                        String input = JOptionPane.showInputDialog("Edge Weight:");
                        try {
                            int weight = Integer.parseInt(input);
                            graph.addEdge(source, clickedNode, weight);
                        } catch (Exception ex) {}
                    }
                }
                multiSelectedNodes.clear(); multiSelectedNodes.add(clickedNode);
            } else {
                if (!multiSelectedNodes.contains(clickedNode)) {
                    multiSelectedNodes.clear(); multiSelectedNodes.add(clickedNode);
                }
            }
        } else {
            multiSelectedNodes.clear(); isAreaSelecting = true;
            selectionBox = new Rectangle(e.getX(), e.getY(), 0, 0);
        }
        panel.repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (isAreaSelecting) {
            int x = Math.min(dragStartPoint.x, e.getX());
            int y = Math.min(dragStartPoint.y, e.getY());
            selectionBox.setBounds(x, y, Math.abs(e.getX() - dragStartPoint.x), Math.abs(e.getY() - dragStartPoint.y));
            selectNodesInBox();
        } else if (!multiSelectedNodes.isEmpty()) {
            if (!hasMovedSincePress) {
                stateManager.saveState(graph, settings);
                hasMovedSincePress = true;
            }
            int dx = e.getX() - dragStartPoint.x;
            int dy = e.getY() - dragStartPoint.y;
            for (Node n : multiSelectedNodes) n.setPosition(n.getX() + dx, n.getY() + dy);
            dragStartPoint = e.getPoint();
        }
        panel.repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (isAreaSelecting) {
            isAreaSelecting = false;
            selectNodesInBox();
            if (selectionBox.width < 5 && selectionBox.height < 5) {
                if (!e.isControlDown()) {
                    stateManager.saveState(graph, settings);
                    String id = "N" + (graph.getNodes().size() + 1);
                    Node newNode = new Node(id, e.getX(), e.getY());
                    graph.addNode(newNode);
                    multiSelectedNodes.clear(); multiSelectedNodes.add(newNode);
                }
            }
            selectionBox = null;
        }
        dragStartPoint = null;
        hasMovedSincePress = false;
        panel.repaint();
    }

    private void selectNodesInBox() {
        multiSelectedNodes.clear();
        for (Node n : graph.getNodes()) {
            if (selectionBox.contains(n.getX(), n.getY())) multiSelectedNodes.add(n);
        }
    }

    private Node findNodeAt(int x, int y) {
        for (Node n : graph.getNodes()) {
            if (Math.abs(n.getX() - x) < 30 && Math.abs(n.getY() - y) < 25) return n;
        }
        return null;
    }

    public void forceSelectNodes(List<Node> newNodes) {
        multiSelectedNodes.clear(); multiSelectedNodes.addAll(newNodes);
        panel.repaint();
    }

    public List<Node> getMultiSelectedNodes() { return multiSelectedNodes; }
    public Rectangle getSelectionBox() { return selectionBox; }
    public boolean isAreaSelecting() { return isAreaSelecting; }
    public Node getStartNode() { return startNode; }
    public Node getEndNode() { return endNode; }
    public void resetSelection() { startNode = null; endNode = null; multiSelectedNodes.clear(); }

    private void showPopupMenu(MouseEvent e, Node node) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem itemStart = new JMenuItem("🚩 Set START");
        JMenuItem itemEnd = new JMenuItem("🎯 Set TARGET");
        JMenuItem itemDel = new JMenuItem("🗑 Delete");

        itemStart.addActionListener(ev -> { startNode = node; panel.repaint(); });
        itemEnd.addActionListener(ev -> { endNode = node; panel.repaint(); });
        itemDel.addActionListener(ev -> {
            stateManager.saveState(graph, settings);
            if (multiSelectedNodes.contains(node)) {
                List<Node> toRemove = new ArrayList<>(multiSelectedNodes);
                for (Node n : toRemove) graph.removeNode(n);
                multiSelectedNodes.clear();
            } else {
                graph.removeNode(node);
            }
            if (graph.findNode(node.getId()) == null) {
                if(startNode == node) startNode = null;
                if(endNode == node) endNode = null;
            }
            panel.repaint();
        });
        menu.add(itemStart); menu.add(itemEnd); menu.addSeparator(); menu.add(itemDel);
        menu.show(e.getComponent(), e.getX(), e.getY());
    }
}