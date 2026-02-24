# 🏥 MedApp - Clinic Management System

MedApp is a comprehensive, full-stack web application designed to manage a medical clinic. It facilitates the daily operations of administrators, doctors, and patients. The system handles user authentication, appointment scheduling with collision detection, medical history tracking, and automated email notifications.

## 🚀 Technologies Used

### Backend

* **Java 21** & **Spring Boot 3.3.5** (Core framework)
* **Spring Security** (Authentication, Authorization, CORS protection)
* **Spring Data JPA & Hibernate** (ORM, Database interactions)
* **PostgreSQL** (Relational database, containerized via Docker)
* **JavaMailSender** (Automated HTML email dispatch)
* **Lombok** (Boilerplate code reduction)
* **Jakarta Validation** (Strict data integrity checks e.g., `@PESEL`)

### Frontend

* **Angular 18+** (Standalone components, reactive forms, RxJS)
* **Tailwind CSS** (Modern, responsive UI styling)
* **FullCalendar** (Interactive appointment scheduling)
* **Angular Material Icons** (UI iconography)

---

## 👥 User Roles & Permissions

The application implements a strict Role-Based Access Control (RBAC) system:

### 1. 👑 Administrator (`ADMIN`)

* Has absolute control over the system.
* Creates doctor accounts (system automatically generates passwords and sends an email to the doctor).
* Manages patient records and can book/cancel appointments for anyone.
* Views comprehensive statistics on the dashboard.

### 2. 👨‍⚕️ Doctor (`DOCTOR`)

* Manages their own appointment schedule.
* Views and edits data of patients assigned to them.
* **Completes Visits:** Adds medical diagnoses and notes after an appointment (`COMPLETED` status).
* Views the full medical history of their patients.

### 3. 🩺 Patient (`PATIENT`)

* Self-registers via the web interface.
* **GDPR/RODO Compliant Calendar:** Patients can view the clinic's calendar to find available slots, but other patients' appointments are masked as *"Reserved"* to protect medical privacy.
* Books and cancels their own appointments.
* Views their personal medical history and updates their profile.

---

## 🔐 Security & Core Flows

### 1. Registration & Email Verification

When a patient registers, their account is initially created with `enabled = false`. The `@Async` EmailService sends a beautiful HTML email containing a unique `UUID` token. The patient cannot log in until they click the verification link.

### 2. Authentication (Basic Auth)

The system uses Basic Authentication over HTTPS. The frontend encrypts credentials using `btoa()` and stores them securely in `sessionStorage`. Spring Security's `CustomUserDetailsService` validates these credentials on every request.

### 3. Password Recovery Flow

1. User requests a reset link.
2. Backend generates a `resetToken` and sends an email.
3. User clicks the link, bringing them to an Angular route `/reset-password?token=...`.
4. The frontend performs cross-field validation (Confirm Password) before sending the new password to the server, where it is hashed using `BCryptPasswordEncoder`.

---

## 🧠 Interesting Technical Highlights (Backend Architecture)

* **`@Transactional` Annotation:** Used heavily in controllers (e.g., `registerUser`, `addDoctor`). It ensures database integrity. If saving the `AppUser` succeeds but saving the `Patient` entity fails, the entire transaction rolls back, preventing "orphan" accounts.
* **`@Async` Email Sending:** The `EmailService` methods are marked with `@Async`. This offloads the slow SMTP communication to a separate background thread. The user gets an immediate response (e.g., "Registration successful"), while the email is being sent in the background.
* **Advanced Appointment Validation:** The `AppointmentService` prevents booking collisions. It mathematically checks if the slot is between working hours (08:00 - 16:00), falls on a weekday, and is exactly in 15-minute intervals.
* **Global Error Handling:** The `@RestControllerAdvice` captures validation exceptions (like invalid PESEL or short passwords) and maps them into a structured JSON `Map<String, String>`. The Angular `ErrorService` parses this map and displays specific error messages exactly under the problematic form fields.
* **Soft Deleting:** Doctors and Patients are never physically removed from the DB (`deleted = true`). This ensures that historical medical records and past appointments remain intact.

---

*(I will create an instruction how to run Docker and Maven)*




