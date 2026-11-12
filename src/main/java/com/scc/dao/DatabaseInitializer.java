
package com.scc.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() {

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // ==========================================
            // USERS TABLE
            // ==========================================

            String usersTable = """
                    CREATE TABLE IF NOT EXISTS users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username TEXT NOT NULL UNIQUE,
                        password TEXT NOT NULL,
                        roles TEXT NOT NULL,
                        status TEXT NOT NULL DEFAULT 'PENDING'
                    )
                    """;

            stmt.execute(usersTable);


            // ==========================================
            // FOODS TABLE
            // ==========================================

            String foodsTable = """
                    CREATE TABLE IF NOT EXISTS foods (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        description TEXT,
                        category TEXT,
                        price REAL NOT NULL,
                        available INTEGER DEFAULT 1
                    )
                    """;

            stmt.execute(foodsTable);


            // ==========================================
            // CATERING PACKAGES TABLE
            // ==========================================

            String packagesTable = """
                    CREATE TABLE IF NOT EXISTS catering_packages (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        description TEXT,
                        price REAL NOT NULL,
                        guest_limit INTEGER,
                        available INTEGER DEFAULT 1
                    )
                    """;

            stmt.execute(packagesTable);


            // ==========================================
            // INVENTORY TABLE
            // ==========================================

            String inventoryTable = """
                    CREATE TABLE IF NOT EXISTS inventory (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        item_name TEXT NOT NULL,
                        quantity REAL NOT NULL,
                        unit TEXT,
                        minimum_stock REAL DEFAULT 0
                    )
                    """;

            stmt.execute(inventoryTable);


            // ==========================================
            // ORDERS TABLE
            // ==========================================

            String ordersTable = """
                    CREATE TABLE IF NOT EXISTS orders (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        customer_id INTEGER NOT NULL,
                        staff_id INTEGER,
                        package_id INTEGER,
                        order_date TEXT,
                        total_amount REAL NOT NULL,
                        status TEXT DEFAULT 'PENDING'
                    )
                    """;

            stmt.execute(ordersTable);


            // ==========================================
            // ORDER ITEMS TABLE
            // ==========================================

            String orderItemsTable = """
                    CREATE TABLE IF NOT EXISTS order_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        order_id INTEGER NOT NULL,
                        food_id INTEGER NOT NULL,
                        quantity INTEGER NOT NULL,
                        unit_price REAL NOT NULL,
                        subtotal REAL NOT NULL
                    )
                    """;

            stmt.execute(orderItemsTable);


            // ==========================================
            // RESERVATIONS TABLE
            // ==========================================

            String reservationsTable = """
                    CREATE TABLE IF NOT EXISTS reservations (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        customer_id INTEGER NOT NULL,
                        staff_id INTEGER,
                        package_id INTEGER,
                        event_name TEXT NOT NULL,
                        event_date TEXT NOT NULL,
                        event_time TEXT,
                        event_location TEXT,
                        guest_count INTEGER,
                        total_amount REAL,
                        status TEXT DEFAULT 'PENDING'
                    )
                    """;

            stmt.execute(reservationsTable);


            // ==========================================
            // PAYMENTS TABLE
            // ==========================================

            String paymentsTable = """
                    CREATE TABLE IF NOT EXISTS payments (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        order_id INTEGER,
                        reservation_id INTEGER,
                        amount REAL NOT NULL,
                        payment_method TEXT,
                        payment_date TEXT,
                        status TEXT DEFAULT 'PENDING'
                    )
                    """;

            stmt.execute(paymentsTable);


            // ==========================================
            // ACTIVITY LOGS TABLE
            // DELIVERABLE 8
            // ==========================================

            String activityLogsTable = """
                    CREATE TABLE IF NOT EXISTS activity_logs (
                        log_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER NOT NULL,
                        action_type TEXT NOT NULL,
                        description TEXT NOT NULL,
                        timestamp TEXT NOT NULL
                            DEFAULT (datetime('now', 'localtime'))
                    )
                    """;

            stmt.execute(activityLogsTable);


            // ==========================================
            // ACTIVITY LOGS INDEX
            // ==========================================

            String activityLogsIndex = """
                    CREATE INDEX IF NOT EXISTS idx_activity_logs_user
                    ON activity_logs(user_id)
                    """;

            stmt.execute(activityLogsIndex);


            System.out.println("Database initialized successfully!");

        } catch (SQLException e) {

            System.out.println(
                    "Database initialization error: " + e.getMessage()
            );

        }
    }
}
