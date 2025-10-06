# 🔌 BTMS API Documentation · v2.0.0

Tài liệu API cho hệ thống BTMS (Badminton Tournament Management System). Hệ thống cung cấp 2 chế độ API:
- PIN mode (đa sân, mỗi sân có mã PIN): Base path `/api/court`
- No-PIN mode (đơn bảng điểm, không yêu cầu PIN): Base path `/api/scoreboard`

Cả hai chế độ đều hỗ trợ cập nhật real-time qua SSE (Server-Sent Events).

---

## 🧭 Base URLs

- Local: `http://localhost:2345`
- Network (LAN): `http://[IP_MAY_CHU]:2345`

Ví dụ:
- PIN mode: `http://[IP]:2345/api/court`
- No-PIN mode: `http://[IP]:2345/api/scoreboard`

CORS: Mặc định cho phép `*` (tất cả origins). Khuyến nghị giới hạn theo môi trường triển khai thực tế.

---

## 📦 Content Types

- Request: `application/json` (cho các endpoint cần body; đa số điều khiển điểm không cần body)
- Response: `application/json; charset=utf-8`
- SSE stream: `text/event-stream`

---

## 🧱 Data Model: Match Snapshot

Dạng JSON trả về bởi các endpoint `/sync`, một số hành động (swap, next, change-server, undo) và sự kiện SSE.

```json
{
  "names": ["Team A", "Team B"],
  "score": [21, 19],
  "games": [1, 0],
  "gameNumber": 1,
  "bestOf": 3,
  "server": 0,
  "doubles": false,
  "gameScores": [[21, 19]]
}
```

- names: Tên/đội A và B
- score: Điểm hiện tại ván đang diễn ra (A, B)
- games: Số ván đã thắng (A, B)
- gameNumber: Ván hiện tại (bắt đầu từ 1)
- bestOf: Số ván tối đa (1 hoặc 3)
- server: Bên đang giao cầu (0=A, 1=B)
- doubles: true nếu là trận đôi
- gameScores: Lịch sử điểm của các ván đã chơi

---

## 📡 SSE (Server-Sent Events)

- Endpoint:
  - PIN mode: `GET /api/court/{pin}/stream`
  - No-PIN mode: `GET /api/scoreboard/stream`
- Content-Type: `text/event-stream`
- Events:
  - `init`: Gửi toàn bộ snapshot khi kết nối
  - `update`: Gửi snapshot khi có thay đổi (điểm, đổi sân, v.v.)
  - `error`: Thông báo lỗi (nếu có)
- Gợi ý client:
  - Dùng EventSource (trình duyệt) hoặc thư viện SSE tương đương
  - Có throttling phía client ~80ms (đã hỗ trợ trong web client)
  - Tự động rơi về polling nếu SSE không khả dụng

---

## 🔑 PIN mode (đa sân)

Base path: `/api/court`

### Health & Info
- `GET /api/court/health` → Kiểm tra tình trạng controller (text/plain)
- `GET /api/court/{pin}` → Thông tin điểm cơ bản (ví dụ: `{ "teamAScore": 0, "teamBScore": 0 }`)
- `GET /api/court/{pin}/status` → Xác thực và thông tin sân theo PIN (JSON)
- `GET /api/court/{pin}/sync` → Snapshot chi tiết trận đấu (JSON theo model ở trên)
- `GET /api/court/{pin}/stream` → SSE stream

### Điều khiển điểm số
- `POST /api/court/{pin}/increaseA`
- `POST /api/court/{pin}/decreaseA`
- `POST /api/court/{pin}/increaseB`
- `POST /api/court/{pin}/decreaseB`

Phản hồi: Thông thường là JSON điểm cơ bản hoặc snapshot tùy action; 200 khi thành công.

### Điều khiển trận đấu
- `POST /api/court/{pin}/reset` → Đặt lại điểm
- `POST /api/court/{pin}/next` → Sang ván tiếp theo
- `POST /api/court/{pin}/swap` → Đổi sân (có ghi dấu SWAP vào chi tiết ván nếu panel sẵn có)
- `POST /api/court/{pin}/change-server` → Đổi người giao cầu
- `POST /api/court/{pin}/undo` → Hoàn tác thao tác gần nhất

Phản hồi: Hầu hết trả về snapshot JSON; 200 khi thành công.

### Endpoint tổng quát (tương thích JS cũ)
- `POST /api/court/{pin}/{action}`
  - `action` ∈ {`increaseA`, `decreaseA`, `increaseB`, `decreaseB`, `reset`, `next`, `swap`, `change-server`, `undo`}
  - Phản hồi: JSON; 200 khi thành công, 400 nếu action không hợp lệ.

---

## 🟩 No-PIN mode (đơn bảng điểm)

Base path: `/api/scoreboard`

### Thông tin & Stream
- `GET /api/scoreboard` → Thông tin điểm cơ bản
- `GET /api/scoreboard/sync` → Snapshot chi tiết trận đấu
- `GET /api/scoreboard/stream` → SSE stream

### Điều khiển điểm số
- `POST /api/scoreboard/increaseA`
- `POST /api/scoreboard/decreaseA`
- `POST /api/scoreboard/increaseB`
- `POST /api/scoreboard/decreaseB`

### Điều khiển trận đấu
- `POST /api/scoreboard/reset`
- `POST /api/scoreboard/next`
- `POST /api/scoreboard/swap`  → Đổi sân (có ghi dấu SWAP vào chi tiết ván nếu panel sẵn có)
- `POST /api/scoreboard/change-server`
- `POST /api/scoreboard/undo`

Phản hồi: JSON; 200 khi thành công.

---

## ⚙️ Ví dụ (curl)

Windows CMD (LAN IP ví dụ: 192.168.1.100, PIN: 1234)

```bat
:: Tăng điểm đội A (PIN mode)
curl http://192.168.1.100:2345/api/court/1234/increaseA

:: Đổi sân (PIN mode)
curl -X POST http://192.168.1.100:2345/api/court/1234/swap

:: Lấy snapshot (No-PIN mode)
curl http://192.168.1.100:2345/api/scoreboard/sync

:: Health check (PIN mode)
curl http://192.168.1.100:2345/api/court/health
```

SSE (trình duyệt, JS):
```js
const es = new EventSource('http://192.168.1.100:2345/api/court/1234/stream');
es.addEventListener('init', e => {
  const snapshot = JSON.parse(e.data);
  console.log('init', snapshot);
});
es.addEventListener('update', e => {
  const snapshot = JSON.parse(e.data);
  console.log('update', snapshot);
});
es.addEventListener('error', e => {
  console.warn('sse error', e);
});
```

---

## 🧪 Mã phản hồi (HTTP)

- 200 OK: Thành công
- 400 Bad Request: `action` không hợp lệ ở endpoint tổng quát
- 500 Internal Server Error: Lỗi không mong muốn (một số nhánh trả Map JSON mặc định)

Lưu ý: Xác thực PIN hiện tại do tầng ứng dụng xử lý (và có thể khác nhau theo cấu hình). Sử dụng `GET /api/court/{pin}/status` để kiểm tra PIN.

---

## 📶 Real-time & Hiệu năng

- SSE server-side phát broadcast bằng thread pool cố định (8 threads).
- Client-side throttling (web) khoảng 80ms để mượt mà và tránh dồn cập nhật.
- Nếu SSE không khả dụng, client sẽ fallback sang polling.

---

## 🔒 Bảo mật & CORS

- PIN là cơ chế ủy quyền nhẹ cho chế độ đa sân (PIN nằm trong URL path).
- CORS mặc định cho phép mọi nguồn (`*`); khuyến nghị giới hạn theo domain nội bộ khi triển khai.
- Hệ thống hướng tới chạy trong mạng LAN tin cậy; nếu xuất Internet, nên đặt sau reverse proxy HTTPS và thêm lớp xác thực bổ sung.

---

## 🧾 Phiên bản & Liên quan

- Phiên bản API: 2.0.0
- Ứng dụng: Spring Boot 3.2.6, Java 17
- Xem thêm:
  - `README.md` (tổng quan, cài đặt)
  - `HUONG_DAN_SU_DUNG.md` (hướng dẫn sử dụng)
  - `BAO_CAO_CONG_NGHE_VA_TINH_NANG_v2.md` (báo cáo kỹ thuật)

---

© Nguyen Viet Hau — BTMS