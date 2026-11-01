
package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.dao.CateringDao;
import com.scc.model.User;

import java.util.Scanner;

public abstract class RoleMenu {

    protected final Scanner scanner;
    protected final UserDao userDao;
    protected final CateringDao cateringDao;
    protected final User user;

    protected RoleMenu(
            Scanner scanner,
            UserDao userDao,
            CateringDao cateringDao,
            User user) {

        this.scanner = scanner;
        this.userDao = userDao;
        this.cateringDao = cateringDao;
        this.user = user;
    }

    protected abstract String title();

    protected void showOptions() {
    }

    protected boolean handleOption(String choice) {
        return false;
    }

    public final void show() {

        System.out.println("\nWelcome, " + user.username() + "!");
        System.out.println("Role: " + user.role());
        System.out.println("Status: " + user.status());

        boolean running = true;

        while (running) {

            System.out.println("\n========== " + title() + " ==========");
            System.out.println("1. View Profile");
            System.out.println("2. Logout");

            showOptions();

            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1" -> viewProfile();

                case "2" -> {
                    System.out.println("Logging out...");
                    running = false;
                }

                default -> {
                    if (!handleOption(choice)) {
                        System.out.println("Invalid option.");
                    }
                }
            }
        }
    }

    protected void viewProfile() {

        System.out.println("\n========== MY PROFILE ==========");
        System.out.println("ID:       " + user.id());
        System.out.println("Username: " + user.username());
        System.out.println("Role:     " + user.role());
        System.out.println("Status:   " + user.status());
    }

    // ================= INPUT HELPERS =================

    protected String readText(String message) {

        System.out.print(message);
        return scanner.nextLine().trim();
    }

    protected int readInt(String message) {

        while (true) {

            try {
                return Integer.parseInt(readText(message));

            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    protected double readDouble(String message) {

        while (true) {

            try {
                double value = Double.parseDouble(readText(message));

                if (Double.isFinite(value)) {
                    return value;
                }

            } catch (NumberFormatException ignored) {
            }

            System.out.println("Please enter a valid amount.");
        }
    }
}
