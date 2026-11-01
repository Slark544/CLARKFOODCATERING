
package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.dao.CateringDao;
import com.scc.model.Role;
import com.scc.model.User;

import java.util.List;
import java.util.Scanner;

public class AdminMenu extends RoleMenu {

    public AdminMenu(
            Scanner scanner,
            UserDao userDao,
            CateringDao cateringDao,
            User user) {

        super(scanner, userDao, cateringDao, user);
    }

    @Override
    protected String title() {
        return "ADMIN DASHBOARD";
    }

    @Override
    protected void showOptions() {

        System.out.println("3. View All Accounts");
        System.out.println("4. View Staff Accounts");
        System.out.println("5. View Customer Accounts");
        System.out.println("6. View Pending Accounts");
        System.out.println("7. Add Account");
        System.out.println("8. Update Account");
        System.out.println("9. Approve Account");
        System.out.println("10. Remove Account");
    }

    @Override
    protected boolean handleOption(String choice) {

        switch (choice) {

            case "3" -> viewAllAccounts();
            case "4" -> viewAccountsByRole(Role.STAFF);
            case "5" -> viewAccountsByRole(Role.CUSTOMER);
            case "6" -> viewPendingAccounts();
            case "7" -> addAccount();
            case "8" -> updateAccount();
            case "9" -> approveAccount();
            case "10" -> removeAccount();

            default -> {
                return false;
            }
        }

        return true;
    }

    // ================= VIEW ACCOUNTS =================

    private void viewAllAccounts() {

        System.out.println("\n========== ALL SYSTEM ACCOUNTS ==========");

        displayAccounts(userDao.getAllUsers());
    }

    private void viewAccountsByRole(Role role) {

        System.out.println("\n========== " + role + " ACCOUNTS ==========");

        displayAccounts(userDao.getUsersByRole(role));
    }

    private void viewPendingAccounts() {

        System.out.println("\n========== PENDING ACCOUNTS ==========");

        displayAccounts(userDao.getPendingUsers());
    }

    private void displayAccounts(List<User> users) {

        if (users.isEmpty()) {
            System.out.println("No accounts found.");
            return;
        }

        String border =
                "+------+------------------+------------+------------+";

        System.out.println(border);

        System.out.printf(
                "| %-4s | %-16s | %-10s | %-10s |%n",
                "ID", "USERNAME", "ROLE", "STATUS"
        );

        System.out.println(border);

        for (User u : users) {

            System.out.printf(
                    "| %-4d | %-16s | %-10s | %-10s |%n",
                    u.id(),
                    truncate(u.username(), 16),
                    u.role(),
                    u.status()
            );
        }

        System.out.println(border);
        System.out.println("Total accounts: " + users.size());
    }

    private String truncate(String value, int max) {

        return value.length() <= max
                ? value
                : value.substring(0, max - 3) + "...";
    }

    // ================= ADD ACCOUNT =================

    private void addAccount() {

        System.out.println("\n========== ADD ACCOUNT ==========");

        String username = readText("Username: ");
        String password = readText("Password: ");

        if (username.isBlank() || password.isBlank()) {
            System.out.println("Username and password are required.");
            return;
        }

        System.out.println("\nSelect Role:");
        System.out.println("1. Admin");
        System.out.println("2. Staff");
        System.out.println("3. Customer");

        Role role = switch (readText("Choose role: ")) {
            case "1" -> Role.ADMIN;
            case "2" -> Role.STAFF;
            case "3" -> Role.CUSTOMER;
            default -> null;
        };

        if (role == null) {
            System.out.println("Invalid role.");
            return;
        }

        if (userDao.createUser(username, password, role)) {
            System.out.println("Account created successfully.");
        } else {
            System.out.println("Account creation failed.");
        }

        viewAllAccounts();
    }

    // ================= UPDATE ACCOUNT =================

    private void updateAccount() {

        System.out.println("\n========== UPDATE ACCOUNT ==========");

        List<User> users = userDao.getAllUsers();

        // Display all accounts before asking for ID.
        displayAccounts(users);

        if (users.isEmpty()) return;

        int id = readInt("Enter User ID to update (0 to cancel): ");

        if (id == 0) return;

        User target = findUser(users, id);

        if (target == null) {
            System.out.println("Account ID not found.");
            return;
        }

        if (target.id() == user.id()) {
            System.out.println("You cannot modify your active admin account.");
            return;
        }

        System.out.println("\nSelected Account:");
        displayAccounts(List.of(target));

        System.out.println("\nSelect New Role:");
        System.out.println("1. Admin");
        System.out.println("2. Staff");
        System.out.println("3. Customer");

        Role newRole = switch (readText("Choose role: ")) {
            case "1" -> Role.ADMIN;
            case "2" -> Role.STAFF;
            case "3" -> Role.CUSTOMER;
            default -> null;
        };

        if (newRole == null) {
            System.out.println("Invalid role.");
            return;
        }

        System.out.println("\nSelect New Status:");
        System.out.println("1. APPROVED");
        System.out.println("2. PENDING");

        String status = switch (readText("Choose status: ")) {
            case "1" -> "APPROVED";
            case "2" -> "PENDING";
            default -> null;
        };

        if (status == null) {
            System.out.println("Invalid status.");
            return;
        }

        if (userDao.updateUserRoleAndStatus(
                id, newRole.name(), status)) {

            System.out.println("Account updated successfully.");

        } else {
            System.out.println("Account update failed.");
        }

        viewAllAccounts();
    }

    // ================= APPROVE ACCOUNT =================

    private void approveAccount() {

        System.out.println("\n========== APPROVE ACCOUNT ==========");

        List<User> pending = userDao.getPendingUsers();

        displayAccounts(pending);

        if (pending.isEmpty()) return;

        int id = readInt("Enter User ID to approve (0 to cancel): ");

        if (id == 0) return;

        User target = findUser(pending, id);

        if (target == null) {
            System.out.println("Account not found in pending list.");
            return;
        }

        if (userDao.approveUser(id)) {
            System.out.println("Account approved successfully.");
        } else {
            System.out.println("Approval failed.");
        }

        viewAllAccounts();
    }

    // ================= REMOVE ACCOUNT =================

    private void removeAccount() {

        System.out.println("\n========== REMOVE ACCOUNT ==========");

        List<User> users = userDao.getAllUsers();

        // Always show accounts first.
        displayAccounts(users);

        if (users.isEmpty()) return;

        int id = readInt("Enter User ID to remove (0 to cancel): ");

        if (id == 0) return;

        User target = findUser(users, id);

        if (target == null) {
            System.out.println("Account not found.");
            return;
        }

        if (target.id() == user.id()) {
            System.out.println("You cannot remove your active admin account.");
            return;
        }

        System.out.println("\nAccount selected:");
        displayAccounts(List.of(target));

        String confirm = readText(
                "Confirm deletion? (YES/NO): "
        );

        if (confirm.equalsIgnoreCase("YES")) {

            if (userDao.deleteUser(id)) {
                System.out.println("Account deleted successfully.");
            } else {
                System.out.println("Account deletion failed.");
            }

        } else {
            System.out.println("Deletion cancelled.");
        }

        viewAllAccounts();
    }

    // ================= FIND ACCOUNT =================

    private User findUser(List<User> users, int id) {

        for (User u : users) {

            if (u.id() == id) {
                return u;
            }
        }

        return null;
    }
}
