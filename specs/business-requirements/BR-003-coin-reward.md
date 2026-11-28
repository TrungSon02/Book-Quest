# BR-003: Coin Reward Rules

## 1. Overview

| Field | Description |
|---|---|
| Business Requirement ID | BR-003 |
| Business Requirement Name | Coin Reward Rules |
| Priority | High |
| Scope | All users |

## 2. Description

The system shall provide a coin-based reward mechanism to encourage users to maintain reading consistency, improve reading comprehension, and participate in challenges and achievements. Users can accumulate coins and spend them to redeem available rewards.

## 3. Business Rules

1. **Reading Streak Rewards:**
    - 3–7 consecutive qualifying days: 20 coins per day.
    - 8–14 consecutive qualifying days: 40 coins per day.
    - 15 or more consecutive qualifying days: 60 coins per day.
    - Streaks shorter than 3 days do not grant coins.
    - Rewards are based on the current streak length and are not awarded retroactively.
    - Streak qualification follows BR-002: Reading Streak Qualification.

2. **Question Rewards:**
    - Users earn 15 coins for each correctly answered comprehension question.
    - Incorrect answers do not grant coins.

3. **21-Day Habit Formation Mode Completion:**
    - Users earn 100 coins upon completing the entire 21-Day Habit Formation Mode.
    - This reward can only be claimed once per account, even if the user restarts the program.

4. **Daily Challenge Rewards:**
    - Each daily challenge has an assigned coin reward.
    - Each challenge can grant its reward only once per day.

5. **Achievement Rewards:**
    - Each achievement has an assigned coin reward.
    - Each achievement can grant its reward only once per account.

6. **Reward Accumulation:**
    - Different earning activities are independent.
    - Users can earn multiple rewards on the same day.
    - All successfully earned rewards are added to the user's coin balance.

7. **Coin Transaction History:**
    - The system shall record both earned and spent coin transactions.
    - Each transaction shall include a description, coin amount, and date/time.
    - Earned coins shall be represented by positive amounts, while spent coins shall be represented by negative amounts.
    - Users shall be able to view their coin transaction history through Account Settings and Reading Statistics.

8. **Coin Balance:**
    - The system shall maintain each user's current coin balance.
    - The system shall prevent transactions that would result in a negative coin balance.
    - Coin rewards shall be credited to the user's balance after the corresponding earning activity is successfully completed.

9. **Reward Notifications:**
    - The system shall display a temporary notification when users successfully earn coins.
    - The notification shall show the number of coins earned and the reason for the reward.
    - The notification shall automatically disappear after a few seconds.

## 4. Related Use Cases

- UC-006: View Reading Progress
- UC-007: Answer AI-Generated Questions
- UC-008: Participate in 21-Day Habit Formation Mode
- UC-009: Earn Coins
- UC-010: Redeem Rewards