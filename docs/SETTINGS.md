# SETTINGS.md

Tài liệu này liệt kê (1) các khóa Prefs hiện đang dùng trong dự án và (2) đề xuất mở rộng những nhóm cấu hình có thể đưa lên trang Cài đặt (Settings Panel) để người dùng cuối / admin tùy chỉnh giao diện & hành vi hệ thống.

---
## 1. Các khóa hiện có (Preferences Keys ĐÃ sử dụng)
| Nhóm | Key | Kiểu | Ý nghĩa | Vị trí sử dụng |
|------|-----|------|---------|----------------|
| Giao diện | `ui.darkTheme` | boolean | Bật/tắt Dark Mode | `SettingsPanel` + đọc sớm trong `MainFrame` |
| Giao diện | `ui.fontScalePercent` | int (80..160) | Tỷ lệ phóng font (%) – ĐÃ áp dụng toàn hệ thống qua cập nhật UIManager | `SettingsPanel` + `MainFrame.applyGlobalFontScale()` |
| Âm thanh | `sound.enabled` | boolean | Bật âm báo bắt đầu/kết thúc trận | `SettingsPanel` + `SoundPlayer` |
| Âm thanh | `sound.start.path` | string | Đường dẫn tuyệt đối file WAV phát khi bắt đầu trận | `SettingsPanel` |
| Âm thanh | `sound.end.path` | string | Đường dẫn tuyệt đối file WAV phát khi kết thúc trận | `SettingsPanel` |
| Giao diện | `ui.alwaysOnTop` | boolean | Giữ cửa sổ chính luôn trên cùng (tạm thời) | `SettingsPanel` → `MainFrame.applyAlwaysOnTopFloating()` |
| Giải đấu | `selectedGiaiDauId` | int | ID giải đang được chọn làm ngữ cảnh làm việc | Nhiều panel: đăng ký nội dung, đăng ký đội, control panel, repository... |
| Giải đấu | `selectedGiaiDauName` | String | Tên giải đang chọn (dùng hiển thị) | `GiaiDauSelectPanel`, `DangKyDoiPanel`, `DangKyNoiDungPanel` |
| Người dùng | `userId` | int | ID người dùng đã đăng nhập (lưu khi auth) | `AuthService`, `GiaiDauDialog` |
| Monitor | `monitor.columns` | int (1..4) | Số cột hiển thị lưới giám sát trận | `MonitorTab` (đã mở rộng) + `SettingsPanel` |
| Bố cục điều khiển | `split.main` | int | Divider location chính | `BadmintonControlPanel.saveSplitLocations()` |
| Bố cục điều khiển | `split.centerRight` | int | Divider khu trung tâm/phải | `BadmintonControlPanel` |
| Bố cục điều khiển | `split.leftVert` | int | Divider panel trái | `BadmintonControlPanel` |
| Bố cục điều khiển | `split.midVert` | int | Divider panel giữa | `BadmintonControlPanel` |
| Bố cục điều khiển | `split.rightVert` | int | Divider panel phải | `BadmintonControlPanel` |

Ghi chú: trang Cài đặt đã có nút "Reset bố cục điều khiển" để xóa các key `split.*` (yêu cầu khởi động lại để chia lại mặc định). Font scale mới lưu Prefs nhưng chưa áp dụng cascade.

---
## 2. Nhóm cấu hình ĐỀ XUẤT mở rộng đưa vào Settings
Dưới đây là các nhóm hợp lý để bổ sung nhằm tăng khả năng cá nhân hóa và vận hành.

### 2.1 Giao diện (UI / Theme)
- [x] Dark Mode (đã có)
- [~] Kích cỡ font chung (spinner lưu `ui.fontScalePercent`, CHƯA áp dụng thật)  
- [ ] Hiển thị logo / ẩn logo đầu trang
- [x] Always-on-top (mới áp dụng frame chính; cần mở rộng cho viewer / dialog)
- [ ] Tùy chọn màu nhấn (accent color) nếu FlatLaf hỗ trợ custom accent

### 2.2 Trận đấu & Điều khiển
- [ ] Tự động focus ô nhập điểm sau khi ghi nhận (auto focus behavior)
- [ ] Bật/tắt xác nhận khi reset set / reset trận
- [ ] Âm thanh báo hoàn thành set (tệp WAV cấu hình)
- [ ] Thời gian timeout mặc định (giây)
- [ ] Hiển thị cảnh báo khi điểm chênh lệch >= X (highlight)
- [ ] Cho phép nhập điểm bằng phím tắt (phím cấu hình)

### 2.3 Giám sát (Monitor / Viewer)
- [x] Số cột tối đa (đã nâng lên 1..4)
- [ ] Khoảng refresh UI (ms) nếu dùng polling (hiện timer cố định 5000ms)
- [ ] Scale chữ lớn hơn cho màn hình trình chiếu (phần trăm)

### 2.4 Layout & Cửa sổ
- [x] Nút "Reset bố cục điều khiển" (xóa `split.*` keys) – ĐÃ có
- [ ] Ghi nhớ vị trí & kích thước cửa sổ chính (hiện chưa lưu)
- [ ] Ghi nhớ danh sách panel hiển thị lần cuối (nếu sau này cho phép ẩn/bật động)

### 2.5 Dữ liệu & Kết nối
- [ ] Hồ sơ kết nối SQL Server tùy chỉnh (host / port / db / user)
- [ ] Bật/tắt auto-connect khi khởi động
- [ ] Chọn network interface mặc định cho broadcast (thay vì truyền qua tham số main)

### 2.6 Tài khoản & Bảo mật
- [ ] Bật xác nhận 2 bước nội bộ (nếu sau này mở rộng)

### 2.7 Xuất / Ảnh / Logs
- [ ] Thư mục mặc định lưu ảnh chụp màn hình
- [ ] Định dạng tên file ảnh (pattern: scoreboard_{date}_{time}.png)
- [ ] Bật/tắt tự động chụp khi trận kết thúc
- [ ] Mức log hiển thị (INFO / WARN / ERROR / DEBUG)

### 2.8 Âm thanh & Thông báo
- [ ] Bật/tắt âm báo sự kiện (match start / match end / timeout)
- [ ] Âm báo tùy chọn (chọn file)
- [ ] Bật thông báo dạng popup khi có trận mới

### 2.9 Tối ưu hiệu năng
- [ ] Chu kỳ cập nhật bộ nhớ (RAM label) – hiện cố định 1000 ms
- [ ] Chế độ giảm tải monitor (giảm tần suất refresh tự động 5s)

---
## 3. Đề xuất cấu trúc UI trang Cài đặt
Chia làm accordion hoặc tabs trái/phải:
1. Giao diện
2. Thi đấu
3. Giám sát
4. Layout
5. Kết nối
6. Xuất & Logs
7. Nâng cao

Mỗi nhóm: panel con + mô tả ngắn + nút "Khôi phục mặc định" (chỉ xóa key nhóm đó).

---
## 4. Định dạng lưu trữ đề xuất
Tất cả vẫn có thể dùng chung lớp `Prefs`. Có thể chuẩn hóa prefix:
- `ui.*`
- `layout.*`
- `match.*`
- `monitor.*`
- `conn.*`
- `export.*`
- `sound.*`
- `perf.*`
- `net.*`

Ví dụ:
```
ui.fontScale = 1.0
match.confirmReset = true
monitor.columns = 3
export.screenshot.autoCapture = false
perf.lowPower = false
net.interface = eth0
```

---
## 5. Hành động triển khai kế tiếp (Roadmap ngắn)
1. Áp dụng thật font scale (duyệt UI, điều chỉnh `UIManager` defaults hoặc custom `FontUIResource`).
2. Mở rộng Always-on-top: áp dụng cho các cửa sổ viewer (MonitorWindow) & dialog quan trọng.
3. Chuẩn hóa binder (`SettingsBinder`) để giảm lặp code khi thêm control mới.
4. Thêm nhóm tab/accordion phân loại rõ ràng thay vì một cột dày đặc.
5. Thêm export/import JSON (backup cấu hình Prefs) – tránh phụ thuộc Windows Registry.
6. Thêm tùy chọn refresh interval cho Monitor + low-power mode.
7. Tùy chọn accent color / flatlaf extras (nếu cần brand).
8. Ghi nhớ vị trí & kích thước MainFrame (lưu `window.x,y,w,h,max`).
9. Áp dụng i18n (resource bundles) rồi bật chọn ngôn ngữ trong Settings.
10. Viết test đơn vị cho lớp tiện ích Prefs adapter (khi refactor binder).

---
## 6. Lưu ý kỹ thuật
- Java Preferences giới hạn kích thước value (thường ~8 KB) – không dùng lưu dữ liệu lớn.
- Nếu cần export/import cấu hình: có thể thêm tính năng “Xuất cấu hình” ghi ra JSON.
- Khi đổi LookAndFeel nên dùng FlatAnimatedLafChange để tránh nháy (đã áp dụng một phần).
- Với tuỳ chọn liên quan network/broadcast: cần validate trước khi apply (port hợp lệ 1024–65535,...).

---
## 7. Tóm tắt ngắn
Trang Cài đặt hiện đã có: Dark Mode, chỉnh số cột Monitor 1..4, lưu font scale (chưa áp dụng), Always-on-top (frame chính) và nút reset layout điều khiển. Các phần còn lại (font scale thực tế, accent color, refresh interval, export settings, i18n...) là bước kế tiếp để hoàn thiện trải nghiệm tùy chỉnh.

---
---
## 8. Thay đổi gần đây liên quan Settings (Changelog ngắn)
| Ngày | Thay đổi | Ghi chú |
|------|----------|---------|
| 2025-09-25 | Loại bỏ tab "Cài đặt" nội bộ trong `MonitorTab` | Mọi tùy chỉnh (số cột, Always-on-top, font scale) chuyển hoàn toàn sang `SettingsPanel` toàn cục. |
| 2025-09-25 | Chuẩn hóa lưu `monitor.columns` chỉ qua SettingsPanel | Tránh lệch trạng thái UI – không còn combo nội bộ. |
| 2025-09-25 | Font scale áp dụng thật (UIManager + updateComponentTreeUI) | Không còn nhân đôi scale, lưu cache font gốc để tái sử dụng. |
| 2025-09-25 | Thêm âm báo bắt đầu / kết thúc trận (WAV) | Keys: sound.enabled, sound.start.path, sound.end.path |
| 2025-09-25 | Viewer áp dụng Always-on-top + font scale khi mở | Sử dụng cùng prefs `ui.alwaysOnTop`, `ui.fontScalePercent`. |
| 2025-09-25 | Cải tiến join multicast | Ưu tiên interface người dùng chọn; nếu không có sẽ tự dò interface multicast hợp lệ, chỉ fallback API deprecated khi bất khả kháng. |

---
*Phiên bản tài liệu: 2025-09-25 (cập nhật 3)*
