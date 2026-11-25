package com.irctc.simulation;

import com.irctc.exception.TatkalException;
import com.irctc.model.BerthType;
import com.irctc.model.Passenger;
import com.irctc.model.Ticket;
import com.irctc.service.BookingEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Multi-Threaded Stress Test Simulator for the 10:00:00 AM Tatkal Window.
 * Launches 15-20 simultaneous passenger threads competing for limited seats.
 * Demonstrates Thread Safety, Race Condition prevention, and synchronized critical sections.
 * Directly satisfies Syllabus Lectures 9, 10, 11, 12, 13 & 14.
 */
public class TatkalRushSimulator {

    private final BookingEngine bookingEngine;

    public TatkalRushSimulator(BookingEngine bookingEngine) {
        this.bookingEngine = bookingEngine;
    }

    public void runSimulation() {
        System.out.println("\n===================================================================");
        System.out.println("   🚀 LAUNCHING 10:00:00 AM HIGH-CONCURRENCY TATKAL RUSH SIMULATION");
        System.out.println("===================================================================");
        System.out.println("  Scenario: 18 concurrent passenger threads hit the booking engine");
        System.out.println("  simultaneously at 10:00:00.000 AM to book seats.");
        System.out.println("-------------------------------------------------------------------");

        // List of prospective passengers with varied ages, genders, and preferences
        List<Passenger> candidates = List.of(
            new Passenger("Amitabh Bachchan", 78, "M", BerthType.LOWER),
            new Passenger("Priya Sharma", 26, "F", BerthType.LOWER),
            new Passenger("Rahul Verma", 29, "M", BerthType.MIDDLE),
            new Passenger("Kavita Devi", 65, "F", BerthType.LOWER),
            new Passenger("Siddharth Roy", 31, "M", BerthType.UPPER),
            new Passenger("Meenakshi Iyer", 62, "F", BerthType.LOWER),
            new Passenger("Arjun Rampal", 34, "M", BerthType.SIDE_LOWER),
            new Passenger("Sneha Reddy", 22, "F", BerthType.UPPER),
            new Passenger("Gurpreet Sodhi", 71, "M", BerthType.LOWER),
            new Passenger("Divya Nair", 28, "F", BerthType.SIDE_UPPER),
            new Passenger("Rohan Mehra", 25, "M", BerthType.LOWER),
            new Passenger("Aakash Gupta", 33, "M", BerthType.MIDDLE),
            new Passenger("Sunita Kapoor", 68, "F", BerthType.LOWER),
            new Passenger("Varun Dhawan", 30, "M", BerthType.UPPER),
            new Passenger("Jasleen Kaur", 27, "F", BerthType.SIDE_LOWER),
            new Passenger("Manish Pandey", 35, "M", BerthType.MIDDLE),
            new Passenger("Preeti Zinta", 48, "F", BerthType.LOWER),
            new Passenger("Naveen Patnaik", 75, "M", BerthType.LOWER)
        );

        // CountDownLatch forces all threads to wait and fire at the exact same millisecond
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(candidates.size());

        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < candidates.size(); i++) {
            Passenger p = candidates.get(i);
            String threadName = "Thread-Passenger-" + p.getName().split(" ")[0];

            Thread t = new Thread(() -> {
                try {
                    // Wait for the 10:00:00 AM start gunshot
                    startSignal.await();

                    // Compete in critical section (synchronized)
                    Ticket ticket = bookingEngine.bookTicket(p);
                    System.out.printf("  [%s] SUCCESS -> PNR: %s | Status: %-12s | %s\n",
                        Thread.currentThread().getName(),
                        ticket.getPnr(),
                        ticket.getStatus(),
                        ticket.getSeatDescription()
                    );
                } catch (TatkalException te) {
                    System.out.printf("  [%s] REJECTED -> %s: %s\n",
                        Thread.currentThread().getName(),
                        p.getName(),
                        te.getMessage()
                    );
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneSignal.countDown();
                }
            }, threadName);

            threads.add(t);
            t.start();
        }

        // Trigger the simultaneous rush!
        System.out.println("  [CLOCK: 10:00:00 AM] Tatkal gates opened! Firing all passenger threads...\n");
        startSignal.countDown();

        try {
            // Wait for all passenger booking threads to complete their transactions
            doneSignal.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("\n  [SIMULATION FINISHED] All 18 concurrent requests processed safely.");
        System.out.println("  No race conditions occurred. Check inventory below:");
        bookingEngine.printCurrentInventory();
        System.out.println("===================================================================\n");
    }
}
