package databaseconnection;

import javax.swing.*;
import javax.swing.table.*;
import java.io.FileOutputStream;
import java.util.List;
// Reduced wildcard imports to prevent naming conflicts
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class PatientPanel extends JPanel {

    private PatientDAO dao = new PatientDAO();
    private DefaultTableModel model;
    private JTable table;
    private int selectedId = -1;

    private JTextField txtName, txtBirth, txtContact, txtAddress, txtEmail;
    private JComboBox<String> cmbGender;

    public PatientPanel() {
        setLayout(new java.awt.BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(java.awt.Color.WHITE);
        add(buildForm(),  java.awt.BorderLayout.WEST);
        add(buildTable(), java.awt.BorderLayout.CENTER);
        loadTable("");
    }

    private JPanel buildForm() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(java.awt.Color.WHITE);
        p.setPreferredSize(new java.awt.Dimension(260, 0));
        p.setBorder(BorderFactory.createTitledBorder("Patient Form"));

        txtName    = tf("Full Name");
        txtBirth   = tf("Birthdate (YYYY-MM-DD)"); 
        cmbGender  = new JComboBox<>(new String[]{"Male","Female","Other"});
        cmbGender.setBorder(BorderFactory.createTitledBorder("Gender"));
        txtContact = tf("Contact No.");
        txtAddress = tf("Address");
        txtEmail   = tf("Email");

        JButton btnAdd  = btn("Add Patient",    new java.awt.Color(144, 238, 144)); 
        JButton btnUpd  = btn("Update",         new java.awt.Color(255, 235, 153)); 
        JButton btnDel  = btn("Delete",         new java.awt.Color(255, 153, 153)); 
        JButton btnClr  = btn("Clear",          new java.awt.Color(220, 220, 220)); 
        
        JButton btnExp  = btn("Export Excel",   new java.awt.Color(173, 255, 47)); 

        btnAdd.addActionListener(e -> addPatient());
        btnUpd.addActionListener(e -> updatePatient());
        btnDel.addActionListener(e -> deletePatient());
        btnClr.addActionListener(e -> clearForm());
        btnExp.addActionListener(e -> exportToExcel());

        JPanel row1 = twoBtn(btnAdd, btnUpd);
        JPanel row2 = twoBtn(btnDel, btnClr);

        for (java.awt.Component c : new java.awt.Component[]{txtName,txtBirth,cmbGender,txtContact,txtAddress,txtEmail,
                Box.createVerticalStrut(8), row1, row2, Box.createVerticalStrut(4), btnExp}) {
            if (c instanceof JComponent) ((JComponent)c).setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 50));
            p.add(c); p.add(Box.createVerticalStrut(4));
        }
        return p;
    }

    private JPanel buildTable() {
        JPanel p = new JPanel(new java.awt.BorderLayout(6,6));
        p.setBackground(java.awt.Color.WHITE);

        JTextField txtSearch = new JTextField();
        txtSearch.setForeground(java.awt.Color.BLACK);
        txtSearch.setBorder(BorderFactory.createTitledBorder("Search Patients"));
        
        JButton btnS = btn("Search", new java.awt.Color(173, 216, 230));
        
        btnS.addActionListener(e -> loadTable(txtSearch.getText()));
        txtSearch.addActionListener(e -> loadTable(txtSearch.getText()));
        JPanel sb = new JPanel(new java.awt.BorderLayout(4,0));
        sb.setBackground(java.awt.Color.WHITE);
        sb.add(txtSearch, java.awt.BorderLayout.CENTER);
        sb.add(btnS, java.awt.BorderLayout.EAST);

        String[] cols = {"ID","Full Name","Birthdate","Gender","Contact","Address","Email","Status"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c){ return false; }
        };
        table = new JTable(model);
        table.setRowHeight(24);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillForm();
        });

        p.add(sb, java.awt.BorderLayout.NORTH);
        p.add(new JScrollPane(table), java.awt.BorderLayout.CENTER);
        return p;
    }

    private void loadTable(String kw) {
        model.setRowCount(0);
        for (Object[] r : dao.searchPatients(kw)) model.addRow(r);
        selectedId = -1;
    }

    private void fillForm() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        selectedId = (int) model.getValueAt(row, 0);
        txtName.setText((String) model.getValueAt(row, 1));
        txtBirth.setText(String.valueOf(model.getValueAt(row, 2)));
        cmbGender.setSelectedItem(model.getValueAt(row, 3));
        txtContact.setText((String) model.getValueAt(row, 4));
        txtAddress.setText(String.valueOf(model.getValueAt(row, 5) == null ? "" : model.getValueAt(row, 5)));
        txtEmail.setText((String) model.getValueAt(row, 6));
    }

    private void addPatient() {
        if (txtName.getText().trim().isEmpty()) { warn("Full name is required."); return; }
        if (dao.addPatient(txtName.getText().trim(), txtBirth.getText().trim(),
                (String)cmbGender.getSelectedItem(), txtContact.getText().trim(),
                txtAddress.getText().trim(), txtEmail.getText().trim())) {
            info("Patient added."); clearForm(); loadTable("");
        } else warn("Failed to add patient. Check if date is YYYY-MM-DD.");
    }

    private void updatePatient() {
        if (selectedId < 0) { warn("Select a patient first."); return; }
        if (dao.updatePatient(selectedId, txtName.getText().trim(), txtBirth.getText().trim(),
                (String)cmbGender.getSelectedItem(), txtContact.getText().trim(),
                txtAddress.getText().trim(), txtEmail.getText().trim())) {
            info("Patient updated."); clearForm(); loadTable("");
        } else warn("Update failed. Check date format.");
    }

    private void deletePatient() {
        if (selectedId < 0) { warn("Select a patient first."); return; }
        int c = JOptionPane.showConfirmDialog(this, "Delete this patient?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) {
            if (dao.deletePatient(selectedId)) { info("Deleted."); clearForm(); loadTable(""); }
            else warn("Delete failed. Patient may have linked records.");
        }
    }

    private void exportToExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Excel File");
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = fileChooser.getSelectedFile().getAbsolutePath();
            if (!path.endsWith(".xlsx")) path += ".xlsx";

            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Patients");
                Row header = sheet.createRow(0);
                for (int i = 0; i < table.getColumnCount(); i++) {
                    header.createCell(i).setCellValue(table.getColumnName(i));
                }

                for (int i = 0; i < table.getRowCount(); i++) {
                    Row row = sheet.createRow(i + 1);
                    for (int j = 0; j < table.getColumnCount(); j++) {
                        Object val = table.getValueAt(i, j);
                        row.createCell(j).setCellValue(val == null ? "" : val.toString());
                    }
                }

                try (FileOutputStream out = new FileOutputStream(path)) {
                    workbook.write(out);
                    info("Excel exported successfully!");
                }
            } catch (Exception e) {
                e.printStackTrace();
                warn("Export failed: " + e.getMessage());
            }
        }
    }

    private void clearForm() {
        txtName.setText(""); txtBirth.setText(""); txtContact.setText("");
        txtAddress.setText(""); txtEmail.setText("");
        cmbGender.setSelectedIndex(0); selectedId = -1; table.clearSelection();
    }

    private JTextField tf(String title) {
        JTextField t = new JTextField();
        t.setForeground(java.awt.Color.BLACK);
        t.setBorder(BorderFactory.createTitledBorder(title));
        return t;
    }

    private JButton btn(String lbl, java.awt.Color bg) {
        JButton b = new JButton(lbl);
        b.setBackground(bg);
        b.setForeground(java.awt.Color.BLACK);
        b.setFocusPainted(false);
        b.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(java.awt.Color.GRAY),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return b;
    }

    private JPanel twoBtn(JButton a, JButton b) {
        JPanel p = new JPanel(new java.awt.GridLayout(1,2,6,0));
        p.setBackground(java.awt.Color.WHITE); p.add(a); p.add(b); return p;
    }
    private void info(String m) { JOptionPane.showMessageDialog(this, m, "Success", JOptionPane.INFORMATION_MESSAGE); }
    private void warn(String m) { JOptionPane.showMessageDialog(this, m, "Notice",  JOptionPane.WARNING_MESSAGE); }
}