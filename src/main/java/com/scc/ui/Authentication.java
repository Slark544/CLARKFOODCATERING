
package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.dao.CateringDao;
import com.scc.model.Role;
import com.scc.model.User;

import java.util.Optional;
import java.util.Scanner;

public class Authentication {

    private final Scanner scanner;
    private final UserDao userDao;
    private final CateringDao cateringDao;

    public Authentication(
            Scanner scanner,
            UserDao userDao,
            CateringDao cateringDao) {

        this.scanner = scanner;
        this.userDao = userDao;
        this.cateringDao = cateringDao;
    }

    // ================= MAIN AUTHENTICATION MENU =================

    public void start() {

        boolean running = true;

        while (running) {

            System.out.println("\n================================");
            System.out.println("       FOOD CATERING SYSTEM");
            System.out.println("================================");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1" -> login();

                case "2" -> register();

                case "3" -> {
                    System.out.println("\nThank you for using the Food Catering System!");
                    running = false;
                }

                default -> System.out.println(
                        "Invalid option. Please choose 1, 2, or 3.");
            }
        }
    }

    // ================= REGISTRATION =================

    private void register() {

        System.out.println("\n========== REGISTRATION ==========");

        System.out.print("Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        if (username.isBlank() || password.isBlank()) {
            System.out.println("Username and password cannot be empty.");
            return;
        }

        if (username.length() < 3) {
            System.out.println("Username must contain at least 3 characters.");
            return;
        }

        if (password.length() < 6) {
            System.out.println("Password must contain at least 6 characters.");
            return;
        }

        // Account type selection
        System.out.println("\nSelect Account Type:");
        System.out.println("1. Admin");
        System.out.println("2. Staff");
        System.out.println("3. Customer");
        System.out.print("Choose account type: ");

        Role role;

        switch (scanner.nextLine().trim()) {

            case "1" -> role = Role.ADMIN;

            case "2" -> role = Role.STAFF;

            case "3" -> role = Role.CUSTOMER;

            default -> {
                System.out.println("Invalid account type.");
                return;
            }
        }

        // Save registration to database
        if (userDao.register(username, password, role)) {

            System.out.println("\nRegistration successful!");
            System.out.println("Account Type: " + role);
            System.out.println("Account Status: PENDING");
            System.out.println("Please wait for administrator approval.");

        } else {

            System.out.println(
                    "Registration failed. Username may already exist.");
        }
    }

    // ================= LOGIN =================

    private void login() {

        System.out.println("\n========== LOGIN ==========");

        System.out.print("Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        if (username.isBlank() || password.isBlank()) {
            System.out.println("Username and password are required.");
            return;
        }

        Optional<User> result = userDao.login(username, password);

        if (result.isEmpty()) {

            System.out.println(
                    "\nLogin failed. Invalid credentials or account not approved.");

            return;
        }

        User user = result.get();

        System.out.println("\nLogin successful!");
        System.out.println("Welcome, " + user.username() + "!");

        routeToMenu(user);
    }

    // ================= ROLE-BASED MENU ROUTING =================

    private void routeToMenu(User user) {

        RoleMenu menu = switch (user.role()) {

            case ADMIN -> new AdminMenu(
                    scanner,
                    userDao,
                    cateringDao,
                    user
            );

            case STAFF -> new StaffMenu(
                    scanner,
                    userDao,
                    cateringDao,
                    user
            );

            case CUSTOMER -> new CustomerMenu(
                    scanner,
                    userDao,
                    cateringDao,
                    user
            );
        };

        menu.show();
    }
}
