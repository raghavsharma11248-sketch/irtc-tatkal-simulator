package com.irctc;

import com.irctc.database.TicketDao;
import com.irctc.exception.InvalidPNRException;
import com.irctc.exception.TatkalException;
import com.irctc.model.BerthType;
import com.irctc.model.Passenger;
import com.irctc.model.Ticket;
import com.irctc.service.AnalyticsService;
import com.irctc.service.BookingEngine;
import com.irctc.service.FileService;
import com.irctc.service.TrainCoachMatrix;
import com.irctc.simulation.TatkalRushSimulator;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Main Application Runner & Interactive Console Switchboard.
 * Connects all system components via Constructor-Based Dependency Injection.
 * Directly satisfies Syllabus Units I to VI (Console CLI, Exception Handling, Collections,
 * Multithreading, JDBC CRUD, Stream API & File I/O).
 */
public class Main {

    public static void main(String[] args) {
        // ===================================================================
        // 🔌 CONSTRUCTOR DEPENDENCY INJECTION WIRING (System Initialization)
        // ===================================================================
        // 1. Initialize 2D Coach Matrix (2 Coaches: B1 & B2, 6 seats each = 12 Confirmed Berths)
        TrainCoachMatrix coachMatrix = new TrainCoachMatrix(2, 6);

        // 2. Initialize Database DAO (JDBC Architecture & PreparedStatement)
        TicketDao ticketDao = new TicketDao();

        // 3. Inject Matrix & DAO into BookingEngine via Constructor!
        BookingEngine bookingEngine = new BookingEngine(coachMatrix, ticketDao);

        // 4. Initialize Stream API & File Handling Services
        AnalyticsService analyticsService = new AnalyticsService();
        FileService fileService = new FileService();

        // 5. Initialize Multi-Threaded Stress Test Simulator
        TatkalRushSimulator rushSimulator = new TatkalRushSimulator(bookingEngine);

        // Pre-create sample CSV template for instant testing
        String sampleCsvPath = "passengers_sample.csv";
        fileService.generateSamplePassengersCsv(sampleCsvPath);

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        System.out.println("\n*******************************************************************");
        System.out.println("  WELCOME TO IRCTC HIGH-CONCURRENCY TATKAL RESERVATION SIMULATOR   ");
        System.out.println("      (Core Java Console Application - Chitkara University)        ");
        System.out.println("*******************************************************************");

        while (running) {
            printMenu();
            System.out.print("👉 Enter your choice (1-10): ");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1" -> {
                    // View 2D Coach-Berth Layout & Inventory
                    coachMatrix.printMatrixView();
                    bookingEngine.printCurrentInventory();
                }
                case "2" -> {
                    // Book Individual Tatkal Ticket
                    handleIndividualBooking(scanner, bookingEngine);
                }
                case "3" -> {
                    // Search PNR Details (JDBC Query)
                    handlePnrSearch(scanner, ticketDao);
                }
                case "4" -> {
                    // Cancel Ticket (Demonstrating wait/notify auto-upgrade)
                    handleCancellation(scanner, bookingEngine);
                }
                case "5" -> {
                    // Run 10:00 AM High-Concurrency Tatkal Rush Simulation
                    rushSimulator.runSimulation();
                }
                case "6" -> {
                    // Generate Reservation Chart & Analytics (Stream API)
                    List<Ticket> allTickets = ticketDao.findAll();
                    analyticsService.printComprehensiveReport(allTickets);
                }
                case "7" -> {
                    // Export Reservation Chart to CSV (File I/O)
                    List<Ticket> tickets = ticketDao.findAll();
                    String exportPath = "tatkal_reservation_chart.csv";
                    fileService.exportReservationChartToCsv(tickets, exportPath);
                }
                case "8" -> {
                    // Import Passengers from CSV & Batch Book
                    handleCsvImportAndBooking(scanner, fileService, bookingEngine);
                }
                case "9" -> {
                    // Toggle Tatkal Window (To test TatkalWindowClosedException)
                    boolean current = bookingEngine.isTatkalWindowOpen();
                    bookingEngine.setTatkalWindowOpen(!current);
                    System.out.println("\n[SYSTEM NOTICE] Tatkal 10:00 AM Window status changed to: " + 
                        (bookingEngine.isTatkalWindowOpen() ? "OPEN ✅" : "CLOSED ❌"));
                }
                case "10" -> {
                    System.out.println("\n[EXIT] Thank you for using IRCTC Tatkal Simulator. Exiting...");
                    running = false;
                }
                default -> System.out.println("\n⚠️ Invalid selection! Please enter a number between 1 and 10.");
            }
        }

        scanner.close();
    }

    private static void printMenu() {
        System.out.println("\n======================= CONSOLE MAIN MENU =======================");
        System.out.println("  1.  View 2D Coach-Berth Seat Layout & Real-Time Inventory");
        System.out.println("  2.  Book Individual Tatkal Ticket (Live User Input)");
        System.out.println("  3.  Search PNR Status (JDBC Database Query & ResultSet Mapping)");
        System.out.println("  4.  Cancel Ticket (Triggers wait/notify Auto-Upgrade Demo)");
        System.out.println("  5.  🚀 Run 10:00 AM Concurrent Tatkal Rush Simulation (Threads)");
        System.out.println("  6.  Generate Reservation Chart & Analytics (Stream API)");
        System.out.println("  7.  Export Reservation Chart to CSV File (File Handling)");
        System.out.println("  8.  Import Passengers from CSV & Batch Book (File + Collections)");
        System.out.println("  9.  Toggle Tatkal Window (Open/Close for Exception Demo)");
        System.out.println("  10. Exit Application");
        System.out.println("=================================================================");
    }

    private static void handleIndividualBooking(Scanner scanner, BookingEngine bookingEngine) {
        System.out.println("\n--- [INDIVIDUAL TATKAL TICKET BOOKING] ---");
        try {
            System.out.print("Enter Passenger Name: ");
            String name = scanner.nextLine().trim();

            System.out.print("Enter Passenger Age: ");
            int age = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Enter Gender (M/F/O): ");
            String gender = scanner.nextLine().trim();

            System.out.println("Choose Berth Preference:");
            System.out.println("  1. LOWER | 2. MIDDLE | 3. UPPER | 4. SIDE_LOWER | 5. SIDE_UPPER");
            System.out.print("Choice (1-5): ");
            String choice = scanner.nextLine().trim();

            BerthType pref = switch (choice) {
                case "1" -> BerthType.LOWER;
                case "2" -> BerthType.MIDDLE;
                case "3" -> BerthType.UPPER;
                case "4" -> BerthType.SIDE_LOWER;
                case "5" -> BerthType.SIDE_UPPER;
                default -> BerthType.LOWER;
            };

            Passenger p = new Passenger(name, age, gender, pref);
            Ticket ticket = bookingEngine.bookTicket(p);

            System.out.println("\n🎉 BOOKING SUCCESSFUL! Here is your e-Ticket receipt:");
            System.out.println(ticket.getDetailedSummary());

        } catch (NumberFormatException e) {
            System.err.println("❌ Invalid age format! Please enter numeric digits.");
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Validation Error: " + e.getMessage());
        } catch (TatkalException e) {
            System.err.println("❌ Tatkal Error: " + e.getMessage());
        }
    }

    private static void handlePnrSearch(Scanner scanner, TicketDao ticketDao) {
        System.out.println("\n--- [JDBC PNR STATUS SEARCH] ---");
        System.out.print("Enter 10-digit PNR Number: ");
        String pnr = scanner.nextLine().trim();

        Optional<Ticket> ticketOpt = ticketDao.findById(pnr);
        if (ticketOpt.isPresent()) {
            System.out.println("\n[RECORD RETRIEVED FROM DATABASE]:");
            System.out.println(ticketOpt.get().getDetailedSummary());
        } else {
            System.err.println("❌ PNR " + pnr + " not found in the reservation ledger.");
        }
    }

    private static void handleCancellation(Scanner scanner, BookingEngine bookingEngine) {
        System.out.println("\n--- [TICKET CANCELLATION & AUTO-PROMOTION DEMO] ---");
        System.out.print("Enter PNR Number to Cancel: ");
        String pnr = scanner.nextLine().trim();

        try {
            Ticket cancelled = bookingEngine.cancelTicket(pnr);
            System.out.printf("✅ PNR %s has been successfully cancelled. Refund processed.\n", cancelled.getPnr());
        } catch (InvalidPNRException e) {
            System.err.println("❌ Cancellation Failed: " + e.getMessage());
        }
    }

    private static void handleCsvImportAndBooking(Scanner scanner, FileService fileService, BookingEngine bookingEngine) {
        System.out.println("\n--- [CSV IMPORT & BATCH BOOKING] ---");
        System.out.print("Enter CSV File Path (Press ENTER for default 'passengers_sample.csv'): ");
        String path = scanner.nextLine().trim();
        if (path.isEmpty()) {
            path = "passengers_sample.csv";
        }

        List<Passenger> imported = fileService.importPassengersFromCsv(path);
        if (imported.isEmpty()) {
            System.out.println("No valid passenger records to book.");
            return;
        }

        System.out.printf("Starting batch booking for %d passengers from CSV...\n", imported.size());
        for (Passenger p : imported) {
            try {
                Ticket t = bookingEngine.bookTicket(p);
                System.out.printf("  ✓ Booked: %-18s -> PNR: %s [%s: %s]\n",
                    p.getName(), t.getPnr(), t.getStatus(), t.getSeatDescription());
            } catch (TatkalException e) {
                System.out.printf("  ✗ Failed: %-18s -> %s\n", p.getName(), e.getMessage());
            }
        }
    }
}
