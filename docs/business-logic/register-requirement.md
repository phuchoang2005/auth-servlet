# Functional Requirement Document: User Registration

## 1. Overview
The Registration module allows new users to create an account on the E-commerce platform. To provide a modern user experience, the system will use **Ajax** for asynchronous form submission and real-time validation, ensuring the page does not reload during the process.

---

## 2. User Stories
* **As a New Visitor**, I want to create an account by providing my details so that I can purchase products and track my orders.
* **As a User**, I want to see instant feedback if my email is already taken or if my password is too weak, so I don't have to wait until I hit "Submit."

---

## 3. Functional Requirements

### 3.1 Field Specifications
| Field            | Type | Constraints    | Validation (Frontend & Backend)                      |
|:-----------------| :--- |:---------------|:-----------------------------------------------------|
| **User Name**    | String | Max 100 chars  | Cannot be empty.                                     |
| **Full Name**    | String | Max 100 chars  | Cannot be empty.                                     |
| **Email**        | String | Unique, Format | Must follow `example@domain.com`.                    |
| **Password**     | String | Min 8 chars    | Must contain 1 uppercase, 1 lowercase, and 1 number. |
| **Adress**       | String | Max 100 chars      | None                                     |
| **Phone Number** | String | Numeric        | 10-11 digits.                                        |

### 3.2 Real-time Validation (Ajax)
The system must perform an asynchronous check for:
* **Email Availability:** Triggered when the "Email" field loses focus (`onblur`). The Servlet should query the Database and return whether the email is already registered.

### 3.3 Form Submission
* The form must be submitted using the **HTTP POST** method via an Ajax call.
* Prevent the default HTML form submission to avoid page refresh.
* Show a **Loading Spinner** while the Servlet processes the request.

---

## 4. Technical Workflow (Ajax & Servlet)



1.  **Client-Side:** User fills the form and clicks "Register."
2.  **Ajax Trigger:** JavaScript captures the form data and sends a JSON object to the Servlet.
3.  **Servlet Processing:**
    * Set response content type to `application/json`.
    * Extract parameters.
    * Validate data (Server-side validation is mandatory even with Frontend validation).
    * Interact with **DAO (Data Access Object)** to save the user to the Database (embedded H2, MySQL-compatibility mode).
4.  **Response:** Servlet returns a JSON response (e.g., `{"status": "success", "message": "User registered successfully"}`).
5.  **UI Update:** JavaScript receives the JSON and updates the DOM to show a success message or specific error labels.

---

## 5. API Design (Draft)

**Endpoint:** `/api/register`  
**Method:** `POST`  
**Payload (Request):**
```json
{
  "username": "string",
  "fullName": "Nguyễn Văn A",
  "email": "nguyenvana@example.com",
  "password": "StrongP@ssw0rd!",
  "address": "string",
  "phone": "0987654321"
}
```

**Success Response (201 OK):**
```json
{
  "timestamp": "string",
  "message": "Đăng ký tài khoản thành công",
  "username": "bosshp2000",
  "email": "nguyenvana@example.com"
}
```

**Error Response (400 Bad Request):**
```json
{
  "code": 400,
  "status": "error",
  "errors": {
    "email": "This email is already registered",
    "password": "Password is too weak"
  }
}
```

---

## 6. Non-Functional Requirements
* **Security:** Passwords **must** be hashed (using BCrypt or Argon2) before being stored in the database. Never store plain-text passwords.
* **Performance:** The Ajax response time should be under 500ms for a smooth UX.
* **UX/UI:** Error messages should appear in red text directly below the invalid input field.

---
