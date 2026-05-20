package databaseconnection;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private User currentUser;

    public MainFrame(User user) {
        this.currentUser = user;
        setTitle("DentaSync Dental Clinic Management System | Welcome, " + user.getFullName());
        setSize(1200, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        buildUI();
        setVisible(true);
    }

    private void buildUI() {

        // ── 1. HEADER ─────────────────────────────────────────────────
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setBackground(new Color(25, 135, 84));
        northPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel lblTitle = new JLabel("DentaSync Dental Clinic");
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        northPanel.add(lblTitle, BorderLayout.WEST);

        JLabel lblWelcome = new JLabel("Logged in as: " + currentUser.getFullName()
                + "  |  Role: " + currentUser.getRole());
        lblWelcome.setForeground(Color.WHITE);
        lblWelcome.setFont(new Font("SansSerif", Font.PLAIN, 12));
        northPanel.add(lblWelcome, BorderLayout.CENTER);

        // Show Manage Users only for admin
        if ("admin".equalsIgnoreCase(currentUser.getRole())) {
            JButton btnManageUsers = new JButton("Manage Users");
            btnManageUsers.setBackground(Color.WHITE);
            btnManageUsers.setForeground(new Color(25, 135, 84));
            btnManageUsers.setFont(new Font("SansSerif", Font.BOLD, 12));
            btnManageUsers.setFocusPainted(false);
            btnManageUsers.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnManageUsers.addActionListener(e -> {
                UserManagementFrame adminFrame = new UserManagementFrame(currentUser);
                adminFrame.setVisible(true);
            });
            northPanel.add(btnManageUsers, BorderLayout.EAST);
        }

        // ── 2. TABS ───────────────────────────────────────────────────
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.BOLD, 13));
        tabs.setBackground(Color.WHITE);

        tabs.addTab("Dashboard",    buildDashboard());
        tabs.addTab("Patients",     new PatientPanel());
        tabs.addTab("Appointments", new AppointmentPanel());
        tabs.addTab("Payments",     new PaymentPanel());
        tabs.addTab("Reports",      new ReportPanel(currentUser));

        // ── 3. FOOTER ─────────────────────────────────────────────────
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        southPanel.setBackground(new Color(240, 240, 240));

        JButton btnLogout = new JButton("Logout");
        btnLogout.setBackground(new Color(220, 53, 69));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                new LoginFrame();
            }
        });

        southPanel.add(btnLogout);

        // ── 4. ASSEMBLE ───────────────────────────────────────────────
        add(northPanel, BorderLayout.NORTH);
        add(tabs,       BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);
    }

    // ── DASHBOARD TAB ─────────────────────────────────────────────────
    private JPanel buildDashboard() {
        JPanel p = new JPanel(new BorderLayout(10, 20));
        p.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        p.setBackground(new Color(244, 246, 249));

        JLabel lbl = new JLabel("Welcome, " + currentUser.getFullName() + "!",
                SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 22));
        lbl.setForeground(new Color(25, 135, 84));

        // Stats cards
        JPanel cards = new JPanel(new GridLayout(1, 3, 20, 0));
        cards.setOpaque(false);

        PatientDAO     pd = new PatientDAO();
        AppointmentDAO ad = new AppointmentDAO();
        PaymentDAO     py = new PaymentDAO();

        int patients     = pd.getAllPatients().size();
        int appointments = ad.getAllAppointments().size();
        double revenue   = py.getAllPayments().stream()
                .filter(r -> "Paid".equals(r[6]))
                .mapToDouble(r -> (Double) r[3])
                .sum();

        cards.add(statCard("Total Patients",
                String.valueOf(patients),       new Color(25, 135, 84)));
        cards.add(statCard("Total Appointments",
                String.valueOf(appointments),   new Color(13, 110, 253)));
        cards.add(statCard("Total Revenue",
                "P" + String.format("%,.2f", revenue), new Color(255, 153, 0)));

        JLabel sub = new JLabel(
                "Use the tabs above to manage patients, appointments, payments and reports.",
                SwingConstants.CENTER);
        sub.setForeground(Color.GRAY);
        sub.setFont(new Font("SansSerif", Font.PLAIN, 12));

        p.add(lbl,   BorderLayout.NORTH);
        p.add(cards, BorderLayout.CENTER);
        p.add(sub,   BorderLayout.SOUTH);
        return p;
    }

    private JPanel statCard(String label, String value, Color color) {
        JPanel card = new JPanel(new GridLayout(2, 1));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2, true),
                BorderFactory.createEmptyBorder(20, 10, 20, 10)
        ));

        JLabel lv = new JLabel(value, SwingConstants.CENTER);
        lv.setFont(new Font("SansSerif", Font.BOLD, 32));
        lv.setForeground(color);

        JLabel ll = new JLabel(label, SwingConstants.CENTER);
        ll.setFont(new Font("SansSerif", Font.PLAIN, 13));
        ll.setForeground(Color.DARK_GRAY);

        card.add(lv);
        card.add(ll);
        return card;
    }
}