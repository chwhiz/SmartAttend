package ui;

import logic.DatabaseManager;
import model.AdminUser;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class AdminManagerFrame extends JFrame {

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

    public AdminManagerFrame() {
        setTitle("Admin Manager");
        setSize(780, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout(0, 0));

        buildTitleBar();
        buildContent();   // ← toolbar + table in one CENTER panel
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

        JLabel title = new JLabel("Admin Manager");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Manage administrator accounts");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(new Color(240, 200, 200));

        JPanel textStack = new JPanel(new GridLayout(2, 1));
        textStack.setBackground(MAROON);
        textStack.add(title);
        textStack.add(sub);

        JButton btnClose = new JButton("✕");
        btnClose.setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));
        btnClose.setForeground(Color.WHITE);
        btnClose.setBackground(MAROON);
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.setOpaque(true);
        btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClose.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnClose.setBackground(DANGER); }
            public void mouseExited(MouseEvent e)  { btnClose.setBackground(MAROON); }
        });
        btnClose.addActionListener(e -> dispose());

        bar.add(textStack, BorderLayout.WEST);
        bar.add(btnClose,  BorderLayout.EAST);

        JPanel goldBar = new JPanel();
        goldBar.setBackground(GOLD);
        goldBar.setPreferredSize(new Dimension(0, 3));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(bar,     BorderLayout.CENTER);
        wrapper.add(goldBar, BorderLayout.SOUTH);
        add(wrapper, BorderLayout.NORTH);
    }

    // =========================================================
    //  CONTENT (toolbar + table together to avoid NORTH conflict)
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
        searchField.setPreferredSize(new Dimension(240, 34));
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

        JButton btnAdd    = toolbarButton("Add",    MAROON,                  Color.WHITE);
        JButton btnEdit   = toolbarButton("Edit",   new Color(52, 73, 94),   Color.WHITE);
        JButton btnDelete = toolbarButton("Delete", DANGER,                  Color.WHITE);
        JButton btnRetoken = toolbarButton("Retoken", new Color(180, 100, 0), Color.WHITE);
        
        btnRetoken.addActionListener(e -> retokenSelected());
        btnAdd.addActionListener(e    -> showAddDialog());
        btnEdit.addActionListener(e   -> showEditDialog());
        btnDelete.addActionListener(e -> deleteSelected());

        btnRow.add(btnAdd);
        btnRow.add(btnEdit);
        btnRow.add(btnDelete);
        btnRow.add(btnRetoken);

        toolbar.add(searchRow, BorderLayout.WEST);
        toolbar.add(btnRow,    BorderLayout.EAST);

        // ── Table ─────────────────────────────────────────────
        String[] cols = {"Admin ID", "Full Name", "Secret Key (TOTP)"};
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

        // Mask secret key column
        table.getColumnModel().getColumn(2).setCellRenderer(
            new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object val,
                        boolean sel, boolean foc, int row, int col) {
                    super.getTableCellRendererComponent(t, "••••••••••••••••",
                        sel, foc, row, col);
                    setBorder(new EmptyBorder(0, 12, 0, 12));
                    setForeground(TEXT_DIM);
                    setBackground(sel
                        ? new Color(138, 26, 19, 40)
                        : row % 2 == 0 ? CARD_BG : new Color(250, 248, 246));
                    return this;
                }
            }
        );

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
    
    
    // RETOKEN
    
    private void retokenSelected() {
    int row = table.getSelectedRow();
    if (row < 0) {
        JOptionPane.showMessageDialog(this, "Select an admin first.",
            "Info", JOptionPane.INFORMATION_MESSAGE);
        return;
    }

    String id   = (String) tableModel.getValueAt(row, 0);
    String name = (String) tableModel.getValueAt(row, 1);

    // ── Confirm dialog with "sure" typed ─────────────────────
    JPanel confirmPanel = new JPanel(new BorderLayout(0, 10));
    confirmPanel.setBackground(Color.WHITE);

    JLabel msg = new JLabel(
        "<html>This will <b>invalidate</b> the current TOTP key for <b>"
        + name + "</b>.<br>Type <b>sure</b> to confirm.</html>");
    msg.setFont(new Font("Segoe UI", Font.PLAIN, 13));

    JTextField confirmField = new JTextField();
    confirmField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    confirmField.setBorder(new CompoundBorder(
        new LineBorder(new Color(220, 215, 210), 1, true),
        new EmptyBorder(6, 10, 6, 10)
    ));

    confirmPanel.add(msg,          BorderLayout.CENTER);
    confirmPanel.add(confirmField, BorderLayout.SOUTH);
    confirmPanel.setPreferredSize(new Dimension(360, 80));

    int result = JOptionPane.showConfirmDialog(this,
        confirmPanel, "Confirm Retoken",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

    if (result != JOptionPane.OK_OPTION) return;

    if (!confirmField.getText().trim().equalsIgnoreCase("sure")) {
        JOptionPane.showMessageDialog(this,
            "Retoken cancelled. You did not type 'sure'.",
            "Cancelled", JOptionPane.INFORMATION_MESSAGE);
        return;
    }

    // ── Show onboarding with new key ─────────────────────────
    TOTPOnboardingDialog onboard = new TOTPOnboardingDialog(this, name);
    onboard.setVisible(true);

    boolean ok = DatabaseManager.updateAdmin(
        id, id, name, onboard.getSecretKey());

    if (ok) {
        loadFromDb();
        showToast("TOTP key regenerated for " + name + ".");
    } else {
        JOptionPane.showMessageDialog(this,
            "Retoken failed.", "DB Error", JOptionPane.ERROR_MESSAGE);
    }
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

        JLabel hint = new JLabel("Secret keys are protected.");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hint.setForeground(TEXT_DIM);

        JLabel countLabel = new JLabel();
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        countLabel.setForeground(TEXT_DIM);
        tableModel.addTableModelListener(e ->
            countLabel.setText("Total: " + tableModel.getRowCount() + " admins"));

        footer.add(hint,       BorderLayout.WEST);
        footer.add(countLabel, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);
    }

    // =========================================================
    //  DATA — all MariaDB, no CSV
    // =========================================================
    private void loadFromDb() {
        DatabaseManager.loadAdmins();   // fresh pull from DB
        tableModel.setRowCount(0);
        for (AdminUser admin : DatabaseManager.adminDb) {
            tableModel.addRow(new Object[]{admin.id, admin.name, admin.secretKey});
        }
    }

    private void filterTable() {
        String q = searchField.getText().trim().toLowerCase();
        tableModel.setRowCount(0);
        for (AdminUser admin : DatabaseManager.adminDb) {
            if (admin.id.toLowerCase().contains(q) ||
                admin.name.toLowerCase().contains(q)) {
                tableModel.addRow(new Object[]{admin.id, admin.name, admin.secretKey});
            }
        }
    }

    // =========================================================
    //  CRUD — direct to MariaDB
    // =========================================================
        private void showAddDialog() {
            // Step 1 — Info
            AdminDialog dlg = new AdminDialog(this, "Add Admin", "", "", null);
            dlg.setVisible(true);
            if (!dlg.isConfirmed()) return;

            // Step 2 — TOTP Onboarding (auto-generates key)
            TOTPOnboardingDialog onboard = new TOTPOnboardingDialog(
                this, dlg.getName());
            onboard.setVisible(true);
            // Even if they close without clicking Done, key is still saved
            // (they can always retoken later)

            boolean ok = DatabaseManager.insertAdmin(
                dlg.getId(), dlg.getName(), onboard.getSecretKey());

            if (ok) {
                loadFromDb();
                showToast("Admin added successfully.");
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to add admin. ID may already exist.",
                    "DB Error", JOptionPane.ERROR_MESSAGE);
            }
        }


    private void showEditDialog() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an admin first.",
                "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String id  = (String) tableModel.getValueAt(row, 0);
        String nm  = (String) tableModel.getValueAt(row, 1);
        // Get real secret key from cache (table shows masked)
        String key = DatabaseManager.adminDb.stream()
            .filter(a -> a.id.equals(id))
            .map(a -> a.secretKey)
            .findFirst().orElse("");

        AdminDialog dlg = new AdminDialog(this, "Edit Admin", id, nm, key);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            boolean ok = DatabaseManager.updateAdmin(
                id, dlg.getId(), dlg.getName(), dlg.getSecretKey());
            if (ok) {
                loadFromDb();
                showToast("Admin updated ✓");
            } else {
                JOptionPane.showMessageDialog(this,
                    "Update failed.", "DB Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return;

        if (DatabaseManager.adminDb.size() <= 1) {
            JOptionPane.showMessageDialog(this,
                "Cannot delete the last admin account.",
                "Blocked", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String id   = (String) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
            "<html>Delete admin <b>" + name + "</b>?<br>" +
            "<small>This cannot be undone.</small></html>",
            "Confirm Delete", JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = DatabaseManager.deleteAdmin(id);
            if (ok) {
                loadFromDb();
                showToast("Admin deleted.");
            } else {
                JOptionPane.showMessageDialog(this,
                    "Delete failed.", "DB Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =========================================================
    //  HELPERS
    // =========================================================
    private void showToast(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Done",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private JButton toolbarButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(7, 14, 7, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // =========================================================
    //  INNER DIALOG
    // =========================================================
    static class AdminDialog extends JDialog {
        private final JTextField fId, fName;
        private final JPasswordField fKey;
        private boolean confirmed = false;

        AdminDialog(Frame parent, String title,
                    String id, String name, String key) {
            super(parent, title, true);
            setSize(420, 340);
            setLocationRelativeTo(parent);
            setResizable(false);
            setUndecorated(true);

            JPanel root = new JPanel(new BorderLayout());
            root.setBorder(new LineBorder(new Color(220, 215, 210), 1));

            // Header
            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(new Color(138, 26, 19));
            header.setBorder(new EmptyBorder(12, 16, 12, 16));
            JLabel lbl = new JLabel(title);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lbl.setForeground(Color.WHITE);
            header.add(lbl);

            JPanel goldBar = new JPanel();
            goldBar.setBackground(new Color(248, 205, 0));
            goldBar.setPreferredSize(new Dimension(0, 2));

            JPanel hWrap = new JPanel(new BorderLayout());
            hWrap.add(header,  BorderLayout.CENTER);
            hWrap.add(goldBar, BorderLayout.SOUTH);

            // Form
            JPanel form = new JPanel(new GridLayout(4, 2, 10, 14));
            form.setBorder(new EmptyBorder(22, 20, 16, 20));
            form.setBackground(Color.WHITE);

            fId   = field(id);
            fName = field(name);
            fKey  = new JPasswordField(key);
            fKey.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            fKey.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 215, 210), 1, true),
                new EmptyBorder(5, 8, 5, 8)
            ));

            // Show/hide toggle for secret key
            JCheckBox cbShow = new JCheckBox("Show secret key");
            cbShow.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            cbShow.setForeground(new Color(130, 130, 130));
            cbShow.setBackground(Color.WHITE);
            cbShow.addActionListener(e ->
                fKey.setEchoChar(cbShow.isSelected() ? (char) 0 : '●'));

            form.add(label("Admin ID:"));         form.add(fId);
            form.add(label("Full Name:"));         form.add(fName);
            form.add(label("TOTP Secret Key:"));   form.add(fKey);
            form.add(new JLabel());                form.add(cbShow);

            // Buttons
            JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
            btns.setBackground(new Color(245, 243, 240));
            btns.setBorder(new MatteBorder(1, 0, 0, 0,
                new Color(220, 215, 210)));

            JButton cancel  = btn("Cancel",  new Color(150, 150, 150));
            JButton confirm = btn("Confirm", new Color(138, 26, 19));

            cancel.addActionListener(e -> dispose());
            confirm.addActionListener(e -> {
                if (fId.getText().trim().isEmpty() ||
                    fName.getText().trim().isEmpty() ||
                    new String(fKey.getPassword()).trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                        "All fields are required.", "Validation",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
                confirmed = true;
                dispose();
            });

            btns.add(cancel);
            btns.add(confirm);

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

        private JButton btn(String text, Color bg) {
            JButton b = new JButton(text);
            b.setFont(new Font("Segoe UI", Font.BOLD, 12));
            b.setBackground(bg);
            b.setForeground(Color.WHITE);
            b.setOpaque(true);
            b.setBorderPainted(false);
            b.setFocusPainted(false);
            b.setBorder(new EmptyBorder(8, 18, 8, 18));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return b;
        }

        public boolean isConfirmed() { return confirmed; }
        public String getId()        { return fId.getText().trim(); }
        public String getName()      { return fName.getText().trim(); }
        public String getSecretKey() { return new String(fKey.getPassword()).trim(); }
    }
}
