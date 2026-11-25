# Loạn 12 Sứ Quân HD — Cổ Phong Hoàng Kim (PC LibGDX & Web / iPhone 17 PWA — v2.1.4-HD)

Dự án làm lại (**HD Remake**) đa nền tảng từ tựa game lịch sử Việt Nam **Loạn 12 Sứ Quân** (`v1.19.3`), dùng chung 100% logic chiến thuật RPG Match-3 và bộ vẽ Vector HD **`720×960` (`3×` Native HD)** trên cả hai nền tảng:
1. **PC Desktop (`LibGDX 1.12.1` / OpenGL LWJGL3)**: Cửa sổ HD `720×960` & Toàn màn hình `F11` @ 60 FPS.
2. **Web HTML5 & iPhone 17 (`TeaVM` + `Canvas2D` + `WebAudio PWA`)**: Chơi mượt 60 FPS trên màn hình **120Hz ProMotion** của **iPhone 17** (`Safari` hoặc cài thành **App toàn màn hình ngoài Màn hình chính** tự động né **Dynamic Island** & thanh **Home Indicator**, hỗ trợ chơi Offline 100% và sao lưu `.sqhd`).

---

## 1. Cách chơi ngay trên iPhone 17 & Trình duyệt Web

### Cách A: Chơi thử tức thì qua mạng Wi-Fi nội bộ (LAN)
1. Đảm bảo máy tính PC và **iPhone 17** đang kết nối **cùng một mạng Wi-Fi**.
2. Chạy tệp **`Run-Web-iPhone.cmd`** (hoặc chọn cấu hình **`Gradle: Chạy Web & iPhone 17 (LAN Wi-Fi)`** trên **IntelliJ IDEA** rồi bấm **Run ▶**).
3. Cửa sổ dòng lệnh sẽ in ra địa chỉ LAN của máy tính (ví dụ: `http://192.168.1.x:8080`).
   - *Lưu ý*: Nếu Windows hỏi quyền **Windows Defender Firewall** cho `java.exe`, hãy tích chọn **Private networks** và bấm **Allow access** để iPhone truy cập được.
4. Mở **Safari** trên **iPhone 17**, nhập địa chỉ `http://192.168.1.x:8080` để vào thẳng game!

### Cách B: Cài thành App toàn màn hình trên Màn hình chính iPhone 17 (PWA)
1. Khi đã mở trang game trên **Safari** của **iPhone 17**, chạm nút **`Cài App`** trên thanh công cụ phía trên (hoặc chạm biểu tượng **Chia sẻ `⬆`** ở thanh dưới của Safari).
2. Chọn **Thêm vào Màn hình chính** (*Add to Home Screen*) -> bấm **Thêm** (*Add*).
3. Mở biểu tượng **12 Sứ Quân HD** ngoài màn hình chính:
   - Hiển thị **toàn màn hình** không còn thanh địa chỉ Safari.
   - Tự động căn lề an toàn (`safe-area-inset`) né cụm **Dynamic Island** phía trên và thanh **Home Indicator** phía dưới của iPhone 17.
   - Tự động lưu bộ nhớ đệm qua **Service Worker (`sw.js`)** để chơi **Offline 100%** (khi đưa lên HTTPS host).
   - Bấm nút **`Lưu trữ`** trên thanh công cụ bất kỳ lúc nào để xuất/nhập tệp sao lưu **`.sqhd`** vào ứng dụng **Tệp (iCloud Drive)**.

### Cách C: Đóng gói & Đưa lên Cloud miễn phí (Cloudflare Pages / Netlify / GitHub Pages)
1. Đóng gói thư mục Web tĩnh tự thân (`dist/web`):
   ```cmd
   Build-Web-HD.cmd
   ```
   *(Hoặc chạy lệnh `.\gradlew.bat :web:buildWebDist`)*
2. Toàn bộ bản Web hoàn chỉnh nằm gọn trong thư mục **`E:\Loan12suquanHD\dist\web`** (đã kèm sẵn `_headers`, `netlify.toml`, `.nojekyll`, `manifest.webmanifest`, `sw.js`, `assets-pack.json`, `js/classes.js` và bộ icon Retina):
   - **Cloudflare Pages / Netlify Drop**: Chỉ cần kéo-thả trực tiếp thư mục **`dist/web`** lên trang quản trị là có ngay đường link `https://...` tốc độ cao để mở & cài PWA trên iPhone 17 ở bất kỳ đâu!
   - **GitHub Pages (Tự động qua GitHub Actions)**: Dự án đã tích hợp sẵn workflow `.github/workflows/deploy-pages.yml`. Sau khi đẩy mã nguồn lên GitHub, chỉ cần vào **Settings → Pages → Build and deployment → Source: chọn `GitHub Actions`**, hệ thống sẽ tự động biên dịch `dist/web` và xuất bản đường link HTTPS (`https://<tài-khoản>.github.io/<tên-repo>/`) để mở & cài App PWA trực tiếp trên **iPhone 17** (đồng thời đính kèm gói JAR cho PC trong tab **Actions**).

---

## 2. Cách mở và chạy bản PC (LibGDX OpenGL) trên IntelliJ IDEA

1. Mở **IntelliJ IDEA** -> chọn **Open** -> trỏ tới thư mục **`E:\Loan12suquanHD`**.
2. Ở góc trên bên phải của IntelliJ, chọn một trong các Run Configuration đã cấu hình sẵn:
   - **`Chạy Loạn 12 Sứ Quân HD (LibGDX)`** *(Application — chạy trực tiếp bản PC OpenGL)*
   - **`Gradle: Chạy Loạn 12 Sứ Quân HD`** *(Gradle `:run`)*
   - **`Gradle: Chạy Web & iPhone 17 (LAN Wi-Fi)`** *(Gradle `:web:serveWebLan`)*
3. Hoặc chạy trực tiếp từ dòng lệnh:
   ```cmd
   Build-And-Run-HD.cmd
   ```

---

## 3. Các nâng cấp Đồ họa, Âm thanh & Đa Nền Tảng (`v2.0.0-HD`)

- **Kiến trúc Đồ họa Đa Nền Tảng (`HdCanvas2D`, `HdSurface`, `HdPath`, `HdPlatformBackend`)**: Tách biệt hoàn toàn tầng kết xuất Vector HD giữa **Desktop (`Java2D` + `OpenGL`)** và **Web/iPhone 17 (`HTML5 Canvas2D` + `Path2D`)** mà dùng chung 100% mã nguồn giao diện, bàn cờ và gameplay.
- **Tương thích màn hình 120Hz ProMotion trên iPhone 17**: Bộ điều nhịp tích lũy khung hình chuẩn 60 FPS giúp tốc độ rơi ngọc, hiệu ứng nổ và chuyển cảnh chạy chính xác tuyệt đối trên cả màn hình 60Hz lẫn 120Hz ProMotion.
- **Bàn cờ Ngọc Vector HD Bám Sát Thiết Kế Gốc (`66×66` / ô)**: Dựng mới bằng Vector sắc nét bám sát 100% thiết kế gốc cho cả 7 viên ngọc (*Kiếm Bạc, Bánh Chưng Xanh buộc lạt vàng, Trái Tim Hồng Ngọc, Âm Dương Đỏ-Lam, Kim Nguyên Bảo, Binh Thư Tím buộc lụa xanh & Kiếm Lửa*).
- **Âm thanh WebAudio Cổ Phong & SFX PCM 44.1 kHz**: Tự động mở khóa âm thanh ngay lần chạm đầu tiên trên iOS Safari, giải mã trực tiếp nhạc nền MIDI đa âm sắc (Đàn Tranh / Sáo Trúc / Trống trận Đông Sơn) kết hợp hiệu ứng chiến đấu 44.1 kHz sắc nét.

---

## 4. Phím tắt điều khiển (PC & Bàn phím rời)

| Phím / Thao tác | Chức năng |
| :--- | :--- |
| **Chạm / Vuốt cảm ứng (iPhone 17)** | Chạm chọn menu/thành trì 1 chạm, vuốt trực tiếp giữa 2 ô ngọc để tráo, vuốt dọc để cuộn hộp thoại |
| **Mũi tên** hoặc **`W / A / S / D`** | Di chuyển con trỏ bàn cờ / đi lại trên Bản đồ / cuộn danh sách |
| **`Enter` / `Space`** | Phím chọn chính (Giữa) — Chọn ngọc / Xác nhận |
| **`Q` / `F1` / `Tab`** | Phím chức năng Trái (`T.Chiêu` / Menu) |
| **`E` / `Esc` / `Chuột phải`** | Phím chức năng Phải (`T.Dừng` / Đóng / Trở về) |
| **`F2`** | Chuyển đổi nhanh 3 chế độ đồ họa: **`60FPS | HD Mới`** ↔ **`60FPS | HD Pixel`** ↔ **`60FPS | Cổ điển`** |
| **`F3`** | Chuyển nhanh mức Âm lượng trực tiếp (`100% → 70% → 35% → Tắt`) |
| **`F4` / `F11`** *(PC LibGDX)* | Bật/Tắt hạt ánh sáng OpenGL / Chuyển đổi Toàn màn hình |
