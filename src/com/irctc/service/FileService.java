package com.irctc.service;

import com.irctc.model.BerthType;
import com.irctc.model.Passenger;
import com.irctc.model.Ticket;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * File Handling and CSV Integration Service.
 * Demonstrates Character Streams (FileReader/FileWriter), Buffered Streams,
 * collection export/import, input validation, and exception-safe resource management (try-with-resources).
 * Directly satisfies Syllabus Lectures 25, 26 & 27.
 */
public class FileService {

    /**
     * Exports a Collection of Ticket objects to a formatted CSV file.
     * Uses FileWriter and BufferedWriter inside try-with-resources (Lecture 25 & 26).
     */
    public boolean exportReservationChartToCsv(List<Ticket> tickets, String filePath) {
        if (tickets == null) {
            System.err.println("[File Error] Ticket list is null.");
            return false;
        }

        File file = new File(filePath);
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }

        // Exception-safe resource handling with try-with-resources (Lecture 27)
        try (FileWriter fw = new FileWriter(file);
             BufferedWriter bw = new BufferedWriter(fw)) {

            // CSV Header
            bw.write("PNR,PassengerName,Age,Gender,SeniorCitizen,TrainNumber,TrainName,Status,Berth,TotalFare,BookingTime");
            bw.newLine();

            // Writing collection data
            for (Ticket t : tickets) {
                String line = String.format("%s,\"%s\",%d,%s,%b,%d,\"%s\",%s,\"%s\",%.2f,\"%s\"",
                    t.getPnr(),
                    t.getPassenger().getName().replace("\"", "\"\""),
                    t.getPassenger().getAge(),
                    t.getPassenger().getGender(),
                    t.getPassenger().isSeniorCitizen(),
                    t.getTrainNumber(),
                    t.getTrainName().replace("\"", "\"\""),
                    t.getStatus().name(),
                    t.getSeatDescription(),
                    t.getTotalFare(),
                    t.getFormattedBookingTime()
                );
                bw.write(line);
                bw.newLine();
            }

            bw.flush();
            System.out.printf("[File Success] Successfully exported %d records to CSV: %s\n", 
                tickets.size(), file.getAbsolutePath());
            return true;

        } catch (IOException e) {
            System.err.println("[File Export Error] Failed to export CSV: " + e.getMessage());
            return false;
        }
    }

    /**
     * Imports passenger entries from a CSV file into a Java Collection (List<Passenger>).
     * Demonstrates FileReader, BufferedReader, line-by-line parsing, and input validation (Lecture 26).
     */
    public List<Passenger> importPassengersFromCsv(String filePath) {
        List<Passenger> passengers = new ArrayList<>();
        File file = new File(filePath);

        if (!file.exists()) {
            System.err.println("[File Error] Import file does not exist: " + file.getAbsolutePath());
            return passengers;
        }

        // Exception-safe resource handling
        try (FileReader fr = new FileReader(file);
             BufferedReader br = new BufferedReader(fr)) {

            String line;
            int lineNumber = 0;

            while ((line = br.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || (lineNumber == 1 && line.toLowerCase().contains("name"))) {
                    continue; // Skip header and comments
                }

                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    try {
                        String name = parts[0].trim().replace("\"", "");
                        int age = Integer.parseInt(parts[1].trim());
                        String gender = parts[2].trim().toUpperCase();

                        BerthType pref = BerthType.LOWER;
                        if (parts.length >= 4) {
                            try {
                                pref = BerthType.valueOf(parts[3].trim().toUpperCase());
                            } catch (IllegalArgumentException ignored) {}
                        }

                        Passenger passenger = new Passenger(name, age, gender, pref);
                        passengers.add(passenger);
                    } catch (Exception ex) {
                        System.err.printf("[CSV Warning] Skipping malformed line %d: %s (%s)\n", 
                            lineNumber, line, ex.getMessage());
                    }
                }
            }

            System.out.printf("[File Success] Successfully imported %d valid passengers from CSV: %s\n",
                passengers.size(), file.getAbsolutePath());

        } catch (IOException e) {
            System.err.println("[File Import Error] Failed to read CSV: " + e.getMessage());
        }

        return passengers;
    }

    /**
     * Creates a sample template CSV file for easy testing and evaluation.
     */
    public void generateSamplePassengersCsv(String filePath) {
        File file = new File(filePath);
        if (file.exists()) return;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write("Name,Age,Gender,PreferredBerth");
            bw.newLine();
            bw.write("Ramesh Kumar,65,M,LOWER");
            bw.newLine();
            bw.write("Pooja Sharma,24,F,UPPER");
            bw.newLine();
            bw.write("Gurpreet Singh,72,M,LOWER");
            bw.newLine();
            bw.write("Ananya Roy,29,F,SIDE_LOWER");
            bw.newLine();
            bw.write("Vikram Malhotra,35,M,MIDDLE");
            bw.newLine();
            bw.flush();
        } catch (IOException e) {
            System.err.println("[File Error] Could not write sample CSV: " + e.getMessage());
        }
    }
}
