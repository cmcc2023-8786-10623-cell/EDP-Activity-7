package databaseconnection;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class PaymentPanel extends JPanel {

    private PaymentDAO     dao  = new PaymentDAO();
    private PatientDAO     pDao = new PatientDAO();
    private DefaultTableModel model;
    private JTable table;
    private int selectedId = -1;

    private JComboBox<String> cmbPatient, cmbMethod, cmbStatus;
    private JTextField txtAmount, txtDate, txtProcedure;
    private String[] patientIds;

    public PaymentPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Color.WHITE);
        add(buildForm(),  BorderLayout.WEST);
        add(buildTable(), BorderLayout.CENTER);
        loadTable("");
    }

    private JPanel buildForm() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setPreferredSize(new Dimension(260, 0));
        p.setBorder(BorderFactory.createTitledBorder("Payment Form"));

        List<String[]> pts = pDao.getPatientList();
        patientIds = new String[pts.size()];
        String[] names = new String[pts.size()];
        for (int i = 0; i < pts.size(); i++) {
            patientIds[i] = pts.get(i)[0];
            names[i]      = pts.get(i)[1];
        }

        cmbPatient = new JComboBox<>(names);
        cmbPatient.setBorder(BorderFactory.createTitledBorder("Patient"));
        cmbMethod  = new JComboBox<>(new String[]{"Cash", "GCash", "Card", "Insurance"});
        cmbMethod.setBorder(BorderFactory.createTitledBorder("Payment Method"));
        cmbStatus  = new JComboBox<>(new String[]{"Paid", "Pending", "Cancelled"});
        cmbStatus.setBorder(BorderFactory.createTitledBorder("Status"));

        txtAmount    = tf("Amount (P)");
        txtDate      = tf("Payment Date (YYYY-MM-DD)");
        txtProcedure = tf("Procedure / Service");

        JButton btnAdd = btn("Record Payment", new Color(25, 135, 84));
        JButton btnUpd = btn("Update Status",  new Color(13, 110, 253));
        JButton btnClr = btn("Clear",          Color.GRAY);

        btnAdd.addActionListener(e -> addPayment());
        btnUpd.addActionListener(e -> updateStatus());
        btnClr.addActionListener(e -> clearForm());

        for (Component c : new Component[]{
                cmbPatient, txtProcedure, txtAmount, txtDate,
                cmbMethod, cmbStatus,
                Box.createVerticalStrut(8),
                twoBtn(btnAdd, btnUpd), btnClr
        }) {
            if (c instanceof JComponent)
                ((JComponent) c).setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
            p.add(c);
            p.add(Box.createVerticalStrut(4));
        }
        return p;
    }

    private JPanel buildTable() {
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setBackground(Color.WHITE);

        JTextField txtSearch = new JTextField();
        txtSearch.setBorder(BorderFactory.createTitledBorder("Search Payments"));
        JButton btnS = btn("Search", new Color(25, 135, 84));
        btnS.addActionListener(e -> loadTable(txtSearch.getText()));
        txtSearch.addActionListener(e -> loadTable(txtSearch.getText()));

        JPanel sb = new JPanel(new BorderLayout(4, 0));
        sb.setBackground(Color.WHITE);
        sb.add(txtSearch, BorderLayout.CENTER);
        sb.add(btnS,      BorderLayout.EAST);

        String[] cols = {"ID", "Patient", "Procedure", "Amount (P)", "Date", "Method", "Status"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(24);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillForm();
        });

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean s, boolean f, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, s, f, r, c);
                if (!s) {
                    String st = (String) t.getValueAt(r, 6);
                    comp.setBackground(
                        "Paid".equals(st)     ? new Color(220, 255, 220) :
                        "Pending".equals(st)  ? new Color(255, 255, 200) :
                                                new Color(255, 220, 220));
                    comp.setForeground(Color.BLACK);
                }
                return comp;
            }
        });

        p.add(sb,                      BorderLayout.NORTH);
        p.add(new JScrollPane(table),  BorderLayout.CENTER);
        return p;
    }

    private void loadTable(String kw) {
        model.setRowCount(0);
        for (Object[] r : dao.searchPayments(kw)) model.addRow(r);
        selectedId = -1;
    }

    private void fillForm() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        selectedId = (int) model.getValueAt(row, 0);
        txtProcedure.setText((String) model.getValueAt(row, 2));
        txtAmount.setText(String.valueOf(model.getValueAt(row, 3)));
        txtDate.setText(String.valueOf(model.getValueAt(row, 4)));
        cmbMethod.setSelectedItem(model.getValueAt(row, 5));
        cmbStatus.setSelectedItem(model.getValueAt(row, 6));
    }

    private void addPayment() {
        if (cmbPatient.getSelectedIndex() < 0) { warn("Select a patient."); return; }
        if (txtAmount.getText().trim().isEmpty() || txtDate.getText().trim().isEmpty()) {
            warn("Amount and date are required."); return;
        }
        try {
            int    pid = Integer.parseInt(patientIds[cmbPatient.getSelectedIndex()]);
            double amt = Double.parseDouble(txtAmount.getText().trim());
            if (dao.addPayment(pid, null, amt, txtDate.getText().trim(),
                    (String) cmbMethod.getSelectedItem(),
                    txtProcedure.getText().trim(),
                    (String) cmbStatus.getSelectedItem())) {
                info("Payment recorded."); clearForm(); loadTable("");
            } else warn("Failed to record payment.");
        } catch (NumberFormatException ex) {
            warn("Amount must be a valid number.");
        }
    }

    private void updateStatus() {
        if (selectedId < 0) { warn("Select a payment first."); return; }
        if (dao.updatePaymentStatus(selectedId, (String) cmbStatus.getSelectedItem())) {
            info("Status updated."); loadTable("");
        } else warn("Update failed.");
    }

    private void clearForm() {
        txtAmount.setText(""); txtDate.setText(""); txtProcedure.setText("");
        cmbPatient.setSelectedIndex(0);
        cmbMethod.setSelectedIndex(0);
        cmbStatus.setSelectedIndex(0);
        selectedId = -1;
        table.clearSelection();
    }

    private JTextField tf(String title) {
        JTextField f = new JTextField();
        f.setBorder(BorderFactory.createTitledBorder(title));
        f.setForeground(Color.BLACK);
        f.setBackground(Color.WHITE);
        return f;
    }

    private JButton btn(String label, Color bg) {
        JButton b = new JButton(label);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setFocusPainted(false);
        return b;
    }

    private JPanel twoBtn(JButton a, JButton b) {
        JPanel p = new JPanel(new GridLayout(1, 2, 6, 0));
        p.setBackground(Color.WHITE);
        p.add(a); p.add(b);
        return p;
    }

    private void info(String m) {
        JOptionPane.showMessageDialog(this, m, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    private void warn(String m) {
        JOptionPane.showMessageDialog(this, m, "Notice", JOptionPane.WARNING_MESSAGE);
    }
}