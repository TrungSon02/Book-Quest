"""Read-only, standalone similarity test for the Book-Quest 20-book SQLite seed.

Put schema.sql and seed_20_books.sql beside this script, install
sentence-transformers and numpy, then run it with Python.
"""

from pathlib import Path
import os
import sqlite3

ROOT = Path(__file__).resolve().parent
# Keep Hugging Face and model downloads beside this script, on D: when installed there.
os.environ.setdefault("HF_HOME", str(ROOT / "huggingface_cache"))

import numpy as np
from sentence_transformers import SentenceTransformer


def load_books():
    """Load the catalog into an in-memory SQLite database (no files written)."""
    with sqlite3.connect(":memory:") as connection:
        connection.row_factory = sqlite3.Row
        for name in ("schema.sql", "seed_20_books.sql"):
            path = ROOT / name
            if not path.is_file():
                raise FileNotFoundError(
                    f"Missing {name}. Download it from the feat/database branch "
                    f"and place it in {ROOT}"
                )
            connection.executescript(path.read_text(encoding="utf-8"))

        rows = connection.execute(
            """
            SELECT b.book_id, b.title, b.author_name, b.description,
                   b.english_level,
                   GROUP_CONCAT(g.genre_name, ', ') AS genres
            FROM books AS b
            LEFT JOIN book_genres AS bg ON bg.book_id = b.book_id
            LEFT JOIN genres AS g ON g.genre_id = bg.genre_id
            GROUP BY b.book_id, b.title, b.author_name, b.description,
                     b.english_level
            ORDER BY b.book_id
            """
        ).fetchall()
    return [dict(row) for row in rows]


def main():
    books = load_books()
    print(f"Loaded {len(books)} books from Book-Quest's SQLite seed.")
    if len(books) != 20:
        print("Note: Expected 20 books in the original database seed.")

    # Description, title and genre are inputs to the pretrained semantic model.
    texts = [
        f"Title: {b['title']}. Genres: {b['genres'] or 'Unknown'}. "
        f"Description: {b['description']}"
        for b in books
    ]

    print("Loading all-MiniLM-L6-v2 (first run downloads the model)...")
    model = SentenceTransformer(
        "sentence-transformers/all-MiniLM-L6-v2",
        device="cpu",
        cache_folder=str(ROOT / "model_cache")
    )
    embeddings = model.encode(
        texts, convert_to_numpy=True, normalize_embeddings=True
    )
    print(f"Ready. Embedding matrix: {embeddings.shape}\n")

    id_to_index = {b["book_id"]: i for i, b in enumerate(books)}

    while True:
        print("Available books:")
        for book in books:
            print(f"{book['book_id']:>2}. {book['title']}")
        choice = input("\nEnter a book ID (or q to quit): ").strip()
        if choice.lower() in {"q", "quit", "exit"}:
            break
        try:
            book_id = int(choice)
        except ValueError:
            print("Please enter a numeric book ID.\n")
            continue
        if book_id not in id_to_index:
            print("Unknown book ID.\n")
            continue

        selected_index = id_to_index[book_id]
        scores = embeddings @ embeddings[selected_index]
        ranked_indices = np.argsort(scores)[::-1]

        print(f"\nTop 5 books similar to: {books[selected_index]['title']}")
        shown = 0
        for raw_index in ranked_indices:
            i = int(raw_index)
            if i == selected_index:
                continue  # Never recommend the exact book selected
            book = books[i]
            shown += 1
            print(
                f"{shown}. {book['title']} | cosine similarity: {scores[i]:.3f}"
                f" | genres: {book['genres']} | level: {book['english_level']}"
            )
            if shown == 5:
                break
        print("\nScores measure text similarity, not probability or reader preference.\n")


if __name__ == "__main__":
    main()
