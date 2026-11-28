# UC-007: Answer AI-Generated Questions

## 1. Overview

| Field | Description |
|---|---|
| Use Case ID | UC-007 |
| Use Case Name | Answer AI-Generated Questions |
| Primary Actor | User |
| Goal | Allow users to answer AI-generated comprehension questions based on the content they have read. |
| Priority | High |

## 2. Preconditions

- The user has successfully logged in.
- The user is on the Reading Page.
- The selected book's content is available.

## 3. Trigger

The user clicks the Next Page button, and the accumulated word count from eligible consecutive reading pages reaches or exceeds 200 words.

## 4. Main Flow

1. The user reads the current page.
2. The system tracks the time spent on the page and determines whether the minimum reading time has been satisfied.
3. When the user clicks Next Page:
    - If the minimum reading time is satisfied, the system adds the page's content and word count to the accumulated reading content.
    - Otherwise, the system resets the entire accumulated reading content and word count to zero.
4. When the accumulated word count reaches or exceeds 200 words, the system sends the entire accumulated reading content to the AI model.
5. The AI model generates a multiple-choice comprehension question containing:
    - One question.
    - Four answer options (A, B, C, D).
    - Exactly one correct answer.
6. The system displays the generated question in a pop-up in the center of the Reading Page.
7. The user selects one answer and submits it.
8. The system evaluates the submitted answer and displays the result:
    - Clearly identifies the correct answer.
    - Indicates whether the user's selected answer was correct or incorrect.
9. The user selects the Continue Reading button to close the pop-up and resume reading.
10. The system resets the accumulated reading content and word count to zero.

## 5. Alternative Flows

- **Word Count Below Threshold:** If the accumulated word count is below 200 words, no question is generated, and the user continues reading.
- **Insufficient Reading Time:** If the user advances from a page before satisfying its minimum reading time, the system resets the entire accumulated reading content and word count.
- **Question Generation Failure:** If the AI model fails to generate a question, the system displays an appropriate error message and allows the user to continue reading. The accumulated reading content and word count are preserved so question generation can be attempted again later.
- **Question Answered:** Once the user submits an answer, the system displays the result. The user cannot retry the same question.
- **Non-Reading Pages:** No question is generated from Table of Contents or Copyright pages.

## 6. Postconditions

### Successful Question Generation and Submission
- The user has submitted an answer to the generated question.
- The correct answer and the user's result have been displayed.
- The accumulated reading content and word count are reset to zero.
- The user can continue reading.

### Question Generation Failure
- The user can continue reading.
- The accumulated reading content and word count are preserved for a future generation attempt.

## 7. Validation and Error Handling

| Condition | Expected System Response |
|---|---|
| Accumulated word count is below 200 words | Do not generate a question. |
| User advances before satisfying the minimum reading time | Reset the entire accumulated reading content and word count. |
| AI model fails to generate a question | Display an error message and preserve the accumulated reading content and word count. |
| Generated question has invalid answer options or no correct answer | Do not display the invalid question; handle it as a generation failure. |
| User attempts to submit without selecting an answer | Prevent submission and request an answer selection. |
| User attempts to answer the same question again | Prevent another submission. |
| User reaches a Table of Contents or Copyright page | Do not generate a question from that page. |