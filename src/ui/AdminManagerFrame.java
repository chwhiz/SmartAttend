// PACKAGES kasi nga naka folder yung mga code sa ui.

package ui;

// IMPORTS syempre, dami kasi ng kailangan natin dito sa UI haha
import logic.DatabaseManager;
import model.AdminUser;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

public class AdminManagerFrame extends JFrame {

    private static final Color MAROON    = new Color(138, 26, 19);
    private static final Color GOLD      = new Color(248, 205, 0);
    private static final Color BG        = new Color(245, 243, 240);
    private static final Color CARD_BG   = Color.WHITE;
    private static final Color TEXT_MAIN = new Color(40, 40, 40);
    private static final Color TEXT_DIM  = new Color(130, 130, 130);
    private static final Color BORDER    = new Color(220, 215, 210);
    private static final Color DANGER    = new Color(192, 57, 43);

    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField searchField;

    public AdminManagerFrame() {
        // setup ng mismong frame properties natin 
        setTitle("Admin Manager");
        setSize(780, 520);
        setUndecorated(true);
        setLocationRelativeTo(null); // gitna natin para aesthetic
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout(0, 0));

        // isa isahin tawagin yung UI build methods kasi hinati hati ko sila
        // kung isang method lang to sobrang haba niya promise sumakit ulo ko rito 
        buildTitleBar();
        buildContent();
        buildFooter();
        
        // load na natin agad data mula db
        loadFromDb();
    }

    /**
     * Builds the custom title bar at the top of the frame.
     * Includes the module title and a close button.
     */
    private void buildTitleBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(MAROON);
        bar.setBorder(new EmptyBorder(12, 18, 12, 18));

        // main title sa taas, yung malaki 
        JLabel title = new JLabel("Admin Manager");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(Color.WHITE);

        // subtitle na maliit sa ilalim para medyo may context
        JLabel sub = new JLabel("Manage administrator accounts");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(new Color(240, 200, 200));

        JPanel textStack = new JPanel(new GridLayout(2, 1));
        textStack.setBackground(MAROON);
        textStack.add(title);
        textStack.add(sub);

        // close button mula sa utility class natin (sana gumana hahaha)
        JButton btnClose = UIBuilder.createCloseButton(this);

        bar.add(textStack, BorderLayout.WEST);
        bar.add(btnClose,  BorderLayout.EAST);

        // accent bar para di ganun ka plain yung line
        JPanel goldBar = UIBuilder.createGoldBar();

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(bar,     BorderLayout.CENTER);
        wrapper.add(goldBar, BorderLayout.SOUTH);
        add(wrapper, BorderLayout.NORTH);
    }

    /**
     * Constructs the main content area which includes the toolbar
     * (search & buttons) and the admin table.
     */
    private void buildContent() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(BG);

        // ito yung toolbar sa taas ng table natin, lagyan natin ng spacing
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setBackground(BG);
        toolbar.setBorder(new EmptyBorder(14, 18, 10, 18));

        // search field syempre para mahanap agad yung admin pag madami na sila
        // though tbh for a system this size, baka isa o dalawa lang admin haha
        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setPreferredSize(new Dimension(160, 32));
        searchField.setBorder(new CompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(4, 10, 4, 10)
        ));
        
        // auto-filter habang nagta-type ka sismars
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

        JButton btnAdd    = UIBuilder.createToolbarButton("Add Admin",    MAROON,                  Color.WHITE);
        JButton btnEdit   = UIBuilder.createToolbarButton("Edit",         new Color(52, 73, 94),   Color.WHITE);
        JButton btnReCard = UIBuilder.createToolbarButton("Update RFID",  new Color(41, 128, 185), Color.WHITE);
        JButton btnRetoken= UIBuilder.createToolbarButton("Reset TOTP",   new Color(230, 126, 34), Color.WHITE);
        JButton btnDelete = UIBuilder.createToolbarButton("Delete",       DANGER,                  Color.WHITE);

        btnAdd.addActionListener(e    -> showAddDialog());
        btnEdit.addActionListener(e   -> showEditDialog());
        btnReCard.addActionListener(e -> updateRfidSelected());
        btnRetoken.addActionListener(e-> resetTotpSelected());
        btnDelete.addActionListener(e -> deleteSelected());

        btnRow.add(btnAdd);
        btnRow.add(btnEdit);
        btnRow.add(btnReCard);
        btnRow.add(btnRetoken);
        btnRow.add(btnDelete);

        toolbar.add(searchRow, BorderLayout.WEST);
        toolbar.add(btnRow,    BorderLayout.EAST);

        String[] cols = {"RFID UID / Admin ID", "School ID", "Full Name"};
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

    /**
     * Set up the footer area for hints and total counts.
     */
    private void buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(new Color(250, 248, 246));
        footer.setBorder(new CompoundBorder(
            new MatteBorder(1, 0, 0, 0, BORDER),
            new EmptyBorder(8, 18, 8, 18)
        ));

        // onting hint din for UX
        JLabel hint = new JLabel("Admin access is now secured via Master RFID.");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hint.setForeground(TEXT_DIM);

        // bilangin lang kung ilan admin currently
        JLabel countLabel = new JLabel();
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        countLabel.setForeground(TEXT_DIM);
        tableModel.addTableModelListener(e ->
            countLabel.setText("Total: " + tableModel.getRowCount() + " admins"));

        footer.add(hint,       BorderLayout.WEST);
        footer.add(countLabel, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);
    }

    /**
     * Refreshes the local admin data from the database then repopulates the table.
     */
    private void loadFromDb() {
        DatabaseManager.loadAdmins();
        tableModel.setRowCount(0); // clear the table muna 
        for (AdminUser admin : DatabaseManager.getAdmins()) {
            tableModel.addRow(new Object[]{admin.id, admin.schoolId, admin.name});
        }
    }

    /**
     * Filters the JTable rows based on the search input.
     */
    private void filterTable() {
        String q = searchField.getText().trim().toLowerCase();
        tableModel.setRowCount(0);
        // loop sa lahat para i-filter based sa name, ID o school ID. 
        // mano mano talaga kasi walang filter method yung basic JTable natin dzai 
        for (AdminUser admin : DatabaseManager.getAdmins()) {
            if (admin.id.toLowerCase().contains(q) ||
                (admin.schoolId != null && admin.schoolId.toLowerCase().contains(q)) ||
                admin.name.toLowerCase().contains(q)) {
                tableModel.addRow(new Object[]{admin.id, admin.schoolId, admin.name});
            }
        }
    }

    /**
     * Displays a dialog sequence to register a new admin: 
     * 1. Asks for Name/School ID
     * 2. Prompts to scan Master RFID
     * 3. Setups TOTP keys
     */
    private void showAddDialog() {
        JTextField nameField = new JTextField();
        JTextField schoolIdField = new JTextField();

        Object[] message = {
            "Full Name:", nameField,
            "School ID (View Only / Internal):", schoolIdField
        };

        // simple prompt lang kasi tinatamad na ko mag-ayos ng isa pang frame
        int option = JOptionPane.showConfirmDialog(this, message, "New Admin Setup", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String schoolId = schoolIdField.getText().trim();
            if (name.isEmpty() || schoolId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Fields cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // pop up yung dialog for RFID scanning
            RFIDCaptureDialog rfidDlg = new RFIDCaptureDialog(this, name);
            rfidDlg.setVisible(true);

            // pag na-scan nang tama, proceed sa enrollment
            if (rfidDlg.isConfirmed()) {
                String capturedId = rfidDlg.getRfidUid();

                // Open TOTP Enrollment kasi kailangan ng 2FA for security
                TOTPOnboardingDialog totpDlg = new TOTPOnboardingDialog(this, name);
                totpDlg.setVisible(true);

                // kapag natapos successfully yung enrollment doon i-insert 
                if (totpDlg.isFinished()) {
                    String secretKey = totpDlg.getSecretKey();
                    boolean ok = DatabaseManager.insertAdmin(capturedId, schoolId, name, secretKey);
                    if (ok) {
                        loadFromDb();
                        showToast("Admin added successfully.");
                    } else {
                        // baka duplicate info sismars
                        JOptionPane.showMessageDialog(this,
                            "Failed to add admin. They may already exist.",
                            "DB Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }

    /**
     * Updates only the RFID info of an existing administrator.
     */
    private void updateRfidSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an admin first.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String oldId    = (String) tableModel.getValueAt(row, 0);
        String schoolId = (String) tableModel.getValueAt(row, 1);
        String name     = (String) tableModel.getValueAt(row, 2);

        // warning message kasi masisira yung current setup pag nag update
        int confirm = JOptionPane.showConfirmDialog(this,
            "Register a new RFID card for " + name + "?\nThe old card will no longer work.",
            "Update RFID", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            RFIDCaptureDialog rfidDlg = new RFIDCaptureDialog(this, name);
            rfidDlg.setVisible(true);

            if (rfidDlg.isConfirmed()) {
                String newId = rfidDlg.getRfidUid();
                if (newId.equals(oldId)) {
                    // niloloko ata tayo eh, same card lang naman 
                    JOptionPane.showMessageDialog(this, "This card is already assigned to this admin.", "Info", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                // hinalungkat ko pa talaga sa list yung dating key para di mawala 
                String oldKey = DatabaseManager.getAdmins().stream().filter(a -> a.id.equals(oldId)).map(a -> a.secretKey).findFirst().orElse("none");
                boolean ok = DatabaseManager.updateAdmin(oldId, newId, schoolId, name, oldKey);
                if (ok) {
                    loadFromDb();
                    showToast("RFID card updated successfully.");
                } else {
                    JOptionPane.showMessageDialog(this, "Update failed. Card may already be registered.", "DB Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    /**
     * Shows dialog to edit basic admin details (Name and School ID).
     */
    private void showEditDialog() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an admin first.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // kunin yung curret info mula sa selected row sa table natin 
        String id       = (String) tableModel.getValueAt(row, 0);
        String oldSchoolId = (String) tableModel.getValueAt(row, 1);
        String oldName  = (String) tableModel.getValueAt(row, 2);

        JTextField nameField = new JTextField(oldName);
        JTextField schoolIdField = new JTextField(oldSchoolId);

        Object[] message = {
            "Full Name:", nameField,
            "School ID:", schoolIdField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Edit Admin Info", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String newName = nameField.getText().trim();
            String newSchoolId = schoolIdField.getText().trim();

            if (!newName.isEmpty() && !newSchoolId.isEmpty()) {
                // kailangan ma-retain yung key kaya ganito procedure hahaha
                String oldKey = DatabaseManager.getAdmins().stream().filter(a -> a.id.equals(id)).map(a -> a.secretKey).findFirst().orElse("none");
                
                // update sa DB, tapos re-render ng table
                boolean ok = DatabaseManager.updateAdmin(id, id, newSchoolId, newName, oldKey);
                if (ok) {
                    loadFromDb();
                    showToast("Admin info updated.");
                } else {
                    JOptionPane.showMessageDialog(this, "Update failed.", "DB Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    /**
     * Resets the 2FA secret key in case an admin loses their phone or deletes the app.
     */
    private void resetTotpSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an admin first.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String id       = (String) tableModel.getValueAt(row, 0);
        String schoolId = (String) tableModel.getValueAt(row, 1);
        String name     = (String) tableModel.getValueAt(row, 2);

        // check if sure talaga sila kase kawawa admin mawawalan access
        int confirm = JOptionPane.showConfirmDialog(this,
            "Regenerate TOTP key for " + name + "?\nThis will invalidate their old Authenticator code.",
            "Reset TOTP", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            TOTPOnboardingDialog totpDlg = new TOTPOnboardingDialog(this, name);
            totpDlg.setVisible(true);

            if (totpDlg.isFinished()) {
                String newKey = totpDlg.getSecretKey();
                boolean ok = DatabaseManager.updateAdmin(id, id, schoolId, name, newKey);
                if (ok) {
                    showToast("TOTP Key has been reset successfully.");
                } else {
                    JOptionPane.showMessageDialog(this, "Update failed.", "DB Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    /**
     * Removes the selected admin from the system.
     */
    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return;

        // safeguard para di ma-lock out everyone. imagine kung nadelete mo sarili mong account 
        if (DatabaseManager.getAdminCount() <= 1) {
            JOptionPane.showMessageDialog(this,
                "Cannot delete the last admin account.",
                "Blocked", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String id   = (String) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 2);

        // naglagay na ako ng bold text para mas obvious sa user
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

    // helper method natin for uniform message prompts, para di puro copy paste.
    private void showToast(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Done",
            JOptionPane.INFORMATION_MESSAGE);
    }
}