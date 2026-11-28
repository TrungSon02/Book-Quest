# UC-004: Read eBook

## 1. Overview

| Field | Description |
|---|---|
| Use Case ID | UC-004 |
| Use Case Name | Read eBook |
| Primary Actor | User |
| Goal | Allow users to read eBooks and customize their reading experience. |
| Priority | High |

## 2. Preconditions

- The user has successfully logged in.
- The user has selected a book from the Home Page.
- The selected book and its content are available in the application.

## 3. Trigger

The user selects a book from the Home Page, causing the system to open the Reading Page.

## 4. Main Flow

1. The system opens the Reading Page and displays a loading screen while retrieving the selected book's content.
2. Once the content is loaded, the system displays the book, initially opening at the first page.
3. The user can perform the following actions:
    - **Select Chapter:** Choose a chapter from the chapter selection menu. The system opens the first page of the selected chapter.
    - **Next Page:** Move to the next page of the current chapter. If the user is on the last page of a chapter, the system automatically moves to the first page of the next chapter.
    - **Previous Page:** Move to the previous page of the current chapter. If the user is on the first page of a chapter, the system automatically moves to the last page of the previous chapter.
    - **Increase Text Size:** Increase the displayed text size using the corresponding button, up to the maximum allowed size.
    - **Decrease Text Size:** Decrease the displayed text size using the corresponding button, down to the minimum allowed size.
    - **Return to Home Page:** Select the Back button to leave the Reading Page and return to the Home Page.

## 5. Alternative Flows

- **Chapter Selection:** The user can select any available chapter and start reading from its first page.
- **Text Size Adjustment:** The user can repeatedly increase or decrease the text size within the predefined limits.
- **Minimum Text Size Reached:** The system prevents further reduction when the minimum text size is reached.
- **Maximum Text Size Reached:** The system prevents further enlargement when the maximum text size is reached.
- **Loading Book Content:** While the book content is being retrieved, the system displays a loading screen until the content is ready.

## 6. Postconditions

### Successful Reading
- The selected book's content is displayed to the user.
- The user can navigate between pages and chapters.
- The displayed text size reflects the user's adjustments.

### Return to Home Page
- The user leaves the Reading Page.
- The system displays the Home Page.

## 7. Validation and Error Handling

| Condition | Expected System Response |
|---|---|
| User attempts to decrease text size below the minimum | Prevent further reduction. |
| User attempts to increase text size above the maximum | Prevent further enlargement. |
| User reaches the first page of the first chapter | Prevent navigation to a previous page. |
| User reaches the last page of the final chapter | Prevent navigation to a next page. |
| Book content cannot be loaded | Display an appropriate error message.