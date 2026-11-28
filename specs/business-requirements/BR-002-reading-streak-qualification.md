# BR-002: Reading Streak Qualification

## Goal

Define how users qualify for a reading streak and how consecutive qualifying days are calculated across Normal Mode and 21-Day Habit Formation Mode.

## Business Rules

### 1. Daily Qualification

A user maintains their reading streak by completing the daily requirement for their current mode or stage:

| Mode / Stage | Daily Requirement |
|---|---|
| Normal Mode | Correctly answer at least one comprehension question. |
| 21-Day Habit Formation – Initial Stage | Open the application. |
| 21-Day Habit Formation – Reading Stage | Read at least 200 words. |
| 21-Day Habit Formation – Normal Stage | Correctly answer at least one comprehension question. |

### 2. Streak Calculation

- A qualifying day is a calendar day on which the user fulfills the requirement for their current mode or stage.
- Consecutive qualifying days increase the user's current streak by one.
- A missed day normally breaks the current streak.
- The system tracks both the current streak and the longest streak achieved by the user.
- Streak qualification is evaluated according to the user's active mode or stage on each day.

### 3. Streak Saver

- Each Streak Saver protects exactly one missed qualifying day.
- Streak Savers can only be used in Normal Mode.
- When the user next opens the application, the system checks for missed qualifying days in Normal Mode.
- For each missed day, the system automatically consumes one available Streak Saver to preserve the user's existing streak.
- Users can protect multiple consecutive missed days, provided they have enough Streak Savers.
- There is no maximum number of consecutive missed days that can be protected, as long as the user has sufficient Streak Savers.
- If the user does not have enough Streak Savers to cover all missed days, any unprotected missed day follows the normal streak-breaking rule.
- Streak Savers do not apply to 21-Day Habit Formation Mode and cannot prevent its stage or program reset rules.

### 4. Streak Saver Notification

- When one or more Streak Savers are consumed, the system displays a temporary notification.
- The notification indicates the number of Streak Savers used and confirms that the user's streak has been protected.