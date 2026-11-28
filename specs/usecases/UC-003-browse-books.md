# UC-003: Browse Books

## 1. Overview

| Field | Description |
|---|---|
| Use Case ID | UC-003 |
| Use Case Name | Browse Books |
| Primary Actor | User |
| Goal | Allow users to discover books through browsing, searching, filtering, and AI-powered recommendations. |
| Priority | High |

## 2. Preconditions

- The user has successfully logged in.
- The user is on the Home Page.
- The book collection is available in the database.

## 3. Trigger

The user opens the Home Page or interacts with the book browsing features.

## 4. Main Flow

1. The system displays the available books on the Home Page.
2. By default, AI Recommendations are enabled.
3. The system retrieves the user's preferred genres, English level, and reading history.
4. The AI recommendation model evaluates the available books based on these factors and sorts them according to their recommendation relevance.
5. The user can scroll through the displayed book collection.
6. The user can perform the following actions:
    - **Search:** Enter a book title or author name to find matching books.
    - **Filter:** Select one genre and/or one English level to narrow down the displayed books.
    - **Toggle AI Recommendations:** Enable or disable AI-powered sorting.
7. When AI Recommendations are disabled, the system sorts the displayed books alphabetically by title.
8. When the user selects a book, the system redirects them directly to the Reading Page.

## 5. Alternative Flows

- **Disable AI Recommendations:** The user turns off the recommendation toggle, and the system switches to alphabetical sorting by book title.
- **Enable AI Recommendations:** The user turns on the recommendation toggle, and the system sorts books according to AI-recommended relevance.
- **Search Books:** The user enters a search query, and the system displays books whose titles or author names match the query.
- **Filter Books:** The user selects a genre, an English level, or both. The system displays books matching the selected criteria.
- **Combine Search and Filters:** The user can search for books while applying genre and/or English level filters simultaneously.
- **No Matching Books:** If no books match the search query or selected filters, the system displays an appropriate message.

## 6. Postconditions

### Successful Book Selection
- The selected book is opened on the Reading Page.

### Browsing Without Selection
- The user remains on the Home Page.
- The current search query, selected filters, and sorting mode remain applied while browsing.

## 7. Validation and Error Handling

| Condition | Expected System Response |
|---|---|
| No books match the search query | Display a message indicating that no matching books were found. |
| No books match the selected filters | Display a message indicating that no books match the selected criteria. |
| Book collection cannot be retrieved | Display an appropriate error message. |
| AI recommendation model fails | Display an error or fallback to alphabetical sorting so the user can continue browsing. |