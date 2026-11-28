# UC-005: Continue Reading

## 1. Overview

| Field | Description |
|---|---|
| Use Case ID | UC-005 |
| Use Case Name | Continue Reading |
| Primary Actor | User |
| Goal | Allow users to resume previously opened books from their last saved reading position and manage their saved reading progress. |
| Priority | High |

## 2. Preconditions

- The user has successfully logged in.
- The user is on the Home Page.
- The user has previously opened at least one book.

## 3. Trigger

The user opens the Home Page, selects a book from the Continue Your Quest section, or chooses to clear a book's saved reading progress.

## 4. Main Flow

1. The system retrieves the user's saved reading progress from the database.
2. The system displays a **Continue Your Quest** section above the main book collection on the Home Page.
3. The system displays books with saved reading progress, ordered by most recently read, with the latest book appearing first.
4. Each book in this section provides access to its saved reading position.
5. When the user selects a book from the Continue Your Quest section:
    - The system retrieves the saved chapter, page, and text size.
    - The system opens the Reading Page at the exact saved position.
    - The system restores the previously saved text size.
6. While the user reads, the system automatically saves their reading progress whenever their chapter, page, or text size changes.
7. When the user selects the Back button to return to the Home Page, the system saves their latest reading progress again.

## 5. Alternative Flows

- **No Saved Reading Progress:** If the user has not started reading any books, the system does not display the Continue Your Quest section.
- **Read Another Book:** The user can open another book from the main book collection. Once reading progress is saved for that book, it is added to the Continue Your Quest section.
- **Clear Saved Progress:** The user selects the option to remove a book's saved reading progress from the Continue Your Quest section. The system deletes the saved chapter, page, and text size for that book.
- **Reopen a Book After Clearing Progress:** The book remains available in the main book collection. If the user opens it again, reading starts from Chapter 1, Page 1, with the default text size.
- **Update Reading Order:** Whenever the user reads a book and its progress is saved, the system updates its position in the Continue Your Quest section according to the most recent reading activity.

## 6. Postconditions

### Successful Reading Progress Saving
- The user's latest reading chapter, page, and text size are stored in the database.
- The book appears in the Continue Your Quest section.
- The section is ordered by most recently read.

### Successful Reading Progress Removal
- The selected book's saved chapter, page, and text size are deleted from the database.
- The book is removed from the Continue Your Quest section.
- The book remains available in the main book collection.

### Successful Reading Resumption
- The selected book opens at its previously saved chapter and page.
- The previously saved text size is restored.

## 7. Validation and Error Handling

| Condition | Expected System Response |
|---|---|
| No saved reading progress exists | Do not display the Continue Your Quest section. |
| Saved reading progress cannot be retrieved | Display an appropriate error message. |
| Saving reading progress fails | Display an appropriate error message and attempt to save again when possible. |
| Removing saved progress fails | Display an error message and keep the existing progress entry. |
| Saved chapter or page is no longer available | Display an appropriate error message and provide a safe way to return to the Home Page. |