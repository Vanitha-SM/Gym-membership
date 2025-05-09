import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App {
    static Scanner sc = new Scanner(System.in);
    private static final Logger logger = Logger.getLogger(App.class.getName());  // Logger instance

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n1. Add Member\n2. View All Members\n3. Make Payment\n4. View Payments\n5. Check-In\n6. Check-Out\n7. View Entry&Exit Tables\n8. View Plans\n9. Exit");
            int choice = sc.nextInt();
            sc.nextLine(); 

            switch (choice) {
                case 1 -> addMember();
                case 2 -> viewMembers();
                case 3 -> makePayment();
                case 4 -> viewPayments();
                case 5 -> checkIn();
                case 6 -> checkOut();
                case 7 -> viewCheckInsAndOuts();
                case 8 -> viewPlans();
                case 9 -> System.exit(0);
            }
        }
    }

    static void addMember() {
        System.out.print("Name: ");
        String name = sc.nextLine();
        System.out.print("Phone: ");
        String phone = sc.nextLine();
        System.out.print("Plan ID (1 for Monthly, 2 for Annual, etc.): ");
        int planId = sc.nextInt();
        sc.nextLine(); 
        System.out.print("Start Date (YYYY-MM-DD): ");
        String start = sc.nextLine();
        System.out.print("End Date (YYYY-MM-DD): ");
        String end = sc.nextLine();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO members (name, phone, plan_id, start_date, end_date) VALUES (?, ?, ?, ?, ?)")) {

            stmt.setString(1, name);
            stmt.setString(2, phone);
            stmt.setInt(3, planId);
            stmt.setDate(4, Date.valueOf(start));
            stmt.setDate(5, Date.valueOf(end));
            stmt.executeUpdate();
            System.out.println("Member added successfully!");

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error adding member", e);  
        }
    }

    static void viewMembers() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM members")) {

            while (rs.next()) {
                System.out.println(rs.getInt("member_id") + " | " +
                        rs.getString("name") + " | " +
                        rs.getString("phone") + " | " +
                        rs.getDate("start_date") + " - " +
                        rs.getDate("end_date"));
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error viewing members", e);
        }
    }

    static void makePayment() {
        System.out.print("Member ID: ");
        int memberId = sc.nextInt();
        System.out.print("Amount: ");
        double amount = sc.nextDouble();
        sc.nextLine(); // consume newline
        System.out.print("Payment Method: ");
        String method = sc.nextLine();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO payments (member_id, amount, method) VALUES (?, ?, ?)")) {

            stmt.setInt(1, memberId);
            stmt.setDouble(2, amount);
            stmt.setString(3, method);

            stmt.executeUpdate();
            System.out.println("Payment recorded successfully!");

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error making payment", e);  
        }
    }

    static void checkIn() {
        System.out.print("Member ID: ");
        int memberId = sc.nextInt();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO checkins (member_id) VALUES (?)")) {

            stmt.setInt(1, memberId);
            stmt.executeUpdate();
            System.out.println("Member checked in successfully!");

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error checking in member", e);  
        }
    }

    static void checkOut() {
        System.out.print("Member ID: ");
        int memberId = sc.nextInt();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE checkins SET checkout_time = CURRENT_TIMESTAMP WHERE member_id = ? AND checkout_time IS NULL")) {

            stmt.setInt(1, memberId);
            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Member checked out successfully!");
            } else {
                System.out.println("No active check-in found for this member.");
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error checking out member", e);  
        }
    }

    static void viewPayments() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM payments")) {

            while (rs.next()) {
                System.out.println(rs.getInt("payment_id") + " | " +
                        rs.getInt("member_id") + " | " +
                        rs.getDouble("amount") + " | " +
                        rs.getString("method"));
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error viewing payments", e);  
        }
    }
    static void viewCheckInsAndOuts() {
    String query = """
        SELECT 
            c.checkin_id,
            m.name AS member_name,
            c.checkin_time,
            c.checkout_time
        FROM 
            checkins c
        JOIN 
            members m ON c.member_id = m.member_id
        ORDER BY 
            c.checkin_time DESC
        """;

    try (Connection conn = DBConnection.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(query)) {

        System.out.printf("%-5s %-20s %-25s %-25s%n", "ID", "Member Name", "Check-In", "Check-Out");
        System.out.println("--------------------------------------------------------------------------------------");

        while (rs.next()) {
            int id = rs.getInt("checkin_id");
            String name = rs.getString("member_name");
            Timestamp checkin = rs.getTimestamp("checkin_time");
            Timestamp checkout = rs.getTimestamp("checkout_time");

            System.out.printf("%-5d %-20s %-25s %-25s%n", 
                              id, name, checkin, 
                              checkout != null ? checkout.toString() : "Not checked out");
        }

    } catch (SQLException e) {
        logger.log(Level.SEVERE, "Error viewing check-ins and check-outs", e);
    }
}

    static void viewPlans() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM membership_plans")) {

            while (rs.next()) {
                System.out.println(rs.getInt("plan_id") + " | " +
                        rs.getString("plan_name") + " | " +
                        rs.getDouble("price"));
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error viewing plans", e);  
        }
    }
}
