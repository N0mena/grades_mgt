# 🎓 Student Grade Management System

A Spring Boot application designed to manage academic data such as students, promotions, groups, teachers, courses, and grades.

The application focuses on providing a reliable and traceable grade management system, including grade history, transcript generation, cloud storage, email delivery, and graduate data export.

---

## 📌 About the Project

The **Student Grade Management System** provides a centralized platform for managing academic information throughout a student's academic journey.

It allows administrators and teachers to manage students, groups, courses, and grades while ensuring that changes to grades remain fully traceable.

A key feature of the system is the **grade modification history**. Whenever a grade is changed, the system records the previous value, the new value, the reason for the modification, the author, and the date.

The application also automates the generation and distribution of student transcripts.

---

## ✨ Main Features

### 👨‍🎓 Academic Management

- Manage students and their academic information
- Manage promotions and groups
- Manage teachers
- Manage courses
- Assign courses to teachers and groups
- Manage student grades

### 📝 Grade History

Grade modifications are fully tracked.

Each modification records:

- Previous grade
- New grade
- Reason for the modification
- User who made the modification
- Modification date

This provides transparency and accountability when academic results are changed.

### 📄 Transcript Generation

The application can generate a student's academic transcript as a PDF.

The transcript contains relevant academic information such as:

- Student information
- Promotion
- Courses
- Grades
- Coefficients
- Overall average

### ☁️ Cloud Storage

Generated transcripts can be stored in **Amazon S3**, allowing them to be accessed without relying on the local application filesystem.

### 📧 Asynchronous Email

The generated transcript can be sent to the student's email address.

Email processing is handled asynchronously so that generating and sending a transcript does not unnecessarily block the main application request.

### 📊 Graduate Export

The system can generate an Excel file containing graduate information for a given promotion.

This makes it easier to retrieve and process academic results outside the application.

### 🌐 Thymeleaf Interface

A lightweight Thymeleaf interface provides a simple way to interact with selected academic features, such as viewing promotions and downloading graduate data.

### 🧪 Automated Testing

The project includes unit and integration tests.

Integration tests use **Testcontainers** to run PostgreSQL in an isolated containerized environment, allowing the application to be tested against a real database environment.

The project targets a minimum test coverage of **80%**.

---

# 🏗️ Technology Stack

| Technology | Purpose |
|---|---|
| Java | Main programming language |
| Spring Boot | Backend framework |
| Spring Data JPA | Database persistence |
| PostgreSQL | Relational database |
| Neon | Cloud PostgreSQL hosting |
| Thymeleaf | Server-side web interface |
| AWS S3 | Transcript storage |
| Apache POI | Excel generation |
| JUnit | Unit and integration testing |
| Mockito | Mocking |
| Testcontainers | Integration testing |
| Gradle | Build and dependency management |
| Git / GitHub | Version control |

---

# 🗄️ Database

The application uses **PostgreSQL** as its relational database.

For development and deployment, the database can be hosted using **Neon**, which provides a serverless cloud PostgreSQL database.

The application connects to Neon through Spring Boot's datasource configuration.

The main domain concepts include:

- Promotions
- Students
- Groups
- Teachers
- Courses
- Grades
- Grade history
- Student group history

---

# 🚀 Getting Started

## Prerequisites

Make sure you have installed:

- Java 17+
- Maven
- Git
- Docker (required for integration tests using Testcontainers)

Check your installations:

```bash
java -version
mvn -version
docker --version
```
---
Authors : 
- MiotyRenala : STD24206
- N0mena : STD24205

Hei academic final project exam
