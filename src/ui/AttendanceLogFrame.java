package ui;

import logic.DatabaseManager;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class AttendanceLogFrame extends JFrame {

    private static final Color MAROON    = UIBuilder.MAROON;
    private static final Color GOLD      = UIBuilder.GOLD;
    private static final Color BG        = UIBuilder.BG;
    private static final Color CARD_BG   = UIBuilder.CARD_BG;
    private static final Color TEXT_MAIN = UIBuilder.TEXT_MAIN;
    private static final Color TEXT_DIM  = UIBuilder.TEXT_DIM;
    private static final Color BORDER    = UIBuilder.BORDER;
    private static final Color SUCCESS   = UIBuilder.SUCCESS;
    private static final Color DANGER    = UIBuilder.DANGER;
    private static final Color ROW_ALT   = new Color(250, 248, 245);

    private DefaultTableModel model;
    private JTable table;
    private JLabel footerLabel;
    private JComboBox<String> sectionCombo;

    public AttendanceLogFrame() {
        setTitle("Attendance Log");
        setSize(960, 560);
        setLocationRelativeTo(null);
        setResizable(true);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout(0, 0));

        buildHeader();
        buildContent();
        loadData("All Sections");
    }

    // ── Header ────────────────────────────────────────────────
    private void buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(MAROON);
        header.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel("Attendance Log  ·  Senior High School");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(GOLD);

        JLabel sub = new JLabel("University of Perpetual Help System DALTA  ·  Molino Campus");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(new Color(255, 240, 200));

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setBackground(MAROON);
        left.add(title);
        left.add(sub);

        // REFACTORED: Use UIBuilder for standard gold bar
        JPanel goldBar = UIBuilder.createGoldBar();

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(header,  BorderLayout.CENTER);
        wrapper.add(goldBar, BorderLayout.SOUTH);
        header.add(left, BorderLayout.WEST);

        add(wrapper, BorderLayout.NORTH);
    }

    // ── Content ─────────────────────────────────────────────────
    private void buildContent() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(new EmptyBorder(16, 20, 0, 20));
        content.setBackground(BG);

        // Toolbar for Section combo
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setBackground(BG);
        toolbar.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        JLabel lblSection = new JLabel("Filter Section: ");
        lblSection.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSection.setForeground(TEXT_MAIN);
        
        sectionCombo = new JComboBox<>();
        sectionCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sectionCombo.addItem("All Sections");
        for (String sec : DatabaseManager.sectionList) {
            sectionCombo.addItem(sec);
        }
        sectionCombo.addActionListener(e -> loadData((String) sectionCombo.getSelectedItem()));
        
        toolbar.add(lblSection);
        toolbar.add(sectionCombo);

        String[] columns = {"#", "Name", "ID Number", "Subject", "Time Period", "Status"};
        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
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
        int[] widths = {36, 220, 130, 200, 150, 120};
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

                // Status column coloring (Index 5)
                if (col == 5 && val != null) {
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
        footerLabel = new JLabel("  0 records loaded from database");
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footerLabel.setForeground(TEXT_DIM);
        footerLabel.setBorder(new EmptyBorder(8, 10, 8, 10));
        footerLabel.setBackground(BG);
        footerLabel.setOpaque(true);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new EmptyBorder(0, 0, 0, 0));
        scroll.getViewport().setBackground(CARD_BG);
        scroll.setBorder(new MatteBorder(1, 0, 0, 0, BORDER));

        content.add(toolbar, BorderLayout.NORTH);
        content.add(scroll, BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);
        add(footerLabel,  BorderLayout.SOUTH);
    }
    
    private void loadData(String sectionFilter) {
        model.setRowCount(0);
        
        String sql;
        if ("All Sections".equals(sectionFilter)) {
            sql = "SELECT student_id, full_name, subject, log_date, log_time, status " +
                  "FROM attendance_log ORDER BY log_date DESC, log_time DESC";
        } else {
            sql = "SELECT student_id, full_name, subject, log_date, log_time, status " +
                  "FROM attendance_log WHERE section = ? ORDER BY log_date DESC, log_time DESC";
        }

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
             
            if (!"All Sections".equals(sectionFilter)) {
                ps.setString(1, sectionFilter);
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                int row = 1;
                while (rs.next()) {
                    String date = rs.getDate("log_date").toString();
                    String time = rs.getTime("log_time").toString().substring(0, 5); // HH:MM
                    
                    // Format student ID safely, fallback if unformatted
                    String studentId = rs.getString("student_id");
                    if (studentId != null && studentId.length() == 9 && !studentId.contains("-")) {
                        studentId = studentId.substring(0, 2) + "-" + 
                                    studentId.substring(2, 6) + "-" + 
                                    studentId.substring(6);
                    }
                    
                    model.addRow(new Object[]{
                        row++,
                        rs.getString("full_name"),
                        studentId,
                        rs.getString("subject"),
                        date + "  " + time,
                        rs.getString("status")
                    });
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Failed to load attendance log:\n" + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        
        if (footerLabel != null) {
            footerLabel.setText("  " + model.getRowCount() + " records loaded from database");
        }
    }
}
