# Sinh câu hỏi đọc hiểu tiếng Anh bằng Gemma 4 31B

Book Quest dùng đoạn văn tiếng Anh làm đầu vào để thử sinh một câu trắc nghiệm đọc hiểu với bốn lựa chọn, đáp án, giải thích và dẫn chứng. Có thể thử trực tiếp trên máy Windows bằng llama.cpp.

## Tải llama.cpp và model

1. Trên máy Windows x64 dùng GPU NVIDIA, tải hai gói **Windows x64 (CUDA 12)** và **CUDA 12.4 DLLs**. Giải nén cả hai vào cùng thư mục có `llama-server.exe`, ví dụ `C:\AI\llama.cpp`. [llama.cpp build b12322](https://github.com/ggml-org/llama.cpp/releases/tag/b12322)
2. Tải file `gemma-4-31B-it-IQ4_XS.gguf` (khoảng 16,4 GB) và lưu vào thư mục riêng, ví dụ `C:\AI\models`. [Tải Gemma 4 31B bản IQ4_XS](https://huggingface.co/unsloth/gemma-4-31B-it-GGUF/resolve/54289fb57eeb7d7b8cb5173a10cb21e2f644ed78/gemma-4-31B-it-IQ4_XS.gguf)

## Chạy trên Windows

Mở PowerShell, sửa hai đường dẫn đầu cho khớp với máy rồi chạy lệnh.

```powershell
$gemmaModel = 'C:\Users\Admin\Documents\Codex\2026-11-12\find-long-s-final-task-research\work\models\gemma-4-31B-it-IQ4_XS.gguf'

& 'D:\Dowloads\llmama_cpp\llama-server.exe' -m $gemmaModel -a 'gemma-4-31B-it-IQ4_XS' -c 12288 -np 1 --fit on --fit-ctx 12288 --fit-target 1536 -fa on -n 1800 --reasoning on --reasoning-budget 1000 --reasoning-budget-message 'Stop drafting. Output only the final quiz in the requested format.' --temp 0 --host 127.0.0.1 --port 8080
```

Lệnh trên đặt context 32.768 token và bật thinking. Trên máy có 8 GB VRAM, llama.cpp dùng thêm RAM để chạy model. Khi terminal báo `listening`, mở [giao diện llama.cpp](http://127.0.0.1:8080).

Trên RTX 4070 Laptop 8 GB VRAM và 32 GB RAM, hai lượt thử trước mất khoảng 2–3 phút mỗi lượt, tính cả thời gian nạp model. Số đo đó dùng context 4.096 token và tắt thinking.

## Tạo câu hỏi qua hai lượt

Người đọc có thể tìm đủ căn cứ trong một đoạn, nhưng cần phân biệt đúng người, lý do hoặc thời điểm để chọn đáp án.

1. **Lọc dữ kiện.** Đánh số các đoạn văn tiếng Anh bằng `[1]`, `[2]`, … rồi thay `{{PASSAGE}}` trong [select_prompt.txt](./select_prompt.txt). Gửi vào chat mới trong llama.cpp. Model trả JSON gồm câu hỏi dự kiến, cụm trả lời `answer_span` và một trích dẫn có số đoạn. Kiểm tra trích dẫn có nguyên văn trong đoạn và chứa cụm trả lời.
2. **Viết và kiểm tra câu hỏi.** Thay `{{SELECTION}}` trong [prompt.txt](./prompt.txt) bằng JSON đã qua bước 1. Thay `{{PASSAGE}}` bằng văn bản đã đánh số, rồi gửi vào một chat mới. Model trả JSON gồm câu hỏi, bốn lựa chọn, `correct_index` (0–3, tương ứng A–D), lời giải và trích dẫn. Đọc lại bốn lựa chọn để xác nhận chỉ một phương án trả lời được câu hỏi.

Nếu model trả `INSUFFICIENT_PASSAGE` ở một trong hai lượt, thử đoạn có hành động, lý do hoặc kết quả rõ hơn.
