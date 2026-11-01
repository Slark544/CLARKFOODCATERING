
package com.scc.dao;

import com.scc.model.Role;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.stream.Collectors;

public class DatabaseInitializer {

    private DatabaseInitializer() {
    }

    public static void initialize() {

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(usersTable());

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS foods (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    description TEXT,
                    category TEXT,
                    price REAL NOT NULL CHECK(price >= 0),
                    available INTEGER DEFAULT 1
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS catering_packages (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    description TEXT,
                    price REAL NOT NULL CHECK(price >= 0),
                    guest_limit INTEGER NOT NULL,
                    available INTEGER DEFAULT 1
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS orders (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    customer_id INTEGER NOT NULL,
                    staff_id INTEGER,
                    package_id INTEGER,
                    order_date TEXT DEFAULT CURRENT_TIMESTAMP,
                    total_amount REAL DEFAULT 0,
                    status TEXT DEFAULT 'PENDING',
                    FOREIGN KEY(customer_id) REFERENCES users(id),
                    FOREIGN KEY(staff_id) REFERENCES users(id),
                    FOREIGN KEY(package_id) REFERENCES catering_packages(id)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS order_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    order_id INTEGER NOT NULL,
                    food_id INTEGER NOT NULL,
                    quantity INTEGER NOT NULL,
                    unit_price REAL NOT NULL,
                    subtotal REAL NOT NULL,
                    FOREIGN KEY(order_id) REFERENCES orders(id),
                    FOREIGN KEY(food_id) REFERENCES foods(id)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS reservations (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    customer_id INTEGER NOT NULL,
                    staff_id INTEGER,
                    package_id INTEGER,
                    event_name TEXT NOT NULL,
                    event_date TEXT NOT NULL,
                    event_time TEXT NOT NULL,
                    event_location TEXT NOT NULL,
                    guest_count INTEGER NOT NULL,
                    total_amount REAL DEFAULT 0,
                    status TEXT DEFAULT 'PENDING',
                    FOREIGN KEY(customer_id) REFERENCES users(id),
                    FOREIGN KEY(staff_id) REFERENCES users(id),
                    FOREIGN KEY(package_id) REFERENCES catering_packages(id)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS payments (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    order_id INTEGER,
                    reservation_id INTEGER,
                    amount REAL NOT NULL CHECK(amount > 0),
                    payment_method TEXT NOT NULL,
                    payment_date TEXT DEFAULT CURRENT_TIMESTAMP,
                    status TEXT DEFAULT 'PAID',
                    FOREIGN KEY(order_id) REFERENCES orders(id),
                    FOREIGN KEY(reservation_id) REFERENCES reservations(id),
                    CHECK(order_id IS NOT NULL OR reservation_id IS NOT NULL)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS inventory (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    item_name TEXT NOT NULL UNIQUE,
                    quantity REAL DEFAULT 0,
                    unit TEXT NOT NULL,
                    minimum_stock REAL DEFAULT 0
                )
            """);

            System.out.println("Database initialized successfully!");

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database initialization failed: " + e.getMessage(), e);
        }
    }

    private static String usersTable() {

        String roles = Arrays.stream(Role.values())
                .map(r -> "'" + r.name() + "'")
                .collect(Collectors.joining(", "));

        return """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL,
                roles TEXT NOT NULL CHECK(roles IN (%s)),
                status TEXT NOT NULL DEFAULT 'PENDING'
            )
        """.formatted(roles);
    }
}
