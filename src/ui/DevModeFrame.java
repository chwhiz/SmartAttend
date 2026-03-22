package ui;

import logic.DatabaseManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.Statement;

public class DevModeFrame extends JDialog {

    private MainFrame mainFrame;

    public DevModeFrame(MainFrame parent) {
        super(parent, "Development Mode Tools", true);
        this.mainFrame = parent;
        
        setSize(480, 640);
        setUndecorated(true);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIBuilder.BG);
        getRootPane().setBorder(new LineBorder(UIBuilder.BORDER, 1));

        buildTitleBar();

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(UIBuilder.BG);
        content.setBorder(new EmptyBorder(20, 20, 20, 20));

        // 1. Bypass TOTP Toggle
        JPanel pnlAuth = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlAuth.setBackground(UIBuilder.BG);
        pnlAuth.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(UIBuilder.BORDER), "Authentication & Security", 
                TitledBorder.LEFT, TitledBorder.TOP, UIBuilder.FONT_SUB, UIBuilder.TEXT_DIM));
        
        JCheckBox chkBypassTotp = new JCheckBox("Bypass Admin TOTP Dialog");
        chkBypassTotp.setFont(UIBuilder.FONT_BODY);
        chkBypassTotp.setForeground(UIBuilder.TEXT_MAIN);
        chkBypassTotp.setBackground(UIBuilder.BG);
        chkBypassTotp.setFocusPainted(false);
        chkBypassTotp.setSelected(DatabaseManager.devBypassTOTP);
        chkBypassTotp.addActionListener(e -> {
            DatabaseManager.devBypassTOTP = chkBypassTotp.isSelected();
        });
        pnlAuth.add(chkBypassTotp);
        
        JCheckBox chkBypassRate = new JCheckBox("Bypass Duplicate Scan Rate Limit");
        chkBypassRate.setFont(UIBuilder.FONT_BODY);
        chkBypassRate.setForeground(UIBuilder.TEXT_MAIN);
        chkBypassRate.setBackground(UIBuilder.BG);
        chkBypassRate.setFocusPainted(false);
        chkBypassRate.setSelected(DatabaseManager.devBypassRateLimit);
        chkBypassRate.addActionListener(e -> {
            DatabaseManager.devBypassRateLimit = chkBypassRate.isSelected();
        });
        pnlAuth.add(chkBypassRate);
        content.add(pnlAuth);
        content.add(Box.createVerticalStrut(15));

        // 2. Clear Tables
        JPanel pnlDbInfo = new JPanel(new GridLayout(2, 2, 15, 15));
        pnlDbInfo.setBackground(UIBuilder.BG);
        pnlDbInfo.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(UIBuilder.BORDER), "Database Utilities", 
                TitledBorder.LEFT, TitledBorder.TOP, UIBuilder.FONT_SUB, UIBuilder.TEXT_DIM));

        JButton btnClearLog = UIBuilder.createToolbarButton("Truncate Log", UIBuilder.DANGER, Color.WHITE);
        JButton btnClearSchedules = UIBuilder.createToolbarButton("Truncate Schedules", UIBuilder.DANGER, Color.WHITE);
        
        btnClearLog.addActionListener(e -> truncateTable("attendance_log"));
        btnClearSchedules.addActionListener(e -> truncateTable("schedules"));
        
        JButton btnPing = UIBuilder.createToolbarButton("DB Ping Test", new Color(52, 73, 94), Color.WHITE);
        btnPing.addActionListener(e -> pingDatabase());

        pnlDbInfo.add(btnClearLog);
        pnlDbInfo.add(btnClearSchedules);
        pnlDbInfo.add(btnPing);
        content.add(pnlDbInfo);
        content.add(Box.createVerticalStrut(15));

        // 3. UI Sandbox
        JPanel pnlUi = new JPanel(new GridLayout(2, 2, 15, 15));
        pnlUi.setBackground(UIBuilder.BG);
        pnlUi.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(UIBuilder.BORDER), "UI Sandbox", 
                TitledBorder.LEFT, TitledBorder.TOP, UIBuilder.FONT_SUB, UIBuilder.TEXT_DIM));

        JButton btnTestInfo = UIBuilder.createToolbarButton("Test Info Toast", UIBuilder.MAROON, Color.WHITE);
        btnTestInfo.addActionListener(e -> {
            if (mainFrame != null) {
                ToastNotification.show(mainFrame, "UI Sandbox", "This is a test info message!");
            }
        });
        pnlUi.add(btnTestInfo);

        content.add(pnlUi);
        content.add(Box.createVerticalStrut(15));

        // 4. Time Simulation
        JPanel pnlTime = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnlTime.setBackground(UIBuilder.BG);
        pnlTime.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(UIBuilder.BORDER), "Time & Date Simulation", 
                TitledBorder.LEFT, TitledBorder.TOP, UIBuilder.FONT_SUB, UIBuilder.TEXT_DIM));

        JCheckBox chkMockTime = new JCheckBox("Override System Time:");
        chkMockTime.setFont(UIBuilder.FONT_BODY);
        chkMockTime.setForeground(UIBuilder.TEXT_MAIN);
        chkMockTime.setBackground(UIBuilder.BG);
        chkMockTime.setFocusPainted(false);
        chkMockTime.setSelected(DatabaseManager.devTimeOffset != null);
        
        JTextField txtMockTime = new JTextField(
            DatabaseManager.devTimeOffset != null ? 
            DatabaseManager.getNowDateTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : 
            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        );
        txtMockTime.setFont(new Font("Consolas", Font.PLAIN, 14));
        txtMockTime.setPreferredSize(new Dimension(150, 28));

        JButton btnApplyTime = UIBuilder.createToolbarButton("Apply Continuous Time", UIBuilder.MAROON, Color.WHITE);
        btnApplyTime.addActionListener(e -> {
            if (chkMockTime.isSelected()) {
                try {
                    java.time.LocalDateTime simulatedStart = java.time.LocalDateTime.parse(
                        txtMockTime.getText().trim(), 
                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                    );
                    java.time.LocalDateTime realNow = java.time.LocalDateTime.now(java.time.ZoneId.of("Asia/Manila"));
                    DatabaseManager.devTimeOffset = java.time.Duration.between(realNow, simulatedStart);
                    ToastNotification.show(mainFrame, "Time Simulated", "System time shifted and will now continue ticking.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Invalid format! Use yyyy-MM-dd HH:mm", "Error", JOptionPane.ERROR_MESSAGE);
                    chkMockTime.setSelected(false);
                    DatabaseManager.devTimeOffset = null;
                }
            } else {
                DatabaseManager.devTimeOffset = null;
            }
        });
        
        chkMockTime.addActionListener(e -> {
            if (!chkMockTime.isSelected()) {
                DatabaseManager.devTimeOffset = null;
            }
        });

        pnlTime.add(chkMockTime);
        pnlTime.add(txtMockTime);
        pnlTime.add(btnApplyTime);

        content.add(pnlTime);

        add(content, BorderLayout.CENTER);
        
        JPanel pnlSouth = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlSouth.setBackground(UIBuilder.BG);
        pnlSouth.setBorder(new EmptyBorder(0, 20, 20, 20));
        JButton btnClose = UIBuilder.createToolbarButton("Close Dev Mode", UIBuilder.TEXT_DIM, Color.WHITE);
        btnClose.addActionListener(e -> dispose());
        pnlSouth.add(btnClose);
        add(pnlSouth, BorderLayout.SOUTH);
    }

    private void buildTitleBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UIBuilder.MAROON);
        bar.setBorder(new EmptyBorder(12, 18, 12, 18));

        JLabel title = new JLabel("Development Mode Tools");
        title.setFont(UIBuilder.FONT_TITLE);
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Bypass & Testing Utilities");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(new Color(240, 200, 200));

        JPanel textStack = new JPanel(new GridLayout(2, 1));
        textStack.setBackground(UIBuilder.MAROON);
        textStack.add(title);
        textStack.add(sub);

        JButton btnClose = UIBuilder.createCloseButton(this);

        bar.add(textStack, BorderLayout.WEST);
        bar.add(btnClose, BorderLayout.EAST);

        JPanel goldBar = UIBuilder.createGoldBar();

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(bar, BorderLayout.CENTER);
        wrapper.add(goldBar, BorderLayout.SOUTH);
        add(wrapper, BorderLayout.NORTH);
    }

    private void truncateTable(String tableName) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to completely wipe the '" + tableName + "' table?\nThis cannot be undone!",
            "Danger Zone", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection con = DatabaseManager.getConnection();
                 Statement st = con.createStatement()) {
                st.executeUpdate("TRUNCATE TABLE " + tableName);
                JOptionPane.showMessageDialog(this, "Table '" + tableName + "' has been cleared.", "Success", JOptionPane.INFORMATION_MESSAGE);
                DatabaseManager.loadDatabases();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to clear table: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void pingDatabase() {
        long start = System.currentTimeMillis();
        try (Connection con = DatabaseManager.getConnection()) {
            boolean valid = con.isValid(2);
            long end = System.currentTimeMillis();
            if (valid) {
                JOptionPane.showMessageDialog(this, "Ping Success! Latency: " + (end - start) + "ms", "Database Ping", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Ping Failed! Connection invalid.", "Database Ping", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ping Failed: " + ex.getMessage(), "Database Ping", JOptionPane.ERROR_MESSAGE);
        }
    }
}
