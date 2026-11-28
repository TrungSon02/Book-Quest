# UC-011: Manage Reading Reminders

## 1. Overview

| Field | Description |
|---|---|
| Use Case ID | UC-011 |
| Use Case Name | Manage Reading Reminders |
| Primary Actor | User |
| Goal | Remind users to maintain their reading habits through scheduled notifications. |
| Priority | Low |

## 2. Preconditions

- The user has successfully logged in.
- The user has granted notification permission for the application to deliver reminders.

## 3. Trigger

- The user accesses the reminder settings through Account Settings.
- The system reaches the user's scheduled reminder time.

## 4. Main Flows

### 4.1 Configure Reminder Time

1. The user accesses the reminder settings through Account Settings.
2. The system displays the current reminder time (default: 5:00 PM).
3. The user selects a new reminder time.
4. The system saves the selected time.
5. The new reminder time takes effect from the next scheduled reminder.

### 4.2 Send Scheduled Reminder

1. The system reaches the user's scheduled reminder time.
2. The system checks whether the user has already claimed their streak during the current calendar day.
3. If the user has already claimed their streak, the system skips that day's reminder.
4. Otherwise, the system checks the user's current reading streak.
5. The system randomly selects exactly one message from the corresponding predefined message list:
    - **Active Streak:** If the current streak is greater than zero, select a streak-related reminder.
    - **No Active Streak:** If the current streak is zero, select an encouraging message inviting the user to start a new reading quest.
6. The system sends the selected message as a standard Android notification.

### 4.3 Handle Reminder Notification

1. The user receives a reading reminder notification.
2. The user taps the notification.
3. The system opens the application directly to the Home Page.

## 5. Alternative Flows

- **User Has Already Claimed Their Streak:** If the user has already claimed their streak during the current calendar day, the system skips that day's reminder.
- **Notification Permission Denied:** If the user denies notification permission, the system does not send reminders.
- **Notification Permission Granted Later:** If the user later grants notification permission through Android Settings, reminders automatically resume using the previously selected reminder time.
- **User Changes Reminder Time:** The newly selected time takes effect from the next scheduled reminder. No immediate notification is sent.

## 6. Postconditions

### Successful Reminder Delivery

- A reminder notification is delivered at the scheduled time.
- The notification contains one randomly selected message from the appropriate message category.

### Reminder Skipped

- No notification is sent if the user has already claimed their streak that day or notification permission has not been granted.

### Reminder Time Updated

- The newly selected reminder time is saved and used for subsequent scheduled reminders.

## 7. Validation and Error Handling

| Condition | Expected System Response |
|---|---|
| User selects an invalid reminder time | Prevent saving the invalid time. |
| Notification permission is denied | Do not send reminders. |
| Notification permission is later granted | Automatically resume reminders using the previously saved time. |
| User changes reminder time | Save the new time and apply it from the next scheduled reminder. |

## 8. Business Rules

- Reading reminders are enabled by default and cannot be disabled through the application.
- The default reminder time is 5:00 PM.
- Users can change the scheduled reminder time through Account Settings.
- Reminders are scheduled daily at the user's selected time.
- A reminder is skipped if the user has already claimed their streak during the current calendar day.
- The reminder behavior applies to both Normal Mode and 21-Day Habit Formation Mode.
- The reminder message is selected based on the user's current reading streak at the scheduled time.
- Exactly one message is randomly selected from the applicable predefined message list for each reminder.
- Notification permission is requested when the user first launches the application.
- Reminders are only delivered when notification permission is granted.
- Tapping a reminder notification opens the application directly to the Home Page.