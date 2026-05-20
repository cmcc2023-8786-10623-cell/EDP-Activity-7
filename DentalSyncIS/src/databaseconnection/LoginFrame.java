package databaseconnection;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private UserDAO dao = new UserDAO();

    public LoginFrame() {

        setTitle("DentaSync IS — Login");

        setSize(420, 320);

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setLocationRelativeTo(null);

        setResizable(false);

        buildUI();

        setVisible(true);
    }

    private void buildUI() {

        JPanel main = new JPanel(
                new BorderLayout(10, 10)
        );

        main.setBorder(
                BorderFactory.createEmptyBorder(
                        20,
                        30,
                        20,
                        30
                )
        );

        main.setBackground(Color.WHITE);

        // Header
        JLabel lblTitle = new JLabel(
                "DentaSync IS",
                SwingConstants.CENTER
        );

        lblTitle.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        24
                )
        );

        lblTitle.setForeground(
                new Color(25, 135, 84)
        );

        JLabel lblSub = new JLabel(
                "Dental Clinic Information System",
                SwingConstants.CENTER
        );

        lblSub.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        12
                )
        );

        lblSub.setForeground(Color.DARK_GRAY);

        JPanel header = new JPanel(
                new GridLayout(2, 1, 0, 4)
        );

        header.setBackground(Color.WHITE);

        header.add(lblTitle);

        header.add(lblSub);

        // Form
        JPanel form = new JPanel(
                new GridLayout(4, 1, 0, 10)
        );

        form.setBackground(Color.WHITE);

        // Username field
        txtUsername = new JTextField();

        txtUsername.setBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(
                                new Color(25, 135, 84),
                                1
                        ),
                        "Username",
                        0,
                        0,
                        new Font(
                                "SansSerif",
                                Font.PLAIN,
                                11
                        ),
                        new Color(25, 135, 84)
                )
        );

        txtUsername.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        14
                )
        );

        txtUsername.setForeground(Color.BLACK);

        txtUsername.setBackground(Color.WHITE);

        // Password field
        txtPassword = new JPasswordField();

        txtPassword.setBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(
                                new Color(25, 135, 84),
                                1
                        ),
                        "Password",
                        0,
                        0,
                        new Font(
                                "SansSerif",
                                Font.PLAIN,
                                11
                        ),
                        new Color(25, 135, 84)
                )
        );

        txtPassword.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        14
                )
        );

        txtPassword.setForeground(Color.BLACK);

        txtPassword.setBackground(Color.WHITE);

        // Login button
        JButton btnLogin = new JButton("LOGIN");

        btnLogin.setBackground(
                new Color(25, 135, 84)
        );

        // CHANGED TEXT COLOR TO BLACK
        btnLogin.setForeground(Color.BLACK);

        btnLogin.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        14
                )
        );

        btnLogin.setFocusPainted(false);

        btnLogin.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        btnLogin.setBorder(
                BorderFactory.createEmptyBorder(
                        10,
                        0,
                        10,
                        0
                )
        );

        btnLogin.addActionListener(
                e -> doLogin()
        );

        // Forgot password button
        JButton btnRecover =
                new JButton("Forgot Password?");

        btnRecover.setBorderPainted(false);

        btnRecover.setContentAreaFilled(false);

        btnRecover.setForeground(
                new Color(25, 135, 84)
        );

        btnRecover.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        12
                )
        );

        btnRecover.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        btnRecover.addActionListener(e -> {

            dispose();

            new PasswordRecoveryFrame();
        });

        form.add(txtUsername);

        form.add(txtPassword);

        form.add(btnLogin);

        form.add(btnRecover);

        main.add(header, BorderLayout.NORTH);

        main.add(form, BorderLayout.CENTER);

        add(main);

        getRootPane().setDefaultButton(btnLogin);
    }

    private void doLogin() {

        String username =
                txtUsername.getText().trim();

        String password =
                new String(
                        txtPassword.getPassword()
                );

        if (username.isEmpty()
                || password.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please enter both username and password.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        User user = dao.login(
                username,
                password
        );

        if (user != null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Welcome, "
                    + user.getFullName()
                    + "!",
                    "Login Successful",
                    JOptionPane.INFORMATION_MESSAGE
            );

            dispose();

            new MainFrame(user);

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "Invalid credentials or account is inactive.",
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}