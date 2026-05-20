package databaseconnection;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class AppointmentPanel extends JPanel {

    private AppointmentDAO dao    = new AppointmentDAO();
    private PatientDAO     pDao   = new PatientDAO();
    private DefaultTableModel model;
    private JTable table;
    private int selectedId = -1;

    private JComboBox<String> cmbPatient, cmbStatus;
    private JTextField txtDate, txtTime, txtProcedure, txtDentist, txtNotes;
    private String[] patientIds;

    public AppointmentPanel() {
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
        p.setPreferredSize(new Dimension(270, 0));
        p.setBorder(BorderFactory.createTitledBorder("Appointment Form"));

        // Load patients into combo
        List<String[]> pts = pDao.getPatientList();
        patientIds = new String[pts.size()];
        String[] names = new String[pts.size()];
        for (int i = 0; i < pts.size(); i++) {
            patientIds[i] = pts.get(i)[0];
            names[i]      = pts.get(i)[1];
        }
        cmbPatient  = new JComboBox<>(names);
        cmbPatient.setBorder(BorderFactory.createTitledBorder("Patient"));
        cmbStatus   = new JComboBox<>(new String[]{"Scheduled","Completed","Cancelled"});
        cmbStatus.setBorder(BorderFactory.createTitledBorder("Status"));

        txtDate      = tf("Date (YYYY-MM-DD)");
        txtTime      = tf("Time (HH:MM)");
        txtProcedure = tf("Procedure");
        txtDentist   = tf("Dentist Name");
        txtNotes     = tf("Notes");

        // --- UPDATED BUTTON COLORS (Pastel palette with Black text) ---
        JButton btnAdd = btn("Schedule",       new Color(144, 238, 144)); // Light Green
        JButton btnUpd = btn("Update Status",  new Color(173, 216, 230)); // Light Blue
        JButton btnDel = btn("Delete",         new Color(255, 153, 153)); // Light Red
        JButton btnClr = btn("Clear",          new Color(220, 220, 220)); // Light Gray

        btnAdd.addActionListener(e -> addAppointment());
        btnUpd.addActionListener(e -> updateStatus());
        btnDel.addActionListener(e -> deleteAppointment());
        btnClr.addActionListener(e -> clearForm());

        for (Component c : new Component[]{cmbPatient,txtDate,txtTime,txtProcedure,
                txtDentist,txtNotes,cmbStatus,
                Box.createVerticalStrut(8), twoBtn(btnAdd,btnUpd), twoBtn(btnDel,btnClr)}) {
            if (c instanceof JComponent) ((JComponent)c).setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
            p.add(c); p.add(Box.createVerticalStrut(4));
        }
        return p;
    }

    private JPanel buildTable() {
        JPanel p = new JPanel(new BorderLayout(6,6));
        p.setBackground(Color.WHITE);

        // Search Section
        JTextField txtSearch = new JTextField();
        txtSearch.setForeground(Color.BLACK);
        txtSearch.setBorder(BorderFactory.createTitledBorder("Search Appointments"));
        
        // Search button with black text
        JButton btnS = btn("Search", new Color(173, 216, 230)); 
        
        btnS.addActionListener(e -> loadTable(txtSearch.getText()));
        txtSearch.addActionListener(e -> loadTable(txtSearch.getText()));
        JPanel sb = new JPanel(new BorderLayout(4,0));
        sb.setBackground(Color.WHITE);
        sb.add(txtSearch, BorderLayout.CENTER); sb.add(btnS, BorderLayout.EAST);

        String[] cols = {"ID","Patient","Date","Time","Procedure","Dentist","Status","Notes"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c){ return false; }
        };
        table = new JTable(model);
        table.setRowHeight(24);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillForm();
        });

        // Color rows by status
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t,Object v,boolean s,boolean f,int r,int c){
                Component comp = super.getTableCellRendererComponent(t,v,s,f,r,c);
                if (!s) {
                    String st = (String) t.getValueAt(r,6);
                    comp.setBackground("Completed".equals(st) ? new Color(220,255,220)
                        : "Cancelled".equals(st) ? new Color(255,220,220) : Color.WHITE);
                    comp.setForeground(Color.BLACK); // Ensure row text is black
                }
                return comp;
            }
        });

        p.add(sb, BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private void loadTable(String kw) {
        model.setRowCount(0);
        for (Object[] r : dao.searchAppointments(kw)) model.addRow(r);
        selectedId = -1;
    }

    private void fillForm() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        selectedId = (int) model.getValueAt(row,0);
        txtDate.setText(String.valueOf(model.getValueAt(row,2)));
        txtTime.setText(String.valueOf(model.getValueAt(row,3)));
        txtProcedure.setText((String) model.getValueAt(row,4));
        txtDentist.setText((String) model.getValueAt(row,5));
        cmbStatus.setSelectedItem(model.getValueAt(row,6));
        txtNotes.setText((String) model.getValueAt(row,7));
    }

    private void addAppointment() {
        if (cmbPatient.getSelectedIndex() < 0) { warn("Select a patient."); return; }
        if (txtDate.getText().trim().isEmpty() || txtTime.getText().trim().isEmpty()) {
            warn("Date and time are required."); return;
        }
        int pid = Integer.parseInt(patientIds[cmbPatient.getSelectedIndex()]);
        if (dao.addAppointment(pid, txtDate.getText().trim(), txtTime.getText().trim(),
                txtProcedure.getText().trim(), txtDentist.getText().trim(), txtNotes.getText().trim())) {
            info("Appointment scheduled."); clearForm(); loadTable("");
        } else warn("Failed to schedule appointment.");
    }

    private void updateStatus() {
        if (selectedId < 0) { warn("Select an appointment first."); return; }
        if (dao.updateStatus(selectedId, (String)cmbStatus.getSelectedItem())) {
            info("Status updated."); loadTable("");
        } else warn("Update failed.");
    }

    private void deleteAppointment() {
        if (selectedId < 0) { warn("Select an appointment first."); return; }
        int c = JOptionPane.showConfirmDialog(this,"Delete this appointment?","Confirm",JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) {
            if (dao.deleteAppointment(selectedId)) { info("Deleted."); clearForm(); loadTable(""); }
            else warn("Delete failed.");
        }
    }

    private void clearForm() {
        txtDate.setText(""); txtTime.setText(""); txtProcedure.setText("");
        txtDentist.setText(""); txtNotes.setText("");
        cmbPatient.setSelectedIndex(0); cmbStatus.setSelectedIndex(0);
        selectedId = -1; table.clearSelection();
    }

    private JTextField tf(String t) {
        JTextField f = new JTextField(); 
        f.setForeground(Color.BLACK);
        f.setBorder(BorderFactory.createTitledBorder(t)); 
        return f;
    }

    // --- UPDATED HELPER METHOD: Sets button text to BLACK ---
    private JButton btn(String l, Color bg) {
        JButton b = new JButton(l); 
        b.setBackground(bg); 
        b.setForeground(Color.BLACK); // Set text color to BLACK
        b.setFocusPainted(false); 
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return b;
    }

    private JPanel twoBtn(JButton a, JButton b) {
        JPanel p = new JPanel(new GridLayout(1,2,6,0)); p.setBackground(Color.WHITE); p.add(a); p.add(b); return p;
    }
    private void info(String m) { JOptionPane.showMessageDialog(this,m,"Success",JOptionPane.INFORMATION_MESSAGE); }
    private void warn(String m) { JOptionPane.showMessageDialog(this,m,"Notice",JOptionPane.WARNING_MESSAGE); }
}