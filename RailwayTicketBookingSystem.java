package RideRail;

import java.sql.*;
import java.util.Scanner;

public class RailwayTicketBookingSystem {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n--- Railway Ticket Booking System ---");
            System.out.println("1. View Available Trains");
            System.out.println("2. Book Ticket");
            System.out.println("3. View Booked Tickets");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");
            int choice = sc.nextInt();
            switch (choice) {
                case 1:
                    viewTrains();
                    break;
                case 2:
                    bookTicket();
                    break;
                case 3:
                    viewBookedTickets();
                    break;
                case 4:
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid choice! Please try again.");
            }
        }
    }
    public static void viewTrains() {
        try (Connection con = DBConnection.getConnection()) {
            String query = "SELECT * FROM trains WHERE seats_available > 0";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            System.out.println("\nTrain ID | Train Name     | Source        | Destination    | Seats Available");
            System.out.println("---------------------------------------------------------------");
            while (rs.next()) {
                System.out.println(rs.getInt("train_id") + " | " +
                                   rs.getString("train_name") + " | " +
                                   rs.getString("source") + " | " +
                                   rs.getString("destination") + " | " +
                                   rs.getInt("seats_available"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void bookTicket() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter your name: ");
        String name = sc.nextLine();
        System.out.print("Enter your email: ");
        String email = sc.nextLine();
        System.out.print("Enter your phone: ");
        String phone = sc.nextLine();

        try (Connection con = DBConnection.getConnection()) {
            // Insert user details
            String userInsertQuery = "INSERT INTO users (name, email, phone) VALUES (?, ?, ?)";
            PreparedStatement userStmt = con.prepareStatement(userInsertQuery, Statement.RETURN_GENERATED_KEYS);
            userStmt.setString(1, name);
            userStmt.setString(2, email);
            userStmt.setString(3, phone);
            userStmt.executeUpdate();

            ResultSet userRs = userStmt.getGeneratedKeys();
            int userId = 0;
            if (userRs.next()) {
                userId = userRs.getInt(1);
            }

            // Display available trains
            System.out.println("\n--- Available Trains ---");
            viewTrains();

            System.out.print("Enter Train ID to book: ");
            int trainId = sc.nextInt();

            // Insert booking details
            String bookingQuery = "INSERT INTO bookings (user_id, train_id, booking_date) VALUES (?, ?, NOW())";
            PreparedStatement bookingStmt = con.prepareStatement(bookingQuery);
            bookingStmt.setInt(1, userId);
            bookingStmt.setInt(2, trainId);
            bookingStmt.executeUpdate();

            // Update seats
            String updateSeatsQuery = "UPDATE trains SET seats_available = seats_available - 1 WHERE train_id = ?";
            PreparedStatement updateStmt = con.prepareStatement(updateSeatsQuery);
            updateStmt.setInt(1, trainId);
            updateStmt.executeUpdate();

            System.out.println("Ticket booked successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void viewBookedTickets() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter your email: ");
        String email = sc.nextLine();

        try (Connection con = DBConnection.getConnection()) {
            String query = "SELECT b.booking_id, t.train_name, t.source, t.destination, b.booking_date " +
                           "FROM bookings b " +
                           "JOIN users u ON b.user_id = u.user_id " +
                           "JOIN trains t ON b.train_id = t.train_id " +
                           "WHERE u.email = ?";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\nBooking ID | Train Name     | Source        | Destination    | Booking Date");
            System.out.println("---------------------------------------------------------------");
            while (rs.next()) {
                System.out.println(rs.getInt("booking_id") + " | " +
                                   rs.getString("train_name") + " | " +
                                   rs.getString("source") + " | " +
                                   rs.getString("destination") + " | " +
                                   rs.getDate("booking_date"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


