🚀 Overview

This LMS provides a complete online learning platform, handling everything from course creation and student enrollment to progress tracking and administration.

It’s ideal for:

🎓 Universities & schools

🏢 Corporate training programs

🌐 Online course platforms

Built with Spring Boot 3, JWT Security, and MySQL/PostgreSQL, it demonstrates clean architecture and production-ready development practices.

✨ Key Features
👤 User Management & Security

🔒 Secure authentication with JWT tokens

👩‍🎓 Students — browse, enroll, and track progress

👨‍🏫 Instructors — create/manage courses, grade assignments

🧑‍💼 Administrators — full system control and user management

🧱 Role-based access control

🔐 Password encryption for security

📚 Course Management

🧭 Create and organize courses with categories and tags

📘 Modular structure with modules and lessons

🔁 Lifecycle: Draft → Published → Archived

🧠 Multiple difficulty levels (Beginner, Intermediate, Advanced)

💰 Flexible pricing and enrollment options

🎯 Learning Experience

🖱️ Easy enrollment for students

📊 Progress and grade tracking

🏅 Completion certificates

📂 Organized, module-based learning

💬 Instructor feedback and grading

🧱 Tech Stack
Layer	Technology
Backend	Java 17, Spring Boot 3.2.0
Security	Spring Security, JWT
Database	MySQL / PostgreSQL (Spring Data JPA)
Build Tool	Maven
Authentication	JWT Tokens
⚙️ Quick Start
🧩 Requirements

☕ Java 17 or newer

🐘 Maven 3.8+

🗄️ MySQL or PostgreSQL

🛠️ Installation
# Clone the repository
git clone https://github.com/omarhammouda0/elp.git
cd elp

# Create your database (example for MySQL)
CREATE DATABASE lms_db;

# Update credentials in src/main/resources/application.properties

# Build and run
mvn clean install
mvn spring-boot:run


Once running, open 👉 http://localhost:8080

👨‍💼 Create Your First Admin Account
POST /api/auth/register
Content-Type: application/json

{
  "userName": "admin",
  "email": "your-email@example.com",
  "password": "your-secure-password",
  "firstName": "Your",
  "lastName": "Name",
  "role": "ADMIN"
}

🧭 How to Use
🎓 Students

Browse and enroll in courses

Track progress

Submit assignments

View grades and completion certificates

👩‍🏫 Instructors

Create and manage courses

Organize modules and lessons

Track student performance

Grade and provide feedback

🛡️ Administrators

Manage all users and roles

Oversee courses and enrollments

Configure system settings

Monitor platform usage

🧰 API Examples
🔐 User Login
POST /api/auth/login
Content-Type: application/json

{
  "email": "student@example.com",
  "password": "student123"
}

📝 Create a Course
POST /api/courses
Authorization: Bearer <your-jwt-token>
Content-Type: application/json

{
  "title": "Introduction to Spring Boot",
  "description": "Learn Spring Boot from the ground up",
  "level": "BEGINNER",
  "category": "Programming"
}

🗂️ Project Structure
src/main/java/
├── auth/           # Authentication & login
├── user/           # User management
├── course/         # Course operations
├── module/         # Course content
├── enrollment/     # Student enrollments
├── category/       # Course categories
└── security/       # Security configuration

🔒 Security Highlights

✅ Passwords securely hashed

✅ Stateless JWT authentication

✅ Role-based permissions at URL and method level

✅ Protection against common vulnerabilities

👨‍💻 About the Developer

Omar Hammouda
💡 Software Developer passionate about building scalable, practical solutions.
I built this LMS to demonstrate how modern Java + Spring Boot can power robust educational platforms.

🐙 GitHub: @omarhammouda0

📧 Email: omarhamoda0@gmail.com

🧩 Getting Help

Check GitHub Issues

Email me directly for support

Review code comments and documentation

📄 License

This project is licensed under the MIT License — free to use, modify, and distribute.

🌟 Support the Project

If you find this project helpful, please ⭐ it on GitHub!
It helps others discover it and supports further development 🚀
