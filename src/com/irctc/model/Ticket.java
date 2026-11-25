package com.irctc.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Ticket entity representing a confirmed, RAC, or waitlisted railway reservation.
 * Connects Passenger and Seat models via Composition.
 * Implements Comparable to satisfy Syllabus Lecture 8 (Sorting & Natural Ordering).
 */
public class Ticket implements Comparable<Ticket> {
    private final String pnr;
    private final Passenger passenger;
    private final int trainNumber;
    private final String trainName;
    private Seat allocatedSeat;
    private BookingStatus status;
    private int racOrWlPosition;
    private final double baseFare;
    private final double tatkalSurcharge;
    private final LocalDateTime bookingTime;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Ticket(String pnr, Passenger passenger, int trainNumber, String trainName,
                  Seat allocatedSeat, BookingStatus status, int racOrWlPosition,
                  double baseFare, double tatkalSurcharge) {
        this.pnr = Objects.requireNonNull(pnr, "PNR cannot be null");
        this.passenger = Objects.requireNonNull(passenger, "Passenger cannot be null");
        this.trainNumber = trainNumber;
        this.trainName = trainName;
        this.allocatedSeat = allocatedSeat;
        this.status = status;
        this.racOrWlPosition = racOrWlPosition;
        this.baseFare = baseFare;
        this.tatkalSurcharge = tatkalSurcharge;
        this.bookingTime = LocalDateTime.now();
    }

    public String getPnr() {
        return pnr;
    }

    public Passenger getPassenger() {
        return passenger;
    }

    public int getTrainNumber() {
        return trainNumber;
    }

    public String getTrainName() {
        return trainName;
    }

    public synchronized Seat getAllocatedSeat() {
        return allocatedSeat;
    }

    public synchronized void setAllocatedSeat(Seat allocatedSeat) {
        this.allocatedSeat = allocatedSeat;
    }

    public synchronized BookingStatus getStatus() {
        return status;
    }

    public synchronized void setStatus(BookingStatus status) {
        this.status = status;
    }

    public synchronized int getRacOrWlPosition() {
        return racOrWlPosition;
    }

    public synchronized void setRacOrWlPosition(int racOrWlPosition) {
        this.racOrWlPosition = racOrWlPosition;
    }

    public double getBaseFare() {
        return baseFare;
    }

    public double getTatkalSurcharge() {
        return tatkalSurcharge;
    }

    public double getTotalFare() {
        return baseFare + tatkalSurcharge;
    }

    public LocalDateTime getBookingTime() {
        return bookingTime;
    }

    public String getFormattedBookingTime() {
        return bookingTime.format(FORMATTER);
    }

    @Override
    public int compareTo(Ticket other) {
        return this.bookingTime.compareTo(other.bookingTime);
    }

    public String getSeatDescription() {
        if (status == BookingStatus.CONFIRMED && allocatedSeat != null) {
            return String.format("%s-%02d (%s)", 
                allocatedSeat.getCoachId(), allocatedSeat.getSeatNumber(), allocatedSeat.getBerthType().getCode());
        } else if (status == BookingStatus.RAC) {
            return "RAC-" + racOrWlPosition;
        } else if (status == BookingStatus.WAITING_LIST) {
            return "WL-" + racOrWlPosition;
        } else {
            return "CANCELLED";
        }
    }

    public String getDetailedSummary() {
        return String.format(
            "===========================================================\n" +
            "  INDIAN RAILWAYS E-TICKET (TATKAL SCHEME)                 \n" +
            "===========================================================\n" +
            "  PNR Number       : %s\n" +
            "  Train Details    : #%d - %s\n" +
            "  Passenger Name   : %s (%d yrs, %s%s)\n" +
            "  Booking Status   : %s\n" +
            "  Allotted Berth   : %s\n" +
            "  Total Fare       : Rs. %.2f (Base: %.2f + Tatkal: %.2f)\n" +
            "  Booking Time     : %s\n" +
            "===========================================================",
            pnr, trainNumber, trainName,
            passenger.getName(), passenger.getAge(), passenger.getGender(),
            passenger.isSeniorCitizen() ? " - SR. CITIZEN" : "",
            status.getDescription(),
            getSeatDescription(),
            getTotalFare(), baseFare, tatkalSurcharge,
            getFormattedBookingTime()
        );
    }

    @Override
    public String toString() {
        return String.format("Ticket[PNR=%s, Status=%s, Passenger=%s, Seat=%s]",
            pnr, status, passenger.getName(), getSeatDescription());
    }
}
