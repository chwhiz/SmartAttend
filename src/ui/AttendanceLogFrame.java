package ui;

import logic.DatabaseManager;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class AttendanceLogFrame extends JFrame {

    private static final Color MAROON    = new Color(138, 26, 19);
    private static final Color GOLD      = new Color(248, 205, 0);
    private static final Color BG        = new Color(245, 243, 240);
    private static final Color CARD_BG   = Color.WHITE;
    private static final Color TEXT_MAIN = new Color(40, 40, 40);
    private static final Color TEXT_DIM  = new Color(130, 130, 130);
    private static final Color BORDER    = new Color(220, 215, 210);
    private static final Color SUCCESS   = new Color(39, 174, 96);
    private static final Color DANGER    = new Color(192, 57, 43);
    private static final Color ROW_ALT   = new Color(250, 248, 245);

    public AttendanceLogFrame() {
        setTitle("Attendance Log — ICT 11-02");
        setSize(960, 560);
        setLocationRelativeTo(null);
        setResizable(true);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout(0, 0));

        buildHeader();
        buildTable();
    }

    // ── Header ────────────────────────────────────────────────
    private void buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(MAROON);
        header.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel("Attendance Log  ·  ICT 11-02");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(GOLD);

        JLabel sub = new JLabel("University of Perpetual Help System DALTA  ·  Molino Campus");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(new Color(255, 240, 200));

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setBackground(MAROON);
        left.add(title);
        left.add(sub);

        JPanel goldBar = new JPanel();
        goldBar.setBackground(GOLD);
        goldBar.setPreferredSize(new Dimension(0, 3));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(header,  BorderLayout.CENTER);
        wrapper.add(goldBar, BorderLayout.SOUTH);
        header.add(left, BorderLayout.WEST);

        add(wrapper, BorderLayout.NORTH);
    }

    // ── Table ─────────────────────────────────────────────────
    private void buildTable() {
        String[] columns = {"#", "Date", "Time", "Student Name", "Section", "Subject", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        // Load from MariaDB
        String sql = "SELECT log_date, log_time, full_name, section, subject, status " +
                     "FROM attendance_log ORDER BY log_date DESC, log_time DESC";

        try (Connection con = DatabaseManager.getConnection();
             Statement  st  = con.createStatement();
             ResultSet  rs  = st.executeQuery(sql)) {

            int row = 1;
            while (rs.next()) {
                model.addRow(new Object[]{
                    row++,
                    rs.getDate  ("log_date").toString(),
                    rs.getTime  ("log_time").toString().substring(0, 5), // HH:MM
                    rs.getString("full_name"),
                    rs.getString("section"),
                    rs.getString("subject"),
                    rs.getString("status")
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Failed to load attendance log:\n" + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setForeground(TEXT_MAIN);
        table.setBackground(CARD_BG);
        table.setRowHeight(28);
        table.setGridColor(BORDER);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(new Color(138, 26, 19, 40));
        table.setSelectionForeground(TEXT_MAIN);
        table.setFocusable(false);

        // Column widths
        int[] widths = {36, 90, 60, 200, 160, 180, 120};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(245, 243, 240));
        header.setForeground(TEXT_DIM);
        header.setBorder(new MatteBorder(0, 0, 2, 0, BORDER));
        header.setReorderingAllowed(false);
        header.setResizingAllowed(true);

        // Alternating rows + status color renderer
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setBorder(new EmptyBorder(0, 10, 0, 10));

                if (sel) {
                    setBackground(new Color(138, 26, 19, 30));
                    setForeground(TEXT_MAIN);
                } else {
                    setBackground(row % 2 == 0 ? CARD_BG : ROW_ALT);
                    setForeground(TEXT_MAIN);
                }

                // Status column coloring
                if (col == 6 && val != null) {
                    String s = val.toString();
                    if (s.contains("LATE"))    setForeground(DANGER);
                    else if (s.equals("PRESENT")) setForeground(SUCCESS);
                    else setForeground(TEXT_DIM);
                    setFont(new Font("Segoe UI", Font.BOLD, 12));
                } else {
                    setFont(new Font("Segoe UI", Font.PLAIN, 13));
                }

                // Row number center-align
                if (col == 0) setHorizontalAlignment(CENTER);
                else setHorizontalAlignment(LEFT);

                return this;
            }
        });

        // Footer — row count
        JLabel footer = new JLabel("  " + model.getRowCount() + " records loaded from database");
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footer.setForeground(TEXT_DIM);
        footer.setBorder(new EmptyBorder(8, 10, 8, 10));
        footer.setBackground(BG);
        footer.setOpaque(true);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new EmptyBorder(0, 0, 0, 0));
        scroll.getViewport().setBackground(CARD_BG);
        scroll.setBorder(new MatteBorder(1, 0, 0, 0, BORDER));

        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(new EmptyBorder(16, 20, 0, 20));
        content.setBackground(BG);
        content.add(scroll, BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);
        add(footer,  BorderLayout.SOUTH);
    }
}
