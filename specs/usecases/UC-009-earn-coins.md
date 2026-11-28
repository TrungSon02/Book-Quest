# UC-009: Earn Coins

## 1. Overview

| Field | Description |
|---|---|
| Use Case ID | UC-009 |
| Use Case Name | Earn Coins |
| Primary Actor | User |
| Goal | Allow users to earn coins by completing eligible activities. |
| Priority | High |

## 2. Preconditions

- The user has successfully logged in.
- The user has an existing coin balance.

## 3. Trigger

The user successfully completes an activity that qualifies for a coin reward.

## 4. Main Flow

1. The user completes an activity that qualifies for a coin reward.
2. The system identifies the completed activity and determines the corresponding reward according to BR-003: Coin Reward Rules.
3. The system verifies that the user is eligible to receive the reward.
4. The system adds the earned coins to the user's coin balance.
5. The system records the transaction in the user's coin transaction history.
6. The system displays a small reward notification at the top of the screen, showing the number of coins earned and the reason.
7. The notification automatically disappears after a few seconds.

## 5. Alternative Flows

- **Reward Already Claimed:** If the user has already received the corresponding reward, the system prevents duplicate crediting.
- **Reward Requirements Not Fulfilled:** If the user does not satisfy the requirements for a reward, the system does not award coins.
- **Coin Balance Update Failure:** If the system cannot update the user's coin balance, it displays an appropriate error message and prevents duplicate transactions when retrying.

## 6. Postconditions

### Successful Coin Reward
- The user's coin balance is increased by the applicable reward amount.
- A transaction record is added to the user's coin transaction history.
- A temporary reward notification is displayed.

### Unsuccessful Reward
- The user's coin balance remains unchanged.
- No duplicate transaction is recorded.

## 7. Validation and Error Handling

| Condition | Expected System Response |
|---|---|
| User attempts to claim an already received reward | Prevent duplicate reward crediting. |
| User does not fulfill the requirements for a reward | Do not award coins. |
| Coin balance cannot be updated | Display an appropriate error message and prevent duplicate transactions when retrying. |

## 8. Business Rules

- All coin reward amounts, eligibility conditions, reward limits, and transaction policies follow **BR-003: Coin Reward Rules**.