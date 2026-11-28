# UC-006: View Reading Progress

## 1. Overview

| Field | Description |
|---|---|
| Use Case ID | UC-006 |
| Use Case Name | View Reading Progress |
| Primary Actor | User |
| Goal | Allow users to track their overall reading statistics and monitor their reading consistency. |
| Priority | Medium |

## 2. Preconditions

- The user has successfully logged in.
- The user is on the Account Settings page.

## 3. Trigger

The user selects the Reading Statistics option from the Account Settings page.

## 4. Main Flow

1. The user selects Reading Statistics from the Account Settings page.
2. The system retrieves the user's reading statistics and daily streak activity from the database.
3. The system displays the following all-time reading statistics:
    - **Total Books Started:** Number of distinct books the user has started reading.
    - **Total Books Completed:** Number of books the user has fully completed.
    - **Total Pages Read:** Cumulative number of pages read.
    - **Total Reading Time:** Cumulative time spent reading.
    - **Total Reading Sessions:** Total number of reading sessions.
    - **Total Words Read:** Cumulative number of words read.
    - **Current Reading Streak:** Number of consecutive days on which the user has fulfilled the streak claim condition.
    - **Longest Reading Streak:** Highest number of consecutive qualifying days achieved by the user.
    - **Longest Reading Session:** Duration of the user's longest individual reading session.
    - **Average Reading Time per Session:** Average duration of the user's reading sessions.
4. The system displays a Daily Reading Activity calendar showing whether the user fulfilled the streak claim condition on each day of the current week.
5. The user reviews their reading statistics and weekly activity.

## 5. Alternative Flows

- **No Reading Activity:** If the user has not started reading any books, the system displays zero values for applicable statistics and an activity calendar with no fulfilled days.
- **View Weekly Activity:** The user can review their activity throughout the current week.

## 6. Postconditions

### Successful Viewing
- The user's reading statistics are displayed.
- The user's daily streak activity for the current week is displayed.
- No reading data is modified.

## 7. Validation and Error Handling

| Condition | Expected System Response |
|---|---|
| Reading statistics cannot be retrieved | Display an appropriate error message. |
| Daily activity data cannot be retrieved | Display an appropriate error message for the activity calendar. |
| User has no reading history | Display zero values for applicable statistics and an empty activity calendar. |

## 8. Business Rules

- The system calculates current and longest reading streaks according to **BR-004: Reading Streak Qualification**.
- The Daily Reading Activity calendar uses the same qualification rules defined in BR-004.
- Average Reading Time per Session is calculated by dividing total reading time by total reading sessions. If no sessions exist, the displayed value is zero.