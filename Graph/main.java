import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class Main {
    private static JPanel mainContainer;
    private static CardLayout cardLayout;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Cyber Graph Dijkstra v9.0 (Full Export)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 850);

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        // --- COMPONENTS ---
        Graph graph = new Graph();
        GraphSettings settings = new GraphSettings();
        GraphPanel graphPanel = new GraphPanel(graph, settings);
        LoggerPanel loggerPanel = new LoggerPanel();
        StateManager stateManager = new StateManager();
        GraphInputHandler inputHandler = new GraphInputHandler(graph, graphPanel, stateManager, settings);

        graphPanel.addMouseListener(inputHandler);
        graphPanel.addMouseMotionListener(inputHandler);
        graphPanel.setInputHandler(inputHandler);

        setupKeyBindings(graphPanel, graph, stateManager, inputHandler, settings);
        DijkstraSolver solver = new DijkstraSolver(graph, graphPanel, loggerPanel);

        // --- VIEW ---
        JPanel editorView = createEditorView(graph, graphPanel, inputHandler, solver, loggerPanel, frame, stateManager, settings);
        JPanel homePanel = createHomePanel();

        mainContainer.add(homePanel, "HOME");
        mainContainer.add(editorView, "EDITOR");
        frame.add(mainContainer);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void setupKeyBindings(JComponent comp, Graph g, StateManager sm, GraphInputHandler ih, GraphSettings gs) {
        InputMap im = comp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = comp.getActionMap();

        im.put(KeyStroke.getKeyStroke("control Z"), "undo");
        am.put("undo", new AbstractAction() { public void actionPerformed(ActionEvent e) { sm.undo(g, gs); comp.repaint(); }});

        im.put(KeyStroke.getKeyStroke("control Y"), "redo");
        am.put("redo", new AbstractAction() { public void actionPerformed(ActionEvent e) { sm.redo(g, gs); comp.repaint(); }});

        im.put(KeyStroke.getKeyStroke("control C"), "copy");
        am.put("copy", new AbstractAction() { public void actionPerformed(ActionEvent e) { sm.copy(ih.getMultiSelectedNodes(), g); }});

        im.put(KeyStroke.getKeyStroke("control V"), "paste");
        am.put("paste", new AbstractAction() { public void actionPerformed(ActionEvent e) { sm.paste(g, ih, gs); comp.repaint(); }});
    }

    private static JPanel createEditorView(Graph g, GraphPanel p, GraphInputHandler ih, DijkstraSolver s, LoggerPanel l, JFrame f, StateManager sm, GraphSettings gs) {
        JPanel view = new JPanel(new BorderLayout());
        view.add(p, BorderLayout.CENTER);
        view.add(l, BorderLayout.EAST);

        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        controls.setBackground(Color.decode("#1e293b"));
        controls.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel sliderP = new JPanel(new FlowLayout(FlowLayout.CENTER));
        sliderP.setOpaque(false);
        JLabel lbl = new JLabel("SPEED:");
        lbl.setForeground(Color.CYAN);
        JSlider slider = new JSlider(50, 2000, 600);
        slider.setBackground(Color.decode("#1e293b"));
        sliderP.add(lbl); sliderP.add(slider);

        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        btnP.setOpaque(false);
        JButton bHome = btn("🏠 HOME", "#475569");
        JButton bReset = btn("🗑 RESET", "#ef4444");
        JButton bRun = btn("▶ RUN", "#22c55e");
        JButton bExport = btn("💾 EXPORT", "#8b5cf6");
        JButton bHelp = btn("❓ HELP", "#3b82f6");

        btnP.add(bHome); btnP.add(bReset); btnP.add(bRun); btnP.add(bExport); btnP.add(bHelp);

        controls.add(sliderP); controls.add(btnP);
        view.add(controls, BorderLayout.SOUTH);

        bHome.addActionListener(e -> cardLayout.show(mainContainer, "HOME"));
        bReset.addActionListener(e -> { sm.saveState(g, gs); g.clear(); ih.resetSelection(); l.clear(); p.repaint(); });

        bRun.addActionListener(e -> {
            if(ih.getStartNode()==null || ih.getEndNode()==null) { JOptionPane.showMessageDialog(f, "Set Start & Target!"); return; }
            bRun.setEnabled(false); bReset.setEnabled(false);
            new SwingWorker<Void,Void>() {
                protected Void doInBackground() throws Exception { s.run(ih.getStartNode(), ih.getEndNode(), slider.getValue()); return null; }
                protected void done() { bRun.setEnabled(true); bReset.setEnabled(true); }
            }.execute();
        });

        // ACTION EXPORT
        bExport.addActionListener(e -> {
            String[] options = {"Save Project (.graph)", "Load Project", "Export Image (.png)", "Print / Save PDF"};
            int choice = JOptionPane.showOptionDialog(f, "Choose Method:", "Export Manager",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

            if (choice == 0) FileManager.saveProject(g, gs, f);
            else if (choice == 1) FileManager.loadProject(g, gs, p, l, f);
            else if (choice == 2) FileManager.exportImage(p, f);
            else if (choice == 3) FileManager.exportToPDF(p, l, f);
        });

        bHelp.addActionListener(e -> JOptionPane.showMessageDialog(f,
                "<html><body style='width:300px'><h2>CONTROLS</h2><ul>" +
                        "<li><b>Double Click:</b> Edit Title / Node Name / Weight</li>" +
                        "<li><b>Drag Area:</b> Group Selection</li>" +
                        "<li><b>Ctrl+C/V:</b> Copy Paste</li>" +
                        "<li><b>Ctrl+Z/Y:</b> Undo Redo</li>" +
                        "<li><b>Export:</b> Save/Load/PDF</li>" +
                        "</ul></body></html>"));

        return view;
    }

    private static JPanel createHomePanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.decode("#0f172a"));
        JLabel t = new JLabel("DIJKSTRA STUDIO");
        t.setFont(new Font("Impact", Font.BOLD, 64));
        t.setForeground(Color.decode("#38bdf8"));
        t.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel v = new JLabel("v9.0 Final");
        v.setForeground(Color.GRAY);
        v.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton b = btn("START", "#3b82f6");
        b.setFont(new Font("SansSerif", Font.BOLD, 20));
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.addActionListener(e -> cardLayout.show(mainContainer, "EDITOR"));
        p.add(Box.createVerticalGlue()); p.add(t); p.add(v); p.add(Box.createRigidArea(new Dimension(0,40))); p.add(b); p.add(Box.createVerticalGlue());
        return p;
    }

    private static JButton btn(String txt, String hex) {
        JButton b = new JButton(txt);
        b.setBackground(Color.decode(hex));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(10,20,10,20));
        return b;
    }
}
