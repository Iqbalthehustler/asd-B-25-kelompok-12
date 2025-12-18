import java.util.*;

public class StateManager {
    private Stack<GraphState> undoStack = new Stack<>();
    private Stack<GraphState> redoStack = new Stack<>();
    private List<Node> clipboardNodes = new ArrayList<>();
    private List<Edge> clipboardEdges = new ArrayList<>();
    private static final int MAX_HISTORY = 50;

    public void saveState(Graph graph, GraphSettings settings) {
        if (undoStack.size() >= MAX_HISTORY) undoStack.remove(0);
        undoStack.push(new GraphState(graph, settings));
        redoStack.clear();
    }

    public void undo(Graph graph, GraphSettings settings) {
        if (undoStack.isEmpty()) return;
        redoStack.push(new GraphState(graph, settings));
        GraphState prev = undoStack.pop();
        restore(graph, settings, prev);
    }

    public void redo(Graph graph, GraphSettings settings) {
        if (redoStack.isEmpty()) return;
        undoStack.push(new GraphState(graph, settings));
        GraphState next = redoStack.pop();
        restore(graph, settings, next);
    }

    private void restore(Graph graph, GraphSettings settings, GraphState state) {
        graph.restoreState(state.nodesSnapshot, state.edgesSnapshot);
        settings.setProjectTitle(state.titleSnapshot);
    }

    public void copy(List<Node> selectedNodes, Graph graph) {
        clipboardNodes.clear(); clipboardEdges.clear();
        if (selectedNodes.isEmpty()) return;
        Map<String, Node> nodeMap = new HashMap<>();

        for (Node n : selectedNodes) {
            Node copyNode = new Node(n.getId(), n.getX(), n.getY());
            copyNode.setLabel(n.getLabel());
            clipboardNodes.add(copyNode);
            nodeMap.put(n.getId(), copyNode);
        }
        for (Edge e : graph.getEdges()) {
            if (selectedNodes.contains(e.getFrom()) && selectedNodes.contains(e.getTo())) {
                clipboardEdges.add(new Edge(nodeMap.get(e.getFrom().getId()), nodeMap.get(e.getTo().getId()), e.getWeight()));
            }
        }
    }

    public void paste(Graph graph, GraphInputHandler inputHandler, GraphSettings settings) {
        if (clipboardNodes.isEmpty()) return;
        saveState(graph, settings);
        Map<Node, Node> pasteMap = new HashMap<>();
        List<Node> newNodes = new ArrayList<>();

        for (Node clipNode : clipboardNodes) {
            String newId = "N" + (System.currentTimeMillis() % 100000) + "_" + newNodes.size();
            Node newNode = new Node(newId, clipNode.getX() + 30, clipNode.getY() + 30);
            newNode.setLabel(clipNode.getLabel());
            graph.addNode(newNode);
            newNodes.add(newNode);
            pasteMap.put(clipNode, newNode);
        }
        for (Edge clipEdge : clipboardEdges) {
            Node newFrom = pasteMap.get(clipEdge.getFrom());
            Node newTo = pasteMap.get(clipEdge.getTo());
            if (newFrom != null && newTo != null) graph.addEdge(newFrom, newTo, clipEdge.getWeight());
        }
        inputHandler.forceSelectNodes(newNodes);
    }

    public static class GraphState {
        public List<Node> nodesSnapshot = new ArrayList<>();
        public List<Edge> edgesSnapshot = new ArrayList<>();
        public String titleSnapshot;

        public GraphState(Graph graph, GraphSettings settings) {
            this.titleSnapshot = settings.getProjectTitle();
            Map<Node, Node> mapping = new HashMap<>();

            for (Node n : graph.getNodes()) {
                Node copy = new Node(n.getId(), n.getX(), n.getY());
                copy.setLabel(n.getLabel());
                copy.distance = n.distance;
                mapping.put(n, copy);
                nodesSnapshot.add(copy);
            }
            for (Edge e : graph.getEdges()) {
                Node from = mapping.get(e.getFrom());
                Node to = mapping.get(e.getTo());
                Edge edgeCopy = new Edge(from, to, e.getWeight());
                edgeCopy.setColor(e.getColor());
                edgesSnapshot.add(edgeCopy);
            }
        }
    }
}