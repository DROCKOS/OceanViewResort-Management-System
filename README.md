# Ocean View Resort Management System

**CIS6003 Advanced Programming — Cardiff Metropolitan University / ICBT Campus**

A full-stack distributed web-based hotel room reservation system built in Java, 
replacing the manual paper-based booking process at Ocean View Resort, 
Galle, Sri Lanka.

---

## System Overview

Ocean View Resort Management System is a three-tier distributed application 
that allows hotel staff to manage room reservations, guest records, billing, 
and room availability through a browser-based interface. The backend exposes 
RESTful web service endpoints, and all data is persisted in a MySQL relational 
database.

---

## Features

- **User Authentication** — SHA-256 password hashing, UUID session tokens, role-based access control
- **Reservation Management** — Create, view, update, cancel, and delete bookings with full validation
- **Guest Management** — Automatic guest record creation using Find-or-Create pattern
- **Billing System** — Automatic bill calculation (nights × room rate), mark as paid, print invoice
- **Room Management** — Add, edit, delete rooms, toggle availability (Admin only delete)
- **Audit Logging** — All system actions logged with user and timestamp
- **Decision Reports** — Reservation status, billing revenue, and room availability reports
- **Search** — Filter reservations by guest name, room number, or status
- **Help Section** — Built-in staff guidance section

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Web Server | Java built-in HttpServer (com.sun.net.httpserver) |
| Database | MySQL 8.0 |
| Frontend | HTML5, CSS3, JavaScript |
| Build Tool | Maven |
| Testing | JUnit 5, Mockito 5 |
| Version Control | Git, GitHub |
| CI/CD | GitHub Actions |

---

## Architecture

The system follows a three-tier architecture:
```
Browser (HTML/JS Frontend)
        ↕ HTTP/JSON
Handler Layer (Controllers) — AuthHandler, ReservationHandler, BillHandler, RoomHandler
        ↕
Service Layer (Business Logic) — AuthService, ReservationService, BillService, RoomService
        ↕
DAO Layer (Data Access) — UserDAO, GuestDAO, RoomDAO, ReservationDAO, BillDAO, AuditLogDAO
        ↕
MySQL Database — users, guests, rooms, reservations, bills, audit_logs
```

---

## Design Patterns

- **Singleton** — DBConnection ensures a single database connection instance
- **DAO Pattern** — All SQL operations isolated in dedicated DAO classes
- **Service Layer** — All business rules centralised in service classes
- **Find-or-Create** — GuestDAO checks for existing guest by contact number before inserting

---

## API Endpoints

| Method | Endpoint | Description | Role |
|---|---|---|---|
| POST | /api/auth/login | Authenticate user | All |
| POST | /api/auth/logout | End session | All |
| GET | /api/reservations | Get all reservations | All Staff |
| POST | /api/reservations | Create reservation | All Staff |
| PUT | /api/reservations/{id} | Update reservation | All Staff |
| DELETE | /api/reservations/{id} | Delete reservation | All Staff |
| POST | /api/bills | Generate bill | All Staff |
| PUT | /api/bills/{id}/pay | Mark bill as paid | All Staff |
| GET | /api/rooms | Get all rooms | All Staff |
| POST | /api/rooms | Add new room | All Staff |
| DELETE | /api/rooms/{id} | Delete room | Admin only |
| GET | /api/audit | View audit logs | Admin only |

---

## Database Schema
```sql
users         — id, username, password_hash, role
guests        — guest_id, name, address, contact_number, email
rooms         — room_id, room_number, room_type, price_per_night, is_available
reservations  — reservation_id, guest_id, room_id, check_in_date, check_out_date, status
bills         — bill_id, reservation_id, total_amount, is_paid, generated_date
audit_logs    — log_id, user_id, action, details, timestamp
```

---

## Running the Project

**Prerequisites:**
- Java 21
- MySQL 8.0
- Maven

**Steps:**

1. Clone the repository
```bash
git clone https://github.com/DROCKOS/OceanViewResort-Management-System.git
cd OceanViewResort-Management-System
```

2. Create the database
```bash
mysql -u root -p < ovr.sql
```

3. Update database credentials in `DBConnection.java`
```java
private static final String URL = "jdbc:mysql://localhost:3306/ocean_view_resort";
private static final String USER = "root";
private static final String PASSWORD = "your_password";
```

4. Build and run
```bash
mvn compile
mvn exec:java -Dexec.mainClass="com.oceanview.Main"
```

5. Open browser at `http://localhost:8080`

**Default credentials:**
- Admin: `admin` / `Admin@1234`
- Staff: `staff` / `Staff@1234`

---

## Running Tests
```bash
mvn test
```

83 automated unit tests across 6 test classes — all passing.

| Test Class | Tests | Coverage |
|---|---|---|
| AuthServiceTest | 13 | Login, logout, session, roles |
| ReservationServiceTest | 18 | Create, cancel, validate, update |
| BillServiceTest | 14 | Generate, calculate, mark paid |
| RoomServiceTest | 17 | Add, delete, update, validate |
| PasswordUtilTest | 7 | SHA-256 hashing |
| DateUtilTest | 14 | Date validation, night calculation |

---

## Version Control

This project follows **GitHub Flow**:
- Feature branches created for each module
- All merges via Pull Requests
- Conventional Commits standard used throughout
- GitHub Actions CI/CD runs `mvn test` on every push to main

---

## Author

**Induwara Weerarathna**
BSc (Hons) Software Engineering
ICBT Campus — Cardiff Metropolitan University
CIS6003 Advanced Programming — 2025/2026
