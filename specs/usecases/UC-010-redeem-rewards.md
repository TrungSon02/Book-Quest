# UC-010: Redeem Rewards

## 1. Overview

| Field | Description |
|---|---|
| Use Case ID | UC-010 |
| Use Case Name | Redeem Rewards |
| Primary Actor | User |
| Goal | Allow users to spend coins to unlock popular books, app skins, and Streak Savers. |
| Priority | Low |

## 2. Preconditions

- The user has successfully logged in.
- The user has an existing coin balance.
- The available books, app skins, and Streak Savers are configured in the system.

## 3. Trigger

The user selects a locked book from the Home Page or accesses the redemption section through Account Settings.

## 4. Main Flows

### 4.1 Purchase a Popular Book

1. The user selects a locked book from the Home Page.
2. The system displays a purchase confirmation pop-up containing:
    - Book title.
    - Coin price.
    - Current coin balance.
    - Confirm and Cancel buttons.
3. The user confirms the purchase.
4. The system verifies that the user has sufficient coins.
5. The system deducts the required coins from the user's balance.
6. The system permanently unlocks the selected book.
7. The system records the coin transaction.
8. The purchased book becomes immediately available for reading.

### 4.2 Purchase and Apply an App Skin

1. The user accesses the App Skins section through Account Settings.
2. The system displays all available skins, including locked and unlocked skins.
3. Each skin is displayed with a preview and its coin price.
4. The system indicates the user's currently applied skin.
5. The user selects a skin:
    - **Locked Skin:** The system displays a purchase confirmation pop-up containing the skin preview, skin name, coin price, current coin balance, and Confirm and Cancel buttons.
    - **Unlocked Skin:** The system temporarily selects the skin without immediately changing the app's appearance.
6. If the user confirms the purchase of a locked skin:
    - The system verifies that the user has sufficient coins.
    - The system deducts the required coins.
    - The system unlocks the selected skin permanently.
    - The system records the coin transaction.
    - The newly purchased skin is not automatically selected or applied.
7. The user selects an unlocked skin to preview and temporarily select it.
8. The user selects the Apply button at the bottom of the page.
9. The system applies the selected skin and saves it as the user's currently active skin in the database.

### 4.3 Purchase Streak Savers

1. The user accesses the Streak Savers section through Account Settings.
2. The system displays the user's currently available number of Streak Savers and their coin price.
3. The user selects the option to purchase a Streak Saver.
4. The system displays a purchase confirmation pop-up containing:
    - Streak Saver name.
    - Coin price.
    - Current coin balance.
    - Confirm and Cancel buttons.
5. The user confirms the purchase.
6. The system verifies that the user has sufficient coins.
7. The system deducts the required coins from the user's balance.
8. The system increases the user's available Streak Saver quantity by one.
9. The system records the coin transaction.

### 4.4 Automatically Use Streak Savers

1. The user opens the application.
2. The system checks whether the user has missed any qualifying days in Normal Mode.
3. For each missed day, the system checks the user's available Streak Saver quantity.
4. If a Streak Saver is available, the system consumes one to protect that missed day.
5. The system repeats this process for each missed day.
6. If one or more Streak Savers are consumed, the system displays a temporary notification showing the number used and confirming that the user's streak is protected.

## 5. Alternative Flows

- **Cancel Purchase:** The user selects Cancel in the purchase confirmation pop-up. The system closes the pop-up without deducting coins or unlocking the item.
- **Insufficient Coin Balance:** If the user's coin balance is insufficient, the system disables the Confirm button and displays an appropriate message. The user cannot proceed with the purchase.
- **Select Another Skin:** If the user selects another unlocked skin before applying the current selection, the new selection replaces the previous temporary selection.
- **Select Currently Applied Skin:** If the user selects the skin that is already active, the Apply button remains disabled.
- **Leave App Skins Page Without Applying:** If the user leaves the page without selecting Apply, the currently active skin remains unchanged.
- **Purchase Failure:** If a purchase fails due to a transaction or database error, the system displays an error message, does not deduct coins or update item ownership, and allows the user to retry.

## 6. Postconditions

### Successful Book Purchase

- The required coins are deducted from the user's balance.
- The selected book is permanently unlocked and available for reading.
- A coin transaction record is created.

### Successful Skin Purchase

- The required coins are deducted from the user's balance.
- The selected skin is permanently unlocked.
- A coin transaction record is created.
- The skin is not automatically applied.

### Successful Skin Application

- The selected unlocked skin becomes the user's active app skin.
- The active skin is saved in the database.

### Successful Streak Saver Purchase

- The required coins are deducted from the user's balance.
- The user's available Streak Saver quantity increases by one.
- A coin transaction record is created.

### Successful Streak Saver Usage

- One Streak Saver is consumed for each protected missed day.
- The user's streak is preserved according to BR-002.
- A temporary notification is displayed if at least one Streak Saver is consumed.

### Cancelled or Failed Purchase

- The user's coin balance remains unchanged.
- The selected item remains locked if the purchase was not successful.
- The user's Streak Saver quantity remains unchanged if the purchase was not successful.
- No unsuccessful purchase transaction is recorded.

## 7. Validation and Error Handling

| Condition | Expected System Response |
|---|---|
| User has insufficient coins | Disable the Confirm button and display an insufficient balance message. |
| User attempts to purchase an already unlocked book or skin | Prevent duplicate purchases. |
| Book or skin purchase fails | Display an error message, preserve the coin balance and item ownership, and allow retrying. |
| Streak Saver purchase fails | Display an error message, preserve the coin balance and Streak Saver quantity, and allow retrying. |
| Coin deduction fails | Do not unlock the selected item or increase the Streak Saver quantity. |
| Item unlocking fails | Ensure the purchase is not partially completed or charged. |
| Active skin cannot be saved | Display an appropriate error message and retain the previously active skin. |

## 8. Business Rules

- Coin prices, balance restrictions, and transaction recording follow **BR-003: Coin Reward Rules**.
- Purchased books and app skins remain permanently unlocked for the user.
- Each book or skin can only be purchased once per account.
- Only unlocked skins can be applied.
- Applying a skin requires explicit confirmation through the Apply button.
- The currently applied skin is saved in the database and restored when the user reopens the application.
- Streak Saver purchasing and usage follow **BR-002: Reading Streak Qualification** and **BR-003: Coin Reward Rules**.