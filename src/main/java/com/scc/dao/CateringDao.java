
package com.scc.dao;

import java.sql.*;

public class CateringDao {

    // ================= FOOD MENU =================

    public void viewFoods() {
        display("""
            SELECT id,name,category,price,available
            FROM foods ORDER BY id
        """, "FOOD MENU");
    }

    public boolean addFood(String name, String description,
                           String category, double price) {

        String sql = """
            INSERT INTO foods(name,description,category,price)
            VALUES(?,?,?,?)
        """;

        return execute(sql, name, description, category, price);
    }

    public boolean deleteFood(int id) {
        return execute("DELETE FROM foods WHERE id=?", id);
    }

    // ================= PACKAGES =================

    public void viewPackages() {
        display("""
            SELECT id,name,description,price,guest_limit,available
            FROM catering_packages ORDER BY id
        """, "CATERING PACKAGES");
    }

    public boolean addPackage(String name, String description,
                              double price, int guestLimit) {

        return execute("""
            INSERT INTO catering_packages
            (name,description,price,guest_limit)
            VALUES(?,?,?,?)
        """, name, description, price, guestLimit);
    }

    public boolean deletePackage(int id) {
        return execute(
                "DELETE FROM catering_packages WHERE id=?", id);
    }

    // ================= ORDERS =================

    public boolean createOrder(int customerId, int foodId, int quantity) {

        if (quantity <= 0) return false;

        String foodSql = """
            SELECT price FROM foods
            WHERE id=? AND available=1
        """;

        String orderSql = """
            INSERT INTO orders(customer_id,total_amount,status)
            VALUES(?,?,'PENDING')
        """;

        try (Connection conn = DatabaseConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {
                double price;

                try (PreparedStatement ps =
                             conn.prepareStatement(foodSql)) {

                    ps.setInt(1, foodId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return false;
                        }
                        price = rs.getDouble("price");
                    }
                }

                double total = price * quantity;
                int orderId;

                try (PreparedStatement ps = conn.prepareStatement(
                        orderSql, Statement.RETURN_GENERATED_KEYS)) {

                    ps.setInt(1, customerId);
                    ps.setDouble(2, total);
                    ps.executeUpdate();

                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return false;
                        }
                        orderId = rs.getInt(1);
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement("""
                    INSERT INTO order_items
                    (order_id,food_id,quantity,unit_price,subtotal)
                    VALUES(?,?,?,?,?)
                """)) {

                    ps.setInt(1, orderId);
                    ps.setInt(2, foodId);
                    ps.setInt(3, quantity);
                    ps.setDouble(4, price);
                    ps.setDouble(5, total);
                    ps.executeUpdate();
                }

                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.out.println("Order error: " + e.getMessage());
            return false;
        }
    }

    public void viewOrders() {
        display("""
            SELECT o.id,u.username,o.order_date,
                   o.total_amount,o.status
            FROM orders o
            JOIN users u ON o.customer_id=u.id
            ORDER BY o.id DESC
        """, "ALL ORDERS");
    }

    public void viewCustomerOrders(int customerId) {
        display("""
            SELECT id,order_date,total_amount,status
            FROM orders WHERE customer_id=?
            ORDER BY id DESC
        """, "MY ORDERS", customerId);
    }

    public boolean updateOrderStatus(int id, String status) {

        if (!status.matches(
                "PENDING|CONFIRMED|PREPARING|READY|COMPLETED|CANCELLED")) {
            return false;
        }

        return execute(
                "UPDATE orders SET status=? WHERE id=?", status, id);
    }

    // ================= RESERVATIONS =================

    public boolean createReservation(
            int customerId,
            int packageId,
            String eventName,
            String eventDate,
            String eventTime,
            String location,
            int guests) {

        if (guests <= 0) return false;

        String packageSql = """
            SELECT price,guest_limit FROM catering_packages
            WHERE id=? AND available=1
        """;

        try (Connection conn = DatabaseConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {
                double price;
                int limit;

                try (PreparedStatement ps =
                             conn.prepareStatement(packageSql)) {

                    ps.setInt(1, packageId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return false;
                        }

                        price = rs.getDouble("price");
                        limit = rs.getInt("guest_limit");
                    }
                }

                if (guests > limit) {
                    conn.rollback();
                    System.out.println(
                            "Guest count exceeds package limit: " + limit);
                    return false;
                }

                try (PreparedStatement ps = conn.prepareStatement("""
                    INSERT INTO reservations
                    (customer_id,package_id,event_name,event_date,
                     event_time,event_location,guest_count,total_amount,status)
                    VALUES(?,?,?,?,?,?,?,?, 'PENDING')
                """)) {

                    ps.setInt(1, customerId);
                    ps.setInt(2, packageId);
                    ps.setString(3, eventName);
                    ps.setString(4, eventDate);
                    ps.setString(5, eventTime);
                    ps.setString(6, location);
                    ps.setInt(7, guests);
                    ps.setDouble(8, price);

                    ps.executeUpdate();
                }

                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.out.println("Reservation error: " + e.getMessage());
            return false;
        }
    }

    public void viewReservations() {
        display("""
            SELECT r.id,u.username,r.event_name,r.event_date,
                   r.event_time,r.event_location,r.guest_count,
                   r.total_amount,r.status
            FROM reservations r
            JOIN users u ON r.customer_id=u.id
            ORDER BY r.id DESC
        """, "RESERVATIONS");
    }

    public void viewCustomerReservations(int customerId) {
        display("""
            SELECT id,event_name,event_date,event_time,
                   event_location,guest_count,total_amount,status
            FROM reservations WHERE customer_id=?
            ORDER BY id DESC
        """, "MY RESERVATIONS", customerId);
    }

    public boolean updateReservationStatus(int id, String status) {

        if (!status.matches(
                "PENDING|CONFIRMED|CANCELLED|COMPLETED")) {
            return false;
        }

        return execute(
                "UPDATE reservations SET status=? WHERE id=?",
                status, id);
    }

    // ================= PAYMENTS =================

    public boolean recordOrderPayment(
            int orderId, double amount, String method) {

        if (amount <= 0) return false;

        return execute("""
            INSERT INTO payments(order_id,amount,payment_method,status)
            SELECT id,?,?, 'PAID'
            FROM orders WHERE id=?
        """, amount, method, orderId);
    }

    public boolean recordReservationPayment(
            int reservationId, double amount, String method) {

        if (amount <= 0) return false;

        return execute("""
            INSERT INTO payments
            (reservation_id,amount,payment_method,status)
            SELECT id,?,?, 'PAID'
            FROM reservations WHERE id=?
        """, amount, method, reservationId);
    }

    public void viewPayments() {
        display("""
            SELECT p.id,
                   COALESCE(p.order_id,0) AS order_id,
                   COALESCE(p.reservation_id,0) AS reservation_id,
                   p.amount,p.payment_method,
                   p.payment_date,p.status
            FROM payments p ORDER BY p.id DESC
        """, "PAYMENT RECORDS");
    }

    public void viewCustomerPayments(int customerId) {
        display("""
            SELECT p.id,p.order_id,p.reservation_id,
                   p.amount,p.payment_method,p.payment_date,p.status
            FROM payments p
            LEFT JOIN orders o ON p.order_id=o.id
            LEFT JOIN reservations r ON p.reservation_id=r.id
            WHERE o.customer_id=? OR r.customer_id=?
            ORDER BY p.id DESC
        """, "MY PAYMENTS", customerId, customerId);
    }

    // ================= INVENTORY =================

    public boolean addInventory(
            String name, double quantity,
            String unit, double minimumStock) {

        if (quantity < 0 || minimumStock < 0) return false;

        return execute("""
            INSERT INTO inventory(item_name,quantity,unit,minimum_stock)
            VALUES(?,?,?,?)
            ON CONFLICT(item_name) DO UPDATE SET
            quantity=excluded.quantity,
            unit=excluded.unit,
            minimum_stock=excluded.minimum_stock
        """, name, quantity, unit, minimumStock);
    }

    public boolean updateInventory(int id, double quantity) {

        if (quantity < 0) return false;

        return execute(
                "UPDATE inventory SET quantity=? WHERE id=?",
                quantity, id);
    }

    public void viewInventory() {
        display("""
            SELECT id,item_name,quantity,unit,minimum_stock,
                   CASE WHEN quantity <= minimum_stock
                   THEN 'LOW STOCK' ELSE 'AVAILABLE' END AS stock_status
            FROM inventory ORDER BY id
        """, "INVENTORY");
    }

    // ================= REPORTS =================

    public void salesReport() {

        display("""
            SELECT COUNT(*) AS total_orders,
                   COALESCE(SUM(total_amount),0) AS total_order_value
            FROM orders
            WHERE status != 'CANCELLED'
        """, "ORDER SALES REPORT");

        display("""
            SELECT COUNT(*) AS total_payments,
                   COALESCE(SUM(amount),0) AS total_collected
            FROM payments WHERE status='PAID'
        """, "PAYMENT REPORT");

        display("""
            SELECT COUNT(*) AS total_reservations
            FROM reservations
            WHERE status != 'CANCELLED'
        """, "RESERVATION REPORT");
    }

    // ================= DATABASE HELPERS =================

    private boolean execute(String sql, Object... values) {

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < values.length; i++) {
                ps.setObject(i + 1, values[i]);
            }

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return false;
        }
    }

    private void display(String sql, String title, Object... values) {

        System.out.println("\n========== " + title + " ==========");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < values.length; i++) {
                ps.setObject(i + 1, values[i]);
            }

            try (ResultSet rs = ps.executeQuery()) {

                ResultSetMetaData meta = rs.getMetaData();
                int columns = meta.getColumnCount();

                for (int i = 1; i <= columns; i++) {
                    System.out.printf("%-22s", meta.getColumnLabel(i));
                }

                System.out.println();

                while (rs.next()) {
                    for (int i = 1; i <= columns; i++) {
                        System.out.printf("%-22s",
                                rs.getString(i));
                    }
                    System.out.println();
                }
            }

        } catch (SQLException e) {
            System.out.println("Display error: " + e.getMessage());
        }
    }
}
