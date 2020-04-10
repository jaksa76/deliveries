package com.zuhlke.deliveries;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
    public static final String PASSWORD = "asdffdsa-123";
    public static final String ANOTHER_PASSWORD = "fdsakjhgd/23";
    UserService userService = new UserService("asdf", "ME");

    @BeforeEach
    void setUp() {
        userService.wipeAllData();
    }

    @Test
    void registeringUser() throws Exception {
        userService.registerUser("Joe", "069/654321", PASSWORD);
        User user = userService.authenticate("069/654321", PASSWORD);
        assertEquals("Joe", user.name);
    }

    @Test
    void registeringUserWithInvalidPhone() throws Exception {
        Exception e = assertThrows(Exception.class, () -> userService.registerUser("Joe", "asdffdsa", PASSWORD));
        assertTrue(e.getMessage().contains("is not valid"));
    }

    @Test
    void registeringUserWithEmptyPhone() throws Exception {
        Exception e = assertThrows(Exception.class, () -> userService.registerUser("Joe", "", PASSWORD));
        assertTrue(e.getMessage().contains("is not valid"));
    }

    @Test
    void registeringUserWithNullPhone() throws Exception {
        Exception e = assertThrows(Exception.class, () -> userService.registerUser("Joe", null, PASSWORD));
        assertTrue(e.getMessage().contains("is not valid"));
    }

    @Test
    void registeringUserWithShortPassword() throws Exception {
        Exception e = assertThrows(Exception.class, () -> userService.registerUser("Joe", "069/654321", "123456"));
        assertTrue(e.getMessage().contains("password"));
    }

    @Test
    void registeringUserWithNullPassword() throws Exception {
        Exception e = assertThrows(Exception.class, () -> userService.registerUser("Joe", "069/654321", null));
        assertTrue(e.getMessage().contains("password"));
    }

    @Test
    void registeringUserWithEmptyPassword() throws Exception {
        Exception e = assertThrows(Exception.class, () -> userService.registerUser("Joe", "069/654321", ""));
        assertTrue(e.getMessage().contains("password"));
    }

    @Test
    void loggingInWithSimilarPhone() throws Exception {
        userService.registerUser("Joe", "069/654321", PASSWORD);
        User user = userService.authenticate("+382 069 654321", PASSWORD);
        assertEquals("Joe", user.name);
    }

    @Test
    void registeringSamePhone() {
        String phoneNumber = "069/654321";
        Exception e = assertThrows(Exception.class, () -> {
            userService.registerUser("Joe", phoneNumber, PASSWORD);
            userService.registerUser("Mary", phoneNumber, ANOTHER_PASSWORD);
        });
        assertTrue(e.getMessage().contains("already registered"));
    }

    @Test
    void registeringSimilarPhone() {
        Exception e = assertThrows(Exception.class, () -> {
            userService.registerUser("Joe", "069/654321", PASSWORD);
            userService.registerUser("Mary", "+382 69/654-321", ANOTHER_PASSWORD);
        });
        assertTrue(e.getMessage().contains("already registered"));
    }
}