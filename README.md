# Quiz Application Backend

Welcome to the **Quiz Application Backend**, a Spring Boot-based REST API for managing quizzes, questions, and user interactions. This project provides a robust backend for creating, taking, and scoring quizzes, with role-based authentication (ADMIN and USER) using JWT, MySQL for data storage, and Log4j2 for logging. It includes features like quiz management, question creation with validation, and score tracking.

## Table of Contents
- [Features](#features)
- [Technologies](#technologies)
- [Prerequisites](#prerequisites)
- [Setup Instructions](#setup-instructions)
- [Project Structure](#project-structure)
- [API Documentation](#api-documentation)
  - [Authentication APIs](#authentication-apis)
  - [Quiz Management APIs](#quiz-management-apis)
  - [Quiz Taking APIs](#quiz-taking-apis)
- [Security](#security)
- [Logging](#logging)

## Features
- **Authentication**: User registration and login with JWT-based authentication.
- **Role-Based Access**:
  - **ADMIN**: Can create, update, delete quizzes/questions, and view all users' scores and answers.
  - **USER**: Can take quizzes and view their own scores.
- **Quiz Management**:
  - Create, update, and delete (soft delete) quizzes with titles.
  - Add, update, and delete (soft delete) questions (SINGLE_CHOICE, MULTIPLE_CHOICE, TEXT) with validation (e.g., one correct answer for single-choice, text answers ≤ 300 characters).
- **Quiz Taking**:
  - Fetch quiz questions (excluding correct answers).
  - Submit answers and receive scores.
  - View personal scores (USER) or all scores (ADMIN).
- **Soft Deletion**: Quizzes and questions are marked inactive instead of deleted to preserve historical scores.
- **Error Handling**: Comprehensive exception handling with meaningful error messages.
- **Logging**: Configured with Log4j2 for console and file-based logging.

## Technologies
- **Spring Boot**: 3.4.10 (or latest compatible version)
- **Spring Security**: JWT-based authentication with role-based access control
- **Spring Data JPA**: For MySQL database interactions
- **MySQL**: Database for storing users, quizzes, questions, and attempts
- **Log4j2**: For logging application events
- **Maven**: Build tool
- **Lombok**: To reduce boilerplate code
- **Java**: 17
- **JJWT**: For JWT token generation and validation

## Prerequisites
- **Java**: JDK 17 installed
- **Maven**: 3.6+ for dependency management
- **Lombok**: Installed IDE plugin (for IntelliJ IDEA, Eclipse, etc.) to support Lombok annotations
- **MySQL**: 8.0+ with a database named `quizdb`
- **IDE**: IntelliJ IDEA, Eclipse, or similar (optional but recommended)
- **Postman**: For testing APIs (optional)

## Setup Instructions
1. **Clone the Repository**:
   ```bash
   git clone https://github.com/your-username/quiz-app-backend.git
   cd quiz-app-backend
   ```

2. **Configure MySQL**:
  
   - Update `src/main/resources/application.yml` with your MySQL credentials:
     ```yaml
     spring:
       datasource:
         url: jdbc:mysql://localhost:3306/quizapp?createDatabaseIfNotExist=true
         username: your_mysql_username
         password: your_mysql_password
     ```

3. **Set JWT Secret**:
   - In `application.yml`, ensure the `jwt.secret` is a secure, random string (at least 32 characters). For testing purposes, a default secret has already been set:

     ```yaml
     jwt:
       secret: your-very-secure-secret-key-at-least-32-chars
       expiration-ms: 86400000
     ```
   - Generate a secure key using:
     ```bash
     openssl rand -base64 32
     ```
   - **Important:** For production environments, do **not** use the testing secret. Instead, store the secret securely using environment variables or a secrets manager.


4. **Build and Run**:
   - Build the project:
     ```bash
     mvn clean install
     ```
   - Run the application:
     ```bash
     mvn spring-boot:run
     ```
   - The server starts on `http://localhost:8080`.

5. **Create an Admin User**:

   An admin user is created by default when the application starts with the following credentials:

   - **Username:** `admin`  
   - **Password:** `admin@123`

   If you register other users via `/api/auth/register`, you can manually set their role to `ADMIN` by running this SQL query on your database:

   ```sql
   UPDATE users SET role = 'ADMIN' WHERE username = 'your_admin_username';
   ```

6. **Test APIs**:
   - Use Postman or curl to test endpoints (see [API Documentation](#api-documentation)).
   - Start with `/api/auth/register` or `/api/auth/login` to get a JWT token.

## Project Structure
```
quizapplication/
  logs/                                 # Daily logs
  src/
    main/
      java/com/quizapp/quizapplication
        config/                         # Security and app configurations
        controller/                     # REST controllers for API endpoints
        dto/                            # Data Transfer Objects for requests/responses
        entity/                         # JPA entities for database models
        enums/                          # Enums for Role and QuestionType
        exception/                      # Global exception handling
        repository/                     # JPA repositories for database access
        security/                       # JWT and authentication logic
        service/                        # Business logic for auth, quizzes, attempts
        QuizAppBackendApplication.java  # Main application class
      resources/
        application.yml                 # App configuration
        log4j2.yml                      # Logging configuration
  pom.xml                               # Maven dependencies
  README.md                             # This file
```

## API Documentation
All APIs require JSON (`Content-Type: application/json`). Most endpoints (except auth) require a JWT token in the `Authorization` header as `Bearer <token>`.

### Authentication APIs
#### 1. Register User
- **Method**: POST
- **Path**: `/api/auth/register`
- **Description**: Creates a new user with role USER and returns a JWT token.
- **Request Body**:
  ```json
  {
    "username": "testuser",
    "password": "securepassword123",
    "email": "testuser@example.com"
  }
  ```
- **Response** (200 OK):
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
  ```

#### 2. Login User
- **Method**: POST
- **Path**: `/api/auth/login`
- **Description**: Authenticates a user and returns a JWT token.
- **Request Body**:
  ```json
  {
    "username": "testuser",
    "password": "securepassword123"
  }
  ```
- **Response** (200 OK):
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
  ```


### Quiz Management APIs
#### 3. Create Quiz (Admin Only)
- **Method**: POST
- **Path**: `/api/quizzes`
- **Description**: Creates a new quiz. Requires ADMIN role.
- **Request Headers**: `Authorization: Bearer <token>`
- **Request Body**:
  ```json
  {
    "title": "Sample Quiz Title"
  }
  ```
- **Response** (200 OK):
  ```json
  {
    "id": 1,
    "title": "Sample Quiz Title",
    "questions": []
  }
  ```

#### 4. Get Quiz Details
- **Method**: GET
- **Path**: `/api/quizzes/{id}` (e.g., `/api/quizzes/1`)
- **Description**: Fetches quiz with questions (excludes correct answers). Requires authentication.
- **Request Headers**: `Authorization: Bearer <token>`
- **Response** (200 OK):
  ```json
  {
    "id": 1,
    "title": "Sample Quiz Title",
    "questions": [
      {
        "id": 1,
        "text": "What is 2+2?",
        "type": "SINGLE_CHOICE",
        "options": [
          { "id": 1, "text": "3" },
          { "id": 2, "text": "4" }
        ]
      },
      {
        "id": 2,
        "text": "Describe the sky.",
        "type": "TEXT",
        "options": []
      }
    ]
  }
  ```

#### 5. Get All Active Quizzes
- **Method**: GET
- **Path**: `/api/quizzes`
- **Description**: Fetches all active quizzes. Requires authentication.
- **Request Headers**: `Authorization: Bearer <token>`
- **Response** (200 OK):
  ```json
  [
    {
      "id": 1,
      "title": "Sample Quiz Title",
      "questions": [...]
    }
  ]
  ```
- **Errors**:
  - 401: Missing/invalid token.

#### 6. Update Quiz (Admin Only)
- **Method**: PUT
- **Path**: `/api/quizzes/{id}` (e.g., `/api/quizzes/1`)
- **Description**: Updates a quiz’s title. Requires ADMIN role and creator ownership.
- **Request Headers**: `Authorization: Bearer <token>`
- **Request Body**:
  ```json
  {
    "title": "Updated Quiz Title"
  }
  ```
- **Response** (200 OK):
  ```json
  {
    "id": 1,
    "title": "Updated Quiz Title",
    "questions": [...]
  }
  ```


#### 7. Delete Quiz (Admin Only)
- **Method**: DELETE
- **Path**: `/api/quizzes/{id}` (e.g., `/api/quizzes/1`)
- **Description**: Soft deletes a quiz (marks as inactive). Requires ADMIN role and creator ownership.
- **Request Headers**: `Authorization: Bearer <token>`
- **Response** (200 OK): Quiz deleted successfully.


#### 8. Add Question to Quiz (Admin Only)
- **Method**: POST
- **Path**: `/api/admin/questions/{quizId}` (e.g., `/api/admin/questions/1`)
- **Description**: Adds a question (SINGLE_CHOICE, MULTIPLE_CHOICE, or TEXT). Requires ADMIN role.
- **Request Headers**: `Authorization: Bearer <token>`
- **Request Body** (SINGLE_CHOICE example):
  ```json
  {
    "text": "What is 2+2?",
    "type": "SINGLE_CHOICE",
    "options": [
      { "text": "3", "isCorrect": false },
      { "text": "4", "isCorrect": true }
    ]
  }
  ```
- **Request Body** (TEXT example):
  ```json
  {
    "text": "Describe the sky.",
    "type": "TEXT",
    "correctAnswerText": "blue"
  }
  ```
- **Response** (200 OK): Question added successfully.


#### 9. Update Question (Admin Only)
- **Method**: PUT
- **Path**: `/api/admin/questions/{id}` (e.g., `/api/admin/questions/1`)
- **Description**: Updates a question’s details. Requires ADMIN role.
- **Request Headers**: `Authorization: Bearer <token>`
- **Request Body** (SINGLE_CHOICE example):
  ```json
  {
    "text": "What is 3+3?",
    "type": "SINGLE_CHOICE",
    "options": [
      { "text": "6", "isCorrect": true },
      { "text": "5", "isCorrect": false }
    ]
  }
  ```
- **Response** (200 OK): Question updated successfully.


#### 10. Delete Question (Admin Only)
- **Method**: DELETE
- **Path**: `/api/admin/questions/{id}` (e.g., `/api/admin/questions/1`)
- **Description**: Soft deletes a question (marks as inactive). Requires ADMIN role.
- **Request Headers**: `Authorization: Bearer <token>`
- **Response** (200 OK): Question deleted successfully.


### Quiz Taking APIs
#### 11. Submit Answers for Quiz
- **Method**: POST
- **Path**: `/api/attempts/{quizId}` (e.g., `/api/attempts/1`)
- **Description**: Submits answers and returns score. Requires authentication.
- **Request Headers**: `Authorization: Bearer <token>`
- **Request Body**:
  ```json
  {
    "answers": [
      {
        "questionId": 1,
        "selectedOptionIds": [2]
      },
      {
        "questionId": 2,
        "answerText": "blue"
      }
    ]
  }
  ```
- **Response** (200 OK):
  ```json
  {
    "score": 2,
    "total": 2
  }
  ```

#### 12. Get My Scores
- **Method**: GET
- **Path**: `/api/attempts/my`
- **Description**: Fetches the current user’s quiz attempt scores. Requires authentication.
- **Request Headers**: `Authorization: Bearer <token>`
- **Response** (200 OK):
  ```json
  [
    {
      "id": 1,
      "userId": 1,
      "quizId": 1,
      "score": 2,
      "total": 2,
      "attemptedAt": "2025-09-27T12:00:00",
      "answers": [
        {
          "questionId": 1,
          "selectedOptionIds": "2",
          "answerText": null
        },
        {
          "questionId": 2,
          "selectedOptionIds": null,
          "answerText": "blue"
        }
      ]
    }
  ]
  ```

#### 13. Get All Scores (Admin Only)
- **Method**: GET
- **Path**: `/api/attempts`
- **Description**: Fetches all users’ quiz attempt scores. Requires ADMIN role.
- **Request Headers**: `Authorization: Bearer <token>`
- **Response** (200 OK): Array of attempts (same format as above).


#### 14. Get Attempt Details
- **Method**: GET
- **Path**: `/api/attempts/{id}` (e.g., `/api/attempts/1`)
- **Description**: Fetches details of a specific attempt (own or any if ADMIN). Requires authentication.
- **Request Headers**: `Authorization: Bearer <token>`
- **Response** (200 OK):
  ```json
  {
    "id": 1,
    "userId": 1,
    "quizId": 1,
    "score": 2,
    "total": 2,
    "attemptedAt": "2025-09-27T12:00:00",
    "answers": [
      {
        "questionId": 1,
        "selectedOptionIds": "2",
        "answerText": null
      },
      {
        "questionId": 2,
        "selectedOptionIds": null,
        "answerText": "blue"
      }
    ]
  }
  ```

## Security
- **JWT Authentication**: All endpoints except `/api/auth/register` and `/api/auth/login` require a JWT token in the `Authorization` header (`Bearer <token>`).
- **Role-Based Access**:
  - ADMIN: Full access to quiz/question creation, update, deletion, and all scores.
  - USER: Can take quizzes and view own scores.
- **Password Storage**: Passwords are hashed using BCrypt.
- **Secret Management**: Store `jwt.secret` securely (e.g., environment variables or secrets manager in production).

## Logging
- Configured with Log4j2 (`log4j2.xml`).
- Logs are written to:
  - Console (for development).
  - File (`logs/quiz-app.log`) with daily rolling.
- Log levels:
  - `com.quizapp.quizapplication`: DEBUG
  - `root`: debug
  - Adjust in `application-dev.yml` or `log4j2.xml` as needed.

