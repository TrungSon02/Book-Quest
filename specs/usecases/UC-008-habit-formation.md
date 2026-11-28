# UC-008: Participate in 21-Day Habit Formation Mode

## 1. Overview

| Field | Description |
|---|---|
| Use Case ID | UC-008 |
| Use Case Name | Participate in 21-Day Habit Formation Mode |
| Primary Actor | User |
| Goal | Help users gradually develop consistent reading habits through a structured 21-day program. |
| Priority | High |

## 2. Preconditions

- The user has successfully logged in.
- The 21-Day Habit Formation Mode is enabled in the user's reading preferences.

## 3. Trigger

The user opens the application while the 21-Day Habit Formation Mode is enabled and the program is active.

## 4. Main Flow

1. The system retrieves the user's current stage, progress, and daily activity status from the database.
2. The system determines the applicable daily requirement based on the user's current stage:
    - **Stage 1 – Initial Stage (Days 1–7):** The user must open the application.
    - **Stage 2 – Reading Stage (Days 1–7):** The user must read at least 200 words.
    - **Stage 3 – Normal Stage (Days 1–7):** The user must answer at least one comprehension question correctly.
3. The system displays the user's current stage and progress, with each stage presented as a separate Day 1–7 progression.
4. The user performs the required daily activity.
5. The system checks whether the user has fulfilled the applicable daily requirement.
6. If the requirement is fulfilled:
    - The system marks the current day as completed.
    - The system advances the user to the next day.
    - If the user completes Day 7 of a stage, the system advances them to Day 1 of the next stage.
7. After completing Day 7 of Stage 3:
    - The system marks the 21-Day Habit Formation Mode as completed.
    - The user automatically transitions to Normal Mode.

## 5. Alternative Flows

- **Daily Requirement Not Fulfilled:** If the user does not satisfy the daily requirement, the system records the day as missed.
- **One Consecutive Missed Day:** The user remains on the current day and must repeat its requirement on the next calendar day.
- **Two Consecutive Missed Days:** The system resets the user's progress within the current stage to Day 1.
- **Five Consecutive Missed Days:** The system resets the entire program to Day 1 of Stage 1, regardless of the user's current stage. This rule takes priority over the two-day stage reset.
- **Program Completed:** After completing all three stages, the system marks the program as completed and transitions the user to Normal Mode.
- **Program Restarted:** If the user manually enables the mode again after disabling it or completing it, the system starts a new program from Day 1 of Stage 1.

## 6. Postconditions

### Successful Daily Progress
- The user's daily activity is recorded.
- The user's stage and day progress are updated accordingly.

### Stage Reset
- The user's progress within the current stage is reset to Day 1.

### Full Program Reset
- The user's progress is reset to Day 1 of Stage 1.

### Program Completion
- The 21-Day Habit Formation Mode is marked as completed.
- The user transitions to Normal Mode.

## 7. Validation and Error Handling

| Condition | Expected System Response |
|---|---|
| User does not fulfill the daily requirement | Record the day as missed and retain the current day for the next attempt. |
| User misses two consecutive days | Reset the current stage to Day 1. |
| User misses five consecutive days | Reset the entire program to Day 1 of Stage 1. |
| User completes Day 7 of a stage | Advance to Day 1 of the next stage. |
| User completes the final stage | Mark the program as completed and transition to Normal Mode. |
| Daily activity cannot be recorded | Display an appropriate error message and allow the system to retry recording the activity. |

## 8. Business Rules

- The program consists of three consecutive stages, each containing seven successful days.
- The daily requirements are:
    - **Initial Stage:** Open the application.
    - **Reading Stage:** Read at least 200 words.
    - **Normal Stage:** Answer at least one comprehension question correctly.
- A missed day does not advance the user's progress.
- One consecutive missed day is tolerated; the user repeats the same day.
- Two consecutive missed days reset the current stage.
- Five consecutive missed days reset the entire program, taking priority over the stage reset.
- The program uses calendar days to determine daily progress.
- Reading streak qualification follows **BR-002: Reading Streak Qualification**.
- Enabling or disabling the mode, including confirmation and restart interactions, is handled by **UC-012: Manage Account Settings**.