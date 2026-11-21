-- Book Quest Database Schema (SQLite DDL)
-- Covers all 8 domain groups (26 tables) from the Book Quest ER specification.

PRAGMA foreign_keys = ON;

-- ============================================================================
-- Group 2 (Core Catalog Referenced by Group 1): Genres & Books
-- ============================================================================

CREATE TABLE genres (
    genre_id   INTEGER PRIMARY KEY AUTOINCREMENT,
    genre_name TEXT NOT NULL UNIQUE
);

CREATE TABLE books (
    book_id          INTEGER PRIMARY KEY AUTOINCREMENT,
    author_name      TEXT NOT NULL,
    title            TEXT NOT NULL,
    description      TEXT NOT NULL,
    english_level    TEXT NOT NULL CHECK (english_level IN ('A1', 'A2', 'B1', 'B2', 'C1', 'C2')),
    word_count       INTEGER NOT NULL CHECK (word_count >= 0),
    cover_image_url  TEXT NOT NULL,
    publication_date TEXT NOT NULL,
    created_at       TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- Group 1. User Management
-- ============================================================================

CREATE TABLE users (
    user_id            INTEGER PRIMARY KEY AUTOINCREMENT,
    username           TEXT NOT NULL,
    email              TEXT NOT NULL UNIQUE,
    password_hash      TEXT NOT NULL,
    english_level      TEXT NOT NULL CHECK (english_level IN ('A1', 'A2', 'B1', 'B2', 'C1', 'C2')),
    timezone           TEXT NOT NULL DEFAULT 'UTC',
    daily_reading_goal INTEGER NOT NULL DEFAULT 15 CHECK (daily_reading_goal > 0),
    preferred_language TEXT NOT NULL DEFAULT 'en',
    account_role       TEXT NOT NULL DEFAULT 'learner' CHECK (account_role IN ('learner', 'admin')),
    created_at         TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_genres (
    user_id  INTEGER NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    genre_id INTEGER NOT NULL REFERENCES genres(genre_id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, genre_id)
);

CREATE TABLE user_books (
    user_id INTEGER NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    book_id INTEGER NOT NULL REFERENCES books(book_id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, book_id)
);

-- ============================================================================
-- Group 2. Book Catalog (Junction, Chapters, Files)
-- ============================================================================

CREATE TABLE book_genres (
    book_id  INTEGER NOT NULL REFERENCES books(book_id) ON DELETE CASCADE,
    genre_id INTEGER NOT NULL REFERENCES genres(genre_id) ON DELETE CASCADE,
    PRIMARY KEY (book_id, genre_id)
);

CREATE TABLE chapters (
    chapter_id       INTEGER PRIMARY KEY AUTOINCREMENT,
    book_id          INTEGER NOT NULL REFERENCES books(book_id) ON DELETE CASCADE,
    chapter_number   INTEGER NOT NULL CHECK (chapter_number > 0),
    title            TEXT NOT NULL,
    word_count       INTEGER NOT NULL CHECK (word_count >= 0),
    epub_chapter_ref TEXT NOT NULL,
    UNIQUE (book_id, chapter_number)
);

CREATE TABLE book_files (
    file_id     INTEGER PRIMARY KEY AUTOINCREMENT,
    book_id     INTEGER NOT NULL REFERENCES books(book_id) ON DELETE CASCADE,
    format      TEXT NOT NULL CHECK (format IN ('EPUB', 'PDF')),
    file_url    TEXT NOT NULL,
    file_size   INTEGER NOT NULL CHECK (file_size >= 0),
    uploaded_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- Group 3. Reading System
-- ============================================================================

CREATE TABLE user_library (
    user_library_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id         INTEGER NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    book_id         INTEGER NOT NULL REFERENCES books(book_id) ON DELETE CASCADE,
    status          TEXT NOT NULL DEFAULT 'WANT_TO_READ' CHECK (status IN ('WANT_TO_READ', 'READING', 'COMPLETED')),
    is_favorite     INTEGER NOT NULL DEFAULT 0 CHECK (is_favorite IN (0, 1)),
    added_at        TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, book_id)
);

CREATE TABLE reading_sessions (
    session_id       INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id          INTEGER NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    book_id          INTEGER NOT NULL REFERENCES books(book_id) ON DELETE CASCADE,
    started_at       TEXT NOT NULL,
    ended_at         TEXT,
    duration_seconds INTEGER NOT NULL DEFAULT 0 CHECK (duration_seconds >= 0),
    words_read       INTEGER NOT NULL DEFAULT 0 CHECK (words_read >= 0),
    start_position   TEXT NOT NULL,
    end_position     TEXT
);

CREATE TABLE reading_progress (
    progress_id      INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id          INTEGER NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    book_id          INTEGER NOT NULL REFERENCES books(book_id) ON DELETE CASCADE,
    chapter_id       INTEGER REFERENCES chapters(chapter_id) ON DELETE SET NULL,
    current_position TEXT NOT NULL,
    progress_percent REAL NOT NULL DEFAULT 0.0 CHECK (progress_percent >= 0 AND progress_percent <= 100),
    last_read_at     TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, book_id)
);

-- ============================================================================
-- Group 4. Quiz System
-- ============================================================================

CREATE TABLE quizzes (
    quiz_id           INTEGER PRIMARY KEY AUTOINCREMENT,
    book_id           INTEGER NOT NULL REFERENCES books(book_id) ON DELETE CASCADE,
    chapter_id        INTEGER REFERENCES chapters(chapter_id) ON DELETE SET NULL,
    session_id        INTEGER REFERENCES reading_sessions(session_id) ON DELETE SET NULL,
    quiz_type         TEXT NOT NULL CHECK (quiz_type IN ('BOOK', 'CHAPTER', 'SESSION')),
    generation_source TEXT NOT NULL CHECK (generation_source IN ('AI', 'ADMIN')),
    generation_model  TEXT,
    created_at        TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE questions (
    question_id   INTEGER PRIMARY KEY AUTOINCREMENT,
    quiz_id       INTEGER NOT NULL REFERENCES quizzes(quiz_id) ON DELETE CASCADE,
    question_text TEXT NOT NULL,
    question_type TEXT NOT NULL DEFAULT 'MULTIPLE_CHOICE',
    explanation   TEXT
);

CREATE TABLE question_options (
    option_id   INTEGER PRIMARY KEY AUTOINCREMENT,
    question_id INTEGER NOT NULL REFERENCES questions(question_id) ON DELETE CASCADE,
    option_text TEXT NOT NULL,
    is_correct  INTEGER NOT NULL DEFAULT 0 CHECK (is_correct IN (0, 1))
);

CREATE TABLE quiz_attempts (
    attempt_id      INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id         INTEGER NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    quiz_id         INTEGER NOT NULL REFERENCES quizzes(quiz_id) ON DELETE CASCADE,
    score           REAL NOT NULL DEFAULT 0.0,
    correct_answers INTEGER NOT NULL DEFAULT 0 CHECK (correct_answers >= 0),
    total_questions INTEGER NOT NULL CHECK (total_questions > 0),
    started_at      TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at    TEXT
);

CREATE TABLE user_answers (
    answer_id           INTEGER PRIMARY KEY AUTOINCREMENT,
    attempt_id          INTEGER NOT NULL REFERENCES quiz_attempts(attempt_id) ON DELETE CASCADE,
    question_id         INTEGER NOT NULL REFERENCES questions(question_id) ON DELETE CASCADE,
    selected_option_ids TEXT NOT NULL DEFAULT '[]',
    answer_text         TEXT,
    is_correct          INTEGER NOT NULL DEFAULT 0 CHECK (is_correct IN (0, 1))
);

-- ============================================================================
-- Group 5. Reading Habit System
-- ============================================================================

CREATE TABLE habit_programs (
    program_id    INTEGER PRIMARY KEY AUTOINCREMENT,
    program_name  TEXT NOT NULL,
    duration_days INTEGER NOT NULL CHECK (duration_days > 0),
    description   TEXT NOT NULL,
    is_active     INTEGER NOT NULL DEFAULT 1 CHECK (is_active IN (0, 1))
);

CREATE TABLE habit_program_days (
    program_day_id INTEGER PRIMARY KEY AUTOINCREMENT,
    program_id     INTEGER NOT NULL REFERENCES habit_programs(program_id) ON DELETE CASCADE,
    day_number     INTEGER NOT NULL CHECK (day_number > 0),
    goal_metric    TEXT NOT NULL CHECK (goal_metric IN ('PAGES', 'WORDS', 'MINUTES')),
    target_value   INTEGER NOT NULL CHECK (target_value > 0),
    UNIQUE (program_id, day_number)
);

CREATE TABLE user_habit_enrollments (
    enrollment_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id       INTEGER NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    program_id    INTEGER NOT NULL REFERENCES habit_programs(program_id) ON DELETE CASCADE,
    start_date    TEXT NOT NULL,
    end_date      TEXT NOT NULL,
    status        TEXT NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'COMPLETED', 'DROPPED'))
);

CREATE TABLE daily_reading_activity (
    activity_id     INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id         INTEGER NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    activity_date   TEXT NOT NULL,
    words_read      INTEGER NOT NULL DEFAULT 0 CHECK (words_read >= 0),
    reading_seconds INTEGER NOT NULL DEFAULT 0 CHECK (reading_seconds >= 0),
    pages_read      INTEGER NOT NULL DEFAULT 0 CHECK (pages_read >= 0),
    goal_completed  INTEGER NOT NULL DEFAULT 0 CHECK (goal_completed IN (0, 1)),
    UNIQUE (user_id, activity_date)
);

-- ============================================================================
-- Group 6. Gamification System
-- ============================================================================

CREATE TABLE coin_transactions (
    transaction_id   INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id          INTEGER NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    amount           INTEGER NOT NULL,
    transaction_type TEXT NOT NULL,
    description      TEXT NOT NULL,
    idempotency_key  TEXT NOT NULL UNIQUE,
    created_at       TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE rewards (
    reward_id    INTEGER PRIMARY KEY AUTOINCREMENT,
    reward_name  TEXT NOT NULL,
    reward_type  TEXT NOT NULL,
    coin_price   INTEGER NOT NULL CHECK (coin_price > 0),
    description  TEXT NOT NULL,
    image_url    TEXT NOT NULL,
    is_available INTEGER NOT NULL DEFAULT 1 CHECK (is_available IN (0, 1))
);

CREATE TABLE reward_redemptions (
    redemption_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id       INTEGER NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    reward_id     INTEGER NOT NULL REFERENCES rewards(reward_id) ON DELETE RESTRICT,
    coins_spent   INTEGER NOT NULL CHECK (coins_spent > 0),
    redeemed_at   TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- Group 7. AI Recommendation System
-- ============================================================================

CREATE TABLE recommendation_runs (
    run_id              INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id             INTEGER NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    model_name          TEXT NOT NULL,
    recommendation_type TEXT NOT NULL,
    created_at          TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE recommendation_items (
    item_id               INTEGER PRIMARY KEY AUTOINCREMENT,
    run_id                INTEGER NOT NULL REFERENCES recommendation_runs(run_id) ON DELETE CASCADE,
    book_id               INTEGER NOT NULL REFERENCES books(book_id) ON DELETE CASCADE,
    rank                  INTEGER NOT NULL CHECK (rank > 0),
    relevance_score       REAL NOT NULL CHECK (relevance_score >= 0 AND relevance_score <= 1),
    recommendation_reason TEXT NOT NULL,
    user_feedback         TEXT
);

-- ============================================================================
-- Group 8. Reading Reminders
-- ============================================================================

CREATE TABLE reading_reminders (
    reminder_id   INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id       INTEGER NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    reminder_time TEXT NOT NULL,
    days_of_week  TEXT NOT NULL DEFAULT '[]',
    is_enabled    INTEGER NOT NULL DEFAULT 1 CHECK (is_enabled IN (0, 1)),
    created_at    TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- Indexes for Foreign Keys & Common Queries
-- ============================================================================

CREATE INDEX idx_books_english_level ON books(english_level);
CREATE INDEX idx_chapters_book_id ON chapters(book_id);
CREATE INDEX idx_book_files_book_id ON book_files(book_id);
CREATE INDEX idx_reading_sessions_user_book ON reading_sessions(user_id, book_id);
CREATE INDEX idx_quizzes_book_id ON quizzes(book_id);
CREATE INDEX idx_questions_quiz_id ON questions(quiz_id);
CREATE INDEX idx_question_options_question_id ON question_options(question_id);
CREATE INDEX idx_quiz_attempts_user_quiz ON quiz_attempts(user_id, quiz_id);
CREATE INDEX idx_user_answers_attempt_id ON user_answers(attempt_id);
CREATE INDEX idx_coin_transactions_user_id ON coin_transactions(user_id);
CREATE INDEX idx_reward_redemptions_user_id ON reward_redemptions(user_id);
CREATE INDEX idx_recommendation_runs_user_id ON recommendation_runs(user_id);
CREATE INDEX idx_recommendation_items_run_id ON recommendation_items(run_id);
CREATE INDEX idx_reading_reminders_user_id ON reading_reminders(user_id);
