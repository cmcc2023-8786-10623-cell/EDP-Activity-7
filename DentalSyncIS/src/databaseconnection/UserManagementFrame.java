package databaseconnection;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class UserManagementFrame extends JFrame {

    private User              currentUser;
    private UserDAO           dao = new UserDAO();
    private JTable            table;
    private DefaultTableModel model;

    private JTextField        txtUsername;
    private JPasswordField    txtPassword;
    private JTextField        txtFullName;
    private JTextField        txtEmail;
    private JComboBox<String> cmbRole;
    private JTextField        txtSearch;

    private int selectedUserId = -1;

    public UserManagementFrame(User currentUser) {
        this.currentUser = currentUser;
        setTitle("DentaSync IS — User Management  |  Logged in: " + currentUser.getFullName());
        setSize(950, 600);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        buildUI();
        loadTable("");
        setVisible(true);
    }

    private void buildUI() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildFormPanel(), buildTablePanel());
        split.setDividerLocation(300);
        split.setResizeWeight(0.0);
        add(split, BorderLayout.CENTER);
        add(buildToolbar(), BorderLayout.NORTH);
    }

    // ── TOOLBAR ───────────────────────────────────────────────────────
    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        bar.setBackground(new Color(25, 135, 84));

        JLabel lbl = new JLabel("DentaSync — User Management");
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 14));

        JButton btnClose = new JButton("Close");
        btnClose.setForeground(Color.WHITE);
        btnClose.setBackground(new Color(220, 53, 69));
        btnClose.setFocusPainted(false);
        btnClose.addActionListener(e -> dispose());

        bar.add(lbl);
        bar.add(Box.createHorizontalStrut(400));
        bar.add(btnClose);
        return bar;
    }

    // ── FORM PANEL ────────────────────────────────────────────────────
    private JPanel buildFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 8));
        panel.setBackground(Color.WHITE);

        txtUsername = styledField("Username");
        txtPassword = new JPasswordField();
        txtPassword.setBorder(BorderFactory.createTitledBorder("Password"));
        txtFullName = styledField("Full Name");
        txtEmail    = styledField("Email");
        cmbRole     = new JComboBox<>(new String[]{"admin", "staff"});
        cmbRole.setBorder(BorderFactory.createTitledBorder("Role"));

        JButton btnAdd    = actionBtn("Add Account",    new Color(25, 135, 84));
        JButton btnUpdate = actionBtn("Update Profile", new Color(13, 110, 253));
        JButton btnActive = actionBtn("Activate",       new Color(40, 167, 69));
        JButton btnInact  = actionBtn("Deactivate",     new Color(220, 53, 69));
        JButton btnClear  = actionBtn("Clear Form",     Color.GRAY);

        btnAdd.addActionListener(e    -> addAccount());
        btnUpdate.addActionListener(e -> updateAccount());
        btnActive.addActionListener(e -> setStatus("active"));
        btnInact.addActionListener(e  -> setStatus("inactive"));
        btnClear.addActionListener(e  -> clearForm());

        JPanel btnRow1 = row(btnAdd,    btnUpdate);
        JPanel btnRow2 = row(btnActive, btnInact);

        for (Component c : new Component[]{
                txtUsername, txtPassword, txtFullName, txtEmail, cmbRole,
                Box.createVerticalStrut(10), btnRow1, btnRow2,
                Box.createVerticalStrut(4), btnClear
        }) {
            if (c instanceof JComponent)
                ((JComponent) c).setMaximumSize(
                    new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height + 10));
            panel.add(c);
            panel.add(Box.createVerticalStrut(4));
        }
        return panel;
    }

    // ── TABLE PANEL ───────────────────────────────────────────────────
    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 4, 8, 8));
        panel.setBackground(Color.WHITE);

        JPanel searchBar = new JPanel(new BorderLayout(6, 0));
        searchBar.setBackground(Color.WHITE);
        txtSearch = new JTextField();
        txtSearch.setBorder(BorderFactory.createTitledBorder("Search (name / username / email)"));
        JButton btnSearch = actionBtn("Search", new Color(25, 135, 84));
        btnSearch.addActionListener(e -> loadTable(txtSearch.getText()));
        txtSearch.addActionListener(e -> loadTable(txtSearch.getText()));
        searchBar.add(txtSearch,  BorderLayout.CENTER);
        searchBar.add(btnSearch,  BorderLayout.EAST);

        String[] cols = {"ID", "Username", "Full Name", "Email", "Role", "Status"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) populateFormFromTable();
        });

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (!sel) {
                    String status = (String) t.getValueAt(r, 5);
                    comp.setBackground("inactive".equals(status) ?
                        new Color(255, 220, 220) : Color.WHITE);
                    comp.setForeground(Color.BLACK);
                }
                return comp;
            }
        });

        panel.add(searchBar,           BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // ── ACTIONS ───────────────────────────────────────────────────────
    private void addAccount() {
        String username = txtUsername.getText().trim();
        String pass     = new String(txtPassword.getPassword());
        String fullName = txtFullName.getText().trim();
        String email    = txtEmail.getText().trim();
        String role     = (String) cmbRole.getSelectedItem();

        if (username.isEmpty() || pass.isEmpty() || fullName.isEmpty() || email.isEmpty()) {
            warn("Fill in all fields to add an account."); return;
        }
        if (pass.length() < 6) { warn("Password must be at least 6 characters."); return; }

        User u = new User();
        u.setUsername(username); u.setFullName(fullName);
        u.setEmail(email);       u.setRole(role);
        u.setStatus("active");

        if (dao.addUser(u, pass)) {
            info("Account added successfully."); clearForm(); loadTable("");
        } else {
            warn("Failed to add account. Username or email may already exist.");
        }
    }

    private void updateAccount() {
        if (selectedUserId < 0) { warn("Select a user from the table first."); return; }
        User u = new User();
        u.setUserId(selectedUserId);
        u.setFullName(txtFullName.getText().trim());
        u.setEmail(txtEmail.getText().trim());
        u.setRole((String) cmbRole.getSelectedItem());

        if (u.getFullName().isEmpty() || u.getEmail().isEmpty()) {
            warn("Full name and email are required."); return;
        }
        if (dao.updateProfile(u)) {
            info("Profile updated."); loadTable("");
        } else warn("Update failed.");
    }

    private void setStatus(String status) {
        if (selectedUserId < 0) { warn("Select a user first."); return; }
        if (selectedUserId == currentUser.getUserId()) {
            warn("You cannot change your own account status."); return;
        }
        String label = "active".equals(status) ? "activate" : "deactivate";
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to " + label + " this account?",
            "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (dao.setStatus(selectedUserId, status)) {
                info("Account " + label + "d."); loadTable("");
            } else warn("Status update failed.");
        }
    }

    // ── HELPERS ───────────────────────────────────────────────────────
    private void loadTable(String keyword) {
        model.setRowCount(0);
        for (User u : dao.searchUsers(keyword)) {
            model.addRow(new Object[]{
                u.getUserId(), u.getUsername(), u.getFullName(),
                u.getEmail(), u.getRole(), u.getStatus()
            });
        }
        selectedUserId = -1;
    }

    private void populateFormFromTable() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        selectedUserId = (int) model.getValueAt(row, 0);
        txtUsername.setText((String) model.getValueAt(row, 1));
        txtUsername.setEditable(false);
        txtFullName.setText((String) model.getValueAt(row, 2));
        txtEmail.setText((String) model.getValueAt(row, 3));
        cmbRole.setSelectedItem(model.getValueAt(row, 4));
        txtPassword.setText("");
    }

    private void clearForm() {
        txtUsername.setText(""); txtUsername.setEditable(true);
        txtPassword.setText(""); txtFullName.setText(""); txtEmail.setText("");
        cmbRole.setSelectedIndex(1); selectedUserId = -1; table.clearSelection();
    }

    private JTextField styledField(String title) {
        JTextField tf = new JTextField();
        tf.setBorder(BorderFactory.createTitledBorder(title));
        tf.setForeground(Color.BLACK);
        tf.setBackground(Color.WHITE);
        return tf;
    }

    private JButton actionBtn(String label, Color bg) {
        JButton b = new JButton(label);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setFocusPainted(false);
        return b;
    }

    private JPanel row(JButton a, JButton b) {
        JPanel p = new JPanel(new GridLayout(1, 2, 6, 0));
        p.setBackground(Color.WHITE);
        p.add(a); p.add(b);
        return p;
    }

    private void info(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Notice",  JOptionPane.WARNING_MESSAGE);
    }
}