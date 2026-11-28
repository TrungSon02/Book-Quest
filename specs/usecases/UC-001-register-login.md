# UC-001: Register / Log in

## 1. Overview

| Field | Description |
|---|---|
| Use Case ID | UC-001 |
| Use Case Name | Register / Log in |
| Primary Actor | User |
| Goal | Allow users to create an account and securely access Book Quest. |
| Priority | High |

## 2. Preconditions

- The user has launched the Book Quest application.
- The user is not currently authenticated.

## 3. Trigger

The user launches the Book Quest application, causing the authentication screen to appear automatically.

## 4. Main Flows

### 4.1 Log in

1. The system automatically displays the Login page.
2. The user enters their username or email in the first field and their password in the second field.
3. The user submits the login form.
4. The system validates the provided credentials:
    - The first field must match a registered username or email.
    - The password must match the corresponding account.
5. If authentication succeeds, the system checks the user's preference configuration status:
    - If the user has not completed initial preference configuration, redirect them to the Preference Page.
    - Otherwise, redirect them to the Home Page.

### 4.2 Register

1. From the Login page, the user selects the option to switch to the Sign-up page.
2. The system displays the registration form.
3. The user enters their email, username, and password.
4. The user submits the registration form.
5. The system validates all required fields:
    - All required fields must be filled in.
    - The email must have a valid format.
    - The email must not already be registered.
    - The username must not already be taken.
    - The password must contain at least 4 characters.
6. The system displays an appropriate error message if validation fails.
7. If all validations pass, the system hashes the password and stores the new account information in the database.
8. The system redirects the user to the Preference Page.

## 5. Alternative Flows

- The user can switch between the Login and Sign-up pages at any time.
- If registration validation fails, the user can correct the relevant fields and submit the form again.
- If the username/email does not match any registered account, the system displays an error message identifying the incorrect login identifier.
- If the password is incorrect, the system displays an incorrect-password error.

## 6. Postconditions

### Successful Registration
- A new user account is created and stored in the database.
- The password is stored as a hash, not as plaintext.
- The user is redirected to the Preference Page.

### Successful Login
- The user is authenticated.
- The user is redirected to the Preference Page if initial preferences have not been configured; otherwise, the user is redirected to the Home Page.

### Unsuccessful Authentication
- No new account is created during a failed registration.
- The user remains on the Login or Sign-up page.

## 7. Validation and Error Handling

| Condition | Expected System Response |
|---|---|
| Missing required registration field | Display an error identifying the missing field. |
| Invalid email format | Display an email format error. |
| Email already registered | Display an email-already-exists error. |
| Username already taken | Display a username-already-taken error. |
| Password shorter than 4 characters | Display a password-length error. |
| Username/email not found during login | Display an error identifying the incorrect login identifier. |
| Incorrect password | Display an incorrect-password error. |

All validation errors should be displayed using red error messages near the corresponding input fields where applicable.