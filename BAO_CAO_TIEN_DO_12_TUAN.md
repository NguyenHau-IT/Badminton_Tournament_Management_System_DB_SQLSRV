# Tiến độ tính năng theo tuần (12 tuần)

Ghi chú: Liệt kê ngắn gọn các tính năng chính thực hiện mỗi tuần trong đồ án.

---
## Tuần 1 — Hoàn tất CSDL & sơ đồ
- Nghiên cứu đề tài, tổng hợp yêu cầu, phạm vi và tiêu chí báo cáo.
- Thiết kế và hoàn thiện ERD (chuẩn hoá quan hệ, khóa ngoại, ràng buộc), tài liệu hóa cấu trúc bảng: Giải, CLB, VĐV, Đăng ký, Bốc thăm/Sơ đồ, Kết quả, Nhật ký.
- Vẽ và hoàn thiện các sơ đồ UML liên quan:
	- Use Case tổng quan hệ thống (quản trị, điều khiển sân, người dùng từ web PIN)
	- Activity: luồng đăng ký, luồng điều khiển điểm, luồng export PDF
	- Sequence: cập nhật điểm và phát SSE, export sơ đồ
- Phác thảo kiến trúc tổng thể (Hybrid Desktop + Web) và chọn công nghệ.
- Khởi tạo dự án, cấu hình kết nối SQL Server (driver/Hikari), khởi động màn hình chính.

Phân công (2 người)
- A: ERD (chuẩn hoá, ràng buộc), Use Case + Activity; tài liệu hoá cấu trúc bảng.
- B: Sequence (SSE/điểm, export), chuẩn DDL/seed dữ liệu; dựng skeleton dự án + kết nối DB.

## Tuần 2 — Bắt đầu code các tính năng
- Xây dựng chức năng đăng nhập với phân quyền (ADMIN/CLIENT),
  chọn giải đấu đang làm việc, lưu trạng thái người dùng, và cập nhật
  điều hướng theo vai trò và giải đã chọn.

Phân công (2 người)
- A: Thiết kế giao diện đăng nhập và hộp thoại chọn giải, xử lý lưu trạng thái phiên làm việc.
- B: Xây nền tảng truy xuất/ghi nhận thông tin giải từ CSDL, cập nhật menu và điều hướng theo vai trò.

## Tuần 3 — Tiếp tục code tính năng
- Hoàn thiện quản lý danh mục: nội dung thi đấu, câu lạc bộ, vận động viên;
	hỗ trợ đăng ký cá nhân/đội theo giải và lọc dữ liệu theo ngữ cảnh giải.

Phân công (2 người)
- A: Thiết kế giao diện CRUD trực quan cho các danh mục, kiểm tra dữ liệu đầu vào.
- B: Xây dựng luồng đăng ký cá nhân/đội, nghiệp vụ lưu/tra cứu theo giải.

## Tuần 4 — Code tính năng
- Xây dựng bốc thăm ngẫu nhiên cho đơn/đôi, lưu lại kết quả;
	hiển thị sơ đồ thi đấu theo từng nội dung để theo dõi.

Phân công (2 người)
- A: Hiện thực thuật toán bốc thăm, chức năng lưu/khôi phục và đặt lại bốc thăm.
- B: Thiết kế màn hình sơ đồ thi đấu, thao tác nhanh từ menu chuột phải.

## Tuần 5 — Code tính năng
- Điều khiển thi đấu nhiều sân đồng thời: tạo 1–5 sân,
  chọn số ván (BO1/BO3), đổi sân/giao cầu, hiển thị bảng điểm dọc/ngang,
  hỗ trợ hoàn tác và đặt lại.

Phân công (2 người)
- A: Quản lý tập hợp sân, trạng thái thi đấu và các thao tác hoàn tác/đặt lại.
- B: Điều khiển chi tiết một sân (BO1/BO3, đổi sân, đổi giao cầu) và màn hình hiển thị điểm.

## Tuần 6 — Code tính năng
- Cung cấp giao diện web điều khiển bằng mã PIN và API điều khiển;
	cập nhật thời gian thực qua SSE; tạo mã QR để truy cập nhanh từ điện thoại.

Phân công (2 người)
- A: Thiết kế trang web nhập PIN và bảng điểm, luồng quét QR và truy cập nhanh.
- B: Kênh cập nhật thời gian thực, cơ chế dự phòng khi mất kết nối.

## Tuần 7 — Code tính năng
- Xuất PDF danh sách đăng ký (tất cả, theo câu lạc bộ, theo nội dung),
  và xuất PDF sơ đồ thi đấu (một file tổng hoặc mỗi nội dung một file) với bố cục rõ ràng.

Phân công (2 người)
- A: Thiết kế mẫu PDF cho danh sách đăng ký (hỗ trợ Unicode, căn lề hợp lý).
- B: Xuất sơ đồ thi đấu linh hoạt (gộp/tách), hỗ trợ chọn thư mục và đặt tên tệp an toàn.

## Tuần 8 — Code tính năng
- Màn hình giám sát dạng lưới (1..4 cột), thu nhận ảnh chụp màn hình từ các sân qua mạng nội bộ,
  và trang nhật ký hệ thống để theo dõi sự kiện.

Phân công (2 người)
- A: Hiển thị lưới giám sát và tối ưu làm tươi giao diện.
- B: Thu nhận ảnh qua UDP, trình xem ảnh và nhật ký hệ thống.

## Tuần 9 — Code tính năng
- Trang Cài đặt: bật/tắt chế độ tối, điều chỉnh cỡ chữ tổng thể,
  chế độ luôn hiển thị trên cùng, âm báo bắt đầu/kết thúc trận;
  áp dụng thay đổi ngay và có thể đặt lại bố cục điều khiển về mặc định.

Phân công (2 người)
- A: Thiết kế và gom nhóm các tùy chọn giao diện/âm thanh, áp dụng ngay không cần khởi động lại.
- B: Xử lý kỹ thuật để phóng chữ toàn cục không bị nhân đôi, hỗ trợ đặt lại bố cục và áp dụng AOT cho cửa sổ hiển thị.

## Tuần 10 — Code tính năng
- Thương hiệu & báo cáo: hiển thị logo lớn ở tiêu đề ứng dụng và cho phép thay đổi logo từ Cài đặt,
  ẩn dòng chữ khi đã có ảnh; chèn logo giải ở góc trên và logo nhà tài trợ ở góc dưới sơ đồ thi đấu,
  đồng thời tinh chỉnh khoảng cách/tỷ lệ để bố cục đẹp.

Phân công (2 người)
- A: Tùy biến tiêu đề ứng dụng với ảnh thương hiệu, thay đổi được từ Cài đặt và cập nhật ngay.
- B: Chèn logo thương hiệu lên sơ đồ thi đấu và tinh chỉnh tỷ lệ/khoảng cách hợp lý.

## Tuần 11 — Kiểm thử & sửa lỗi tiềm ẩn
- Test E2E các luồng; viết checklist; sửa lỗi UI/UX; tối ưu DB/HikariCP; health checks; xử lý SSE edge cases.

Phân công (2 người)
- A: Test UI/E2E, checklist, sửa lỗi UI/UX, kịch bản demo.
- B: Tối ưu DB/HikariCP, health checks, xử lý edge cases SSE.

## Tuần 12 — Làm báo cáo & đóng gói
- Hoàn thiện báo cáo/thuyết trình; cập nhật `README.md` hướng dẫn chạy/triển khai; tài liệu sử dụng (`HUONG_DAN_SU_DUNG.md`); đóng gói MSI (`jpackage`) phục vụ demo.

Phân công (2 người)
- A: Báo cáo + slide, cập nhật README/tài liệu sử dụng, chụp ảnh minh họa.
- B: Đóng gói MSI (`jpackage`), kiểm thử cài đặt, checklist triển khai/demo.

---
## Phân chia mảng công việc (tham khảo)
- A: UI/UX Desktop, PDF, Settings.
- B: Backend, DB, Realtime, Packaging.

## Mốc tiến độ (tham khảo)
- M1 (1–3): Khởi tạo + danh mục/đăng ký.
- M2 (4–6): Bốc thăm, sơ đồ, đa sân, web + SSE.
- M3 (7–9): PDF/Monitor/Settings.
- M4 (10–12): Branding, ổn định, đóng gói & tài liệu.

## Ghi chú điều chỉnh
- Có thể tráo đổi tuần giữa A/B tùy năng lực; đảm bảo review chéo mỗi tuần và cập nhật nhật ký học tập.
- Nếu thiếu thời gian/nhân lực, ưu tiên: Đăng ký → Điều khiển sân → SSE → PDF sơ đồ.
- Các cảnh báo analyzer (style) ở `MainFrame` hiện chưa ảnh hưởng build, đưa vào kế hoạch cải tiến chất lượng (Tuần 11).
