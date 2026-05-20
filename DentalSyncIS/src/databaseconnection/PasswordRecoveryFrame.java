package databaseconnection;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class PasswordRecoveryFrame extends JFrame {

    private JTextField     txtEmail;
    private JTextField     txtCode;
    private JPasswordField txtNewPass;
    private JPasswordField txtConfirmPass;

    private String  sentCode   = null;
    private boolean codeValid  = false;
    private UserDAO dao        = new UserDAO();

    public PasswordRecoveryFrame() {
        setTitle("DentaSync IS — Password Recovery");
        setSize(480, 380);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        buildUI();
        setVisible(true);
    }

    private void buildUI() {
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        main.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("Password Recovery", SwingConstants.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTitle.setForeground(new Color(25, 135, 84));

        // Step 1: email + send code
        JPanel step1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        step1.setBackground(Color.WHITE);
        txtEmail = new JTextField(22);
        JButton btnSend = new JButton("Send Code");
        styleBtn(btnSend, false);
        btnSend.addActionListener(e -> sendCode());
        step1.add(new JLabel("Email:"));
        step1.add(txtEmail);
        step1.add(btnSend);

        // Step 2: verify code
        JPanel step2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        step2.setBackground(Color.WHITE);
        txtCode = new JTextField(10);
        JButton btnVerify = new JButton("Verify Code");
        styleBtn(btnVerify, false);
        btnVerify.addActionListener(e -> verifyCode());
        step2.add(new JLabel("Code:   "));
        step2.add(txtCode);
        step2.add(btnVerify);

        // Step 3: new password
        JPanel step3 = new JPanel(new GridLayout(3, 1, 0, 8));
        step3.setBackground(Color.WHITE);
        txtNewPass     = new JPasswordField();
        txtConfirmPass = new JPasswordField();
        txtNewPass.setBorder(BorderFactory.createTitledBorder("New Password"));
        txtConfirmPass.setBorder(BorderFactory.createTitledBorder("Confirm Password"));
        JButton btnReset = new JButton("Reset Password");
        styleBtn(btnReset, true);
        btnReset.addActionListener(e -> resetPassword());
        step3.add(txtNewPass);
        step3.add(txtConfirmPass);
        step3.add(btnReset);

        JButton btnBack = new JButton("← Back to Login");
        btnBack.setBorderPainted(false);
        btnBack.setContentAreaFilled(false);
        btnBack.setForeground(new Color(25, 135, 84));
        btnBack.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> { dispose(); new LoginFrame(); });

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(Color.WHITE);
        center.add(Box.createVerticalStrut(8));
        center.add(step1);
        center.add(Box.createVerticalStrut(4));
        center.add(step2);
        center.add(Box.createVerticalStrut(8));
        center.add(step3);

        main.add(lblTitle,  BorderLayout.NORTH);
        main.add(center,    BorderLayout.CENTER);
        main.add(btnBack,   BorderLayout.SOUTH);
        add(main);
    }

    private void styleBtn(JButton b, boolean primary) {
        if (primary) {
            b.setBackground(new Color(25, 135, 84));
            b.setForeground(Color.WHITE);
        }
        b.setFocusPainted(false);
        b.setFont(new Font("SansSerif", Font.PLAIN, 12));
    }

    private void sendCode() {
        String email = txtEmail.getText().trim();
        if (email.isEmpty()) { warn("Enter your email address."); return; }

        if (!dao.emailExists(email)) {
            warn("No account found with that email.");
            return;
        }
        sentCode = String.valueOf(100000 + new Random().nextInt(900000));
        dao.saveResetCode(email, sentCode);

        // In a real system, send via SMTP. Here we display it for demo purposes.
        JOptionPane.showMessageDialog(this,
            "Your reset code is: " + sentCode +
            "\n(In production this would be emailed.)",
            "Code Sent", JOptionPane.INFORMATION_MESSAGE);
    }

    private void verifyCode() {
        String email = txtEmail.getText().trim();
        String code  = txtCode.getText().trim();
        if (email.isEmpty() || code.isEmpty()) { warn("Enter email and code."); return; }

        if (dao.verifyResetCode(email, code)) {
            codeValid = true;
            JOptionPane.showMessageDialog(this, "Code verified! Enter your new password.");
        } else {
            warn("Invalid or expired code.");
        }
    }

    private void resetPassword() {
        if (!codeValid) { warn("Please verify your code first."); return; }

        String np  = new String(txtNewPass.getPassword());
        String cnf = new String(txtConfirmPass.getPassword());

        if (np.length() < 6) { warn("Password must be at least 6 characters."); return; }
        if (!np.equals(cnf))  { warn("Passwords do not match."); return; }

        String email = txtEmail.getText().trim();
        if (dao.resetPassword(email, np)) {
            JOptionPane.showMessageDialog(this,
                "Password reset successfully!", "Success",
                JOptionPane.INFORMATION_MESSAGE);
            dispose();
            new LoginFrame();
        } else {
            warn("Failed to reset password. Try again.");
        }
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Notice",
            JOptionPane.WARNING_MESSAGE);
    }
}