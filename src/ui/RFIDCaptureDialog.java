package ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import logic.DatabaseManager;

public class RFIDCaptureDialog extends JDialog {

    private static final Color MAROON  = new Color(138, 26, 19);
    private static final Color GOLD    = new Color(248, 205, 0);
    private static final Color BG      = new Color(245, 243, 240);
    private static final Color BORDER  = new Color(220, 215, 210);
    private static final Color TEXT_DIM = new Color(130, 130, 130);
    private static final Color SUCCESS  = new Color(39, 174, 96);
    private static final Color DANGER   = new Color(192, 57, 43);

    private JTextField hiddenInput;
    private String capturedUid = null;
    private boolean confirmed  = false;

    public RFIDCaptureDialog(Frame parent, String studentName) {
        super(parent, "Tap RFID — Step 2 of 2", true);
        setSize(420, 360);
        setLocationRelativeTo(parent);
        setResizable(false);
        setUndecorated(true);
        buildUI(studentName);
    }

    private void buildUI(String studentName) {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(new LineBorder(BORDER, 1));
        root.setBackground(Color.WHITE);

        // ── Header ────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(MAROON);
        header.setBorder(new EmptyBorder(12, 16, 12, 16));

        JPanel titleStack = new JPanel(new GridLayout(2, 1, 0, 2));
        titleStack.setBackground(MAROON);

        JLabel lblTitle = new JLabel("Tap RFID Card");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Registering: " + studentName);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(new Color(240, 200, 200));

        titleStack.add(lblTitle);
        titleStack.add(lblSub);
        header.add(titleStack, BorderLayout.CENTER);

        JPanel goldBar = new JPanel();
        goldBar.setBackground(GOLD);
        goldBar.setPreferredSize(new Dimension(0, 3));

        JPanel hWrap = new JPanel(new BorderLayout());
        hWrap.add(header,  BorderLayout.CENTER);
        hWrap.add(goldBar, BorderLayout.SOUTH);

        // ── Body ──────────────────────────────────────────────
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(28, 32, 20, 32));

        // RFID illustration
        JPanel rfidIcon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                int cx = getWidth()/2, cy = getHeight()/2;
                // Signal arcs
                g2.setColor(new Color(220, 215, 210));
                g2.setStroke(new BasicStroke(3,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                for (int i = 1; i <= 3; i++) {
                    int r = i * 16;
                    g2.drawArc(cx-r, cy-r, r*2, r*2, 45,  90);
                    g2.drawArc(cx-r, cy-r, r*2, r*2, 225, 90);
                }
                // Card
                g2.setColor(MAROON);
                g2.fillRoundRect(cx-20, cy-28, 40, 56, 6, 6);
                g2.setColor(GOLD);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(cx-14, cy-10, 28, 16, 3, 3);
            }
        };
        rfidIcon.setBackground(Color.WHITE);
        rfidIcon.setPreferredSize(new Dimension(100, 100));
        rfidIcon.setMaximumSize(new Dimension(100, 100));
        rfidIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblInstruct = new JLabel("Please tap the student's RFID card");
        lblInstruct.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblInstruct.setForeground(new Color(40, 40, 40));
        lblInstruct.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblInstruct.setBorder(new EmptyBorder(12, 0, 4, 0));

        JLabel lblWaiting = new JLabel("Waiting for card...");
        lblWaiting.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblWaiting.setForeground(TEXT_DIM);
        lblWaiting.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Status label — updates after scan
        JLabel lblStatus = new JLabel(" ");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblStatus.setBorder(new EmptyBorder(8, 0, 0, 0));

        // Hidden input — captures RFID silently
        hiddenInput = new JTextField();
        hiddenInput.setPreferredSize(new Dimension(1, 1));
        hiddenInput.setMaximumSize(new Dimension(1, 1));
        hiddenInput.setOpaque(false);
        hiddenInput.setBorder(null);
        hiddenInput.setCaretColor(Color.WHITE); // invisible caret

        hiddenInput.addActionListener(e -> {
            String uid = hiddenInput.getText().trim();
            hiddenInput.setText("");
            if (uid.isEmpty()) return;

            // Check if UID already registered
            if (DatabaseManager.studentDb.containsKey(uid)) {
                lblStatus.setForeground(DANGER);
                lblStatus.setText("This card is already registered!");
                lblWaiting.setText("Please tap a different card.");
                return;
            }

            capturedUid = uid;
            lblStatus.setForeground(SUCCESS);
            lblStatus.setText("Card detected: " + uid);
            lblWaiting.setText("Ready to save.");

            // Auto-confirm after 1 second
            new javax.swing.Timer(1000, ev -> {
                confirmed = true;
                dispose();
            }) {{ setRepeats(false); }}.start();
        });

        body.add(rfidIcon);
        body.add(lblInstruct);
        body.add(lblWaiting);
        body.add(lblStatus);
        body.add(hiddenInput);   // invisible but present for focus

        // ── Footer ────────────────────────────────────────────
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footer.setBackground(new Color(245, 243, 240));
        footer.setBorder(new MatteBorder(1, 0, 0, 0, BORDER));

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCancel.setBackground(new Color(150, 150, 150));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setOpaque(true);
        btnCancel.setBorderPainted(false);
        btnCancel.setFocusPainted(false);
        btnCancel.setBorder(new EmptyBorder(8, 18, 8, 18));
        btnCancel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> dispose());

        footer.add(btnCancel);

        root.add(hWrap,   BorderLayout.NORTH);
        root.add(body,    BorderLayout.CENTER);
        root.add(footer,  BorderLayout.SOUTH);
        setContentPane(root);
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) {
            // Auto-focus hidden input so scanner goes straight here
            SwingUtilities.invokeLater(() -> hiddenInput.requestFocusInWindow());
        }
    }

    public boolean isConfirmed() { return confirmed; }
    public String  getRfidUid()  { return capturedUid; }
}
