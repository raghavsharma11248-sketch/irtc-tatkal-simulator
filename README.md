# IRCTC Tatkal High-Concurrency Railway Reservation Simulator
**A Core Java Console Application strictly aligned with Chitkara University Course Plan (Lectures 1–27)**

---

## 📌 Project Overview
The **IRCTC Tatkal Concurrency Simulator** models the peak 10:00:00 AM Tatkal ticket booking rush in Indian Railways. Built purely as a console/terminal application (no GUI), it demonstrates thread-safety under heavy concurrency, 2D coach-berth matrix allocation, automated waitlist promotion using `wait()` and `notifyAll()`, generic JDBC database persistence, Java 8 Stream API analytics, and CSV File I/O.

---

## 📂 Project Structure & Class Hierarchy
```text
irctc-tatkal-simulator/
├── run.bat                                (One-click compile and run script)
├── README.md                              (Documentation & Viva guide)
├── passengers_sample.csv                  (Sample CSV template for batch import)
├── bin/                                   (Compiled .class bytecode)
└── src/
    └── com/
        └── irctc/
            ├── model/
            │   ├── BerthType.java         (Enum: LOWER, MIDDLE, UPPER, SIDE_LOWER, SIDE_UPPER)
            │   ├── BookingStatus.java     (Enum: CONFIRMED, RAC, WAITING_LIST, CANCELLED)
            │   ├── Passenger.java         (Model with Encapsulation & Senior Citizen logic)
            │   ├── Seat.java              (Thread-safe atomic allocate/release berth model)
            │   └── Ticket.java            (Composition model, implements Comparable<Ticket>)
            │
            ├── exception/
            │   ├── TatkalException.java             (Checked Base Exception)
            │   ├── TatkalWindowClosedException.java (10 AM Window guard)
            │   ├── SeatNotAvailableException.java   (Quota full exception)
            │   └── InvalidPNRException.java         (PNR validation exception)
            │
            ├── database/
            │   ├── CrudRepository.java    (Generic Interface: Generics <T, ID>)
            │   └── TicketDao.java         (JDBC PreparedStatement, ResultSet CRUD mapping)
            │
            ├── service/
            │   ├── TrainCoachMatrix.java  (2D Array Matrix: layout[coach][seat])
            │   ├── BookingEngine.java     (Synchronized locks, PriorityQueue, wait/notify)
            │   ├── AnalyticsService.java  (Stream API: filter, map, sorted, distinct, limit, skip)
            │   └── FileService.java       (FileReader/FileWriter, try-with-resources, CSV)
            │
            ├── simulation/
            │   └── TatkalRushSimulator.java (Multi-Threaded 18-thread 10 AM Stress Test)
            │
            └── Main.java                  (Master Switchboard & Interactive Console Menu)
```

---

## 🎓 Syllabus Alignment Matrix (Chitkara University)

| Syllabus Lecture | Academic Topic Covered | Project Implementation & File |
| :--- | :--- | :--- |
| **Lect 1–4** | OOPs, Encapsulation, Custom Exceptions | `Passenger.java`, `Seat.java`, `TatkalException.java` |
| **Lect 2** | Multi-Dimensional Array Handling | `TrainCoachMatrix.java` (`Seat[][] layout` 2D array) |
| **Lect 5–8** | Collections, `PriorityQueue`, `Comparator` | `BookingEngine.java` (Senior citizen & female priority queue) |
| **Lect 9–14** | Multithreading & Concurrency, Locks | `TatkalRushSimulator.java` & `synchronized(seatLock)` in `BookingEngine` |
| **Lect 15–16** | Inter-Thread Communication (`wait()`/`notify()`) | `cancelTicket()` triggers `notifyAll()` to auto-promote RAC/WL |
| **Lect 17–20** | JDBC, `PreparedStatement`, `ResultSet` CRUD | `TicketDao.java` (Secure SQL, typed getters, object mapping) |
| **Lect 21–23** | Lambda Expressions & Stream API Pipelines | `AnalyticsService.java` (`filter`, `map`, `sorted`, `distinct`, `limit`, `skip`) |
| **Lect 24** | Introductory Generics & Type Safety | `CrudRepository<T, ID>` generic interface |
| **Lect 25–27** | Character Streams, CSV Read/Write, Resources | `FileService.java` (`try-with-resources`, CSV import/export) |

---

## 🚀 How to Run the Project

### Option A: Using the One-Click Script (Recommended)
Open Command Prompt (cmd) or PowerShell in this directory:
```bat
cd "C:\Users\RAGHAV ASHARMA\.gemini\antigravity\scratch\irctc-tatkal-simulator"
run.bat
```

### Option B: Manual Command-Line Compilation & Execution
```powershell
# 1. Compile all Java sources into bin directory
javac -d bin src\com\irctc\model\*.java src\com\irctc\exception\*.java src\com\irctc\database\*.java src\com\irctc\service\*.java src\com\irctc\simulation\*.java src\com\irctc\*.java

# 2. Run the application
java -cp bin com.irctc.Main
```

---

## 👨‍🏫 Viva Preparation Cheat-Sheet (Direct Examiner Answers)

1. **Q: How did you prevent race conditions during the 10:00 AM booking rush?**
   * *Ans:* "We implemented thread synchronization using a dedicated monitor lock (`synchronized(seatLock)`). This makes seat checking, berth allocation, and PNR generation an atomic transaction, preventing two concurrent threads from booking the same seat."

2. **Q: How does `wait()` and `notify()` work in your project?**
   * *Ans:* "In `BookingEngine.cancelTicket()`, when a confirmed passenger cancels their seat, the engine releases the berth and calls `seatLock.notifyAll()`. This wakes up the waiting priority queue, instantly promoting the first RAC passenger to Confirmed and the first Waiting List passenger to RAC without CPU polling."

3. **Q: Where have you used Java 8 Stream API?**
   * *Ans:* "In `AnalyticsService.java`, we process booked tickets using intermediate operations: `filter()` for senior citizens, `map()` and `distinct()` for occupied berth types, `sorted()` and `limit(3)` for top revenue fares, and `skip(2)` for waitlist pagination."

4. **Q: How is JDBC implemented?**
   * *Ans:* "In `TicketDao.java`, we implemented `CrudRepository<Ticket, String>`. We use `PreparedStatement` with parameter binding to prevent SQL injection, and map `ResultSet` cursor rows to `Ticket` domain objects using typed getters."
