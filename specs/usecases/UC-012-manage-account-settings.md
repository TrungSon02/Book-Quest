# UC-012: Manage Account Settings

## 1. Overview

| Field | Description |
|---|---|
| Use Case ID | UC-012 |
| Use Case Name | Manage Account Settings |
| Primary Actor | User |
| Goal | Allow users to manage their account information, reading preferences, and access other account-related features. |
| Priority | Low |

## 2. Preconditions

- The user has successfully logged in.

## 3. Trigger

The user selects the Account icon from the bottom navigation bar.

## 4. Main Flows

### 4.1 View Account Page

1. The user selects the Account icon from the bottom navigation bar.
2. The system displays the Account Page containing:
    - Current coin balance at the top.
    - Basic Account Information section.
    - Reading Preferences section.
    - Shop entry.
    - Coin Transaction History entry.
    - Reading Reminder Settings entry.

### 4.2 Update Basic Account Information

1. The system displays the user's current username and email address.
2. The user edits one or more of the following fields:
    - Username.
    - Email.
    - Password.
3. If the user changes any of these fields, the user must enter their current password for confirmation.
4. The user selects the Save Changes button in the Basic Account Information section.
5. The system verifies the current password.
6. The system validates the updated information:
    - Username must be unique.
    - Email must have a valid format and be unique.
    - Password must contain at least 4 characters.
7. The system saves the updated account information.
8. The system displays a temporary success notification at the top of the screen.

### 4.3 Update Reading Preferences

1. The system displays the user's previously saved reading preferences:
    - Preferred genres.
    - English proficiency level.
    - 21-Day Habit Formation Mode status.
2. The user modifies one or more preferences.
3. The user selects the Save Changes button in the Reading Preferences section.
4. The system validates the updated preferences.
5. The system saves the updated preferences.
6. If the 21-Day Habit Formation Mode status changes:
    - **Disabled:** The system discards the user's current 21-Day program progress.
    - **Enabled:** The system starts a new 21-Day program from Day 1 of the Initial Stage.
7. The system displays a temporary success notification at the top of the screen.

### 4.4 Access Shop

1. The user selects the Shop entry on the Account Page.
2. The system opens the Shop.
3. The Shop displays two categories:
    - App Skins.
    - Streak Savers.
4. The user can browse and purchase available items according to **UC-010: Redeem Rewards**.

### 4.5 Access Coin Transaction History

1. The user selects the Coin Transaction History entry.
2. The system opens the Coin Transaction History page according to **UC-013: View Coin Transaction History**.

### 4.6 Access Reading Reminder Settings

1. The user selects the Reading Reminder Settings entry.
2. The system opens the reminder settings according to **UC-011: Manage Reading Reminders**.

## 5. Alternative Flows

- **Incorrect Current Password:** If the user enters an incorrect current password, the system displays an error message and does not save the updated account information.
- **Duplicate Username:** If the updated username is already used by another account, the system displays an appropriate error message and does not save the changes.
- **Duplicate Email:** If the updated email is already used by another account, the system displays an appropriate error message and does not save the changes.
- **Invalid Email Format:** If the updated email format is invalid, the system displays an appropriate error message and does not save the changes.
- **Invalid Password:** If the new password contains fewer than 4 characters, the system displays an appropriate error message and does not save the changes.
- **No Account Information Changes:** If the user selects Save Changes without modifying any account information, the system does not perform an unnecessary update.
- **No Reading Preference Changes:** If the user selects Save Changes without modifying any reading preferences, the system does not perform an unnecessary update.
- **No Preferred Genre Selected:** If the user removes all selected genres, the system displays an appropriate error message and does not save the changes.
- **Account Update Failure:** If saving the updated account information fails, the system displays an error message and retains the previously saved information.
- **Reading Preference Update Failure:** If saving the updated reading preferences fails, the system displays an error message and retains the previously saved preferences.

## 6. Postconditions

### Successful Account Information Update

- The updated username, email, and/or password are saved in the database.
- The user remains logged in.
- A temporary success notification is displayed.

### Successful Reading Preference Update

- The updated reading preferences are saved in the database.
- Any changes to 21-Day Habit Formation Mode are handled according to UC-008.
- A temporary success notification is displayed.

### Accessing Other Account Features

- The user is redirected to the corresponding page when selecting Shop, Coin Transaction History, or Reading Reminder Settings.

## 7. Validation and Error Handling

| Condition | Expected System Response |
|---|---|
| Incorrect current password | Reject the account information update and display an error message. |
| Username is already taken | Reject the update and display an appropriate error message. |
| Email is already registered | Reject the update and display an appropriate error message. |
| Invalid email format | Reject the update and display an appropriate error message. |
| Password contains fewer than 4 characters | Reject the update and display an appropriate error message. |
| No preferred genre selected | Reject the preference update and display an appropriate error message. |
| Account information update fails | Retain the previously saved account information. |
| Reading preference update fails | Retain the previously saved reading preferences. |

## 8. Business Rules

- Account information validation follows the constraints defined in **UC-001: Register/Login**.
- Changing the username, email, or password requires the user's current password for confirmation.
- Username and email must remain unique across all accounts.
- The new password must contain at least 4 characters.
- The user remains logged in after successfully changing their password.
- Reading preferences follow the rules defined in **UC-002: Manage Reading Preferences**.
- Users must select at least one preferred genre.
- Users must select one English proficiency level from A1, A2, B1, B2, C1, or C2.
- Changes to 21-Day Habit Formation Mode follow the rules defined in **UC-008: Participate in 21-Day Habit Formation Mode**.
- The current coin balance is displayed at the top of the Account Page.
- The Shop contains App Skins and Streak Savers. Purchasing and applying these items follow **UC-010: Redeem Rewards**.
- Coin Transaction History is accessed through **UC-013: View Coin Transaction History**.
- Reading Reminder configuration follows **UC-011: Manage Reading Reminders**.