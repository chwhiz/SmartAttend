package ui;

import logic.DatabaseManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SectionManagerDialog extends JDialog {
    private JList<String> list;
    private DefaultListModel<String> listModel;

    public SectionManagerDialog(Frame parent) {
        super(parent, "Manage Sections", true);
        setSize(400, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(245, 243, 240));

        JPanel pnlList = new JPanel(new BorderLayout());
        pnlList.setBorder(new EmptyBorder(10, 10, 10, 10));
        pnlList.setBackground(new Color(245, 243, 240));
        
        listModel = new DefaultListModel<>();
        for(String s : DatabaseManager.sectionList) {
            listModel.addElement(s);
        }
        
        list = new JList<>(listModel);
        list.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 215, 210)));
        pnlList.add(scroll, BorderLayout.CENTER);
        
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pnlButtons.setBackground(new Color(245, 243, 240));
        
        JButton btnAdd = UIBuilder.createToolbarButton("Add Section", new Color(39, 174, 96), Color.WHITE);
        JButton btnDelete = UIBuilder.createToolbarButton("Delete Section", new Color(192, 57, 43), Color.WHITE);
        JButton btnClose = UIBuilder.createToolbarButton("Close", new Color(150, 150, 150), Color.WHITE);
        
        btnAdd.addActionListener(e -> {
            String newSec = JOptionPane.showInputDialog(this, "Enter new section name:");
            if (newSec != null && !newSec.trim().isEmpty()) {
                newSec = newSec.trim();
                // Avoid using reserved regex chars unless intended, keep it simple
                if (DatabaseManager.insertSection(newSec)) {
                    listModel.clear();
                    for(String s : DatabaseManager.sectionList) {
                        listModel.addElement(s);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add section. It might already exist.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        btnDelete.addActionListener(e -> {
            String sel = list.getSelectedValue();
            if (sel != null) {
                int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete section: " + sel + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (DatabaseManager.deleteSection(sel)) {
                        listModel.removeElement(sel);
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to delete section.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a section to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        btnClose.addActionListener(e -> dispose());
        
        pnlButtons.add(btnAdd);
        pnlButtons.add(btnDelete);
        pnlButtons.add(btnClose);
        
        add(pnlList, BorderLayout.CENTER);
        add(pnlButtons, BorderLayout.SOUTH);
    }
}
