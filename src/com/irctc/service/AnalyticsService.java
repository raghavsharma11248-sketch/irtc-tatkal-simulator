package com.irctc.service;

import com.irctc.model.BookingStatus;
import com.irctc.model.Ticket;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Analytics and Reporting Service powered by Java 8 Stream API and Lambda Expressions.
 * Directly satisfies Syllabus Lectures 21, 22 & 23:
 *  - Lambda syntax, functional interfaces, method references
 *  - Stream pipelines: filter(), map(), sorted(), distinct(), limit(), skip(), reduce(), collect()
 */
public class AnalyticsService {

    public void printComprehensiveReport(List<Ticket> tickets) {
        System.out.println("\n===================================================================");
        System.out.println("     🚆 IRCTC ANALYTICS & RESERVATION CHART (STREAM API ENGINE)    ");
        System.out.println("===================================================================");

        if (tickets.isEmpty()) {
            System.out.println("  No booking records found in database/ledger.");
            System.out.println("===================================================================\n");
            return;
        }

        // 1. Total Revenue via mapToDouble() and sum() [Terminal Reduction]
        double totalRevenue = tickets.stream()
            .filter(t -> t.getStatus() != BookingStatus.CANCELLED)
            .mapToDouble(Ticket::getTotalFare)
            .sum();

        // 2. Count by BookingStatus via groupingBy and counting
        Map<BookingStatus, Long> statusCount = tickets.stream()
            .collect(Collectors.groupingBy(Ticket::getStatus, Collectors.counting()));

        System.out.printf("  Total Bookings Processed : %d\n", tickets.size());
        System.out.printf("  Confirmed (CNF) Count    : %d\n", statusCount.getOrDefault(BookingStatus.CONFIRMED, 0L));
        System.out.printf("  RAC Count                : %d\n", statusCount.getOrDefault(BookingStatus.RAC, 0L));
        System.out.printf("  Waiting List (WL) Count  : %d\n", statusCount.getOrDefault(BookingStatus.WAITING_LIST, 0L));
        System.out.printf("  Cancelled Count          : %d\n", statusCount.getOrDefault(BookingStatus.CANCELLED, 0L));
        System.out.printf("  Total Active Revenue     : Rs. %.2f\n", totalRevenue);
        System.out.println("-------------------------------------------------------------------");

        // 3. Senior Citizens Filtered List via filter() [Lecture 23]
        List<Ticket> seniorCitizenTickets = tickets.stream()
            .filter(t -> t.getPassenger().isSeniorCitizen())
            .toList();

        System.out.println("  [Stream filter] Senior Citizens Booked (" + seniorCitizenTickets.size() + "):");
        if (seniorCitizenTickets.isEmpty()) {
            System.out.println("   None");
        } else {
            seniorCitizenTickets.forEach(t -> System.out.printf("   - PNR %s: %s, Age: %d -> %s\n",
                t.getPnr(), t.getPassenger().getName(), t.getPassenger().getAge(), t.getSeatDescription()));
        }
        System.out.println("-------------------------------------------------------------------");

        // 4. Distinct Berths Allotted via map() and distinct() [Lecture 23]
        List<String> distinctBerths = tickets.stream()
            .filter(t -> t.getStatus() == BookingStatus.CONFIRMED && t.getAllocatedSeat() != null)
            .map(t -> t.getAllocatedSeat().getBerthType().getDescription())
            .distinct()
            .toList();
        System.out.println("  [Stream map + distinct] Unique Berth Types In Use:");
        System.out.println("   " + String.join(", ", distinctBerths));
        System.out.println("-------------------------------------------------------------------");

        // 5. Sorted by Total Fare with limit(3) [Lecture 23: Top 3 Revenue Tickets]
        System.out.println("  [Stream sorted + limit(3)] Top Bookings by Fare:");
        tickets.stream()
            .sorted(Comparator.comparing(Ticket::getTotalFare).reversed())
            .limit(3)
            .forEach(t -> System.out.printf("   - PNR %s | %s | Fare: Rs. %.2f | Status: %s\n",
                t.getPnr(), t.getPassenger().getName(), t.getTotalFare(), t.getStatus()));
        System.out.println("-------------------------------------------------------------------");

        // 6. Pagination demo with skip() [Lecture 23: Skip first 2 tickets]
        if (tickets.size() > 2) {
            System.out.println("  [Stream skip(2)] Remaining Records (Pagination Demonstration):");
            tickets.stream()
                .skip(2)
                .limit(3)
                .forEach(t -> System.out.printf("   - PNR %s | Passenger: %s (%s)\n",
                    t.getPnr(), t.getPassenger().getName(), t.getStatus()));
            System.out.println("-------------------------------------------------------------------");
        }

        // 7. Final Confirmed Passenger Chart (Sorted by Coach and Seat Number)
        System.out.println("  OFFICIAL RESERVATION CHART (CONFIRMED PASSENGERS):");
        List<Ticket> confirmedList = tickets.stream()
            .filter(t -> t.getStatus() == BookingStatus.CONFIRMED && t.getAllocatedSeat() != null)
            .sorted(Comparator.comparing((Ticket t) -> t.getAllocatedSeat().getCoachId())
                .thenComparingInt(t -> t.getAllocatedSeat().getSeatNumber()))
            .toList();

        if (confirmedList.isEmpty()) {
            System.out.println("   (No confirmed passengers yet)");
        } else {
            System.out.printf("   %-12s %-20s %-6s %-8s %-12s\n", "PNR", "PASSENGER", "AGE", "GENDER", "BERTH");
            System.out.println("   -------------------------------------------------------------");
            for (Ticket t : confirmedList) {
                System.out.printf("   %-12s %-20s %-6d %-8s %-12s\n",
                    t.getPnr(), t.getPassenger().getName(), t.getPassenger().getAge(),
                    t.getPassenger().getGender(), t.getSeatDescription());
            }
        }
        System.out.println("===================================================================\n");
    }
}
