# BR-001: Book Quest Application

## 1. Project Overview

### 1.1 Purpose

Book Quest is an Android eBook reading application designed primarily to help users develop and maintain a consistent reading habit. Although many people recognize the benefits of reading, they often struggle to start reading regularly due to a lack of motivation or the perception that reading requires too much time and effort.

Book Quest addresses this problem by making reading more approachable and rewarding through habit-building techniques, particularly the 2-Minute Rule. The application also aims to help users improve their English reading comprehension through AI-generated questions based on the content they have read.

### 1.2 Primary Goal

To help users establish a sustainable reading habit by reducing the initial effort required to start reading and encouraging consistent engagement through achievable daily goals and positive reinforcement.

### 1.3 Secondary Goal

To support users in improving their English reading comprehension through interactive, AI-generated questions that evaluate their understanding of previously read content.

### 1.4 Target Audience

Book Quest primarily targets:

- Individuals who want to develop and maintain a regular reading habit.
- Individuals who want to improve their English reading comprehension skills.

## 2. Project Scope

### 2.1 Core Features

Book Quest must provide the following core features:

1. **User Accounts:** Allow users to register, log in, and manage their accounts.
2. **eBook Library and Reading:** Allow users to browse, select, and read available English eBooks.
3. **AI-Generated Questions:** Generate comprehension questions based on recently read content to evaluate users' understanding.
4. **Coin System:** Reward users for reading-related activities and achievements. Coins can be accumulated and spent on selected in-app features.
5. **21-Day Habit Formation Mode:** Apply the 2-Minute Rule through gradually increasing daily reading targets over a 21-day period.
6. **Achievements, Daily Challenges, and Rewards:** Provide achievements and daily challenges that reward users with coins. Users can spend accumulated coins on various in-app rewards, such as unlocking application skins and popular books.

### 2.2 Supplementary Features

The application will also provide supporting features to enhance the overall reading experience:

- **AI Book Recommendations:** Recommend books based on users' preferences and reading-related information.
- **Reading Preferences and Settings:** Allow users to configure their reading preferences and application settings.
- **Reading Reminders:** Remind users to maintain their reading habits.
- **Reading Progress Tracking:** Track users' reading progress and provide relevant statistics.
- **Reading Streak Tracking:** Track consecutive days of qualifying reading activity to encourage consistency.

### 2.3 Out of Scope

The following functionalities are explicitly excluded from the project's scope:

- Social features, such as following friends or sharing reading progress.
- Publishing or uploading books as an author.
- Purchasing eBooks using real money.
- Audiobook functionality.
- An iOS application version.
- Converting earned coins into real money.
- Downloading books for offline reading.

## 3. Business Approach

### 3.1 Habit Formation Through the 2-Minute Rule

Book Quest uses the 2-Minute Rule to make reading easier to start and gradually turn it into a consistent habit.

The 21-Day Habit Formation Mode follows these principles:

- The daily activity requirements gradually progress from simple actions, such as opening the application, to reading a minimum number of pages.
- The daily reading targets gradually increase as users progress through the 21-day period.
- Users are free to read beyond the minimum daily target.
- Completing the applicable daily activity requirement is sufficient to maintain the user's reading streak.
- After completing the 21-day mode, users automatically transition to normal reading mode, where answering at least one comprehension question correctly is required to maintain their reading streak.

The increasing requirements are intended to encourage users to develop their reading habits naturally, rather than force them to read more than they want to.

### 3.2 Improving Reading Comprehension

Book Quest uses AI-generated comprehension questions to encourage active reading and evaluate users' understanding.

After users read a certain number of pages, the application presents a randomly generated question based on recently read content. This encourages users to pay attention to what they read and helps them assess their comprehension.

### 3.3 Gamification and Motivation

Book Quest incorporates a coin-based reward system to make reading more engaging.

Users can earn coins through reading-related activities, achievements, and daily challenges. Accumulated coins can then be used to unlock application skins and selected books.

These rewards aim to provide positive reinforcement and encourage users to maintain their reading habits.

## 4. Success Criteria and Metrics

### 4.1 Reading Habit Formation

The success of the primary goal can be evaluated through the following indicators:

- Users consistently complete their minimum daily reading targets.
- Users maintain their reading streaks.
- Users gradually increase their reading amount during the 21-day mode.
- Users continue reading regularly after completing the 21-day mode.

### 4.2 English Reading Comprehension

The success of the secondary goal can be evaluated through:

- Users correctly answer AI-generated comprehension questions.
- Users demonstrate improved question accuracy over time.
- Users can read and understand books appropriate to their English proficiency levels.

### 4.3 Overall Project Success

The project will be considered successful if:

- All planned features work correctly.
- Kotlin, the database, and AI components are successfully integrated.
- The application provides a usable and coherent reading experience.

## 5. Technical Constraints

- **Platform:** Android.
- **Programming Language:** Kotlin.
- **UI Framework:** Jetpack Compose.
- **Content Language:** English eBooks only.
- **AI Integration:** The team plans to integrate an offline AI model packaged locally with the application.

The exact AI integration approach may be refined during implementation.
