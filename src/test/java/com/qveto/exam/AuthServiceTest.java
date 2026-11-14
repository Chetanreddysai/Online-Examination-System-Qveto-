package com.qveto.exam;

import com.qveto.exam.model.Role;
import com.qveto.exam.model.User;
import com.qveto.exam.service.AuthService;
import com.qveto.exam.util.SessionManager;
import com.qveto.exam.validation.ValidationResult;
import static com.qveto.exam.Assert.*;

public class AuthServiceTest {

    private AuthService authService;

    public void setUp() {
        authService = new AuthService();
        SessionManager.logout();
    }

    public void testAdminLoginSuccess() {
        setUp();
        User user = authService.login("admin@qveto.com", "Admin@123");
        assertNotNull(user, "Admin login should succeed");
        assertEquals(Role.ADMIN, user.getRole());
        assertTrue(SessionManager.isAdmin());
    }

    public void testStudentLoginSuccess() {
        setUp();
        User user = authService.login("student1@qveto.com", "Student@123");
        assertNotNull(user, "Student login should succeed");
        assertEquals(Role.STUDENT, user.getRole());
        assertTrue(SessionManager.isStudent());
    }

    public void testInvalidPasswordLogin() {
        setUp();
        User user = authService.login("admin@qveto.com", "WrongPassword");
        assertNull(user, "Login with wrong password should fail");
        assertFalse(SessionManager.isAuthenticated());
    }

    public void testRegisterNewStudent() {
        setUp();
        String uniqueEmail = "newstudent_" + System.currentTimeMillis() + "@qveto.com";
        ValidationResult result = authService.register(
                "New Student", uniqueEmail, "Test@123", "Test@123", "+91 91234 56789"
        );
        assertTrue(result.isValid(), "Registration of valid student should succeed");

        // Verify login with newly registered credentials
        User loggedIn = authService.login(uniqueEmail, "Test@123");
        assertNotNull(loggedIn);
        assertEquals("New Student", loggedIn.getFullName());
    }

    public void testLogout() {
        setUp();
        authService.login("student1@qveto.com", "Student@123");
        assertTrue(SessionManager.isAuthenticated());

        authService.logout();
        assertFalse(SessionManager.isAuthenticated());
        assertNull(SessionManager.getCurrentUser());
    }
}
