package ui;

import logic.DatabaseManager;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;

public class SplashScreen extends JWindow {

    private static final Color MAROON   = UIBuilder.MAROON;
    private static final Color GOLD     = UIBuilder.GOLD;
    private static final Color CARD_BG  = UIBuilder.CARD_BG;
    private static final Color TEXT_DIM = UIBuilder.TEXT_DIM;
    private static final Color BORDER   = UIBuilder.BORDER;
    private static final Color DANGER   = UIBuilder.DANGER;

    private JLabel    lblStep;
    private JLabel    lblSubStep;
    private JPanel    progressTrack;
    private JPanel    progressFill;
    private JPanel    body;
    private Runnable  onReady;

    private boolean   retryShown  = false;
    private JButton   btnRetry    = null;
    private Component retryStrut  = null;

    public SplashScreen(Runnable onReady) {
        this.onReady = onReady;
        setSize(560, 380);
        setLocationRelativeTo(null);
        buildUI();
        setVisible(true);
        SwingUtilities.invokeLater(() ->
            setShape(new RoundRectangle2D.Double(
                0, 0, getWidth(), getHeight(), 20, 20)));
    }

    // =========================================================
    //  UI
    // =========================================================
    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CARD_BG);
        root.setBorder(new LineBorder(BORDER, 1));

        // ── Gold top stripe ───────────────────────────────────
        JPanel goldTop = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(GOLD);
                g2.fill(new RoundRectangle2D.Double(
                    0, 0, getWidth(), getHeight() * 2, 20, 20));
                g2.dispose();
            }
        };
        goldTop.setOpaque(false);
        goldTop.setPreferredSize(new Dimension(0, 5));

        // ── Header ────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(MAROON);
        header.setBorder(new EmptyBorder(20, 28, 20, 28));

        JPanel titleStack = new JPanel(new GridLayout(3, 1, 0, 3));
        titleStack.setBackground(MAROON);

        JLabel line1 = new JLabel("UNIVERSITY OF PERPETUAL HELP SYSTEM DALTA");
        line1.setFont(new Font("Impact", Font.PLAIN, 16));
        line1.setForeground(GOLD);

        JLabel line2 = new JLabel("Molino Campus  ·  Senior High School Department");
        line2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        line2.setForeground(new Color(255, 240, 200));

        JLabel line3 = new JLabel("Attendance System  v4.0");
        line3.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        line3.setForeground(new Color(220, 180, 180));

        titleStack.add(line1);
        titleStack.add(line2);
        titleStack.add(line3);

        JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        logoRow.setBackground(MAROON);
        logoRow.add(loadLogo("/30th.png",       60, 60));
        logoRow.add(loadLogo("/uphsd_logo.png", 40, 60));
        logoRow.add(loadLogo("/shs.png",        60, 60));

        header.add(titleStack, BorderLayout.CENTER);
        header.add(logoRow,    BorderLayout.EAST);

        JPanel hWrap = new JPanel(new BorderLayout());
        hWrap.add(goldTop, BorderLayout.NORTH);
        hWrap.add(header,  BorderLayout.CENTER);

        // ── Body ──────────────────────────────────────────────
        body = new JPanel();
        body.setBackground(CARD_BG);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(28, 32, 24, 32));

        lblStep = new JLabel("Initializing...");
        lblStep.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblStep.setForeground(MAROON);
        lblStep.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblSubStep = new JLabel("Please wait.");
        lblSubStep.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubStep.setForeground(TEXT_DIM);
        lblSubStep.setBorder(new EmptyBorder(4, 0, 20, 0));
        lblSubStep.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ── Progress bar ──────────────────────────────────────
        progressTrack = new JPanel(null);
        progressTrack.setBackground(new Color(230, 225, 220));
        progressTrack.setMaximumSize(new Dimension(Integer.MAX_VALUE, 6));
        progressTrack.setPreferredSize(new Dimension(0, 6));
        progressTrack.setAlignmentX(Component.LEFT_ALIGNMENT);

        progressFill = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(MAROON);
                g2.fill(new RoundRectangle2D.Double(
                    0, 0, getWidth(), getHeight(), 6, 6));
                g2.dispose();
            }
        };
        progressFill.setOpaque(false);
        progressFill.setBounds(0, 0, 0, 6);
        progressTrack.add(progressFill);

        // ── Step indicators ───────────────────────────────────
        JPanel steps = new JPanel(new GridLayout(1, 3, 12, 0));
        steps.setBackground(CARD_BG);
        steps.setBorder(new EmptyBorder(18, 0, 0, 0));
        steps.setAlignmentX(Component.LEFT_ALIGNMENT);
        steps.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        steps.add(buildStepDot("1", "Connect", "Database"));
        steps.add(buildStepDot("2", "Load",    "Records"));
        steps.add(buildStepDot("3", "Prepare", "Interface"));

        // ── Footer ────────────────────────────────────────────
        JLabel lblVersion = new JLabel("Made with love by ICT 11-02");
        lblVersion.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblVersion.setForeground(new Color(180, 175, 170));
        lblVersion.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblVersion.setBorder(new EmptyBorder(18, 0, 0, 0));

        body.add(lblStep);
        body.add(lblSubStep);
        body.add(progressTrack);
        body.add(steps);
        body.add(Box.createVerticalGlue());
        body.add(lblVersion);

        root.add(hWrap, BorderLayout.NORTH);
        root.add(body,  BorderLayout.CENTER);
        setContentPane(root);
    }

    // =========================================================
    //  HELPERS
    // =========================================================
    private JLabel loadLogo(String path, int w, int h) {
        java.net.URL url = getClass().getResource(path);
        if (url != null) {
            Image scaled = new ImageIcon(url).getImage()
                .getScaledInstance(w, h, Image.SCALE_SMOOTH);
            JLabel lbl = new JLabel(new ImageIcon(scaled));
            lbl.setPreferredSize(new Dimension(w, h));
            return lbl;
        }
        JLabel lbl = new JLabel("");
        lbl.setPreferredSize(new Dimension(w, h));
        return lbl;
    }

    private JPanel buildStepDot(String num, String title, String sub) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setBackground(CARD_BG);

        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(230, 225, 220));
                g2.fillOval(0, 0, 28, 28);
                g2.setColor(TEXT_DIM);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(num,
                    (28 - fm.stringWidth(num)) / 2,
                    (28 + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        dot.setOpaque(false);
        dot.setPreferredSize(new Dimension(28, 28));

        JPanel text = new JPanel(new GridLayout(2, 1, 0, 1));
        text.setBackground(CARD_BG);

        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 11));
        t.setForeground(TEXT_DIM);

        JLabel s = new JLabel(sub);
        s.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        s.setForeground(new Color(180, 175, 170));

        text.add(t);
        text.add(s);
        p.add(dot,  BorderLayout.WEST);
        p.add(text, BorderLayout.CENTER);
        return p;
    }

    // =========================================================
    //  STEP UPDATER
    // =========================================================
    private void setStep(String step, String sub, int progressPct) {
        SwingUtilities.invokeLater(() -> {
            lblStep.setText(step);
            lblSubStep.setText(sub);

            int targetW = (int)(progressTrack.getWidth() * (progressPct / 100.0));
            Timer t = new Timer(10, null);
            int[] curW = { progressFill.getWidth() };
            t.addActionListener(e -> {
                curW[0] = Math.min(curW[0] + 6, targetW);
                progressFill.setBounds(0, 0, curW[0], 6);
                progressTrack.repaint();
                if (curW[0] >= targetW) ((Timer) e.getSource()).stop();
            });
            t.start();
        });
    }

    // =========================================================
    //  RUN SEQUENCE
    // =========================================================
    public void start() {
        new Thread(() -> {
            try {

                // ── Step 1: Connect ───────────────────────────
                setStep("Connecting to database...",
                    "Establishing connection to MariaDB server.", 10);
                Thread.sleep(600);

                boolean connected;
                String  connectError = "";
                try {
                    DatabaseManager.getConnection().close();
                    connected = true;
                } catch (SQLException ex) {
                    connected = false;
                    String m = ex.getMessage() != null
                        ? ex.getMessage().toLowerCase() : "";
                    if (m.contains("access denied"))
                        connectError = "ERR 01 - Please contact your administrator.";
                    else if (m.contains("communications link") || m.contains("timed out"))
                        connectError = "Cannot reach server (ERR 02) — Please contact your administrator.";
                    else if (m.contains("unknown database"))
                        connectError = "ERR 03 DB_QUE- Please contact your administrator";
                    else if (m.contains("no suitable driver"))
                        connectError = "ERR 04 DB_JDBC - Please contact your administrator.";
                    else
                        connectError = ex.getMessage();
                } catch (Exception ex) {
                    connected    = false;
                    connectError = ex.getMessage();
                }

                if (!connected) {
                    final String err = connectError;
                    SwingUtilities.invokeLater(() ->
                        showError("Cannot connect to database.", err));
                    return;
                }

                setStep("Connected.", "Connection successful.", 30);
                Thread.sleep(400);

                // ── Step 2: Load records ──────────────────────
                setStep("Loading database records...",
                    "Fetching students, admins, and schedules.", 45);
                Thread.sleep(300);

                boolean loaded;
                String  loadError = "";
                try {
                    loaded = DatabaseManager.loadDatabases();
                    if (!loaded) loadError =
                        "Tables may not exist yet. Please add and try again.";
                } catch (Exception ex) {
                    loaded    = false;
                    String m  = ex.getMessage() != null
                        ? ex.getMessage().toLowerCase() : "";
                    if (m.contains("doesn't exist") || m.contains("does not exist"))
                        loadError = "A required table is missing. Contact your admin.";
                    else
                        loadError = ex.getMessage();
                }

                if (!loaded) {
                    final String err = loadError;
                    SwingUtilities.invokeLater(() ->
                        showError("Failed to load records.", err));
                    return;
                }

                setStep("Records loaded.",
                    "Students: "  + DatabaseManager.studentDb.size()
                    + "  ·  Admins: "    + DatabaseManager.getAdminCount()
                    + "  ·  Schedules: " + DatabaseManager.scheduleDb.size(),
                    70);
                Thread.sleep(500);

                // ── Step 3: Prepare GUI ───────────────────────
                setStep("Preparing interface...",
                    "Building UI components.", 85);
                Thread.sleep(500);

                setStep("Ready.", "Launching attendance system.", 100);
                Thread.sleep(600);

                // ── Launch ────────────────────────────────────
                SwingUtilities.invokeLater(() -> {
                    dispose();
                    onReady.run();
                });

            } catch (InterruptedException ignored) {}
        }).start();
    }

    // =========================================================
    //  ERROR STATE
    // =========================================================
    private void showError(String title, String msg) {
        lblStep.setForeground(DANGER);
        lblStep.setText(title);
        lblSubStep.setText(msg != null ? msg : "An unknown error occurred.");

        if (retryShown) return;
        retryShown = true;

        btnRetry = UIBuilder.createToolbarButton("Retry", MAROON, Color.WHITE);
        btnRetry.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRetry.addActionListener(e -> {
            if (btnRetry   != null) body.remove(btnRetry);
            if (retryStrut != null) body.remove(retryStrut);
            btnRetry   = null;
            retryStrut = null;
            retryShown = false;

            lblStep.setForeground(MAROON);
            progressFill.setBounds(0, 0, 0, 6);
            progressTrack.repaint();

            body.revalidate();
            body.repaint();

            start();
        });

        retryStrut = Box.createVerticalStrut(12);

        body.add(retryStrut);
        body.add(btnRetry);
        body.revalidate();
        body.repaint();
    }

    // =========================================================
    //  LAUNCH
    // =========================================================
    public static void launch() {
        // --- 1. SET UP FLATLAF THEME ---
        try {
            // Apply the modern light theme to keep the school colors
            javax.swing.UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
            
            // Set some global modern UI properties
            javax.swing.UIManager.put( "Button.arc", 8 );      // Softer UI component corners
            javax.swing.UIManager.put( "Component.arc", 8 );
            javax.swing.UIManager.put( "TextComponent.arc", 8 );
            javax.swing.UIManager.put( "ScrollBar.thumbArc", 999 );
            javax.swing.UIManager.put( "ScrollBar.thumbInsets", new java.awt.Insets( 2, 2, 2, 2 ) );

        } catch (Exception ex) {
            System.err.println("Failed to initialize modern interface.");
        }

        // --- 2. LAUNCH THE SPLASH SCREEN ---
        javax.swing.SwingUtilities.invokeLater(() -> {
            SplashScreen splash = new SplashScreen(() ->
                new MainFrame().setVisible(true));
            splash.start();
        });
    }
}
