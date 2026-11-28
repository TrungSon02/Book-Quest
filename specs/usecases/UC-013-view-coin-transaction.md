# UC-013: View Coin Transaction History

## 1. Overview

| Field | Description |
|---|---|
| Use Case ID | UC-013 |
| Use Case Name | View Coin Transaction History |
| Primary Actor | User |
| Goal | Allow users to review their history of earned and spent coins. |
| Priority | Medium |

## 2. Preconditions

- The user has successfully logged in.
- The user is on the Account Settings page.

## 3. Trigger

The user selects the Coin Transaction History option from the Account Settings page.

## 4. Main Flow

1. The user selects Coin Transaction History from the Account Settings page.
2. The system retrieves the user's coin transaction history from the database.
3. The system displays the transaction history, including both earned and spent transactions.
4. For each transaction, the system displays:
    - **Description:** The reason for the transaction.
    - **Coin Amount:** A positive amount for earned coins or a negative amount for spent coins.
    - **Date and Time:** When the transaction occurred.
5. The user reviews their coin transaction history.

## 5. Alternative Flows

- **No Transaction History:** If the user has not earned or spent any coins, the system displays an appropriate message indicating that no transactions are available.

## 6. Postconditions

### Successful Viewing
- The user's coin transaction history is displayed.
- No coin balance or transaction data is modified.

## 7. Validation and Error Handling

| Condition | Expected System Response |
|---|---|
| Transaction history cannot be retrieved | Display an appropriate error message. |
| User has no transaction history | Display a message indicating that no transactions are available. |

## 8. Business Rules

- Coin transaction records follow **BR-003: Coin Reward Rules**.