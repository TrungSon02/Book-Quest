# UC-002: Manage Reading Preferences

## 1. Overview

| Field | Description |
|---|---|
| Use Case ID | UC-002 |
| Use Case Name | Manage Reading Preferences |
| Primary Actor | User |
| Goal | Allow users to configure their reading preferences to personalize their reading experience. |
| Priority | High |

## 2. Preconditions

- The user has successfully logged in or registered.
- The user is authenticated.

## 3. Trigger

- The user is redirected to the Preference Page after registration or when logging in without completing initial preference configuration.
- Alternatively, the user selects the option to manage reading preferences through Account Settings.

## 4. Main Flow

1. The system displays the Preference Page.
2. The system presents the following preference options:
    - **Preferred Genres:** Display the available genres retrieved from the database and allow users to select multiple genres.
    - **English Level:** Allow users to select one level: A1, A2, B1, B2, C1, or C2.
    - **21-Day Habit Formation Mode:** Provide a toggle to enable or disable the mode. It is enabled by default for new users.
3. The user selects their preferred genres and English level.
4. The user chooses whether to enable the 21-Day Habit Formation Mode.
5. The user submits their preferences.
6. The system validates the submitted preferences:
    - At least one genre must be selected.
    - An English level must be selected.
    - The 21-Day Habit Formation Mode must have a valid enabled or disabled value.
7. The system saves the preferences to the database.
8. The system redirects the user to the Home Page.

## 5. Alternative Flows

- The user can access the Preference Page later through Account Settings to update their preferences.
- When updating existing preferences, the system displays the user's previously saved selections.
- The user can modify any preference and save the changes.
- If validation fails, the system displays an appropriate error message and allows the user to correct their selections.

## 6. Postconditions

### Successful Preference Configuration
- The user's selected genres, English level, and 21-Day Habit Formation Mode setting are stored in the database.
- The user is redirected to the Home Page.

### Unsuccessful Preference Configuration
- Invalid preferences are not saved.
- The user remains on the Preference Page.

## 7. Validation and Error Handling

| Condition | Expected System Response |
|---|---|
| No genre selected | Display an error requesting at least one preferred genre. |
| No English level selected | Display an error requesting an English level. |
| Invalid or missing preference values | Display an appropriate validation error. |
| Database saving fails | Display an error indicating that preferences could not be saved and allow the user to retry.