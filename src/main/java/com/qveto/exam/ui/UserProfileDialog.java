package com.qveto.exam.ui;

import com.qveto.exam.model.User;
import com.qveto.exam.service.AuthService;
import com.qveto.exam.util.AppConstants;
import com.qveto.exam.util.SessionManager;
import com.qveto.exam.validation.ValidationResult;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Modal dialog for candidate and administrator profile viewing,
 * display name updates, and secure password changes.
 */
public class UserProfileDialog extends JDialog {

    private final AuthService authService;
    private final Runnable onProfileUpdated;

    // Profile fields
    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JLabel roleLabel;
    private JLabel profileErrorLabel;

    // Password fields
    private JPasswordField oldPassField;
    private JPasswordField newPassField;
    private JPasswordField confirmPassField;
    private JLabel passErrorLabel;

    public UserProfileDialog(Frame owner, AuthService authService, Runnable onProfileUpdated) {
        super(owner, "My Profile & Account Security", true);
        this.authService = authService;
        this.onProfileUpdated = onProfileUpdated;

        setSize(480, 520);
        setLocationRelativeTo(owner);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(AppConstants.FONT_BODY_BOLD);

        tabbedPane.addTab("Profile Details", createProfilePanel());
        tabbedPane.addTab("Change Password", createPasswordPanel());

        getContentPane().add(tabbedPane);
    }

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(AppConstants.COLOR_CARD_BG);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel title = new JLabel("Personal Account Details");
        title.setFont(AppConstants.FONT_HEADER);
        title.setForeground(AppConstants.COLOR_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        profileErrorLabel = new JLabel(" ");
        profileErrorLabel.setFont(AppConstants.FONT_BODY);
        profileErrorLabel.setForeground(AppConstants.COLOR_DANGER);
        profileErrorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        User current = SessionManager.getCurrentUser();

        nameField = createTextField();
        if (current != null) nameField.setText(current.getFullName());

        emailField = createTextField();
        emailField.setEditable(false);
        emailField.setBackground(new Color(243, 244, 246));
        if (current != null) emailField.setText(current.getEmail());

        phoneField = createTextField();
        if (current != null && current.getPhone() != null) phoneField.setText(current.getPhone());

        roleLabel = new JLabel(current != null ? current.getRole().name() : "STUDENT");
        roleLabel.setFont(AppConstants.FONT_BODY_BOLD);
        roleLabel.setForeground(AppConstants.COLOR_SECONDARY);

        JButton saveProfileBtn = new JButton("Save Profile Changes");
        saveProfileBtn.setFont(AppConstants.FONT_BODY_BOLD);
        saveProfileBtn.setForeground(Color.WHITE);
        saveProfileBtn.setBackground(AppConstants.COLOR_PRIMARY);
        saveProfileBtn.setFocusPainted(false);
        saveProfileBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveProfileBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        saveProfileBtn.addActionListener(e -> updateProfile());

        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 4)));
        panel.add(profileErrorLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        panel.add(createFieldLabel("Full Display Name:"));
        panel.add(Box.createRigidArea(new Dimension(0, 4)));
        panel.add(nameField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        panel.add(createFieldLabel("Registered Email Address (Permanent):"));
        panel.add(Box.createRigidArea(new Dimension(0, 4)));
        panel.add(emailField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        panel.add(createFieldLabel("Contact Phone:"));
        panel.add(Box.createRigidArea(new Dimension(0, 4)));
        panel.add(phoneField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        panel.add(createFieldLabel("Account Authority Level:"));
        panel.add(Box.createRigidArea(new Dimension(0, 4)));
        panel.add(roleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        panel.add(saveProfileBtn);

        return panel;
    }

    private JPanel createPasswordPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(AppConstants.COLOR_CARD_BG);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel title = new JLabel("Update Account Password");
        title.setFont(AppConstants.FONT_HEADER);
        title.setForeground(AppConstants.COLOR_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        passErrorLabel = new JLabel(" ");
        passErrorLabel.setFont(AppConstants.FONT_BODY);
        passErrorLabel.setForeground(AppConstants.COLOR_DANGER);
        passErrorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        oldPassField = createPasswordField();
        newPassField = createPasswordField();
        confirmPassField = createPasswordField();

        JButton changePassBtn = new JButton("Update Password");
        changePassBtn.setFont(AppConstants.FONT_BODY_BOLD);
        changePassBtn.setForeground(Color.WHITE);
        changePassBtn.setBackground(AppConstants.COLOR_SECONDARY);
        changePassBtn.setFocusPainted(false);
        changePassBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        changePassBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        changePassBtn.addActionListener(e -> changePassword());

        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 4)));
        panel.add(passErrorLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        panel.add(createFieldLabel("Current Password:"));
        panel.add(Box.createRigidArea(new Dimension(0, 4)));
        panel.add(oldPassField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        panel.add(createFieldLabel("New Password (Min. 6 characters):"));
        panel.add(Box.createRigidArea(new Dimension(0, 4)));
        panel.add(newPassField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        panel.add(createFieldLabel("Confirm New Password:"));
        panel.add(Box.createRigidArea(new Dimension(0, 4)));
        panel.add(confirmPassField);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        panel.add(changePassBtn);

        return panel;
    }

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(AppConstants.FONT_BODY_BOLD);
        lbl.setForeground(AppConstants.COLOR_TEXT_PRIMARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setFont(AppConstants.FONT_BODY);
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppConstants.COLOR_BORDER, 1),
                new EmptyBorder(4, 8, 4, 8)
        ));
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
        return tf;
    }

    private JPasswordField createPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(AppConstants.FONT_BODY);
        pf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        pf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(AppConstants.COLOR_BORDER, 1),
                new EmptyBorder(4, 8, 4, 8)
        ));
        pf.setAlignmentX(Component.LEFT_ALIGNMENT);
        return pf;
    }

    private void updateProfile() {
        User user = SessionManager.getCurrentUser();
        if (user == null) {
            profileErrorLabel.setText("Session expired. Please sign in again.");
            return;
        }

        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();

        ValidationResult res = authService.updateProfile(user.getUserId(), name, phone);
        if (!res.isValid()) {
            profileErrorLabel.setText(res.getFirstError());
            return;
        }

        JOptionPane.showMessageDialog(this,
                "Profile information successfully updated!",
                "Profile Updated",
                JOptionPane.INFORMATION_MESSAGE);

        if (onProfileUpdated != null) {
            onProfileUpdated.run();
        }
        dispose();
    }

    private void changePassword() {
        User user = SessionManager.getCurrentUser();
        if (user == null) {
            passErrorLabel.setText("Session expired. Please sign in again.");
            return;
        }

        String oldPass = new String(oldPassField.getPassword());
        String newPass = new String(newPassField.getPassword());
        String confirm = new String(confirmPassField.getPassword());

        ValidationResult res = authService.updatePassword(user.getUserId(), oldPass, newPass, confirm);
        if (!res.isValid()) {
            passErrorLabel.setText(res.getFirstError());
            return;
        }

        JOptionPane.showMessageDialog(this,
                "Password updated successfully! Please use your new password next time you sign in.",
                "Password Changed",
                JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }
}
