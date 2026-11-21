package com.order;

import java.sql.*;
import java.util.Scanner;

public class OrderManagementSystem {

    static final String URL =
            "jdbc:mysql://localhost:3306/ecommerce_db";
    static final String USER = "root";
    static final String PASSWORD = "Pragyan@2006";

    static Connection con;
    static Scanner sc = new Scanner(System.in);

    // Database Connection
    static void connect() throws SQLException {
        con = DriverManager.getConnection(URL, USER, PASSWORD);
        System.out.println("Database Connected!");
    }

    // (a) Insert three orders
    static void insertOrders() throws SQLException {

        String sql = "INSERT INTO Orders VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            for (int i = 1; i <= 3; i++) {
                System.out.println("Enter Order ID:");
                int id = sc.nextInt();
                sc.nextLine();

                System.out.println("Customer Name:");
                String name = sc.nextLine();

                System.out.println("Product Name:");
                String product = sc.nextLine();

                System.out.println("Quantity:");
                int quantity = sc.nextInt();

                System.out.println("Unit Price:");
                double price = sc.nextDouble();
                sc.nextLine();

                System.out.println("Status:");
                String status = sc.nextLine();

                ps.setInt(1, id);
                ps.setString(2, name);
                ps.setString(3, product);
                ps.setInt(4, quantity);
                ps.setDouble(5, price);
                ps.setString(6, status);

                try {
                    ps.executeUpdate();
                    System.out.println("Order Inserted!");
                } catch (SQLIntegrityConstraintViolationException e) {
                    System.out.println("Duplicate Order ID!");
                }
            }
        }
    }

    // (b) Search orders
    static void searchOrders() throws SQLException {

        System.out.println("Enter Status:");
        String status = sc.nextLine();

        String sql = "SELECT * FROM Orders WHERE status=? " +
                "AND quantity > 2 ORDER BY unit_price DESC";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                System.out.println(
                        rs.getInt("order_id") + " " +
                                rs.getString("customer_name") + " " +
                                rs.getString("product_name") + " " +
                                rs.getInt("quantity") + " " +
                                rs.getDouble("unit_price") + " " +
                                rs.getString("status")
                );
            }
        }
    }

    // (c) Increase quantity
    static void increaseQuantity() throws SQLException {

        System.out.println("Enter Order ID:");
        int id = sc.nextInt();

        System.out.println("Enter Additional Quantity:");
        int qty = sc.nextInt();
        sc.nextLine();

        String sql = "UPDATE Orders SET quantity=quantity+? WHERE order_id=?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, qty);
            ps.setInt(2, id);

            int rows = ps.executeUpdate();

            if (rows > 0)
                System.out.println("Quantity Updated!");
            else
                System.out.println("Order Not Found!");
        }
    }

    // (d) Update status
    static void updateStatus() throws SQLException {

        System.out.println("Enter Order ID:");
        int id = sc.nextInt();
        sc.nextLine();

        String check = "SELECT * FROM Orders WHERE order_id=?";

        try (PreparedStatement ps = con.prepareStatement(check)) {
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                System.out.println("Order Not Found!");
                return;
            }
        }

        System.out.println("Enter New Status:");
        String status = sc.nextLine();

        if (!status.equalsIgnoreCase("Pending") &&
                !status.equalsIgnoreCase("Shipped") &&
                !status.equalsIgnoreCase("Delivered")) {
            System.out.println("Invalid Status!");
            return;
        }

        String sql = "UPDATE Orders SET status=? WHERE order_id=?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        }

        System.out.println("Updated Order:");

        try (PreparedStatement ps = con.prepareStatement(
                "SELECT * FROM Orders WHERE order_id=?")) {
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                System.out.println(
                        rs.getInt("order_id") + " " +
                                rs.getString("customer_name") + " " +
                                rs.getString("product_name") + " " +
                                rs.getInt("quantity") + " " +
                                rs.getDouble("unit_price") + " " +
                                rs.getString("status")
                );
            }
        }
    }

    // (e) Calculate total and delete cancelled order
    static void calculateAndDelete() throws SQLException {

        String sum = "SELECT SUM(quantity*unit_price) AS total FROM Orders";

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sum)) {

            if (rs.next())
                System.out.println("Total Order Value: " +
                        rs.getDouble("total"));
        }

        System.out.println("Enter Order ID to Delete:");
        int id = sc.nextInt();
        sc.nextLine();

        String sql = "DELETE FROM Orders " +
                "WHERE order_id=? AND status='Cancelled'";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);

            int rows = ps.executeUpdate();

            if (rows > 0)
                System.out.println("Order Deleted!");
            else
                System.out.println("Cannot Delete: Not Found or Not Cancelled!");
        }
    }

    // Main Menu
    public static void main(String[] args) {

        try {
            connect();

            int choice;

            do {
                System.out.println("\n===== ORDER MANAGEMENT =====");
                System.out.println("1. Insert Orders");
                System.out.println("2. Search Orders");
                System.out.println("3. Increase Quantity");
                System.out.println("4. Update Status");
                System.out.println("5. Total Value and Delete");
                System.out.println("0. Exit");

                System.out.print("Enter Choice: ");
                choice = sc.nextInt();
                sc.nextLine();

                switch (choice) {
                    case 1: insertOrders(); break;
                    case 2: searchOrders(); break;
                    case 3: increaseQuantity(); break;
                    case 4: updateStatus(); break;
                    case 5: calculateAndDelete(); break;
                    case 0: System.out.println("Goodbye!"); break;
                    default: System.out.println("Invalid Choice!");
                }

            } while (choice != 0);

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            try {
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
