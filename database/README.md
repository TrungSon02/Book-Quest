# Book Quest Database (SQL Only)

SQL implementation of all **8 schema groups (26 tables)** from the Book Quest specification, seeded **only for Group 2 (Book Catalog)** with **20 books** and EPUB `file_url` paths pointing to `book_data/`.

## Files

- **PostgreSQL** (matches exact diagram types including `BIGINT`, `JSONB`, `SMALLINT[]`, `TIMESTAMP`, `DATE`, `TIME`, `DECIMAL`):
  - `database/schema.sql` — creates all 26 tables, constraints, and indexes
  - `database/seed_20_books.sql` — inserts Group 2 data only (`genres`, `books`, `book_genres`, `chapters`, `book_files`) for 20 books
- **SQLite** (ready for local SQLite / Android `SQLiteOpenHelper`):
  - `database/sqlite/schema.sql` — creates all 26 tables, constraints, and indexes
  - `database/sqlite/seed_20_books.sql` — inserts Group 2 data only (`genres`, `books`, `book_genres`, `chapters`, `book_files`) for 20 books
- **`book_data/`** — directory for the 20 `.epub` files referenced by `book_files.file_url`

## Tables Created (26)

1. **Group 1 — User Management**: `users`, `user_genres`, `user_books`
2. **Group 2 — Book Catalog (Seeded for 20 books)**: `genres`, `books`, `book_genres`, `chapters`, `book_files`
3. **Group 3 — Reading System**: `user_library`, `reading_sessions`, `reading_progress`
4. **Group 4 — Quiz System**: `quizzes`, `questions`, `question_options`, `quiz_attempts`, `user_answers`
5. **Group 5 — Reading Habit System**: `habit_programs`, `habit_program_days`, `user_habit_enrollments`, `daily_reading_activity`
6. **Group 6 — Gamification System**: `coin_transactions`, `rewards`, `reward_redemptions`
7. **Group 7 — AI Recommendation System**: `recommendation_runs`, `recommendation_items`
8. **Group 8 — Reading Reminders**: `reading_reminders`

## Usage

### PostgreSQL
```bash
psql -U <user> -d <db> -f database/schema.sql
psql -U <user> -d <db> -f database/seed_20_books.sql
```

### SQLite
```bash
sqlite3 book_quest.db < database/sqlite/schema.sql
sqlite3 book_quest.db < database/sqlite/seed_20_books.sql
```
