package ui;

import logic.DatabaseManager;
import model.AdminUser;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class AdminAuthDialog extends JDialog {

    private static final Color MAROON    = UIBuilder.MAROON;
    private static final Color GOLD      = UIBuilder.GOLD;
    private static final Color BG        = UIBuilder.BG;
    private static final Color CARD_BG   = UIBuilder.CARD_BG;
    private static final Color TEXT_MAIN = UIBuilder.TEXT_MAIN;
    private static final Color TEXT_DIM  = UIBuilder.TEXT_DIM;
    private static final Color BORDER    = UIBuilder.BORDER;

    private boolean authenticated = false;
    private AdminUser foundAdmin  = null;
    private final boolean requireTotp;
    private String customSubtitle = null;

    private JPanel cardPanel;
    private CardLayout cardLayout;

    private JTextField    adminIdField;
    private JPasswordField totpField;
    private JLabel        lblWelcome;

    // ── Normal auth (ID only) ─────────────────────────────────
    public AdminAuthDialog(Frame parent) {
        this(parent, false);
    }

    public AdminAuthDialog(JFrame parent) {
        this(parent, false);
    }

    // ── Critical auth (ID + TOTP) ─────────────────────────────
    public AdminAuthDialog(Frame parent, boolean requireTotp) {
        this(parent, requireTotp, null);
    }

    public AdminAuthDialog(JFrame parent, boolean requireTotp) {
        this(parent, requireTotp, null);
    }

    // ── Auth with Custom Subtitle (e.g. Manual Override Auth) ──
    public AdminAuthDialog(Frame parent, boolean requireTotp, String customSubtitle) {
        super(parent, "Admin Authentication", true);
        this.requireTotp = requireTotp;
        this.customSubtitle = customSubtitle;
        init();
    }

    private void init() {
        setSize(420, 320);
        setLocationRelativeTo(getOwner());
        setResizable(false);
        setUndecorated(true);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());

        buildTitleBar();
        buildCards();
        
        // Auto-focus the invisible RFID input
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                adminIdField.requestFocusInWindow();
            }
        });
    }

    // ── Title Bar ─────────────────────────────────────────────
    private void buildTitleBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(MAROON);
        bar.setBorder(new EmptyBorder(12, 18, 12, 18));

        JLabel title = new JLabel("Admin Authentication");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(Color.WHITE);

        JLabel stepIndicator = new JLabel(requireTotp ? "Step 1 of 2" : "");
        stepIndicator.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        stepIndicator.setForeground(new Color(240, 200, 200));
        stepIndicator.setName("stepIndicator");

        bar.add(title,         BorderLayout.WEST);
        bar.add(stepIndicator, BorderLayout.EAST);

        // REFACTORED: Use UIBuilder for standard gold bar
        JPanel goldBar = UIBuilder.createGoldBar();

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(bar,     BorderLayout.CENTER);
        wrapper.add(goldBar, BorderLayout.SOUTH);
        add(wrapper, BorderLayout.NORTH);

        // Draggable
        final Point[] drag = {null};
        bar.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { drag[0] = e.getPoint(); }
        });
        bar.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                Point loc = getLocation();
                setLocation(loc.x + e.getX() - drag[0].x,
                            loc.y + e.getY() - drag[0].y);
            }
        });
    }

    // ── Cards ─────────────────────────────────────────────────
    private void buildCards() {
        cardLayout = new CardLayout();
        cardPanel  = new JPanel(cardLayout);
        cardPanel.setBackground(BG);

        cardPanel.add(buildStep1(), "step1");
        cardPanel.add(buildStep2(), "step2");

        add(cardPanel, BorderLayout.CENTER);
    }

    private JLabel lblStep1Sub; // Class-level variable to update text

    // ── Step 1: Admin ID ──────────────────────────────────────
    private JPanel buildStep1() {
        JPanel panel = new JPanel();
        panel.setBackground(BG);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(16, 32, 24, 32));

        JLabel heading = new JLabel("Admin Auth");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 17));
        heading.setForeground(TEXT_MAIN);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblStep1Sub = new JLabel(
            customSubtitle != null ? customSubtitle
            : (requireTotp
                ? "Tap Admin Card (TOTP required next)."
                : "Please tap your Admin Card.")
        );
        lblStep1Sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStep1Sub.setForeground(TEXT_DIM);
        lblStep1Sub.setBorder(new EmptyBorder(4, 0, 16, 0));
        lblStep1Sub.setAlignmentX(Component.CENTER_ALIGNMENT);

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
                g2.setColor(BORDER);
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
        rfidIcon.setBackground(BG);
        rfidIcon.setPreferredSize(new Dimension(100, 100));
        rfidIcon.setMaximumSize(new Dimension(100, 100));
        rfidIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        adminIdField = new JTextField();
        adminIdField.setPreferredSize(new Dimension(1, 1));
        adminIdField.setMaximumSize(new Dimension(1, 1));
        adminIdField.setOpaque(false);
        adminIdField.setBorder(null);
        adminIdField.setForeground(BG); // invisible text
        adminIdField.setCaretColor(BG); // invisible caret
        adminIdField.addFocusListener(fieldFocusListener(adminIdField));
        adminIdField.addActionListener(e -> validateStep1());

        JPanel btnRow1 = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnRow1.setBackground(BG);
        btnRow1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnRow1.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton cancelBtn = UIBuilder.createToolbarButton("Cancel", new Color(230, 225, 220), TEXT_MAIN);
        cancelBtn.setPreferredSize(new Dimension(100, 32));

        cancelBtn.addActionListener(e -> dispose());

        btnRow1.add(cancelBtn);

        panel.add(heading);
        panel.add(lblStep1Sub);
        panel.add(Box.createVerticalStrut(10));
        panel.add(rfidIcon);
        panel.add(adminIdField); // hidden
        panel.add(Box.createVerticalStrut(20));
        panel.add(btnRow1);

        // Auto focus hidden field when panel clicked
        panel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { adminIdField.requestFocusInWindow(); }
        });

        return panel;
    }

    // ── Step 2: TOTP (critical only) ─────────────────────────
    private JPanel buildStep2() {
        JPanel panel = new JPanel();
        panel.setBackground(BG);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(28, 32, 24, 32));

        lblWelcome = new JLabel("Welcome!");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblWelcome.setForeground(MAROON);
        lblWelcome.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Enter the 6-digit code from your Authenticator app.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(TEXT_DIM);
        sub.setBorder(new EmptyBorder(4, 0, 20, 0));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel fieldLabel = new JLabel("Authenticator Code");
        fieldLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        fieldLabel.setForeground(TEXT_DIM);
        fieldLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        totpField = new JPasswordField();
        totpField.setFont(new Font("Segoe UI", Font.BOLD, 22));
        totpField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        totpField.setBackground(CARD_BG);
        totpField.setForeground(TEXT_MAIN);
        totpField.setCaretColor(MAROON);
        totpField.setHorizontalAlignment(JTextField.CENTER);
        totpField.setBorder(new CompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));
        totpField.setAlignmentX(Component.LEFT_ALIGNMENT);
        totpField.addFocusListener(fieldFocusListener(totpField));
        totpField.addActionListener(e -> validateStep2());

        JPanel btnRow = new JPanel(new GridLayout(1, 3, 8, 0));
        btnRow.setBackground(BG);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        // REFACTORED: Use UIBuilder for buttons
        JButton backBtn   = UIBuilder.createToolbarButton("← Back", new Color(230, 225, 220), TEXT_MAIN);
        JButton cancelBtn = UIBuilder.createToolbarButton("Cancel", new Color(230, 225, 220), TEXT_MAIN);
        JButton verifyBtn = UIBuilder.createToolbarButton("Verify", MAROON, Color.WHITE);

        backBtn.addActionListener(e -> {
            foundAdmin = null;
            adminIdField.setText("");
            cardLayout.show(cardPanel, "step1");
            updateStepIndicator("Step 1 of 2");
            adminIdField.requestFocusInWindow();
        });
        
        cancelBtn.addActionListener(e -> dispose());
        verifyBtn.addActionListener(e -> validateStep2());
        
        btnRow.add(backBtn);
        btnRow.add(cancelBtn);
        btnRow.add(verifyBtn);

        panel.add(lblWelcome);
        panel.add(sub);
        panel.add(fieldLabel);
        panel.add(Box.createVerticalStrut(6));
        panel.add(totpField);
        panel.add(Box.createVerticalStrut(20));
        panel.add(btnRow);

        return panel;
    }

    // ── Validation ────────────────────────────────────────────
    private void validateStep1() {
        String input = adminIdField.getText().trim();
        if (input.isEmpty()) return;

        foundAdmin = DatabaseManager.authenticateAdmin(input);

        if (foundAdmin == null) {
            adminIdField.setText(""); // clear for next scan attempt
            lblStep1Sub.setText("Invalid Card. Please try again.");
            lblStep1Sub.setForeground(new Color(192, 57, 43)); // DANGER color
            Toolkit.getDefaultToolkit().beep(); // give feedback
            
            // Return back to normal instructions after 2 seconds
            new javax.swing.Timer(2000, e -> {
                lblStep1Sub.setText(requireTotp
                    ? "Tap Admin Card (TOTP required next)."
                    : "Please tap your Admin Card.");
                lblStep1Sub.setForeground(TEXT_DIM);
            }) {{ setRepeats(false); }}.start();
            return;
        }

        if (requireTotp && !DatabaseManager.devBypassTOTP) {
            // Critical mode → go to TOTP step
            lblWelcome.setText("Welcome, " + foundAdmin.name + "!");
            totpField.setText("");
            cardLayout.show(cardPanel, "step2");
            updateStepIndicator("Step 2 of 2");
            totpField.requestFocus();
        } else {
            // Normal mode OR Dev mode bypass → ID is enough, done
            authenticated = true;
            dispose();
        }
    }

    private void validateStep2() {
        String code = new String(totpField.getPassword()).trim();
        if (code.isEmpty()) { shakeField(totpField); return; }

        // --- MASTER OVERRIDE KEY ---
        // You can change this to any hidden code you want.
        // It bypasses the TOTP requirement for authorized admins.
        if (code.equalsIgnoreCase("dalta-molino")) {
            authenticated = true;
            dispose();
            return;
        }

        try {
            String generated = logic.SimpleTOTP.getTOTPCode(foundAdmin.secretKey);
            if (generated.equals(code)) {
                authenticated = true;
                dispose();
            } else {
                totpField.setText("");
                totpField.setBorder(new CompoundBorder(
                    new LineBorder(new Color(192, 57, 43), 2, true),
                    new EmptyBorder(7, 11, 7, 11)
                ));
                shakeField(totpField);
                // ← Also silent — no popup
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "TOTP error: " + ex.getMessage(), "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Helpers ───────────────────────────────────────────────
    private FocusAdapter fieldFocusListener(JTextField field) {
        return new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                field.setBorder(new CompoundBorder(
                    new LineBorder(MAROON, 2, true),
                    new EmptyBorder(7, 11, 7, 11)
                ));
            }
            public void focusLost(FocusEvent e) {
                field.setBorder(new CompoundBorder(
                    new LineBorder(BORDER, 1, true),
                    new EmptyBorder(8, 12, 8, 12)
                ));
            }
        };
    }

    private void updateStepIndicator(String text) {
        for (Component c : ((JPanel)((JPanel) getContentPane()
                .getComponent(0)).getComponent(0)).getComponents()) {
            if (c instanceof JLabel && "stepIndicator".equals(c.getName())) {
                ((JLabel) c).setText(text);
                break;
            }
        }
    }

    private void shakeField(JComponent field) {
        Point orig = field.getLocation();
        javax.swing.Timer t = new javax.swing.Timer(30, null);
        int[] count = {0};
        int[] offsets = {-8, 8, -6, 6, -4, 4, 0};
        t.addActionListener(e -> {
            if (count[0] < offsets.length) {
                field.setLocation(orig.x + offsets[count[0]++], orig.y);
            } else {
                field.setLocation(orig);
                t.stop();
            }
        });
        t.start();
        field.requestFocus();
    }

    public boolean isAuthenticated() { return authenticated; }
}
