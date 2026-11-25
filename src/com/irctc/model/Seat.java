package com.irctc.model;

/**
 * Represents an individual berth inside a train coach.
 * Provides thread-safe atomic allocate and release methods.
 */
public class Seat {
    private final String coachId;  // e.g. "B1", "B2"
    private final int seatNumber;  // 1 to 72 (or demo count)
    private final BerthType berthType;
    private boolean booked;
    private Passenger allocatedPassenger;

    public Seat(String coachId, int seatNumber, BerthType berthType) {
        this.coachId = coachId;
        this.seatNumber = seatNumber;
        this.berthType = berthType;
        this.booked = false;
        this.allocatedPassenger = null;
    }

    public String getCoachId() {
        return coachId;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public BerthType getBerthType() {
        return berthType;
    }

    public synchronized boolean isBooked() {
        return booked;
    }

    public synchronized Passenger getAllocatedPassenger() {
        return allocatedPassenger;
    }

    /**
     * Atomically allocates this seat to a passenger.
     */
    public synchronized boolean allocate(Passenger passenger) {
        if (this.booked) {
            return false;
        }
        this.booked = true;
        this.allocatedPassenger = passenger;
        return true;
    }

    /**
     * Atomically releases this seat on cancellation.
     */
    public synchronized Passenger release() {
        if (!this.booked) {
            return null;
        }
        Passenger previous = this.allocatedPassenger;
        this.booked = false;
        this.allocatedPassenger = null;
        return previous;
    }

    @Override
    public String toString() {
        return String.format("[%s-%02d %s: %s]",
            coachId, seatNumber, berthType.getCode(), booked ? "OCCUPIED" : "VACANT");
    }
}
