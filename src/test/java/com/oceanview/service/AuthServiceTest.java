package com.oceanview.service;

import com.oceanview.dao.AuditLogDAO;
import com.oceanview.dao.UserDAO;
import com.oceanview.model.User;
import com.oceanview.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserDAO userDAO;

    @Mock
    private AuditLogDAO auditLogDAO;

    @InjectMocks
    private AuthService authService;

    private User adminUser;

    @BeforeEach
    void setUp() {
        adminUser = new User(1, "admin", PasswordUtil.hash("Admin@1234"), "ADMIN");
    }

    // ── Login ─────────────────────────────────────────────────────────────
    @Test
    void testLoginWithValidCredentialsReturnsToken() {
        when(userDAO.getUserByUsername("admin")).thenReturn(adminUser);
        String token = authService.login("admin", "Admin@1234");
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testLoginWithWrongPasswordReturnsNull() {
        when(userDAO.getUserByUsername("admin")).thenReturn(adminUser);
        String token = authService.login("admin", "wrongpassword");
        assertNull(token);
    }

    @Test
    void testLoginWithNonExistentUserReturnsNull() {
        when(userDAO.getUserByUsername("nobody")).thenReturn(null);
        String token = authService.login("nobody", "somepassword");
        assertNull(token);
    }

    @Test
    void testLoginWithNullUsernameReturnsNull() {
        String token = authService.login(null, "Admin@1234");
        assertNull(token);
    }

    @Test
    void testLoginWithNullPasswordReturnsNull() {
        String token = authService.login("admin", null);
        assertNull(token);
    }

    // ── Session ───────────────────────────────────────────────────────────
    @Test
    void testValidSessionAfterLogin() {
        when(userDAO.getUserByUsername("admin")).thenReturn(adminUser);
        String token = authService.login("admin", "Admin@1234");
        assertTrue(authService.isValidSession(token));
    }

    @Test
    void testInvalidSessionForRandomToken() {
        assertFalse(authService.isValidSession("fake-token-123"));
    }

    @Test
    void testNullTokenIsInvalidSession() {
        assertFalse(authService.isValidSession(null));
    }

    // ── Logout ────────────────────────────────────────────────────────────
    @Test
    void testSessionInvalidAfterLogout() {
        when(userDAO.getUserByUsername("admin")).thenReturn(adminUser);
        when(userDAO.getUserById(1)).thenReturn(adminUser);
        String token = authService.login("admin", "Admin@1234");
        authService.logout(token);
        assertFalse(authService.isValidSession(token));
    }

    // ── getUserByToken ─────────────────────────────────────────────────────
    @Test
    void testGetUserByTokenReturnsCorrectUser() {
        when(userDAO.getUserByUsername("admin")).thenReturn(adminUser);
        when(userDAO.getUserById(1)).thenReturn(adminUser);
        String token = authService.login("admin", "Admin@1234");
        User result = authService.getUserByToken(token);
        assertNotNull(result);
        assertEquals("admin", result.getUsername());
    }

    @Test
    void testGetUserByInvalidTokenReturnsNull() {
        assertNull(authService.getUserByToken("invalid-token"));
    }

    // ── getUserRole ───────────────────────────────────────────────────────
    @Test
    void testGetUserRoleReturnsAdmin() {
        when(userDAO.getUserByUsername("admin")).thenReturn(adminUser);
        when(userDAO.getUserById(1)).thenReturn(adminUser);
        String token = authService.login("admin", "Admin@1234");
        assertEquals("ADMIN", authService.getUserRole(token));
    }

    @Test
    void testTwoLoginsProduceDifferentTokens() {
        when(userDAO.getUserByUsername("admin")).thenReturn(adminUser);
        String token1 = authService.login("admin", "Admin@1234");
        String token2 = authService.login("admin", "Admin@1234");
        assertNotEquals(token1, token2);
    }
}
