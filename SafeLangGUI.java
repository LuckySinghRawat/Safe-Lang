import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SafeLangGUI {

    // Themes
    private static final Color BG_APP = new Color(0x121212);
    private static final Color BG_EDITOR = new Color(0x1A1A1A);
    private static final Color FG_TEXT = new Color(0x4FC3F7); // Blue for rest of code
    private static final Color BORDER_COLOR = new Color(0x2A2A2A);

    private static final Color COLOR_KEYWORD = new Color(0xFF5C5C); // Red for keywords

    private static final Color BTN_BG = new Color(0x8B0000);
    private static final Color BTN_HOVER = new Color(0xB22222);

    private JTextPane codeArea;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new SafeLangGUI().createUI());
    }

    private void createUI() {
        JFrame frame = new JFrame("Safe-Lang IDE");
        frame.setSize(1100, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(BG_APP);
        frame.setLayout(new BorderLayout());

        // ===== TOP PANEL (Run Button) =====
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        topPanel.setBackground(BG_APP);
        topPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        JButton runButton = new RoundedButton("Run Code");
        topPanel.add(runButton);
        frame.add(topPanel, BorderLayout.NORTH);

        // ===== LEFT PANEL (Code Editor) =====
        JPanel leftPanel = createPanelWithTitle("CODE EDITOR");
        
        codeArea = new JTextPane();
        codeArea.setBackground(BG_EDITOR);
        codeArea.setForeground(FG_TEXT);
        codeArea.setCaretColor(Color.WHITE);
        codeArea.setFont(new Font("Consolas", Font.PLAIN, 15));
        codeArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Line Numbers (Bonus)
        JTextArea lineNumbers = new JTextArea("1");
        lineNumbers.setBackground(new Color(0x161616));
        lineNumbers.setForeground(new Color(0x666666));
        lineNumbers.setFont(new Font("Consolas", Font.PLAIN, 15));
        lineNumbers.setEditable(false);
        lineNumbers.setBorder(new EmptyBorder(10, 5, 10, 5));
        lineNumbers.setFocusable(false);

        // Document Listener for Syntax Highlighting and Line Numbers
        codeArea.getDocument().addDocumentListener(new DocumentListener() {
            public String getText() {
                int caretPosition = codeArea.getDocument().getLength();
                Element root = codeArea.getDocument().getDefaultRootElement();
                StringBuilder txt = new StringBuilder("1\n");
                for (int i = 2; i <= root.getElementIndex(caretPosition) + 1; i++) {
                    txt.append(i).append("\n");
                }
                return txt.toString();
            }

            @Override
            public void insertUpdate(DocumentEvent e) { update(); }
            @Override
            public void removeUpdate(DocumentEvent e) { update(); }
            @Override
            public void changedUpdate(DocumentEvent e) { }

            private void update() {
                lineNumbers.setText(getText());
                SwingUtilities.invokeLater(() -> highlightSyntax());
            }
        });

        JScrollPane codeScroll = createScrollPane(codeArea);
        codeScroll.setRowHeaderView(lineNumbers);
        leftPanel.add(codeScroll, BorderLayout.CENTER);

        // ===== RIGHT TOP (Tokens Table) =====
        JPanel tokenPanel = createPanelWithTitle("TOKENS");
        
        String[] columns = { "TYPE", "VALUE" };
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable tokenTable = new JTable(tableModel);
        tokenTable.setBackground(BG_EDITOR);
        tokenTable.setForeground(FG_TEXT);
        tokenTable.setFont(new Font("Consolas", Font.PLAIN, 13));
        tokenTable.setGridColor(BORDER_COLOR);
        tokenTable.setFillsViewportHeight(true);
        tokenTable.setBorder(null);
        tokenTable.setRowHeight(25);

        JTableHeader header = tokenTable.getTableHeader();
        header.setBackground(new Color(0x222222));
        header.setForeground(new Color(0xAAAAAA));
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBorder(new LineBorder(BORDER_COLOR));

        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
        cellRenderer.setBorder(new EmptyBorder(0, 5, 0, 5));
        tokenTable.setDefaultRenderer(Object.class, cellRenderer);

        JScrollPane tokenScroll = createScrollPane(tokenTable);
        tokenPanel.add(tokenScroll, BorderLayout.CENTER);

        // ===== RIGHT BOTTOM (Output Area) =====
        JPanel outputPanel = createPanelWithTitle("OUTPUT CONSOLE");
        
        JTextArea outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setBackground(BG_EDITOR);
        outputArea.setForeground(FG_TEXT);
        outputArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        outputArea.setMargin(new Insets(10, 10, 10, 10));
        outputArea.setBorder(null);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);

        JScrollPane outputScroll = createScrollPane(outputArea);
        outputPanel.add(outputScroll, BorderLayout.CENTER);

        // Split panes
        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tokenPanel, outputPanel);
        styleSplitPane(rightSplit);
        rightSplit.setDividerLocation(300);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightSplit);
        styleSplitPane(mainSplit);
        mainSplit.setDividerLocation(650);

        // Wrap main split pane to add margins
        JPanel mainWrapper = new JPanel(new BorderLayout());
        mainWrapper.setBackground(BG_APP);
        mainWrapper.setBorder(new EmptyBorder(0, 10, 10, 10));
        mainWrapper.add(mainSplit, BorderLayout.CENTER);

        frame.add(mainWrapper, BorderLayout.CENTER);

        // ===== RUN BUTTON ACTION =====
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tableModel.setRowCount(0);
                outputArea.setForeground(FG_TEXT); // reset
                outputArea.setText("");

                String code = codeArea.getText();
                try {
                    lexer lex = new lexer(code);
                    List<token> tokens = lex.tokenize();

                    for (token t : tokens) {
                        tableModel.addRow(new Object[] { t.getCategory(), t.value });
                    }

                    if (tokens.size() <= 1) { // Only EOF token
                        outputArea.setText("No code provided.");
                    } else {
                        parser parser = new parser(tokens);
                        parser.parse();
                        String result = parser.getOutput();
                        
                        if (result == null || result.trim().isEmpty()) {
                            outputArea.setText("Execution Successful!\n(No output generated)");
                        } else {
                            outputArea.setText(result);
                        }
                    }

                } catch (Exception ex) {
                    outputArea.setForeground(COLOR_KEYWORD); // Error in red
                    outputArea.setText("Error: " + ex.getMessage());
                }
            }
        });

        frame.setVisible(true);
    }

    private JPanel createPanelWithTitle(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_APP);
        
        JLabel label = new JLabel(" " + title);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(new Color(0x888888));
        label.setBorder(new EmptyBorder(5, 5, 8, 5));
        
        panel.add(label, BorderLayout.NORTH);
        return panel;
    }

    private JScrollPane createScrollPane(Component view) {
        JScrollPane scroll = new JScrollPane(view);
        scroll.setBorder(new LineBorder(BORDER_COLOR, 1));
        scroll.getVerticalScrollBar().setBackground(BG_APP);
        scroll.getHorizontalScrollBar().setBackground(BG_APP);
        scroll.getVerticalScrollBar().setUI(new MinimalScrollBarUI());
        scroll.getHorizontalScrollBar().setUI(new MinimalScrollBarUI());
        return scroll;
    }

    private void styleSplitPane(JSplitPane splitPane) {
        splitPane.setBorder(null);
        splitPane.setDividerSize(6);
        splitPane.setBackground(BG_APP);
        BasicSplitPaneUI ui = new BasicSplitPaneUI() {
            @Override
            public BasicSplitPaneDivider createDefaultDivider() {
                return new BasicSplitPaneDivider(this) {
                    @Override
                    public void paint(Graphics g) {
                        g.setColor(BG_APP);
                        g.fillRect(0, 0, getSize().width, getSize().height);
                    }
                };
            }
        };
        splitPane.setUI(ui);
    }

    private boolean isHighlighting = false;

    private void highlightSyntax() {
        if (isHighlighting) return;
        isHighlighting = true;
        
        try {
            StyledDocument doc = codeArea.getStyledDocument();
            String code = doc.getText(0, doc.getLength());
            
            // Default text
            Style normalStyle = codeArea.getStyle("Normal");
            if (normalStyle == null) normalStyle = codeArea.addStyle("Normal", null);
            StyleConstants.setForeground(normalStyle, FG_TEXT);
            doc.setCharacterAttributes(0, code.length(), normalStyle, true);

            // Highlight keywords
            highlightPattern(doc, code, "\\b(Safelet|Safeprint|Safeif|Safeelse|Safewhile|safeinput|int|str)\\b", COLOR_KEYWORD);

        } catch (BadLocationException e) {
            e.printStackTrace();
        } finally {
            isHighlighting = false;
        }
    }

    private void highlightPattern(StyledDocument doc, String text, String patternStr, Color color) {
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(text);
        
        Style style = codeArea.addStyle("Highlight", null);
        StyleConstants.setForeground(style, color);
        
        while (matcher.find()) {
            doc.setCharacterAttributes(matcher.start(), matcher.end() - matcher.start(), style, false);
        }
    }

    // Custom Rounded Button
    class RoundedButton extends JButton {
        public RoundedButton(String text) {
            super("  " + text + "  "); // padding
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setBackground(BTN_BG);
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setBackground(BTN_HOVER);
                    repaint();
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    setBackground(BTN_BG);
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            super.paintComponent(g);
            g2.dispose();
        }
    }

    // Custom ScrollBar UI
    class MinimalScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(0x444444);
            this.trackColor = BG_APP;
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isDragging ? new Color(0x666666) : thumbColor);
            g2.fillRoundRect(thumbBounds.x + 2, thumbBounds.y + 2, thumbBounds.width - 4, thumbBounds.height - 4, 8, 8);
            g2.dispose();
        }
        
        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            g.setColor(trackColor);
            g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        }
    }
}