package ui;

import logic.DatabaseManager;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class StudentManagerFrame extends JFrame {

    private static final Color MAROON    = new Color(138, 26, 19);
    private static final Color GOLD      = new Color(248, 205, 0);
    private static final Color BG        = new Color(245, 243, 240);
    private static final Color CARD_BG   = Color.WHITE;
    private static final Color TEXT_MAIN = new Color(40, 40, 40);
    private static final Color TEXT_DIM  = new Color(130, 130, 130);
    private static final Color BORDER    = new Color(220, 215, 210);
    private static final Color DANGER    = new Color(192, 57, 43);
    private static final Color SUCCESS   = new Color(39, 174, 96);

    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField searchField;

    public StudentManagerFrame() {
        setTitle("Student Manager");
        setSize(1000, 600);   // wider for extra RFID column
        setUndecorated(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout(0, 0));

        buildTitleBar();
        buildContent();
        buildFooter();
        loadFromDb();
    }

    // =========================================================
    //  TITLE BAR
    // =========================================================
    private void buildTitleBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(MAROON);
        bar.setBorder(new EmptyBorder(12, 18, 12, 18));

        JLabel title = new JLabel("Student Manager");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Manage enrolled students");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(new Color(240, 200, 200));

        JPanel textStack = new JPanel(new GridLayout(2, 1));
        textStack.setBackground(MAROON);
        textStack.add(title);
        textStack.add(sub);

        // REFACTORED: Use UIBuilder for close button directly
        JButton btnClose = UIBuilder.createCloseButton(this);

        bar.add(textStack, BorderLayout.WEST);
        bar.add(btnClose,  BorderLayout.EAST);

        // REFACTORED: Use UIBuilder for gold bar separator
        JPanel goldBar = UIBuilder.createGoldBar();

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(bar,     BorderLayout.CENTER);
        wrapper.add(goldBar, BorderLayout.SOUTH);
        add(wrapper, BorderLayout.NORTH);
    }

    // =========================================================
    //  CONTENT
    // =========================================================
    private void buildContent() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(BG);

        // ── Toolbar ───────────────────────────────────────────
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setBackground(BG);
        toolbar.setBorder(new EmptyBorder(14, 18, 10, 18));

        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setPreferredSize(new Dimension(260, 34));
        searchField.setBorder(new CompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(4, 10, 4, 10)
        ));
        searchField.getDocument().addDocumentListener(
            new javax.swing.event.DocumentListener() {
                public void insertUpdate(javax.swing.event.DocumentEvent e)  { filterTable(); }
                public void removeUpdate(javax.swing.event.DocumentEvent e)  { filterTable(); }
                public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            });

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        searchRow.setBackground(BG);
        JLabel searchIcon = new JLabel("Search: ");
        searchIcon.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchRow.add(searchIcon);
        searchRow.add(searchField);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setBackground(BG);

        // REFACTORED: Use UIBuilder to replace manual toolbar generation logic
        JButton btnAdd    = UIBuilder.createToolbarButton("Add",    MAROON,                Color.WHITE);
        JButton btnEdit   = UIBuilder.createToolbarButton("Edit",   new Color(52, 73, 94), Color.WHITE);
        JButton btnDelete = UIBuilder.createToolbarButton("Delete", DANGER,                Color.WHITE);

        btnAdd.addActionListener(e    -> showAddDialog());
        btnEdit.addActionListener(e   -> showEditDialog());
        btnDelete.addActionListener(e -> deleteSelected());

        btnRow.add(btnAdd);
        btnRow.add(btnEdit);
        btnRow.add(btnDelete);

        toolbar.add(searchRow, BorderLayout.WEST);
        toolbar.add(btnRow,    BorderLayout.EAST);

        // ── Table — 4 columns now ──────────────────────────────
        String[] cols = {"RFID UID", "Student ID", "Full Name", "Section"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(32);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(138, 26, 19, 30));
        table.setSelectionForeground(TEXT_MAIN);
        table.setFocusable(false);

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(160); // RFID UID
        table.getColumnModel().getColumn(1).setPreferredWidth(120); // Student ID
        table.getColumnModel().getColumn(2).setPreferredWidth(240); // Full Name
        table.getColumnModel().getColumn(3).setPreferredWidth(160); // Section

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(245, 240, 235));
        header.setForeground(MAROON);
        header.setBorder(new MatteBorder(0, 0, 2, 0, GOLD));
        header.setReorderingAllowed(false);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                setBackground(sel
                    ? new Color(138, 26, 19, 40)
                    : row % 2 == 0 ? CARD_BG : new Color(250, 248, 246));
                return this;
            }
        });

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) showEditDialog();
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new MatteBorder(1, 0, 0, 0, BORDER));
        scroll.getViewport().setBackground(CARD_BG);

        content.add(toolbar, BorderLayout.NORTH);
        content.add(scroll,  BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);
    }

    // =========================================================
    //  FOOTER
    // =========================================================
    private void buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(new Color(250, 248, 246));
        footer.setBorder(new CompoundBorder(
            new MatteBorder(1, 0, 0, 0, BORDER),
            new EmptyBorder(8, 18, 8, 18)
        ));

        JLabel hint = new JLabel("Double-click a row to edit  ·  All changes saved directly to MariaDB");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hint.setForeground(TEXT_DIM);

        JLabel countLabel = new JLabel();
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        countLabel.setForeground(TEXT_DIM);
        tableModel.addTableModelListener(e ->
            countLabel.setText("Total: " + tableModel.getRowCount() + " students"));

        footer.add(hint,       BorderLayout.WEST);
        footer.add(countLabel, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);
    }

    // =========================================================
    //  DATA
    // =========================================================
    private void loadFromDb() {
        DatabaseManager.loadStudents();
        tableModel.setRowCount(0);
        for (Map.Entry<String, String[]> entry : DatabaseManager.studentDb.entrySet()) {
            String rfid    = entry.getKey();           // rfid_uid
            String name    = entry.getValue()[0];
            String section = entry.getValue()[1];
            String id      = entry.getValue()[2];      // student_id
            tableModel.addRow(new Object[]{rfid, id, name, section});
        }
    }

    private void filterTable() {
        String query = searchField.getText().trim().toLowerCase();
        tableModel.setRowCount(0);
        for (Map.Entry<String, String[]> entry : DatabaseManager.studentDb.entrySet()) {
            String rfid    = entry.getKey();
            String name    = entry.getValue()[0].toLowerCase();
            String section = entry.getValue()[1].toLowerCase();
            String id      = entry.getValue()[2].toLowerCase();
            if (rfid.toLowerCase().contains(query)
                    || id.contains(query)
                    || name.contains(query)
                    || section.contains(query)) {
                tableModel.addRow(new Object[]{rfid, entry.getValue()[2], entry.getValue()[0], entry.getValue()[1]});
            }
        }
    }

    // =========================================================
    //  CRUD
    // =========================================================
    private void showAddDialog() {
        // Step 1 — Student info
        StudentInfoDialog info = new StudentInfoDialog(this, "Add Student — Step 1 of 2",
            "", "", "");
        info.setVisible(true);
        if (!info.isConfirmed()) return;
        // ── Check if Student ID already exists ───────────────────
           String newId = info.getId();
           boolean idExists = DatabaseManager.studentDb.values().stream()
               .anyMatch(data -> data[2].equalsIgnoreCase(newId));
        
                if (idExists) {
         ToastNotification.show(this,
             "Duplicate Student ID",
             newId + " is already registered.");
         return;
     }
         
        // Step 2 — Tap RFID
        RFIDCaptureDialog rfid = new RFIDCaptureDialog(this,
            info.getName() + " (" + info.getId() + ")");
        rfid.setVisible(true);
        if (!rfid.isConfirmed()) return;

        boolean ok = DatabaseManager.insertStudent(
            rfid.getRfidUid(),
            info.getId(),
            info.getName(),
            info.getSection()
        );

        if (ok) {
            loadFromDb();
            showToast("Student added successfully!");
        } else {
            JOptionPane.showMessageDialog(this,
                "Failed to add student. RFID or Student ID may already exist.",
                "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showEditDialog() {
        int row = table.getSelectedRow();
        if (row < 0) { showToast("Select a student first."); return; }

        String rfid    = (String) tableModel.getValueAt(row, 0);
        String id      = (String) tableModel.getValueAt(row, 1);
        String name    = (String) tableModel.getValueAt(row, 2);
        String section = (String) tableModel.getValueAt(row, 3);

        // Step 1 — Edit info
        StudentInfoDialog info = new StudentInfoDialog(this, "Edit Student", id, name, section);
        info.setVisible(true);
        if (!info.isConfirmed()) return;

        // Ask if they want to re-tap RFID
        int choice = JOptionPane.showConfirmDialog(this,
            "<html>Do you want to <b>re-register the RFID card</b> as well?<br>" +
            "<small>Choose No to keep the existing card.</small></html>",
            "Update RFID?", JOptionPane.YES_NO_CANCEL_OPTION);

        if (choice == JOptionPane.CANCEL_OPTION) return;

        String newRfid = rfid; // default: keep existing

        if (choice == JOptionPane.YES_OPTION) {
            RFIDCaptureDialog rfidDlg = new RFIDCaptureDialog(this,
                info.getName() + " (" + info.getId() + ")");
            rfidDlg.setVisible(true);
            if (!rfidDlg.isConfirmed()) return;
            newRfid = rfidDlg.getRfidUid();
        }

        boolean ok = DatabaseManager.updateStudent(
            rfid, newRfid, info.getId(), info.getName(), info.getSection());

        if (ok) {
            loadFromDb();
            showToast("Student updated.");
        } else {
            JOptionPane.showMessageDialog(this,
                "Update failed.", "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { showToast("Select a student first."); return; }

        String rfid = (String) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 2);
        String id   = (String) tableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
            "<html>Delete <b>" + name + "</b> (" + id + ")?<br>" +
            "<small>This cannot be undone.</small></html>",
            "Confirm Delete", JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = DatabaseManager.deleteStudent(rfid);
            if (ok) {
                loadFromDb();
                showToast("Student deleted.");
            } else {
                JOptionPane.showMessageDialog(this,
                    "Delete failed.", "DB Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showToast(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Done",
            JOptionPane.INFORMATION_MESSAGE);
    }

    // =========================================================
    //  HELPERS
    // =========================================================
    // Empty because we refactored out the toolbarButton method

    // =========================================================
    //  INNER DIALOG — Step 1: Student Info
    // =========================================================
    static class StudentInfoDialog extends JDialog {
        private final JTextField fId, fName;
        private final JComboBox<String> cbSection;
        private boolean confirmed = false;

        StudentInfoDialog(Frame parent, String title,
                          String id, String name, String section) {
            super(parent, title, true);
            setSize(420, 320);
            setLocationRelativeTo(parent);
            setResizable(false);
            setUndecorated(true);

            JPanel root = new JPanel(new BorderLayout());
            root.setBorder(new LineBorder(new Color(220, 215, 210), 1));

            // Header
            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(new Color(138, 26, 19));
            header.setBorder(new EmptyBorder(12, 16, 12, 16));

            JPanel titleStack = new JPanel(new GridLayout(2, 1, 0, 2));
            titleStack.setBackground(new Color(138, 26, 19));

            JLabel lblTitle = new JLabel(title);
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblTitle.setForeground(Color.WHITE);

            JLabel lblSub = new JLabel("Fill in student details before tapping the card.");
            lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lblSub.setForeground(new Color(240, 200, 200));

            titleStack.add(lblTitle);
            titleStack.add(lblSub);
            header.add(titleStack, BorderLayout.CENTER);

            JPanel goldBar = new JPanel();
            goldBar.setBackground(new Color(248, 205, 0));
            goldBar.setPreferredSize(new Dimension(0, 2));

            JPanel hWrap = new JPanel(new BorderLayout());
            hWrap.add(header,  BorderLayout.CENTER);
            hWrap.add(goldBar, BorderLayout.SOUTH);

            // Form
            JPanel form = new JPanel(new GridLayout(3, 2, 10, 14));
            form.setBorder(new EmptyBorder(22, 20, 16, 20));
            form.setBackground(Color.WHITE);

            fId      = field(id);
            fName    = field(name);
            
            // Populating UI Dropdown exactly mapped to unified DatabaseManager.sectionList
            java.util.List<String> sortedSections = new ArrayList<>(DatabaseManager.sectionList);
            java.util.Collections.sort(sortedSections);
            
            cbSection = new JComboBox<>(sortedSections.toArray(new String[0]));
            if (section.isEmpty() && !sortedSections.isEmpty()) {
                cbSection.setSelectedIndex(0);
            } else {
                cbSection.setSelectedItem(section);
            }
            cbSection.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            cbSection.setBackground(Color.WHITE);
            cbSection.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 215, 210), 1, true),
                new EmptyBorder(0, 0, 0, 0)
            ));

            form.add(label("Student ID (xx-xxxx-xxx):")); form.add(fId);
            form.add(label("Full Name:"));                 form.add(fName);
            form.add(label("Section:"));                   form.add(cbSection);

            // Buttons
            JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
            btns.setBackground(new Color(245, 243, 240));
            btns.setBorder(new MatteBorder(1, 0, 0, 0, new Color(220, 215, 210)));

            // REFACTORED: Use UIBuilder for inner dialog buttons
            JButton btnCancel  = UIBuilder.createToolbarButton("Cancel",       new Color(150, 150, 150), Color.WHITE);
            JButton btnNext    = UIBuilder.createToolbarButton("Continue", new Color(138, 26, 19), Color.WHITE);

            btnCancel.addActionListener(e -> dispose());
            btnNext.addActionListener(e -> {
                String secVal = cbSection.getSelectedItem() != null ? cbSection.getSelectedItem().toString().trim() : "";
                if (fId.getText().trim().isEmpty() ||
                    fName.getText().trim().isEmpty() ||
                    secVal.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                        "All fields are required.", "Validation",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
                confirmed = true;
                dispose();
            });

            btns.add(btnCancel);
            btns.add(btnNext);

            root.add(hWrap, BorderLayout.NORTH);
            root.add(form,  BorderLayout.CENTER);
            root.add(btns,  BorderLayout.SOUTH);
            setContentPane(root);
        }

        private JTextField field(String val) {
            JTextField f = new JTextField(val);
            f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            f.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 215, 210), 1, true),
                new EmptyBorder(5, 8, 5, 8)
            ));
            return f;
        }

        private JLabel label(String text) {
            JLabel l = new JLabel(text);
            l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            l.setForeground(new Color(130, 130, 130));
            return l;
        }

        public boolean isConfirmed() { return confirmed; }
        public String getId()        { return fId.getText().trim(); }
        public String getName()      { return fName.getText().trim(); }
        public String getSection()   { return cbSection.getSelectedItem() != null ? cbSection.getSelectedItem().toString().trim() : ""; }
    }
}
