import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

public class LoggerPanel extends JPanel {
    private JTextPane textPane;
    private StyledDocument doc;

    public LoggerPanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(300, 0));
        setBackground(Color.decode("#0f172a"));
        setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.decode("#334155")));

        JLabel header = new JLabel("  📜 ACTIVITY LOG", JLabel.LEFT);
        header.setFont(new Font("Consolas", Font.BOLD, 16));
        header.setForeground(Color.decode("#38bdf8"));
        header.setPreferredSize(new Dimension(0, 40));
        add(header, BorderLayout.NORTH);

        textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setBackground(Color.decode("#1e293b"));
        textPane.setFont(new Font("Consolas", Font.PLAIN, 12));
        doc = textPane.getStyledDocument();
        add(new JScrollPane(textPane), BorderLayout.CENTER);
    }

    public void addLog(String text, String colorHex, boolean isBold) {
        SimpleAttributeSet key = new SimpleAttributeSet();
        StyleConstants.setForeground(key, Color.decode(colorHex));
        StyleConstants.setBold(key, isBold);
        try {
            doc.insertString(doc.getLength(), text + "\n", key);
            textPane.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) { e.printStackTrace(); }
    }

    public void addSeparator() { addLog("-------------------------", "#475569", false); }
    public void clear() { textPane.setText(""); }
    public String getLogContent() { return textPane.getText(); }
}