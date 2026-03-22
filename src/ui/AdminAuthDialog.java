package ui;

import logic.DatabaseManager;
import model.AdminUser;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class AdminAuthDialog extends JDialog {

    private static final Color MAROON    = new Color(138, 26, 19);
    private static final Color GOLD      = new Color(248, 205, 0);
    private static final Color BG        = new Color(245, 243, 240);
    private static final Color CARD_BG   = Color.WHITE;
    private static final Color TEXT_MAIN = new Color(40, 40, 40);
    private static final Color TEXT_DIM  = new Color(130, 130, 130);
    private static final Color BORDER    = new Color(220, 215, 210);

    private boolean authenticated = false;
    private AdminUser foundAdmin  = null;
    private final boolean requireTotp;

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
        super(parent, "Admin Authentication", true);
        this.requireTotp = requireTotp;
        init();
    }

    public AdminAuthDialog(JFrame parent, boolean requireTotp) {
        super(parent, "Admin Authentication", true);
        this.requireTotp = requireTotp;
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

        JPanel goldBar = new JPanel();
        goldBar.setBackground(GOLD);
        goldBar.setPreferredSize(new Dimension(0, 3));

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

    // ── Step 1: Admin ID ──────────────────────────────────────
    private JPanel buildStep1() {
        JPanel panel = new JPanel();
        panel.setBackground(BG);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(28, 32, 24, 32));

        JLabel heading = new JLabel("Enter Admin ID");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 17));
        heading.setForeground(TEXT_MAIN);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ← Subtle hint difference — no mention of "step 1 of 2" for normal mode
        JLabel sub = new JLabel(requireTotp
            ? "Please enter your AuthID."
            : "Enter your ID to proceed.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(TEXT_DIM);
        sub.setBorder(new EmptyBorder(4, 0, 20, 0));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel fieldLabel = new JLabel("Admin ID");
        fieldLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        fieldLabel.setForeground(TEXT_DIM);
        fieldLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        adminIdField = new JTextField();
        adminIdField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        adminIdField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        adminIdField.setBackground(CARD_BG);
        adminIdField.setForeground(TEXT_MAIN);
        adminIdField.setCaretColor(MAROON);
        adminIdField.setBorder(new CompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));
        adminIdField.setAlignmentX(Component.LEFT_ALIGNMENT);
        adminIdField.addFocusListener(fieldFocusListener(adminIdField));
        adminIdField.addActionListener(e -> validateStep1());

        JPanel btnRow1 = new JPanel(new GridLayout(1, 2, 10, 0));
        btnRow1.setBackground(BG);
        btnRow1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnRow1.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton cancelBtn = buildSecondaryButton("Cancel");
        JButton nextBtn   = buildButton(requireTotp ? "Continue →" : "Authenticate");

        cancelBtn.addActionListener(e -> dispose());
        nextBtn.addActionListener(e -> validateStep1());

        btnRow1.add(cancelBtn);
        btnRow1.add(nextBtn);

        panel.add(heading);
        panel.add(sub);
        panel.add(fieldLabel);
        panel.add(Box.createVerticalStrut(6));
        panel.add(adminIdField);
        panel.add(Box.createVerticalStrut(20));
        panel.add(btnRow1);

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

        JButton backBtn   = buildSecondaryButton("← Back");
        JButton cancelBtn = buildSecondaryButton("Cancel");
        JButton verifyBtn = buildButton("Verify");

        backBtn.addActionListener(e -> {
            cardLayout.show(cardPanel, "step1");
            updateStepIndicator("Step 1 of 2");
            adminIdField.requestFocus();
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
        if (input.isEmpty()) { shakeField(adminIdField); return; }

        foundAdmin = null;
        for (AdminUser admin : DatabaseManager.adminDb) {
            if (admin.id.equalsIgnoreCase(input)) {
                foundAdmin = admin;
                break;
            }
        }

        if (foundAdmin == null) {
            adminIdField.setBorder(new CompoundBorder(
                new LineBorder(new Color(192, 57, 43), 2, true),
                new EmptyBorder(7, 11, 7, 11)
            ));
            shakeField(adminIdField);
            return; // ← silent fail, no error message — don't reveal why
        }

        if (requireTotp) {
            // Critical mode → go to TOTP step
            lblWelcome.setText("Welcome, " + foundAdmin.name + "!");
            totpField.setText("");
            cardLayout.show(cardPanel, "step2");
            updateStepIndicator("Step 2 of 2");
            totpField.requestFocus();
        } else {
            // Normal mode → ID is enough, done
            authenticated = true;
            dispose();
        }
    }

    private void validateStep2() {
        String code = new String(totpField.getPassword()).trim();
        if (code.isEmpty()) { shakeField(totpField); return; }

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
    private JButton buildButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(MAROON);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(100, 20, 15)); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(MAROON); }
        });
        return btn;
    }

    private JButton buildSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setBackground(new Color(230, 225, 220));
        btn.setForeground(TEXT_MAIN);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

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
