package com.irctc.service;

import com.irctc.model.BerthType;
import com.irctc.model.Passenger;
import com.irctc.model.Seat;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the train's physical seat layout using a 2D Array Matrix:
 * layout[coachIndex][seatIndex]
 * Directly satisfies Syllabus Lecture 2 (Multi-Dimensional Array Handling).
 */
public class TrainCoachMatrix {
    private final String[] coachNames;
    private final int seatsPerCoach;
    private final Seat[][] layout; // 2D Array Matrix

    public TrainCoachMatrix(int coachCount, int seatsPerCoach) {
        this.coachNames = new String[coachCount];
        for (int i = 0; i < coachCount; i++) {
            this.coachNames[i] = "B" + (i + 1); // e.g. B1, B2
        }
        this.seatsPerCoach = seatsPerCoach;
        this.layout = new Seat[coachCount][seatsPerCoach];
        initializeMatrix();
    }

    private void initializeMatrix() {
        for (int i = 0; i < coachNames.length; i++) {
            for (int j = 0; j < seatsPerCoach; j++) {
                int seatNumber = j + 1;
                BerthType berthType = BerthType.getBerthForSeatNumber(seatNumber);
                layout[i][j] = new Seat(coachNames[i], seatNumber, berthType);
            }
        }
    }

    public int getTotalSeats() {
        return coachNames.length * seatsPerCoach;
    }

    public synchronized int getAvailableSeatCount() {
        int count = 0;
        for (int i = 0; i < coachNames.length; i++) {
            for (int j = 0; j < seatsPerCoach; j++) {
                if (!layout[i][j].isBooked()) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Searches and atomically allocates a berth in the 2D array.
     * Prioritizes LOWER berth for Senior Citizens (age >= 60).
     */
    public synchronized Seat allocateBestBerth(Passenger passenger) {
        // Priority 1: Senior Citizens receive LOWER berth
        if (passenger.isSeniorCitizen()) {
            for (int i = 0; i < coachNames.length; i++) {
                for (int j = 0; j < seatsPerCoach; j++) {
                    Seat seat = layout[i][j];
                    if (!seat.isBooked() && seat.getBerthType() == BerthType.LOWER) {
                        if (seat.allocate(passenger)) {
                            return seat;
                        }
                    }
                }
            }
        }

        // Priority 2: Check for passenger's preferred berth
        BerthType pref = passenger.getPreferredBerth();
        for (int i = 0; i < coachNames.length; i++) {
            for (int j = 0; j < seatsPerCoach; j++) {
                Seat seat = layout[i][j];
                if (!seat.isBooked() && seat.getBerthType() == pref) {
                    if (seat.allocate(passenger)) {
                        return seat;
                    }
                }
            }
        }

        // Priority 3: Fallback to any vacant berth
        for (int i = 0; i < coachNames.length; i++) {
            for (int j = 0; j < seatsPerCoach; j++) {
                Seat seat = layout[i][j];
                if (!seat.isBooked()) {
                    if (seat.allocate(passenger)) {
                        return seat;
                    }
                }
            }
        }

        return null; // All matrix berths are occupied
    }

    public synchronized boolean releaseSeat(String coachId, int seatNumber) {
        for (int i = 0; i < coachNames.length; i++) {
            if (coachNames[i].equalsIgnoreCase(coachId)) {
                int col = seatNumber - 1;
                if (col >= 0 && col < seatsPerCoach) {
                    layout[i][col].release();
                    return true;
                }
            }
        }
        return false;
    }

    public List<Seat> getAllSeats() {
        List<Seat> list = new ArrayList<>();
        for (int i = 0; i < coachNames.length; i++) {
            for (int j = 0; j < seatsPerCoach; j++) {
                list.add(layout[i][j]);
            }
        }
        return list;
    }

    public synchronized void printMatrixView() {
        System.out.println("\n+-----------------------------------------------------------------+");
        System.out.println("|           2D COACH-BERTH SEAT ALLOCATION MATRIX                 |");
        System.out.println("+-----------------------------------------------------------------+");
        for (int i = 0; i < coachNames.length; i++) {
            System.out.printf("  Coach [%s]:\n   ", coachNames[i]);
            for (int j = 0; j < seatsPerCoach; j++) {
                Seat s = layout[i][j];
                String status = s.isBooked() ? "[X]" : "[O]";
                System.out.printf(" %02d-%s:%s ", s.getSeatNumber(), s.getBerthType().getCode(), status);
                if ((j + 1) % 4 == 0 && j < seatsPerCoach - 1) {
                    System.out.print("\n   ");
                }
            }
            System.out.println("\n");
        }
        System.out.println("  Legend: [O] = Vacant | [X] = Booked");
        System.out.println("+-----------------------------------------------------------------+\n");
    }
}
