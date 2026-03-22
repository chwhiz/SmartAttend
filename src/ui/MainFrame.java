package ui;

import logic.AttendanceProcessor;
import logic.DatabaseManager;
import model.AttendanceResult;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Locale;

public class MainFrame extends JFrame {

    private static final ZoneId PH_TIME = ZoneId.of("Asia/Manila");

    private static final Color MAROON    = new Color(138, 26, 19);
    private static final Color GOLD      = new Color(248, 205, 0);
    private static final Color BG        = new Color(245, 243, 240);
    private static final Color CARD_BG   = Color.WHITE;
    private static final Color TEXT_MAIN = new Color(40, 40, 40);
    private static final Color TEXT_DIM  = new Color(130, 130, 130);
    private static final Color DANGER    = new Color(192, 57, 43);
    private static final Color SUCCESS   = new Color(39, 174, 96);
    private static final Color BORDER    = new Color(220, 215, 210);
    private static final Color NOVO_BG   = new Color(30, 30, 30);
    private static final Color NOVO_BTN  = new Color(50, 50, 50);
    private static final Color NOVO_HOV  = new Color(70, 70, 70);

    private JLabel lblTimestamp, lblStats, lblStatus;
    private JLabel lblName, lblId, lblSubject, lblSched;
    private JLabel lblSection, lblDate, lblTime;
    private JLabel lblScanCount, lblLastScanned;

    private JPanel cardPanel;
    private JTextField inputField;
    private javax.swing.Timer clockTimer;
    private CheckmarkPanel checkmarkPanel;

    private JPanel novoBar;
    private boolean novoVisible = false;
    private javax.swing.Timer novoAutoHide;

    private int scanCountToday = 0;
    private final Map<String, LocalDateTime> lastScanMap = new HashMap<>();
    private static final int DUPLICATE_MINUTES = 5;

    public MainFrame() {
        setTitle("University of Perpetual Help System DALTA - Senior High School");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { }
        });

        getContentPane().setBackground(BG);
        setLayout(new BorderLayout(0, 0));

        buildTopBar();
        buildCardArea();
        buildNovoBar();
        buildInputArea();
        registerNovoShortcut();
        startClock();
    }

    // =========================================================
    //  TOP BAR
    // =========================================================
    private void buildTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(MAROON);
        top.setBorder(new EmptyBorder(10, 20, 10, 20));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        left.setBackground(MAROON);
        left.add(loadLogo("/uphsd_logo.png", 70, 90));

        JPanel nameStack = new JPanel(new GridLayout(3, 1, 0, 2));
        nameStack.setBackground(MAROON);

        JLabel line1 = new JLabel("UNIVERSITY OF PERPETUAL HELP SYSTEM DALTA");
        line1.setFont(new Font("Impact", Font.PLAIN, 20));
        line1.setForeground(GOLD);

        JLabel line2 = new JLabel("Molino Campus  ·  Senior High School Department");
        line2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        line2.setForeground(new Color(255, 240, 200));

        JLabel line3 = new JLabel("Attendance System  v4.0");
        line3.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        line3.setForeground(new Color(220, 180, 180));

        nameStack.add(line1);
        nameStack.add(line2);
        nameStack.add(line3);
        left.add(nameStack);

        JPanel right = new JPanel(new BorderLayout(0, 4));
        right.setBackground(MAROON);

        JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        logoRow.setBackground(MAROON);
        logoRow.add(loadLogo("/30th.png", 60, 60));
        logoRow.add(loadLogo("/shs.png",  60, 60));
        logoRow.add(loadLogo("/iso.png",  166, 60));

        lblTimestamp = new JLabel("", SwingConstants.RIGHT);
        lblTimestamp.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTimestamp.setForeground(new Color(240, 220, 200));

        right.add(logoRow,      BorderLayout.CENTER);
        right.add(lblTimestamp, BorderLayout.SOUTH);

        top.add(left,  BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);

        JPanel goldBar = new JPanel();
        goldBar.setBackground(GOLD);
        goldBar.setPreferredSize(new Dimension(0, 4));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(top,     BorderLayout.CENTER);
        wrapper.add(goldBar, BorderLayout.SOUTH);

        add(wrapper, BorderLayout.NORTH);
    }

    // =========================================================
    //  NOVO BAR
    // =========================================================
    private void buildNovoBar() {
        novoBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        novoBar.setBackground(NOVO_BG);
        novoBar.setBorder(new MatteBorder(2, 0, 0, 0, GOLD));
        novoBar.setVisible(false);

        JButton btnViewLog  = novoButton("View Log",    false);
        JButton btnStudents = novoButton("Students",    true);
        JButton btnAdmins   = novoButton("Admins",      true);
        JButton btnSchedule = novoButton("Schedule",    true);
        JButton btnReload   = novoButton("Reload DB",   false);
        JButton btnExit     = novoButton("Exit System", true);

        btnViewLog.addActionListener(e -> {
            if (authCheck(false)) new AttendanceLogFrame().setVisible(true);
        });
        btnStudents.addActionListener(e -> {
            if (authCheck(true)) new StudentManagerFrame().setVisible(true);
        });
        btnAdmins.addActionListener(e -> {
            if (authCheck(true)) new AdminManagerFrame().setVisible(true);
        });
        btnSchedule.addActionListener(e -> {
            if (authCheck(true)) new ScheduleManagerFrame().setVisible(true);
        });
        btnReload.addActionListener(e -> {
            if (authCheck(false)) {
                DatabaseManager.loadDatabases();
                updateStats();
                showNovoToast("Databases reloaded");
            }
        });
        btnExit.addActionListener(e -> {
            if (authCheck(true)) System.exit(0);
        });

        novoBar.add(btnViewLog);
        novoBar.add(novoDivider());
        novoBar.add(btnStudents);
        novoBar.add(btnAdmins);
        novoBar.add(btnSchedule);
        novoBar.add(novoDivider());
        novoBar.add(btnReload);
        novoBar.add(novoDivider());
        novoBar.add(btnExit);
    }

    private JButton novoButton(String text, boolean critical) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", critical ? Font.BOLD : Font.PLAIN, 13));
        btn.setForeground(critical ? new Color(255, 200, 200) : Color.WHITE);
        btn.setBackground(NOVO_BTN);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(NOVO_HOV);
                if (critical) btn.setForeground(GOLD);
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(NOVO_BTN);
                btn.setForeground(critical ? new Color(255, 200, 200) : Color.WHITE);
            }
        });
        return btn;
    }

    private JSeparator novoDivider() {
        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setForeground(new Color(80, 80, 80));
        sep.setPreferredSize(new Dimension(1, 32));
        return sep;
    }

    private void showNovoToast(String msg) {
        JLabel toast = new JLabel(msg);
        toast.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        toast.setForeground(SUCCESS);
        toast.setBorder(new EmptyBorder(0, 16, 0, 0));
        novoBar.add(toast);
        novoBar.revalidate();
        novoBar.repaint();
        new javax.swing.Timer(2500, e -> {
            novoBar.remove(toast);
            novoBar.revalidate();
            novoBar.repaint();
        }) {{ setRepeats(false); }}.start();
    }

    private void registerNovoShortcut() {
        KeyStroke ks = KeyStroke.getKeyStroke(
            KeyEvent.VK_B,
            InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(ks, "toggleNovo");
        getRootPane().getActionMap().put("toggleNovo", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { toggleNovoBar(); }
        });
    }

    private void toggleNovoBar() {
        novoVisible = !novoVisible;
        novoBar.setVisible(novoVisible);
        revalidate();
        repaint();
        if (novoVisible) {
            if (novoAutoHide != null) novoAutoHide.stop();
            novoAutoHide = new javax.swing.Timer(30_000, e -> {
                novoVisible = false;
                novoBar.setVisible(false);
                revalidate();
                repaint();
            });
            novoAutoHide.setRepeats(false);
            novoAutoHide.start();
        } else {
            if (novoAutoHide != null) novoAutoHide.stop();
        }
    }

    private boolean authCheck(boolean requireTotp) {
        AdminAuthDialog auth = new AdminAuthDialog(this, requireTotp);
        auth.setVisible(true);
        if (!auth.isAuthenticated()) {
            novoVisible = false;
            novoBar.setVisible(false);
            revalidate();
            repaint();
            return false;
        }
        return true;
    }

    // =========================================================
    //  CARD AREA
    // =========================================================
    private void buildCardArea() {
        cardPanel = new JPanel(new CardLayout());
        cardPanel.setBackground(BG);
        cardPanel.setBorder(new EmptyBorder(28, 40, 16, 40));

        cardPanel.add(buildStatusCard(),    "status");
        cardPanel.add(buildCheckmarkCard(), "checkmark");
        cardPanel.add(buildResultCard(),    "result");

        JPanel centerArea = new JPanel(new BorderLayout(0, 0));
        centerArea.setBackground(BG);
        centerArea.add(cardPanel, BorderLayout.CENTER);

        add(centerArea, BorderLayout.CENTER);
    }

    // ── Status Card ───────────────────────────────────────────
    private JPanel buildStatusCard() {
        JPanel card = new JPanel(new BorderLayout(0, 0));
        card.setBackground(CARD_BG);
        card.setBorder(new CompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(40, 48, 40, 48)
        ));

        JPanel leftPane = new JPanel();
        leftPane.setBackground(CARD_BG);
        leftPane.setLayout(new BoxLayout(leftPane, BoxLayout.Y_AXIS));
        leftPane.setBorder(new EmptyBorder(0, 0, 0, 40));

        JLabel lblGreetingStatic = new JLabel(getGreeting() + "!");
        lblGreetingStatic.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblGreetingStatic.setForeground(MAROON);
        lblGreetingStatic.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblClock = new JLabel("", SwingConstants.LEFT);
        lblClock.setFont(new Font("Segoe UI", Font.BOLD, 48));
        lblClock.setForeground(TEXT_MAIN);
        lblClock.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblDayDate = new JLabel("", SwingConstants.LEFT);
        lblDayDate.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblDayDate.setForeground(TEXT_DIM);
        lblDayDate.setAlignmentX(Component.LEFT_ALIGNMENT);

        DateTimeFormatter timeFmt = DateTimeFormatter
            .ofPattern("hh:mm:ss a").withLocale(Locale.ENGLISH);
        DateTimeFormatter dateFmt = DateTimeFormatter
            .ofPattern("EEEE, MMMM dd yyyy").withLocale(Locale.ENGLISH);

        new javax.swing.Timer(1000, e -> {
            ZonedDateTime now = ZonedDateTime.now(PH_TIME);
            lblClock.setText(now.format(timeFmt));
            lblDayDate.setText(now.format(dateFmt));
        }) {{ setInitialDelay(0); start(); }};

        JPanel sectionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        sectionRow.setBackground(CARD_BG);
        sectionRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        sectionRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        sectionRow.add(loadLogo("/ict1102_logo.png", 40, 40));

        JLabel lblSectionTag = new JLabel("ICT 11-02");
        lblSectionTag.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblSectionTag.setForeground(MAROON);
        sectionRow.add(lblSectionTag);

        JLabel lblCredit = new JLabel("Codepreneurs for Computer Programming 2");
        lblCredit.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblCredit.setForeground(TEXT_DIM);
        lblCredit.setBorder(new EmptyBorder(4, 0, 0, 0));
        lblCredit.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftPane.add(Box.createVerticalGlue());
        leftPane.add(lblGreetingStatic);
        leftPane.add(Box.createVerticalStrut(16));
        leftPane.add(makeSep());
        leftPane.add(Box.createVerticalStrut(16));
        leftPane.add(lblClock);
        leftPane.add(Box.createVerticalStrut(4));
        leftPane.add(lblDayDate);
        leftPane.add(Box.createVerticalStrut(16));
        leftPane.add(makeSep());
        leftPane.add(Box.createVerticalStrut(16));
        leftPane.add(sectionRow);
        leftPane.add(lblCredit);
        leftPane.add(Box.createVerticalGlue());

        JPanel rightPane = new JPanel();
        rightPane.setBackground(CARD_BG);
        rightPane.setLayout(new BoxLayout(rightPane, BoxLayout.Y_AXIS));
        rightPane.setBorder(new CompoundBorder(
            new MatteBorder(0, 1, 0, 0, BORDER),
            new EmptyBorder(0, 40, 0, 0)
        ));

        JPanel activeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        activeRow.setBackground(CARD_BG);
        activeRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblDot = new JLabel("●");
        lblDot.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblDot.setForeground(SUCCESS);

        JLabel lblActive = new JLabel("ACTIVE");
        lblActive.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblActive.setForeground(SUCCESS);

        final boolean[] visible = {true};
        new javax.swing.Timer(800, e -> {
            visible[0] = !visible[0];
            lblDot.setForeground(visible[0] ? SUCCESS : CARD_BG);
        }).start();

        activeRow.add(lblDot);
        activeRow.add(lblActive);

        JLabel lblWaiting = new JLabel("Waiting for RFID Scan.");
        lblWaiting.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblWaiting.setForeground(TEXT_DIM);
        lblWaiting.setBorder(new EmptyBorder(6, 0, 20, 0));
        lblWaiting.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblScanCount = new JLabel("Total scans today: 0");
        lblScanCount.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblScanCount.setForeground(MAROON);
        lblScanCount.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblLastScanned = new JLabel("Last: —");
        lblLastScanned.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblLastScanned.setForeground(TEXT_DIM);
        lblLastScanned.setBorder(new EmptyBorder(4, 0, 20, 0));
        lblLastScanned.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel rfidIcon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                int cx = getWidth() / 2, cy = getHeight() / 2;
                g2.setColor(BORDER);
                g2.setStroke(new BasicStroke(3,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                for (int i = 1; i <= 3; i++) {
                    int r = i * 18;
                    g2.drawArc(cx-r, cy-r, r*2, r*2, 45,  90);
                    g2.drawArc(cx-r, cy-r, r*2, r*2, 225, 90);
                }
                g2.setColor(MAROON);
                g2.fillRoundRect(cx-16, cy-22, 32, 44, 6, 6);
                g2.setColor(GOLD);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(cx-12, cy-10, 24, 14, 3, 3);
            }
        };
        rfidIcon.setBackground(CARD_BG);
        rfidIcon.setPreferredSize(new Dimension(120, 120));
        rfidIcon.setMaximumSize(new Dimension(120, 120));
        rfidIcon.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblRfidTitle = new JLabel("RFID Technology");
        lblRfidTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblRfidTitle.setForeground(TEXT_MAIN);
        lblRfidTitle.setBorder(new EmptyBorder(12, 0, 6, 0));
        lblRfidTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblDesc1 = new JLabel("This system supports RFID technology.");
        lblDesc1.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDesc1.setForeground(TEXT_DIM);
        lblDesc1.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblDesc2 = new JLabel("Please tap your ID in the designated spot.");
        lblDesc2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDesc2.setForeground(TEXT_DIM);
        lblDesc2.setBorder(new EmptyBorder(2, 0, 0, 0));
        lblDesc2.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblStats = new JLabel("");
        lblStats.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblStats.setForeground(TEXT_DIM);
        lblStats.setBorder(new EmptyBorder(24, 0, 0, 0));
        lblStats.setAlignmentX(Component.LEFT_ALIGNMENT);

        rightPane.add(Box.createVerticalGlue());
        rightPane.add(activeRow);
        rightPane.add(lblWaiting);
        rightPane.add(lblScanCount);
        rightPane.add(lblLastScanned);
        rightPane.add(rfidIcon);
        rightPane.add(lblRfidTitle);
        rightPane.add(lblDesc1);
        rightPane.add(lblDesc2);
        rightPane.add(Box.createVerticalGlue());
        rightPane.add(lblStats);

        card.add(leftPane,  BorderLayout.CENTER);
        card.add(rightPane, BorderLayout.EAST);

        return card;
    }

    // ── Checkmark Card ────────────────────────────────────────
    private JPanel buildCheckmarkCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(new LineBorder(BORDER, 1, true));
        checkmarkPanel = new CheckmarkPanel();
        card.add(checkmarkPanel, BorderLayout.CENTER);
        return card;
    }

    // ── Result Card ───────────────────────────────────────────
    private JPanel buildResultCard() {
        JPanel resultPanel = new JPanel();
        resultPanel.setBackground(CARD_BG);
        resultPanel.setBorder(new CompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(36, 48, 36, 48)
        ));
        resultPanel.setLayout(new BorderLayout(40, 0));

        JPanel leftPane = new JPanel();
        leftPane.setBackground(CARD_BG);
        leftPane.setLayout(new BoxLayout(leftPane, BoxLayout.Y_AXIS));
        leftPane.setBorder(new EmptyBorder(0, 0, 0, 36));

        JLabel scanTitle = new JLabel("Scan Result");
        scanTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        scanTitle.setForeground(MAROON);
        scanTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblGreeting = new JLabel(getGreeting());
        lblGreeting.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblGreeting.setForeground(TEXT_DIM);
        lblGreeting.setBorder(new EmptyBorder(10, 0, 2, 0));
        lblGreeting.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblName = new JLabel("");
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblName.setForeground(TEXT_MAIN);
        lblName.setBorder(new EmptyBorder(0, 0, 16, 0));
        lblName.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblStatus = new JLabel("");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblStatus.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblId      = createResultValueLabel();
        lblSection = createResultValueLabel();

        leftPane.add(scanTitle);
        leftPane.add(lblGreeting);
        leftPane.add(lblName);
        leftPane.add(lblStatus);
        leftPane.add(Box.createVerticalStrut(16));
        leftPane.add(makeSep());
        leftPane.add(Box.createVerticalStrut(12));
        leftPane.add(buildInlineRow("Student ID :", lblId));
        leftPane.add(Box.createVerticalStrut(6));
        leftPane.add(buildInlineRow("Section    :", lblSection));

        JPanel rightPane = new JPanel(new GridLayout(4, 1, 0, 12));
        rightPane.setBackground(CARD_BG);
        rightPane.setBorder(new MatteBorder(0, 1, 0, 0, BORDER));

        lblSubject = createResultValueLabel();
        lblSched   = createResultValueLabel();
        lblDate    = createResultValueLabel();
        lblTime    = createResultValueLabel();

        rightPane.add(buildResultRow("Subject",     lblSubject));
        rightPane.add(buildResultRow("Schedule",    lblSched));
        rightPane.add(buildResultRow("Date Logged", lblDate));
        rightPane.add(buildResultRow("Time Logged", lblTime));

        resultPanel.add(leftPane,  BorderLayout.WEST);
        resultPanel.add(rightPane, BorderLayout.CENTER);

        return resultPanel;
    }

    // =========================================================
    //  INPUT AREA
    // =========================================================
    private void buildInputArea() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 6));
        wrapper.setBackground(BG);
        wrapper.setBorder(new EmptyBorder(0, 40, 24, 40));

        JLabel prompt = new JLabel("Scan your University ID.");
        prompt.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        prompt.setForeground(TEXT_DIM);

        inputField = new JTextField();
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        inputField.setForeground(TEXT_MAIN);
        inputField.setBackground(CARD_BG);
        inputField.setCaretColor(MAROON);
        inputField.setPreferredSize(new Dimension(0, 44));
        inputField.setBorder(new CompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(8, 14, 8, 14)
        ));
        inputField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                inputField.setBorder(new CompoundBorder(
                    new LineBorder(MAROON, 2, true),
                    new EmptyBorder(7, 13, 7, 13)));
            }
            public void focusLost(FocusEvent e) {
                inputField.setBorder(new CompoundBorder(
                    new LineBorder(BORDER, 1, true),
                    new EmptyBorder(8, 14, 8, 14)));
            }
        });
        inputField.addActionListener(e -> handleInput(inputField.getText().trim()));

        wrapper.add(prompt,     BorderLayout.NORTH);
        wrapper.add(inputField, BorderLayout.CENTER);

        JPanel southStack = new JPanel(new BorderLayout());
        southStack.setBackground(BG);
        southStack.add(novoBar,  BorderLayout.NORTH);
        southStack.add(wrapper,  BorderLayout.CENTER);

        add(southStack, BorderLayout.SOUTH);
    }

    // =========================================================
    //  LOGIC
    // =========================================================
        private void handleInput(String raw) {
        inputField.setText("");
        if (raw.isEmpty()) return;

        if (raw.equalsIgnoreCase("EXIT")) {
            if (authCheck(true)) System.exit(0);
            return;
        }

        // RFID uid — no format validation, pass directly
        lookupAndLog(raw);
    }

        private void lookupAndLog(String rfidUid) {
        LocalDateTime lastScan = lastScanMap.get(rfidUid);
        if (lastScan != null) {
            long minsAgo = java.time.temporal.ChronoUnit.MINUTES
                .between(lastScan, LocalDateTime.now(PH_TIME));
            if (minsAgo < DUPLICATE_MINUTES) {
                long remaining = DUPLICATE_MINUTES - minsAgo;
                String[] data = DatabaseManager.studentDb.get(rfidUid);
                String label  = data != null ? data[2] : rfidUid;
                ToastNotification.show(this,
                    "Already Scanned",
                    label + " — Please try again in " + remaining + " min.");
                return;
            }
        }

        String[] data = DatabaseManager.studentDb.get(rfidUid);
        if (data == null) {
            showError(rfidUid);
            return;
        }

        lastScanMap.put(rfidUid, LocalDateTime.now(PH_TIME));
        AttendanceResult r = AttendanceProcessor.process(rfidUid);

        scanCountToday++;
        lblScanCount.setText("Total scans today: " + scanCountToday);

        DateTimeFormatter fmt = DateTimeFormatter
            .ofPattern("hh:mm a").withLocale(Locale.ENGLISH);
        lblLastScanned.setText("Last: " + r.name + "  ·  " +
            LocalTime.now(PH_TIME).format(fmt));

        showResult(r);
    }

    // ── Show result card (valid scan) ─────────────────────────
    private void showResult(AttendanceResult r) {
        lblName.setText(r.name);
        lblName.setForeground(TEXT_MAIN);   // ← reset in case previous was error
        lblId.setText(r.displayID);
        lblSubject.setText(r.subject);
        lblSched.setText(r.timeDetails);
        lblSection.setText(r.section);
        lblDate.setText(LocalDate.now(PH_TIME)
            .format(DateTimeFormatter.ofPattern("MMM dd, yyyy")
            .withLocale(Locale.ENGLISH)));
        lblTime.setText(LocalTime.now(PH_TIME)
            .format(DateTimeFormatter.ofPattern("hh:mm a")
            .withLocale(Locale.ENGLISH)));

        boolean late = r.status.contains("LATE");
        lblStatus.setForeground(late ? DANGER : SUCCESS);
        lblStatus.setText(r.status);

        CardLayout cl = (CardLayout) cardPanel.getLayout();
        cl.show(cardPanel, "checkmark");

        checkmarkPanel.startAnimation(late, () -> {
            cl.show(cardPanel, "result");
            new javax.swing.Timer(5000, e -> cl.show(cardPanel, "status"))
                {{ setRepeats(false); }}.start();
        });
    }

    // ── Show error card (not found) ───────────────────────────
    private void showError(String displayID) {
        lblName.setText("Not Found");
        lblName.setForeground(DANGER);
        lblId.setText(displayID.isEmpty() ? "—" : displayID);
        lblSubject.setText("—");
        lblSched.setText("—");
        lblSection.setText("—");
        lblDate.setText(LocalDate.now(PH_TIME)
            .format(DateTimeFormatter.ofPattern("MMM dd, yyyy")
            .withLocale(Locale.ENGLISH)));
        lblTime.setText(LocalTime.now(PH_TIME)
            .format(DateTimeFormatter.ofPattern("hh:mm a")
            .withLocale(Locale.ENGLISH)));
        lblStatus.setForeground(DANGER);
        lblStatus.setText("Please check with your coordinator if your ID has been registered.");

        CardLayout cl = (CardLayout) cardPanel.getLayout();
        cl.show(cardPanel, "checkmark");

        // ← X animation, not checkmark
        checkmarkPanel.startErrorAnimation(() -> {
            cl.show(cardPanel, "result");
            new javax.swing.Timer(6000, e -> {
                lblName.setForeground(TEXT_MAIN);
                cl.show(cardPanel, "status");
            }) {{ setRepeats(false); }}.start();
        });
    }

    // =========================================================
    //  CLOCK + STATS
    // =========================================================
    private void startClock() {
        DateTimeFormatter dtf = DateTimeFormatter
            .ofPattern("EEE, MMM dd  hh:mm:ss a")
            .withLocale(Locale.ENGLISH);
        clockTimer = new javax.swing.Timer(1000, e ->
            lblTimestamp.setText(ZonedDateTime.now(PH_TIME).format(dtf)));
        clockTimer.start();
        updateStats();
    }

    private void updateStats() {
        lblStats.setText("Students: " + DatabaseManager.studentDb.size()
            + "     Admins: " + DatabaseManager.adminDb.size()
            + "     Schedule entries: " + DatabaseManager.scheduleDb.size());
    }

    // =========================================================
    //  HELPERS
    // =========================================================
    private String getGreeting() {
        int hour = LocalTime.now(PH_TIME).getHour();
        if (hour < 12) return "Good morning";
        if (hour < 18) return "Good afternoon";
        return "Good evening";
    }

    private JSeparator makeSep() {
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        return sep;
    }

    private JLabel createResultValueLabel() {
        JLabel l = new JLabel("");
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        l.setForeground(TEXT_MAIN);
        return l;
    }

    private JPanel buildResultRow(String key, JLabel valueLabel) {
        JPanel row = new JPanel(new GridLayout(2, 1, 0, 4));
        row.setBackground(CARD_BG);
        row.setBorder(new EmptyBorder(0, 20, 0, 0));
        JLabel k = new JLabel(key);
        k.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        k.setForeground(TEXT_DIM);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        valueLabel.setForeground(TEXT_MAIN);
        row.add(k);
        row.add(valueLabel);
        return row;
    }

    private JPanel buildInlineRow(String key, JLabel valueLabel) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setBackground(CARD_BG);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel k = new JLabel(key + "  ");
        k.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        k.setForeground(TEXT_DIM);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        valueLabel.setForeground(TEXT_MAIN);
        row.add(k);
        row.add(valueLabel);
        return row;
    }

    private JLabel loadLogo(String path, int w, int h) {
        java.net.URL url = getClass().getResource(path);
        if (url != null) {
            Image scaled = new ImageIcon(url).getImage()
                .getScaledInstance(w, h, Image.SCALE_SMOOTH);
            JLabel lbl = new JLabel(new ImageIcon(scaled));
            lbl.setPreferredSize(new Dimension(w, h));
            return lbl;
        } else {
            JLabel lbl = new JLabel(path.replace("/", "").replace(".png", ""));
            lbl.setForeground(GOLD);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
            lbl.setPreferredSize(new Dimension(w, h));
            return lbl;
        }
    }

    // =========================================================
    //  CHECKMARK / X ANIMATION
    // =========================================================
    class CheckmarkPanel extends JPanel {
        private float   progress = 0f, opacity = 1f;
        private boolean isLate   = false;
        private boolean isError  = false;   // ← X mode flag
        private javax.swing.Timer drawTimer, fadeTimer;

        CheckmarkPanel() { setBackground(CARD_BG); }

        // Normal scan (green check or red check for late)
        public void startAnimation(boolean late, Runnable onComplete) {
            isError  = false;
            isLate   = late;
            progress = 0f;
            opacity  = 1f;
            runAnimation(onComplete);
        }

        // Not found (red X + "NOT FOUND" label)
        public void startErrorAnimation(Runnable onComplete) {
            isError  = true;
            isLate   = false;
            progress = 0f;
            opacity  = 1f;
            runAnimation(onComplete);
        }

        private void runAnimation(Runnable onComplete) {
            if (drawTimer != null) drawTimer.stop();
            if (fadeTimer != null) fadeTimer.stop();

            drawTimer = new javax.swing.Timer(16, null);
            drawTimer.addActionListener(e -> {
                progress = Math.min(1f, progress + 0.04f);
                repaint();
                if (progress >= 1f) {
                    drawTimer.stop();
                    new javax.swing.Timer(500, hv -> {
                        fadeTimer = new javax.swing.Timer(16, null);
                        fadeTimer.addActionListener(fe -> {
                            opacity = Math.max(0f, opacity - 0.05f);
                            repaint();
                            if (opacity <= 0f) {
                                fadeTimer.stop();
                                onComplete.run();
                            }
                        });
                        fadeTimer.start();
                    }) {{ setRepeats(false); }}.start();
                }
            });
            drawTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            int cx = w/2, cy = h/2, r = Math.min(w, h) / 4;

            g2.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, opacity));

            Color c = (isError || isLate) ? DANGER : SUCCESS;

            // Glow circle
            g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 50));
            g2.fillOval(cx-r, cy-r, r*2, r*2);

            // Outline circle
            g2.setColor(c);
            g2.setStroke(new BasicStroke(4));
            g2.drawOval(cx-r, cy-r, r*2, r*2);

            if (isError) {
                // ── X symbol ──────────────────────────────────
                g2.setStroke(new BasicStroke(7,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(c);
                int pad = r / 2;

                // Line 1: top-left to bottom-right
                float s1 = Math.min(1f, progress / 0.5f);
                g2.drawLine(cx - pad, cy - pad,
                    (int)(cx - pad + (pad * 2) * s1),
                    (int)(cy - pad + (pad * 2) * s1));

                // Line 2: top-right to bottom-left
                float s2 = Math.max(0f, (progress - 0.5f) / 0.5f);
                if (s2 > 0)
                    g2.drawLine(cx + pad, cy - pad,
                        (int)(cx + pad - (pad * 2) * s2),
                        (int)(cy - pad + (pad * 2) * s2));

                // Label
                if (progress >= 1f) {
                    String txt = "NOT FOUND";
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 24));
                    g2.setColor(c);
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(txt,
                        cx - fm.stringWidth(txt) / 2,
                        cy + r + 44);
                }

            } else {
                // ── Checkmark ─────────────────────────────────
                if (progress > 0) {
                    g2.setStroke(new BasicStroke(7,
                        BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.setColor(c);
                    int x1=cx-r/2, y1=cy,
                        x2=cx-r/8, y2=cy+r/2,
                        x3=cx+r/2, y3=cy-r/3;
                    float s1 = Math.min(1f, progress / 0.4f);
                    float s2 = Math.max(0f, (progress - 0.4f) / 0.6f);
                    g2.drawLine(x1, y1,
                        (int)(x1+(x2-x1)*s1),
                        (int)(y1+(y2-y1)*s1));
                    if (s2 > 0)
                        g2.drawLine(x2, y2,
                            (int)(x2+(x3-x2)*s2),
                            (int)(y2+(y3-y2)*s2));
                }
                if (progress >= 1f) {
                    String txt = isLate ? "LATE" : "LOGGED";
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 24));
                    g2.setColor(c);
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(txt,
                        cx - fm.stringWidth(txt) / 2,
                        cy + r + 44);
                }
            }
            g2.dispose();
        }
    }

    // =========================================================
    //  MAIN
    // =========================================================
    public static void main(String[] args) {
        SplashScreen.launch();   // ← replaces old 3-liner
    }
}
