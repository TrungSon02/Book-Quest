# Sinh câu hỏi đọc hiểu tiếng Anh bằng Gemma 4 31B

Book Quest dùng đoạn văn tiếng Anh làm đầu vào để thử sinh một câu trắc nghiệm đọc hiểu với bốn lựa chọn, đáp án, giải thích và dẫn chứng. Có thể thử trực tiếp trên máy Windows bằng llama.cpp.

## Tải llama.cpp và model

1. Trên máy Windows x64 dùng GPU NVIDIA, tải hai gói **Windows x64 (CUDA 12)** và **CUDA 12.4 DLLs**. Giải nén cả hai vào cùng thư mục có `llama-server.exe`, ví dụ `C:\AI\llama.cpp`. [llama.cpp build b12322](https://github.com/ggml-org/llama.cpp/releases/tag/b12322)
2. Tải file `gemma-4-31B-it-IQ4_XS.gguf` (khoảng 16,4 GB) và lưu vào thư mục riêng, ví dụ `C:\AI\models`. [Tải Gemma 4 31B bản IQ4_XS](https://huggingface.co/unsloth/gemma-4-31B-it-GGUF/resolve/54289fb57eeb7d7b8cb5173a10cb21e2f644ed78/gemma-4-31B-it-IQ4_XS.gguf)

## Chạy trên Windows

Mở PowerShell, sửa hai đường dẫn đầu cho khớp với máy rồi chạy lệnh.

```powershell
$llamaExe = 'C:\AI\llama.cpp\llama-server.exe'
$modelPath = 'C:\AI\models\gemma-4-31B-it-IQ4_XS.gguf'

& $llamaExe -m $modelPath -c 32768 -np 1 --fit on --fit-ctx 32768 --fit-target 1024 -fa on --reasoning on --temp 0 --host 127.0.0.1 --port 8080
```

Lệnh trên đặt context 32.768 token và bật thinking. Trên máy có 8 GB VRAM, llama.cpp dùng thêm RAM để chạy model. Khi terminal báo `listening`, mở [giao diện llama.cpp](http://127.0.0.1:8080).

Trên RTX 4070 Laptop 8 GB VRAM và 32 GB RAM, hai lượt thử trước mất khoảng 2–3 phút mỗi lượt, tính cả thời gian nạp model. Số đo đó dùng context 4.096 token và tắt thinking.

## Dùng prompt

1. Mở [prompt.txt](./prompt.txt) và sao chép toàn bộ nội dung.
2. Thay `{{PASSAGE}}` dưới dòng `PASSAGE` bằng đoạn văn tiếng Anh cần kiểm tra. Nếu đoạn dài, bắt đầu với khoảng 1.500 từ.
3. Dán vào một cuộc trò chuyện mới trong giao diện llama.cpp và gửi.
4. Đối chiếu hai trích dẫn với văn bản gốc và kiểm tra chỉ một phương án trả lời được câu hỏi. Nếu model trả `INSUFFICIENT_PASSAGE`, thử một đoạn có thêm ngữ cảnh.
