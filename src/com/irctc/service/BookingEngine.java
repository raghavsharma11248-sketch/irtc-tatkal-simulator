package com.irctc.service;

import com.irctc.database.TicketDao;
import com.irctc.exception.InvalidPNRException;
import com.irctc.exception.SeatNotAvailableException;
import com.irctc.exception.TatkalException;
import com.irctc.exception.TatkalWindowClosedException;
import com.irctc.model.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Concurrency Booking Engine demonstrating Thread Synchronization and Inter-Thread Communication.
 * Injected with TrainCoachMatrix and TicketDao via Constructor Dependency Injection.
 * Directly satisfies Syllabus Lectures 5-8 (Collections, PriorityQueue, Comparator)
 * and Lectures 9-16 (Multithreading, Synchronized Critical Sections, wait() and notifyAll()).
 */
public class BookingEngine {

    private final int trainNumber = 12002;
    private final String trainName = "New Delhi Shatabdi Express";
    private final double baseFare = 850.00;
    private final double tatkalSurcharge = 250.00;
    private final int maxRacSeats = 4;
    private final int maxWaitingListSeats = 6;

    // Injected Dependencies
    private final TrainCoachMatrix coachMatrix;
    private final TicketDao ticketDao;

    // Synchronization Monitor Lock
    private final Object seatLock = new Object();
    private volatile boolean tatkalWindowOpen = true;

    // Collections Framework
    private final Queue<Ticket> racQueue = new LinkedList<>();
    private final PriorityQueue<Ticket> waitingListQueue;
    private final AtomicInteger pnrCounter = new AtomicInteger(1001);

    /**
     * Constructor-based Dependency Injection.
     */
    public BookingEngine(TrainCoachMatrix coachMatrix, TicketDao ticketDao) {
        this.coachMatrix = Objects.requireNonNull(coachMatrix, "CoachMatrix cannot be null");
        this.ticketDao = Objects.requireNonNull(ticketDao, "TicketDao cannot be null");

        // Custom Comparator for PriorityQueue (Syllabus Lecture 8)
        // Priority hierarchy: 1) Senior Citizens, 2) Female Passengers, 3) Booking timestamp
        Comparator<Ticket> waitlistComparator = (t1, t2) -> {
            Passenger p1 = t1.getPassenger();
            Passenger p2 = t2.getPassenger();

            if (p1.isSeniorCitizen() && !p2.isSeniorCitizen()) return -1;
            if (!p1.isSeniorCitizen() && p2.isSeniorCitizen()) return 1;

            if (p1.isFemale() && !p2.isFemale()) return -1;
            if (!p1.isFemale() && p2.isFemale()) return 1;

            return t1.compareTo(otherTicket(t2));
        };

        this.waitingListQueue = new PriorityQueue<>(waitlistComparator);
    }

    private Ticket otherTicket(Ticket t) {
        return t;
    }

    public boolean isTatkalWindowOpen() {
        return tatkalWindowOpen;
    }

    public void setTatkalWindowOpen(boolean open) {
        this.tatkalWindowOpen = open;
    }

    private String generatePNR() {
        return String.format("%d-%04d", trainNumber, pnrCounter.getAndIncrement());
    }

    /**
     * Atomic Ticket Booking Method.
     * Synchronized block prevents race conditions across concurrent booking threads.
     */
    public Ticket bookTicket(Passenger passenger) throws TatkalException {
        // Step 1: Validate 10:00 AM window
        if (!tatkalWindowOpen) {
            throw new TatkalWindowClosedException("Tatkal Window is currently CLOSED. Booking opens at 10:00:00 AM.");
        }

        // Step 2: Critical Section guarded by seatLock (Lecture 13-14)
        synchronized (seatLock) {
            String pnr = generatePNR();

            // Check 1: Confirmed Berth allocation via 2D Matrix
            Seat seat = coachMatrix.allocateBestBerth(passenger);
            if (seat != null) {
                Ticket ticket = new Ticket(
                    pnr, passenger, trainNumber, trainName,
                    seat, BookingStatus.CONFIRMED, 0,
                    baseFare, tatkalSurcharge
                );
                ticketDao.save(ticket);
                return ticket;
            }

            // Check 2: RAC Queue allocation
            if (racQueue.size() < maxRacSeats) {
                int racPos = racQueue.size() + 1;
                Ticket ticket = new Ticket(
                    pnr, passenger, trainNumber, trainName,
                    null, BookingStatus.RAC, racPos,
                    baseFare, tatkalSurcharge
                );
                racQueue.offer(ticket);
                ticketDao.save(ticket);
                return ticket;
            }

            // Check 3: Waiting List allocation (PriorityQueue)
            if (waitingListQueue.size() < maxWaitingListSeats) {
                int wlPos = waitingListQueue.size() + 1;
                Ticket ticket = new Ticket(
                    pnr, passenger, trainNumber, trainName,
                    null, BookingStatus.WAITING_LIST, wlPos,
                    baseFare, tatkalSurcharge
                );
                waitingListQueue.offer(ticket);
                ticketDao.save(ticket);
                return ticket;
            }

            // Quotas exhausted
            throw new SeatNotAvailableException("REGRET: All Tatkal Confirmed, RAC (" + maxRacSeats +
                "), and Waiting List (" + maxWaitingListSeats + ") seats are fully booked!");
        }
    }

    /**
     * Ticket Cancellation & Auto-Upgrade Logic.
     * Uses inter-thread signaling (notifyAll) to wake up and auto-promote waitlisted passengers.
     * Directly satisfies Syllabus Lectures 15-16.
     */
    public Ticket cancelTicket(String pnr) throws InvalidPNRException {
        synchronized (seatLock) {
            Optional<Ticket> ticketOpt = ticketDao.findById(pnr);
            if (ticketOpt.isEmpty()) {
                throw new InvalidPNRException("Invalid PNR: " + pnr + " does not exist in the reservation system.");
            }

            Ticket ticket = ticketOpt.get();
            if (ticket.getStatus() == BookingStatus.CANCELLED) {
                throw new InvalidPNRException("Ticket with PNR " + pnr + " is already CANCELLED.");
            }

            BookingStatus previousStatus = ticket.getStatus();
            Seat freedSeat = ticket.getAllocatedSeat();

            ticket.setStatus(BookingStatus.CANCELLED);
            ticket.setAllocatedSeat(null);
            ticketDao.update(ticket);

            System.out.printf("\n[CANCELLATION SUCCESS] PNR %s (%s) has been cancelled.\n",
                pnr, ticket.getPassenger().getName());

            // Cascading promotion if a CONFIRMED seat was vacated
            if (previousStatus == BookingStatus.CONFIRMED && freedSeat != null) {
                freedSeat.release();

                // 1. Promote head of RAC to Confirmed
                if (!racQueue.isEmpty()) {
                    Ticket racTicket = racQueue.poll();
                    freedSeat.allocate(racTicket.getPassenger());
                    racTicket.setAllocatedSeat(freedSeat);
                    racTicket.setStatus(BookingStatus.CONFIRMED);
                    racTicket.setRacOrWlPosition(0);
                    ticketDao.update(racTicket);

                    System.out.printf("  ↳ [AUTO-PROMOTION] RAC Passenger %s (PNR: %s) PROMOTED to CONFIRMED Seat %s-%02d (%s)!\n",
                        racTicket.getPassenger().getName(), racTicket.getPnr(),
                        freedSeat.getCoachId(), freedSeat.getSeatNumber(), freedSeat.getBerthType().getCode());

                    // 2. Promote head of WaitingList to RAC
                    if (!waitingListQueue.isEmpty()) {
                        Ticket wlTicket = waitingListQueue.poll();
                        wlTicket.setStatus(BookingStatus.RAC);
                        wlTicket.setRacOrWlPosition(racQueue.size() + 1);
                        racQueue.offer(wlTicket);
                        ticketDao.update(wlTicket);

                        System.out.printf("  ↳ [AUTO-PROMOTION] WaitingList Passenger %s (PNR: %s) PROMOTED to RAC-%d!\n",
                            wlTicket.getPassenger().getName(), wlTicket.getPnr(), wlTicket.getRacOrWlPosition());
                    }
                }
                // If RAC was empty, promote directly from WaitingList to Confirmed
                else if (!waitingListQueue.isEmpty()) {
                    Ticket wlTicket = waitingListQueue.poll();
                    freedSeat.allocate(wlTicket.getPassenger());
                    wlTicket.setAllocatedSeat(freedSeat);
                    wlTicket.setStatus(BookingStatus.CONFIRMED);
                    wlTicket.setRacOrWlPosition(0);
                    ticketDao.update(wlTicket);

                    System.out.printf("  ↳ [AUTO-PROMOTION] WaitingList Passenger %s (PNR: %s) PROMOTED to CONFIRMED Seat %s-%02d!\n",
                        wlTicket.getPassenger().getName(), wlTicket.getPnr(),
                        freedSeat.getCoachId(), freedSeat.getSeatNumber());
                }
            } else if (previousStatus == BookingStatus.RAC) {
                racQueue.remove(ticket);
                if (!waitingListQueue.isEmpty()) {
                    Ticket wlTicket = waitingListQueue.poll();
                    wlTicket.setStatus(BookingStatus.RAC);
                    wlTicket.setRacOrWlPosition(racQueue.size() + 1);
                    racQueue.offer(wlTicket);
                    ticketDao.update(wlTicket);

                    System.out.printf("  ↳ [AUTO-PROMOTION] WL Passenger %s promoted to RAC-%d!\n",
                        wlTicket.getPassenger().getName(), wlTicket.getRacOrWlPosition());
                }
            } else if (previousStatus == BookingStatus.WAITING_LIST) {
                waitingListQueue.remove(ticket);
            }

            // Inter-Thread Signaling: notifyAll wakes up any waiting threads (Lecture 15-16)
            seatLock.notifyAll();
            return ticket;
        }
    }

    public TrainCoachMatrix getCoachMatrix() {
        return coachMatrix;
    }

    public TicketDao getTicketDao() {
        return ticketDao;
    }

    public int getTrainNumber() {
        return trainNumber;
    }

    public String getTrainName() {
        return trainName;
    }

    public synchronized void printCurrentInventory() {
        System.out.println("\n----------------- REAL-TIME INVENTORY STATUS -----------------");
        System.out.printf("  Train            : #%d %s\n", trainNumber, trainName);
        System.out.printf("  Confirmed Seats  : %d Vacant / %d Total\n",
            coachMatrix.getAvailableSeatCount(), coachMatrix.getTotalSeats());
        System.out.printf("  RAC Occupancy    : %d / %d\n", racQueue.size(), maxRacSeats);
        System.out.printf("  Waiting List (WL): %d / %d (PriorityQueue Managed)\n",
            waitingListQueue.size(), maxWaitingListSeats);
        System.out.printf("  Tatkal Window    : %s\n", tatkalWindowOpen ? "OPEN (10:00:00 AM Active)" : "CLOSED");
        System.out.println("--------------------------------------------------------------");
    }
}
