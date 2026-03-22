package ui;

import logic.DatabaseManager;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;

public class ScheduleManagerFrame extends JFrame {

    private static final Color MAROON      = new Color(138, 26, 19);
    private static final Color GOLD        = new Color(248, 205, 0);
    private static final Color BG          = new Color(245, 243, 240);
    private static final Color CARD_BG     = Color.WHITE;
    private static final Color TEXT_MAIN   = new Color(40, 40, 40);
    private static final Color TEXT_DIM    = new Color(130, 130, 130);
    private static final Color BORDER      = new Color(220, 215, 210);
    private static final Color BREAK_BG    = new Color(173, 216, 230);  // light blue
    private static final Color HEADER_BG   = new Color(50, 50, 50);
    private static final Color HEADER_FG   = Color.WHITE;
    private static final Color TIME_BG     = new Color(245, 243, 240);

    private static final String[] SECTIONS = {
        "ABM 11-01","ABM 11-02","ABM 11-03",
        "AD 11-01","AD 11-02",
        "HE 11-01","HE 11-02","HE 11-03","HE 11-04",
        "HUMSS 11-01","HUMSS 11-02","HUMSS 11-03","HUMSS 11-04",
        "ICT 11-01","ICT 11-02","ICT 11-03",
        "STEM 11-01","STEM 11-02","STEM 11-03","STEM 11-04","STEM 11-05",
        "STEM 11-06","STEM 11-07","STEM 11-08","STEM 11-09","STEM 11-10",
        "STEM 11-11","STEM 11-12","STEM 11-13","STEM 11-14","STEM 11-15",
        "STEM 11-16","STEM 11-17",
        "ABM 12-01","ABM 12-02","ABM 12-03",
        "AD 12-01","AD 12-02",
        "HE 12-01","HE 12-02","HE 12-03",
        "HUMSS 12-01","HUMSS 12-02","HUMSS 12-03",
        "ICT 12-01","ICT 12-02","ICT 12-03",
        "STEM 12-01","STEM 12-02","STEM 12-03","STEM 12-04","STEM 12-05",
        "STEM 12-06","STEM 12-07","STEM 12-08","STEM 12-09","STEM 12-10",
        "STEM 12-11","STEM 12-12","STEM 12-13","STEM 12-14","STEM 12-15",
        "STEM 12-16","STEM 12-17"
    };

    // Time slots exactly as per the class program
    private static final String[] TIME_SLOTS = {
        "7:00 – 7:30",
        "7:30 – 8:30",
        "8:30 – 9:30",
        "9:30 – 10:00",   // BREAK
        "10:00 – 11:00",
        "11:00 – 12:00",
        "12:00 – 1:00",   // LUNCH BREAK
        "1:00 – 2:00",
        "2:00 – 3:00",
        "3:00 – 4:00",
        "4:00 – 5:00"
    };

    // Time slots as DB-queryable start times
    private static final String[] SLOT_START = {
        "07:00", "07:30", "08:30", "09:30",
        "10:00", "11:00", "12:00",
        "13:00", "14:00", "15:00", "16:00"
    };
    private static final String[] SLOT_END = {
        "07:30", "08:30", "09:30", "10:00",
        "11:00", "12:00", "13:00",
        "14:00", "15:00", "16:00", "17:00"
    };

    private static final String[] BREAK_SLOTS = {"9:30 – 10:00", "12:00 – 1:00"};

    private static final String[] DAYS = {
        "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"
    };

    private JComboBox<String> cbSection;
    private JLabel lblSection;

    // Grid: [timeSlot][day] → subject string
    private final String[][] grid = new String[TIME_SLOTS.length][DAYS.length];
    // Grid DB IDs: [timeSlot][day] → db id (-1 if none)
    private final int[][] gridIds = new int[TIME_SLOTS.length][DAYS.length];

    private JPanel gridPanel;

    public ScheduleManagerFrame() {
        setTitle("Schedule Manager — UPHSD SHS");
        setSize(1100, 640);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(BG);

        buildHeader();
        buildToolbar();
        buildGrid();
        loadSchedule();
    }

    // ── Header ────────────────────────────────────────────────
    private void buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(MAROON);
        header.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel("SHS Class Program  ·  Schedule Manager");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(GOLD);

        JLabel sub = new JLabel(
            "Second Semester, A.Y. 2025–2026  ·  University of Perpetual Help System DALTA");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(new Color(255, 240, 200));

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setBackground(MAROON);
        left.add(title);
        left.add(sub);
        header.add(left, BorderLayout.WEST);

        JPanel goldBar = new JPanel();
        goldBar.setBackground(GOLD);
        goldBar.setPreferredSize(new Dimension(0, 3));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(header,  BorderLayout.CENTER);
        wrapper.add(goldBar, BorderLayout.SOUTH);
        add(wrapper, BorderLayout.NORTH);
    }

    // ── Toolbar ───────────────────────────────────────────────
    private void buildToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        bar.setBackground(BG);
        bar.setBorder(new MatteBorder(0, 0, 1, 0, BORDER));

        JLabel lbl = new JLabel("Section:");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(TEXT_DIM);
        bar.add(lbl);

        cbSection = new JComboBox<>(SECTIONS);
        cbSection.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbSection.setPreferredSize(new Dimension(160, 32));
        cbSection.setSelectedItem("ICT 11-02");
        bar.add(cbSection);

        JButton btnLoad = styledButton("Load Schedule", MAROON, Color.WHITE);
        btnLoad.addActionListener(e -> loadSchedule());
        bar.add(btnLoad);

        JButton btnSave = styledButton("Save All Changes", new Color(39, 174, 96), Color.WHITE);
        btnSave.addActionListener(e -> saveAll());
        bar.add(btnSave);

        JButton btnClear = styledButton("Clear Section", new Color(192, 57, 43), Color.WHITE);
        btnClear.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Clear ALL schedule entries for " + cbSection.getSelectedItem() + "?",
                "Confirm Clear", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) clearSection();
        });
        bar.add(btnClear);

        add(bar, BorderLayout.AFTER_LAST_LINE);

        // Wrap header + toolbar
        Component north = ((BorderLayout) getContentPane().getLayout())
            .getLayoutComponent(BorderLayout.NORTH);
        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.add(north, BorderLayout.NORTH);
        topWrapper.add(bar,   BorderLayout.SOUTH);
        getContentPane().remove(north);
        add(topWrapper, BorderLayout.NORTH);
    }

    // ── Grid ──────────────────────────────────────────────────
    private void buildGrid() {
        gridPanel = new JPanel(new GridBagLayout());
        gridPanel.setBackground(CARD_BG);

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.BOTH;
        gc.weighty = 1;

        // Section title label (updated on load)
        lblSection = new JLabel("ICT 11-02", SwingConstants.CENTER);
        lblSection.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSection.setForeground(MAROON);
        lblSection.setBorder(new EmptyBorder(8, 0, 8, 0));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setBackground(CARD_BG);
        titleRow.add(lblSection, BorderLayout.CENTER);

        gc.gridx = 0; gc.gridy = 0;
        gc.gridwidth = DAYS.length + 1;
        gc.weighty = 0;
        gridPanel.add(titleRow, gc);
        gc.gridwidth = 1;

        // Column headers: TIME | MON | TUE | WED | THU | FRI
        gc.gridy = 1; gc.weighty = 0;

        gc.gridx = 0;
        gridPanel.add(headerCell("TIME"), gc);
        for (int d = 0; d < DAYS.length; d++) {
            gc.gridx = d + 1;
            String dayLabel = DAYS[d];
            if (d == 4) dayLabel = "<html><center>FRIDAY<br><i style='font-size:9px'>Online Classes</i></center></html>";
            gridPanel.add(headerCell(dayLabel), gc);
        }

        // Rows
        for (int t = 0; t < TIME_SLOTS.length; t++) {
            gc.gridy = t + 2;
            gc.weighty = 1;
            boolean isBreak = isBreakSlot(TIME_SLOTS[t]);

            // Time cell
            gc.gridx = 0;
            gridPanel.add(timeCell(TIME_SLOTS[t], isBreak), gc);

            for (int d = 0; d < DAYS.length; d++) {
                gc.gridx = d + 1;
                grid[t][d]    = "";
                gridIds[t][d] = -1;
                gridPanel.add(buildCell(t, d, isBreak), gc);
            }
        }

        JScrollPane scroll = new JScrollPane(gridPanel);
        scroll.setBorder(new EmptyBorder(0, 0, 0, 0));
        scroll.getViewport().setBackground(CARD_BG);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(new EmptyBorder(12, 16, 12, 16));
        wrapper.setBackground(BG);
        wrapper.add(scroll, BorderLayout.CENTER);

        add(wrapper, BorderLayout.CENTER);
    }

    private JPanel buildCell(int t, int d, boolean isBreak) {
        JPanel cell = new JPanel(new BorderLayout());
        cell.setBackground(isBreak ? BREAK_BG : CARD_BG);
        cell.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        cell.setPreferredSize(new Dimension(160, isBreak ? 32 : 52));

        if (isBreak) {
            JLabel lbl = new JLabel(
                TIME_SLOTS[t].contains("12") ? "LUNCH BREAK" : "BREAK",
                SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lbl.setForeground(new Color(30, 80, 120));
            cell.add(lbl, BorderLayout.CENTER);
        } else {
            JTextField tf = new JTextField();
            tf.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            tf.setForeground(TEXT_MAIN);
            tf.setBackground(CARD_BG);
            tf.setBorder(new EmptyBorder(4, 6, 4, 6));
            tf.setHorizontalAlignment(SwingConstants.CENTER);
            tf.setName("cell_" + t + "_" + d);

            // Store text field reference via client property on cell
            cell.putClientProperty("textfield", tf);
            cell.add(tf, BorderLayout.CENTER);
        }

        return cell;
    }

    // ── Load from DB ──────────────────────────────────────────
    private void loadSchedule() {
    String section = (String) cbSection.getSelectedItem();
    lblSection.setText(section);

    // Clear grid
    for (int t = 0; t < TIME_SLOTS.length; t++)
        for (int d = 0; d < DAYS.length; d++) {
            grid[t][d]    = "";
            gridIds[t][d] = -1;
        }

    String sql = "SELECT id, day, subject, start_time, end_time FROM schedules WHERE section = ?";
    try (Connection con = DatabaseManager.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, section);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            int    id       = rs.getInt("id");
            String day      = rs.getString("day");
            String subject  = rs.getString("subject");
            String startStr = rs.getTime("start_time").toString().substring(0, 5);
            String endStr   = rs.getTime("end_time").toString().substring(0, 5);

            int dIdx      = getDayIndex(day);
            int startSlot = getSlotIndex(startStr);
            int endSlot   = getEndSlotIndex(endStr); // last slot BEFORE end time

            if (dIdx < 0 || startSlot < 0) continue;

            // Fill ALL slots within the time range with the same subject
            for (int t = startSlot; t <= endSlot; t++) {
                if (isBreakSlot(TIME_SLOTS[t])) continue;
                grid[t][dIdx]    = subject;
                gridIds[t][dIdx] = id;
            }
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this,
            "Load error: " + e.getMessage(), "DB Error",
            JOptionPane.ERROR_MESSAGE);
        return;
    }

    updateGridUI();
}


    private void updateGridUI() {
        for (Component comp : gridPanel.getComponents()) {
            if (comp instanceof JPanel cell) {
                Object tf = cell.getClientProperty("textfield");
                if (tf instanceof JTextField field) {
                    String name = field.getName(); // cell_t_d
                    String[] parts = name.split("_");
                    if (parts.length == 3) {
                        int t = Integer.parseInt(parts[1]);
                        int d = Integer.parseInt(parts[2]);
                        field.setText(grid[t][d]);
                    }
                }
            }
        }
    }

    // ── Save All ──────────────────────────────────────────────
    private void saveAll() {
    String section = (String) cbSection.getSelectedItem();

    // Read current text field values into grid[][]
    for (Component comp : gridPanel.getComponents()) {
        if (comp instanceof JPanel cell) {
            Object tf = cell.getClientProperty("textfield");
            if (tf instanceof JTextField field) {
                String[] parts = field.getName().split("_");
                if (parts.length == 3) {
                    int t = Integer.parseInt(parts[1]);
                    int d = Integer.parseInt(parts[2]);
                    grid[t][d] = field.getText().trim();
                }
            }
        }
    }

    // Delete ALL existing entries for this section first — clean slate
    try (Connection con = DatabaseManager.getConnection();
         PreparedStatement ps = con.prepareStatement(
             "DELETE FROM schedules WHERE section = ?")) {
        ps.setString(1, section);
        ps.executeUpdate();
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this,
            "Clear error: " + e.getMessage(), "DB Error",
            JOptionPane.ERROR_MESSAGE);
        return;
    }

    // Now consolidate consecutive same-subject slots → single DB entry
    int saved = 0;

    try (Connection con = DatabaseManager.getConnection()) {
        for (int d = 0; d < DAYS.length; d++) {
            int t = 0;
            while (t < TIME_SLOTS.length) {
                // Skip break slots
                if (isBreakSlot(TIME_SLOTS[t])) { t++; continue; }

                String subject = grid[t][d];

                // Skip empty slots
                if (subject.isEmpty()) { t++; continue; }

                // Find how far this subject extends consecutively
                int startT = t;
                while (t + 1 < TIME_SLOTS.length
                        && !isBreakSlot(TIME_SLOTS[t + 1])
                        && grid[t + 1][d].equals(subject)) {
                    t++;
                }
                int endT = t;

                // startT → endT = one DB entry
                String startTime = SLOT_START[startT] + ":00";
                String endTime   = SLOT_END[endT]     + ":00";

                try (PreparedStatement ps = con.prepareStatement(
                         "INSERT INTO schedules (section, day, subject, start_time, end_time) " +
                         "VALUES (?, ?, ?, ?, ?)")) {
                    ps.setString(1, section);
                    ps.setString(2, DAYS[d]);
                    ps.setString(3, subject);
                    ps.setString(4, startTime);
                    ps.setString(5, endTime);
                    ps.executeUpdate();
                    saved++;
                }

                t++;
            }
        }

        JOptionPane.showMessageDialog(this,
            "✔  " + saved + " entries saved for " + section,
            "Save Complete", JOptionPane.INFORMATION_MESSAGE);

        loadSchedule();
        DatabaseManager.loadDatabases();

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this,
            "Save error: " + e.getMessage(), "DB Error",
            JOptionPane.ERROR_MESSAGE);
    }
}

// Returns the last slot index whose END time matches the given end time
private int getEndSlotIndex(String endTime) {
    for (int i = SLOT_END.length - 1; i >= 0; i--)
        if (SLOT_END[i].equals(endTime)) return i;
    return -1;
}


    // ── Clear Section ─────────────────────────────────────────
    private void clearSection() {
        String section = (String) cbSection.getSelectedItem();
        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "DELETE FROM schedules WHERE section = ?")) {
            ps.setString(1, section);
            ps.executeUpdate();
            loadSchedule();
            JOptionPane.showMessageDialog(this,
                "Schedule cleared for " + section,
                "Done", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error: " + e.getMessage(), "DB Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Helpers ───────────────────────────────────────────────
    private JPanel headerCell(String text) {
        JPanel cell = new JPanel(new BorderLayout());
        cell.setBackground(HEADER_BG);
        cell.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80), 1));
        cell.setPreferredSize(new Dimension(text.equals("TIME") ? 110 : 160, 42));

        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(HEADER_FG);
        lbl.setBorder(new EmptyBorder(4, 4, 4, 4));
        cell.add(lbl, BorderLayout.CENTER);
        return cell;
    }

    private JPanel timeCell(String time, boolean isBreak) {
        JPanel cell = new JPanel(new BorderLayout());
        cell.setBackground(isBreak ? BREAK_BG : TIME_BG);
        cell.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        cell.setPreferredSize(new Dimension(110, isBreak ? 32 : 52));

        JLabel lbl = new JLabel(time, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI",
            isBreak ? Font.BOLD : Font.PLAIN, 11));
        lbl.setForeground(isBreak ? new Color(30, 80, 120) : TEXT_MAIN);
        lbl.setBorder(new EmptyBorder(4, 6, 4, 6));
        cell.add(lbl, BorderLayout.CENTER);
        return cell;
    }

    private boolean isBreakSlot(String slot) {
        for (String b : BREAK_SLOTS)
            if (b.equals(slot)) return true;
        return false;
    }

    private int getDayIndex(String day) {
        for (int i = 0; i < DAYS.length; i++)
            if (DAYS[i].equalsIgnoreCase(day)) return i;
        return -1;
    }

    private int getSlotIndex(String startTime) {
        for (int i = 0; i < SLOT_START.length; i++)
            if (SLOT_START[i].equals(startTime)) return i;
        return -1;
    }

    private JButton styledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 16, 30));
        return btn;
    }
}
