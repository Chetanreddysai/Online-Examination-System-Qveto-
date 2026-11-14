package com.qveto.exam.ui;

import com.qveto.exam.service.AuthService;
import com.qveto.exam.service.ExamService;
import com.qveto.exam.util.AppConstants;
import com.qveto.exam.util.DBConnection;
import com.qveto.exam.util.SessionManager;

import javax.swing.*;
import java.awt.*;

/**
 * Main application window utilizing CardLayout for seamless screen transitions.
 * Coordinates views between Login, Registration, Admin Dashboard, and Student Dashboard.
 */
public class MainFrame extends JFrame {

    private final CardLayout cardLayout;
    private final JPanel cardsPanel;

    private final AuthService authService;
    private final ExamService examService;

    private LoginPanel loginPanel;
    private RegisterPanel registerPanel;
    private AdminDashboardPanel adminDashboardPanel;
    private StudentDashboardPanel studentDashboardPanel;

    public MainFrame() {
        super(AppConstants.APP_TITLE + " - " + AppConstants.APP_VERSION);
        this.authService = new AuthService();
        this.examService = new ExamService();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 680);
        setMinimumSize(new Dimension(860, 580));
        setLocationRelativeTo(null);

        // Initialize CardLayout container
        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);
        cardsPanel.setBackground(AppConstants.COLOR_BACKGROUND);

        initViews();

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(cardsPanel, BorderLayout.CENTER);

        // Show login by default
        showCard(AppConstants.CARD_LOGIN);
    }

    private void initViews() {
        loginPanel = new LoginPanel(this, authService);
        registerPanel = new RegisterPanel(this, authService);
        adminDashboardPanel = new AdminDashboardPanel(this, authService, examService);
        studentDashboardPanel = new StudentDashboardPanel(this, authService, examService);

        cardsPanel.add(loginPanel, AppConstants.CARD_LOGIN);
        cardsPanel.add(registerPanel, AppConstants.CARD_REGISTER);
        cardsPanel.add(adminDashboardPanel, AppConstants.CARD_ADMIN_DASHBOARD);
        cardsPanel.add(studentDashboardPanel, AppConstants.CARD_STUDENT_DASHBOARD);
    }

    /**
     * Navigates to a specific card panel by name.
     */
    public void showCard(String cardName) {
        if (AppConstants.CARD_ADMIN_DASHBOARD.equals(cardName)) {
            adminDashboardPanel.refreshData();
        } else if (AppConstants.CARD_STUDENT_DASHBOARD.equals(cardName)) {
            studentDashboardPanel.refreshData();
        } else if (AppConstants.CARD_LOGIN.equals(cardName)) {
            loginPanel.resetFields();
        }
        cardLayout.show(cardsPanel, cardName);
    }

    public void handleLogout() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to log out?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            authService.logout();
            showCard(AppConstants.CARD_LOGIN);
        }
    }
}
