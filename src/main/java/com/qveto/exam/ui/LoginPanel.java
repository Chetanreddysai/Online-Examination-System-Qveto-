package com.qveto.exam.ui;

import com.qveto.exam.model.Role;
import com.qveto.exam.model.User;
import com.qveto.exam.service.AuthService;
import com.qveto.exam.util.AppConstants;
import com.qveto.exam.util.DBConnection;
import com.qveto.exam.validation.InputValidator;
import com.qveto.exam.validation.ValidationResult;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Authentication screen featuring role-based routing, input validation,
 * quick demo credential buttons, and persistent connection diagnostic.
 */
public class LoginPanel extends JPanel {

    private final MainFrame mainFrame;
    private final AuthService authService;

    private JTextField emailField;
    private JPasswordField passwordField;
    private JLabel statusLabel;
    private JLabel dbStatusBadge;

    public LoginPanel(MainFrame mainFrame, AuthService authService) {
        this.mainFrame = mainFrame;
        this.authService = authService;
        initUI();
    }

    private void initUI() {
        setLayout(new GridBagLayout());
        setBackground(AppConstants.COLOR_BACKGROUND);

        // Center Login Card
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(AppConstants.COLOR_CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppConstants.COLOR_BORDER, 1, true),
                new EmptyBorder(32, 40, 32, 40)
        ));
        card.setPreferredSize(new Dimension(460, 560));
        card.setMaximumSize(new Dimension(460, 560));

        // Header Title
        JLabel titleLabel = new JLabel("Online Examination Portal");
        titleLabel.setFont(AppConstants.FONT_TITLE);
        titleLabel.setForeground(AppConstants.COLOR_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Qveto Assessment & Certification Platform");
        subtitleLabel.setFont(AppConstants.FONT_SMALL);
        subtitleLabel.setForeground(AppConstants.COLOR_TEXT_MUTED);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Status / Message Banner
        statusLabel = new JLabel(" ");
        statusLabel.setFont(AppConstants.FONT_BODY);
        statusLabel.setForeground(AppConstants.COLOR_DANGER);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Email Field
        JLabel emailLabel = new JLabel("Email Address");
        emailLabel.setFont(AppConstants.FONT_BODY_BOLD);
        emailLabel.setForeground(AppConstants.COLOR_TEXT_PRIMARY);
        emailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        emailField = new JTextField();
        emailField.setFont(AppConstants.FONT_BODY);
        emailField.setPreferredSize(new Dimension(380, 38));
        emailField.setMaximumSize(new Dimension(380, 38));
        emailField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppConstants.COLOR_BORDER, 1),
                new EmptyBorder(5, 10, 5, 10)
        ));

        // Password Field
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(AppConstants.FONT_BODY_BOLD);
        passwordLabel.setForeground(AppConstants.COLOR_TEXT_PRIMARY);
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField = new JPasswordField();
        passwordField.setFont(AppConstants.FONT_BODY);
        passwordField.setPreferredSize(new Dimension(380, 38));
        passwordField.setMaximumSize(new Dimension(380, 38));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppConstants.COLOR_BORDER, 1),
                new EmptyBorder(5, 10, 5, 10)
        ));

        // Enter key submits login
        KeyAdapter enterSubmit = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        };
        emailField.addKeyListener(enterSubmit);
        passwordField.addKeyListener(enterSubmit);

        // Sign In Button
        JButton signInButton = new JButton("Sign In");
        signInButton.setFont(AppConstants.FONT_HEADER);
        signInButton.setForeground(Color.WHITE);
        signInButton.setBackground(AppConstants.COLOR_PRIMARY);
        signInButton.setFocusPainted(false);
        signInButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        signInButton.setPreferredSize(new Dimension(380, 42));
        signInButton.setMaximumSize(new Dimension(380, 42));
        signInButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        signInButton.addActionListener(e -> performLogin());

        // Register Navigation Button
        JButton registerButton = new JButton("Need an account? Register as Student");
        registerButton.setFont(AppConstants.FONT_BODY);
        registerButton.setForeground(AppConstants.COLOR_PRIMARY);
        registerButton.setContentAreaFilled(false);
        registerButton.setBorderPainted(false);
        registerButton.setFocusPainted(false);
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerButton.addActionListener(e -> mainFrame.showCard(AppConstants.CARD_REGISTER));

        // Quick Demo Fill Panel
        JPanel demoPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        demoPanel.setBackground(AppConstants.COLOR_CARD_BG);
        demoPanel.setMaximumSize(new Dimension(380, 32));

        JButton demoAdminBtn = new JButton("Fill Demo Admin");
        demoAdminBtn.setFont(AppConstants.FONT_SMALL);
        demoAdminBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        demoAdminBtn.addActionListener(e -> {
            emailField.setText("admin@qveto.com");
            passwordField.setText("Admin@123");
            statusLabel.setText(" ");
        });

        JButton demoStudentBtn = new JButton("Fill Demo Student");
        demoStudentBtn.setFont(AppConstants.FONT_SMALL);
        demoStudentBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        demoStudentBtn.addActionListener(e -> {
            emailField.setText("student1@qveto.com");
            passwordField.setText("Student@123");
            statusLabel.setText(" ");
        });

        demoPanel.add(demoAdminBtn);
        demoPanel.add(demoStudentBtn);

        // Database status badge
        boolean dbConnected = DBConnection.isDatabaseConnected();
        dbStatusBadge = new JLabel(dbConnected ? "● MySQL Database Connected" : "● Offline / Standalone Demo Mode");
        dbStatusBadge.setFont(AppConstants.FONT_SMALL);
        dbStatusBadge.setForeground(dbConnected ? AppConstants.COLOR_SUCCESS : new Color(217, 119, 6));
        dbStatusBadge.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Layout Assembly
        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 4)));
        card.add(subtitleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 16)));
        card.add(statusLabel);
        card.add(Box.createRigidArea(new Dimension(0, 12)));

        card.add(emailLabel);
        card.add(Box.createRigidArea(new Dimension(0, 4)));
        card.add(emailField);
        card.add(Box.createRigidArea(new Dimension(0, 14)));

        card.add(passwordLabel);
        card.add(Box.createRigidArea(new Dimension(0, 4)));
        card.add(passwordField);
        card.add(Box.createRigidArea(new Dimension(0, 20)));

        card.add(signInButton);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(registerButton);
        card.add(Box.createRigidArea(new Dimension(0, 14)));

        JLabel quickTestLabel = new JLabel("Quick Test Credentials:");
        quickTestLabel.setFont(AppConstants.FONT_SMALL);
        quickTestLabel.setForeground(AppConstants.COLOR_TEXT_MUTED);
        quickTestLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(quickTestLabel);
        card.add(Box.createRigidArea(new Dimension(0, 4)));
        card.add(demoPanel);
        card.add(Box.createRigidArea(new Dimension(0, 16)));
        card.add(dbStatusBadge);

        add(card);
    }

    private void performLogin() {
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        ValidationResult val = InputValidator.validateLogin(email, password);
        if (!val.isValid()) {
            statusLabel.setForeground(AppConstants.COLOR_DANGER);
            statusLabel.setText(val.getFirstError());
            return;
        }

        User user = authService.login(email, password);
        if (user == null) {
            statusLabel.setForeground(AppConstants.COLOR_DANGER);
            statusLabel.setText("Invalid email or password. Please try again.");
            return;
        }

        statusLabel.setText(" ");
        if (user.getRole() == Role.ADMIN) {
            mainFrame.showCard(AppConstants.CARD_ADMIN_DASHBOARD);
        } else {
            mainFrame.showCard(AppConstants.CARD_STUDENT_DASHBOARD);
        }
    }

    public void resetFields() {
        emailField.setText("");
        passwordField.setText("");
        statusLabel.setText(" ");
        boolean dbConnected = DBConnection.isDatabaseConnected();
        dbStatusBadge.setText(dbConnected ? "● MySQL Database Connected" : "● Offline / Standalone Demo Mode");
        dbStatusBadge.setForeground(dbConnected ? AppConstants.COLOR_SUCCESS : new Color(217, 119, 6));
    }
}
