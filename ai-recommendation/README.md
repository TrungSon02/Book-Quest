# AI Book Recommendation

## AI Model

**Model:** [all-MiniLM-L6-v2](https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2)

The model converts book information into 384-dimensional numerical vectors (embeddings). Cosine similarity is then used to compare these embeddings and identify the five most similar books.

The model is downloaded automatically from Hugging Face when the program runs for the first time. No additional training or GPU is required.

## Requirements

- Python 3.12
- PyTorch
- Sentence Transformers
- NumPy

## Installation

Open PowerShell in the `ai-recommendation` folder.

**1. Create a Python virtual environment:**

```powershell
py -3.12 -m venv .venv
```

**2. Install the required libraries:**

```powershell
.\.venv\Scripts\python.exe -m pip install -r requirements.txt
```

## Run the Recommendation System

Run the following command:

```powershell
.\.venv\Scripts\python.exe recommend_books.py
```

The program automatically:

1. Loads the 20 books from `schema.sql` and `seed_20_books.sql`.
2. Extracts each book's title, description, and genres.
3. Generates embeddings using MiniLM.
4. Calculates cosine similarity between books.
5. Displays the available books and prompts the user to enter a book ID.
6. Returns the five most similar books with their similarity scores.

## Example Usage

After the model finishes loading, the program displays a list of 20 books.

Enter a book ID, for example:

```text
6
```

This selects **The Hobbit**.

The system calculates similarity scores against the other books and displays the five highest-ranked recommendations.

Enter another book ID to get new recommendations, or enter `q` to exit.

## Project Files

```text
ai-recommendation/
├── recommend_books.py
├── schema.sql
├── seed_20_books.sql
├── requirements.txt
├── README.md
└── .gitignore
```

- `recommend_books.py`: Main AI recommendation program.
- `schema.sql`: SQLite database schema.
- `seed_20_books.sql`: Sample catalog containing 20 books.
- `requirements.txt`: Required Python dependencies.
- `.gitignore`: Excludes virtual environments, cached models, and temporary files.