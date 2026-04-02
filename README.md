# 🏦 Pearson Bank – Core Banking API

Pearson Bank is a backend **Core Banking REST API** built with **Java and Spring Boot**, implementing secure authentication, account management, and transaction processing.

This project demonstrates backend development concepts such as JWT security, authentication and authorization, database transactions, CRUD, exception handling, API design, clean architecture, and cloud deployment.

---

## 🚀 Features

- Customer registration & login
- JWT-based authentication & authorization
- Bank account creation
- Secure fund transfers
- Transaction history retrieval
- Role-based access control
- Global exception handling

---

## 🛠 Tech Stack

- Java 22
- Spring Boot
- Spring Security (JWT)
- Spring Data JPA (Hibernate)
- MySQL / PostgreSQL
- Maven
- Render (Deployment)

---

## 🔐 Authentication

All protected endpoints require a JWT access token.

Authorization: Bearer <access_token>
________________________________________
📌 API Base URL
https://pearson-bank-backend.onrender.com/api
________________________________________
📂 Project Structure (PearsonBank Directory)
├── config
├── controller
├── dto
├── entity
├── event
├── exception
├── repository
├── scheduler
├── security
├── service
│   └── serviceimpl
├── types
└── util
________________________________________
📄 API Documentation
Detailed API documentation is available in this repository:
📄 PearsonBank_API_Documentation.docx
________________________________________
⚙️ Running Locally
•	git clone https://github.com/your-username/pearson-bank-backend.git
•	cd pearson-bank-backend
•	mvn spring-boot:run
________________________________________
☁️ Deployment
•	Backend deployed on Render
•	Environment variables used for secrets
•	Production-ready configuration
•	Database: Supabase PostgreSQL
•	APIs are always live for testing via Swagger at /swagger-ui.html or via postman
________________________________________
👩🏽‍💻 Author
Zainab Ajumobi
Backend Software Engineer – Java & Spring Boot

