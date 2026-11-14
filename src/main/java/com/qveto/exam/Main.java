package com.qveto.exam;

import com.qveto.exam.ui.MainFrame;

import javax.swing.*;

/**
 * Main application entrypoint for the Qveto Online Examination System (Project 3).
 * Initializes Look-and-Feel and launches the Swing GUI on the Event Dispatch Thread (EDT).
 */
public class Main {

    public static void main(String[] args) {
        // Set System Look and Feel for native operating system styling
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("[Main] Could not set system look-and-feel: " + e.getMessage());
        }

        // Launch UI safely on Swing Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(
                        null,
                        "Failed to initialize Online Examination System: " + e.getMessage(),
                        "Startup Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}
