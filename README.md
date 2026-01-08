# DoctorLy – Clinic & Doctor Booking System

**DoctorLy** is a role-based backend API for managing **private clinics, doctors, and patients**.  
It provides features for **booking appointments**, **doctor and clinic management**, and **secure user authentication** with **JWT tokens**.  
Clinics include **geolocation data** for distance-based searches, and images are stored on **AWS S3**.

---

##  Features

###  Authentication & Roles
- JWT-based authentication.
- Role-based access:
    - **ADMIN** – manage all data.
    - **CLINIC** – manage clinic details and assigned doctors.
    - **DOCTOR** – manage availability, profile, and appointments.
    - **PATIENT** – book and manage own appointments.
- OAuth2 login (e.g., Google), soon.
- Passwords stored securely using hashing.

---

### Clinics
- Register new clinics with:
    - Name, email, phone, address, city, postcode, description.
    - Image upload stored on **AWS S3**.
- Automatic **geolocation** from address using Nominatim (OpenStreetMap).
- Search clinics **by distance** (MongoDB 2dsphere index).
- Each clinic can have multiple doctors assigned.
- Clinics can receive and manage reviews.

---

### ️ Doctors
- Doctors can:
    - Create and manage their own account.
    - Set profile info (specialisation, bio, image).
    - Create **availability rules** (working days, time slots).
    - Manage appointments with patients.
- Each doctor is linked to a **specific clinic**.

---

### Patients
- Register, log in, and manage their profile.
- Search for doctors or clinics by name, specialty, or location.
- Book and cancel appointments.
- View personal appointment history.
- Leave reviews for clinics.

---

### Appointments
- Patients can book available slots with doctors.
- Doctors and clinics can view and manage appointments.
- Appointment status: `PENDING`, `CONFIRMED`, `CANCELLED`.
- Each appointment links:
    - `patientId`
    - `doctorId`
    - `clinicId`
    - `startTime`, `endTime`, `status`.

---

### Availability Rules & Time Off

DoctorLy supports a scheduling model based on **recurring availability** and **time-off exceptions**:

- **AvailabilityRule**
    - Stored per doctor and per day of week.
    - Fields:
        - `doctorId`
        - `dayOfWeek` (1–7, Monday–Sunday)
        - `start`, `end` (working hours for that day, local time)
        - `slotMinutes` (length of a single appointment, e.g. 30 minutes)
        - `bufferBeforeMinutes`, `bufferAfterMinutes` (optional buffers around slots)
        - `breaks` – a list of windows during the day when the doctor does not accept appointments (e.g. lunch).
    - Used to generate all recurring bookable time slots.

- **TimeOff**
    - Represents absolute UTC ranges when the doctor is unavailable.
    - Fields:
        - `doctorId`
        - `start`, `end` (as `Instant`)
        - `reason` (e.g. “Annual Leave”, “Sick”, “Conference”)
    - Can represent full-day or partial-day unavailability.
    - Time-off overrides availability rules when generating free slots.

Together, `AvailabilityRule` and `TimeOff` ensure that patients only see valid, bookable appointment times.

---

### Reviews
- Patients can leave reviews for doctors and clinics after attended appointments.
- Each review includes a rating (1–5) and an optional comment.
- Duplicate reviews from the same patient for the same doctor are prevented.
- Doctor average ratings are automatically recalculated after each review is added or removed.

---

### Doctor Specialities
- Administrators can create and manage medical specialities.
- Each speciality must have a unique, non-empty title.
- Duplicate speciality titles are automatically prevented.
- The API supports prefix-based search for specialities, making it easy to find doctors.

---

### Invitations
- Clinics can invite doctors to join their team using the invitation system.
- Invitations include the clinic ID, doctor ID, and email, and start with a `PENDING` status.
- Doctors can view all pending invitations and choose to accept or decline them.
- When a doctor accepts an invitation, they are automatically added to the clinic.
- If an invitation is declined or already processed, it can no longer be accepted.
- Validation ensures doctors cannot receive duplicate invitations for the same clinic.

---
### Storage & Infrastructure
- **MongoDB Atlas** – main database.
- **AWS S3** – image storage (clinic logos, doctor avatars).
- **Nominatim / OpenStreetMap** – geocoding for clinic addresses.
- **Spring Boot 3** – backend framework.
- **Spring Security** – authentication and authorization.

---

## Tech Stack

| Component | Technology |
|------------|-------------|
| Language | Java 17 |
| Framework | Spring Boot 3 |
| Database | MongoDB |
| Security | Spring Security + JWT |
| File Storage | AWS S3 |
| Geocoding | Nominatim (OpenStreetMap) |
| Build Tool | Maven |

---

## 🧩 Data Model (Simplified)

```text
User
 ├─ id
 ├─ email
 ├─ password
 ├─ roles [ADMIN, CLINIC, DOCTOR, PATIENT]

Clinic
 ├─ id
 ├─ name, email, phone, address, city, postCode
 ├─ description, imageUrl
 ├─ location (GeoJSON point: longitude, latitude)
 ├─ doctors[]
 ├─ reviews[]

Doctor
 ├─ id
 ├─ userId
 ├─ clinicId
 ├─ specialization, bio, imageUrl
 ├─ availabilityRules[]
 ├─ appointments[]

Patient
 ├─ id
 ├─ userId
 ├─ profile info
 ├─ appointments[]

Appointment
 ├─ id
 ├─ clinicId, doctorId, patientId
 ├─ startTime, endTime
 ├─ status (PENDING, CONFIRMED, CANCELLED)

Review
 ├─ id
 ├─ clinicId, patientId, appointmentId
 ├─ rating, comment, createdAt