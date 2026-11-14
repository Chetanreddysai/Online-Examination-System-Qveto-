package com.qveto.exam.ui;

import com.qveto.exam.service.AuthService;
import com.qveto.exam.util.AppConstants;
import com.qveto.exam.validation.ValidationResult;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Student Registration screen with real-time validation and error notifications.
 */
public class RegisterPanel extends JPanel {

    private final MainFrame mainFrame;
    private final AuthService authService;

    private JTextField nameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField phoneField;
    private JLabel statusLabel;

    public RegisterPanel(MainFrame mainFrame, AuthService authService) {
        this.mainFrame = mainFrame;
        this.authService = authService;
        initUI();
    }

    private void initUI() {
        setLayout(new GridBagLayout());
        setBackground(AppConstants.COLOR_BACKGROUND);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(AppConstants.COLOR_CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppConstants.COLOR_BORDER, 1, true),
                new EmptyBorder(28, 40, 28, 40)
        ));
        card.setPreferredSize(new Dimension(480, 620));
        card.setMaximumSize(new Dimension(480, 620));

        // Header
        JLabel titleLabel = new JLabel("Create Student Account");
        titleLabel.setFont(AppConstants.FONT_TITLE);
        titleLabel.setForeground(AppConstants.COLOR_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Join Qveto Examination System to attend assessments");
        subtitleLabel.setFont(AppConstants.FONT_SMALL);
        subtitleLabel.setForeground(AppConstants.COLOR_TEXT_MUTED);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(AppConstants.FONT_BODY);
        statusLabel.setForeground(AppConstants.COLOR_DANGER);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Fields
        nameField = createStyledTextField();
        emailField = createStyledTextField();
        passwordField = createStyledPasswordField();
        confirmPasswordField = createStyledPasswordField();
        phoneField = createStyledTextField();

        // Register Button
        JButton registerBtn = new JButton("Create Account");
        registerBtn.setFont(AppConstants.FONT_HEADER);
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setBackground(AppConstants.COLOR_SECONDARY);
        registerBtn.setFocusPainted(false);
        registerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerBtn.setPreferredSize(new Dimension(400, 40));
        registerBtn.setMaximumSize(new Dimension(400, 40));
        registerBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerBtn.addActionListener(e -> performRegistration());

        // Back to Login Button
        JButton backBtn = new JButton("← Back to Sign In");
        backBtn.setFont(AppConstants.FONT_BODY);
        backBtn.setForeground(AppConstants.COLOR_PRIMARY);
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        backBtn.addActionListener(e -> {
            resetFields();
            mainFrame.showCard(AppConstants.CARD_LOGIN);
        });

        // Assemble Layout
        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 4)));
        card.add(subtitleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(statusLabel);
        card.add(Box.createRigidArea(new Dimension(0, 10)));

        card.add(createFieldLabel("Full Legal Name"));
        card.add(Box.createRigidArea(new Dimension(0, 2)));
        card.add(nameField);
        card.add(Box.createRigidArea(new Dimension(0, 10)));

        card.add(createFieldLabel("Email Address"));
        card.add(Box.createRigidArea(new Dimension(0, 2)));
        card.add(emailField);
        card.add(Box.createRigidArea(new Dimension(0, 10)));

        card.add(createFieldLabel("Mobile Phone (Optional)"));
        card.add(Box.createRigidArea(new Dimension(0, 2)));
        card.add(phoneField);
        card.add(Box.createRigidArea(new Dimension(0, 10)));

        card.add(createFieldLabel("Password (Min. 6 characters)"));
        card.add(Box.createRigidArea(new Dimension(0, 2)));
        card.add(passwordField);
        card.add(Box.createRigidArea(new Dimension(0, 10)));

        card.add(createFieldLabel("Confirm Password"));
        card.add(Box.createRigidArea(new Dimension(0, 2)));
        card.add(confirmPasswordField);
        card.add(Box.createRigidArea(new Dimension(0, 18)));

        card.add(registerBtn);
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        card.add(backBtn);

        add(card);
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(AppConstants.FONT_BODY_BOLD);
        label.setForeground(AppConstants.COLOR_TEXT_PRIMARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(AppConstants.FONT_BODY);
        field.setPreferredSize(new Dimension(400, 36));
        field.setMaximumSize(new Dimension(400, 36));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppConstants.COLOR_BORDER, 1),
                new EmptyBorder(4, 8, 4, 8)
        ));
        return field;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(AppConstants.FONT_BODY);
        field.setPreferredSize(new Dimension(400, 36));
        field.setMaximumSize(new Dimension(400, 36));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppConstants.COLOR_BORDER, 1),
                new EmptyBorder(4, 8, 4, 8)
        ));
        return field;
    }

    private void performRegistration() {
        String name = nameField.getText();
        String email = emailField.getText();
        String pass = new String(passwordField.getPassword());
        String confirm = new String(confirmPasswordField.getPassword());
        String phone = phoneField.getText();

        ValidationResult result = authService.register(name, email, pass, confirm, phone);
        if (!result.isValid()) {
            statusLabel.setForeground(AppConstants.COLOR_DANGER);
            statusLabel.setText(result.getFirstError());
            return;
        }

        JOptionPane.showMessageDialog(
                this,
                "Student account created successfully!\nPlease sign in with your email and password.",
                "Registration Successful",
                JOptionPane.INFORMATION_MESSAGE
        );

        resetFields();
        mainFrame.showCard(AppConstants.CARD_LOGIN);
    }

    public void resetFields() {
        nameField.setText("");
        emailField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
        phoneField.setText("");
        statusLabel.setText(" ");
    }
}
