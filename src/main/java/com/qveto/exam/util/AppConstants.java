package com.qveto.exam.util;

import java.awt.Color;
import java.awt.Font;

/**
 * Global application constants, theme colors, fonts, and UI card identifiers.
 */
public final class AppConstants {

    private AppConstants() {}

    // Application Metadata
    public static final String APP_TITLE = "Qveto Online Examination System";
    public static final String APP_VERSION = "v1.2.0 (Week 3 Build)";

    // Navigation Card Names for CardLayout
    public static final String CARD_LOGIN = "CARD_LOGIN";
    public static final String CARD_REGISTER = "CARD_REGISTER";
    public static final String CARD_ADMIN_DASHBOARD = "CARD_ADMIN_DASHBOARD";
    public static final String CARD_STUDENT_DASHBOARD = "CARD_STUDENT_DASHBOARD";
    public static final String CARD_TIMED_EXAM = "CARD_TIMED_EXAM";
    public static final String CARD_RESULT = "CARD_RESULT";

    // Palette Colors
    public static final Color COLOR_PRIMARY = new Color(30, 64, 175);       // Deep Blue (#1E40AF)
    public static final Color COLOR_PRIMARY_HOVER = new Color(29, 78, 216); // Lighter Blue
    public static final Color COLOR_SECONDARY = new Color(13, 148, 136);    // Teal (#0D9488)
    public static final Color COLOR_BACKGROUND = new Color(243, 244, 246);  // Light Gray (#F3F4F6)
    public static final Color COLOR_CARD_BG = Color.WHITE;
    public static final Color COLOR_TEXT_PRIMARY = new Color(17, 24, 39);   // Near Black (#111827)
    public static final Color COLOR_TEXT_MUTED = new Color(107, 114, 128);  // Gray (#6B7280)
    public static final Color COLOR_SUCCESS = new Color(16, 185, 129);      // Green (#10B981)
    public static final Color COLOR_WARNING = new Color(245, 158, 11);      // Amber (#F59E0B)
    public static final Color COLOR_DANGER = new Color(239, 68, 68);        // Red (#EF4444)
    public static final Color COLOR_BORDER = new Color(229, 231, 235);      // Soft Gray Border

    // Typography
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_SUBHEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
}
