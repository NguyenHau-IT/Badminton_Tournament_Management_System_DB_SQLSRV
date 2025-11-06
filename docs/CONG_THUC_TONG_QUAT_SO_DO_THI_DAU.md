# Công thức tổng quát và cơ chế hoạt động vẽ sơ đồ thi đấu (Bracket 16 ➜ 1)

Tài liệu này mô tả các công thức tính toán toạ độ, cách đánh số, quy tắc ghép cặp, luật seed người chơi/đội theo số lượng, xử lý BYE, vẽ đường nối nhánh, và ánh xạ lưu CSDL của màn hình "Sơ đồ thi đấu" trong dự án.

Mục tiêu: giải thích để có thể tái hiện/kiểm tra logic hoặc mở rộng trong tương lai.

---

## 1) Cấu trúc tổng quan

- Bracket loại trực tiếp gồm 5 cột (COLUMNS = 5):
  - Cột 1: 16 ô (vòng 1)
  - Cột 2: 8 ô (tứ kết)
  - Cột 3: 4 ô (bán kết)
  - Cột 4: 2 ô (chung kết)
  - Cột 5: 1 ô (vô địch)
- Số ô trên mỗi cột: `SPOTS = [16, 8, 4, 2, 1]`.
- Chỉ số ô trong cột (thu_tu) bắt đầu từ 0.
- Mỗi ô hiển thị một "tên" (TEAM) và một số thứ tự liên tục (order) để tiện theo dõi.

---

## 2) Hệ toạ độ và kích thước

Hằng số hiển thị:
- Bề rộng ô: `CELL_WIDTH = 180`
- Chiều cao ô: `CELL_HEIGHT = 30`
- Gốc X: `X0 = 35`
- Bước X giữa các cột: `DX = 160`
- Độ dịch phải thêm cho các cột > 1: `BASE_INNER_RIGHT_OFFSET = 40`
- Gốc Y: `START_Y = 10`
- Độ lệch đi lên cho các cột > 1 để nằm gần giữa 2 lá con: `INNER_UP_OFFSET = 20`

### 2.1) Công thức toạ độ X

Trong mã nguồn đang dùng:

- Với mọi cột `COL ∈ {1..5}`:
  $$
  X = X0 + (COL - 1) * DX + \max(0, COL-1) * BASE\_INNER\_RIGHT\_OFFSET
  $$

Vì `BASE_INNER_RIGHT_OFFSET = 40` và `DX = 160`, có thể viết gọn:

- Với mọi `COL ≥ 1`:
  $$
  X = 35 + 200 * (COL - 1)
  $$

### 2.2) Công thức bước dọc (vertical step)

- Bước của cột `COL`:
  $$
  step(COL) = 40 \cdot 2^{(COL-1)}
  $$

### 2.3) Công thức toạ độ Y

- Nếu `COL = 1` (cột ngoài cùng):
  $$
  Y = START\_Y + THU\_TU \cdot step(COL)
  $$

- Nếu `COL > 1` (cột trong):
  - Tính trước: `baseY = START_Y + THU_TU * step(COL)`
  - Dời lên cho nằm gần giữa 2 ô con:
  $$
  Y = baseY + \frac{step(COL)}{2} - INNER\_UP\_OFFSET
  $$

Lưu ý: đảm bảo `Y ≥ 0` (nếu âm thì ép về 0).

---

## 3) Số thứ tự liên tục (order)

- Mỗi ô có một số thứ tự hiển thị liên tục tăng dần: duyệt từ trái ➜ phải, trên ➜ dưới.
- Công dụng:
  - Dễ tra cứu và lưu CSDL (trường `VI_TRI` chính là `order`).
  - Hiển thị số ngay trong ô để hỗ trợ quan sát.

---

## 4) Vẽ đường nối nhánh (pair ➜ parent)

- Ở mỗi cột `COL` (trừ cột cuối), các ô được ghép theo cặp `(t, t+1)` với `t` chẵn (`t = 0, 2, 4, ...`).
- Ô cha (parent) nằm ở cột kế tiếp `COL+1` với chỉ số:
  $$
  parent\_thu\_tu = \left\lfloor \frac{t}{2} \right\rfloor
  $$
- Vẽ 2 đoạn ngang từ 2 lá con sang một trục dọc giữa, nối dọc, rồi nối ngang sang tâm ô cha (theo đúng vị trí `Y`).

---

## 5) Quy tắc seed đội theo số lượng (N)

Giả sử tổng số đội/đôi hợp lệ `N` (tối đa 16). Chọn cột seed và block size:

- `N ≥ 9` ➜ seed ở cột 1, block `M = 16`
- `5 ≤ N ≤ 8` ➜ seed ở cột 2, block `M = 8`
- `3 ≤ N ≤ 4` ➜ seed ở cột 3, block `M = 4`
- `N = 2` ➜ seed ở cột 4, block `M = 2`
- `N = 1` ➜ cột 4, `M = 1` (không có trận)

Nếu `N > 16` sẽ cảnh báo (không hỗ trợ hơn 16).

### 5.1) Top-heavy fill (ưu tiên nửa trên)

Mục tiêu: nhánh trên luôn có nhiều hơn hoặc bằng nhánh dưới.

Thuật toán đệ quy (mã giả):

```
fillTopHeavy(start, block, n, out):
  if n <= 0: return
  if block == 1:
    out.add(start); return
  half = block / 2
  nTop = ceil(n / 2)
  nBot = n - nTop
  fillTopHeavy(start,       half, nTop, out)  # nửa trên
  fillTopHeavy(start+half,  half, nBot, out)  # nửa dưới
```

Trả về danh sách các chỉ số `THU_TU` (trong block M) để đặt lần lượt các đội theo thứ tự được cấp.

---

## 6) Xử lý BYE ở vòng đầu

- Duyệt tất cả các cặp ở cột đang seed: cặp `p` gồm 2 ô có `THU_TU = 2p` và `2p+1`.
- Nếu một trong hai có tên và ô còn lại trống (BYE):
  - Xoá tên ở hai ô lá ngoài (đặt `null` để rỗng).
  - Ghi trực tiếp tên đội thắng vào ô trong của cột kế theo chỉ số cha `p` (text override).

Lưu ý: có thể điều chỉnh để chỉ “check trận đầu” bằng cách dừng sau cặp `p=0`.

---

## 7) Lưu vào CSDL (SO_DO_DOI)

Khi bấm nút "Lưu":

1. Xoá toàn bộ bản ghi cũ trong `SO_DO_DOI` thuộc `(ID_GIAI, ID_NOI_DUNG)` đang chọn.
2. Duyệt mọi ô đang hiển thị, với những ô có `text` (tên đội) thì thêm bản ghi mới:
   - `ID_GIAI` = giải đang chọn
   - `ID_NOI_DUNG` = nội dung đang chọn
   - `TEN_TEAM` = `s.text`
   - `TOA_DO_X` = `s.x` (tọa độ X của ô)
   - `TOA_DO_Y` = `s.y` (tọa độ Y của ô)
   - `VI_TRI` = `s.order` (số thứ tự liên tục của ô – cũng là "số slot")
   - `SO_DO` = `s.col` (cột/vòng để tham chiếu)
   - `THOI_GIAN` = thời điểm lưu

Ràng buộc: với một `(ID_GIAI, ID_NOI_DUNG)`, cột `VI_TRI` đóng vai trò khoá logic (unique) để xác định một ô duy nhất.

---

## 8) Ví dụ nhanh

- Cột 1 (`COL=1`), ô đầu (`THU_TU=0`):
  - `step = 40 * 2^(1-1) = 40`
  - `X = 35 + 200*(1-1) = 35`
  - `Y = 10 + 0 * 40 = 10`

- Cột 2 (`COL=2`), ô thứ 3 trong cột (`THU_TU=2`):
  - `step = 40 * 2^(2-1) = 80`
  - `X = 35 + 200*(2-1) = 235`
  - `baseY = 10 + 2 * 80 = 170`
  - `Y = 170 + 80/2 - 20 = 190`

- Ô cha của cặp `(t=4, t+1=5)` ở cột 2 là `parent` tại cột 3, chỉ số:
  - `parent_thu_tu = floor(4/2) = 2`

---

## 9) Giới hạn và mở rộng

- Tối đa 16 đội. Khi cần hơn 16, cần mở rộng `SPOTS` và dây nối tương ứng.
- Các hằng số (khoảng cách X/Y, offsets) có thể tinh chỉnh để thay đổi bố cục.
- Có thể lưu thêm `ID_CLB` khi gắn được id đội/clb với tên hiển thị.
- Có thể bổ sung chức năng “Load từ SO_DO_DOI” để vẽ lại đúng sơ đồ đã lưu.

---

## 10) Liên hệ với mã nguồn

- Panel: `src/main/java/com/example/btms/ui/draw/SoDoThiDauPanel.java`
  - Lớp vẽ: inner class `BracketCanvas`
  - Công thức X/Y và số thứ tự: trong `rebuildSlots()`
  - Quy tắc seeding top-heavy: `computeTopHeavyPositionsWithinBlock(...)`
  - BYE: xử lý trong `loadFromDb()` (đặt override cho cột kế)
  - Lưu CSDL: `saveBracket()` dùng `SoDoDoiService`

- Model: `src/main/java/com/example/btms/model/bracket/SoDoDoi.java`
- Repository: `src/main/java/com/example/btms/repository/bracket/SoDoDoiRepository.java`
- Service: `src/main/java/com/example/btms/service/bracket/SoDoDoiService.java`

---

Tài liệu này phản ánh đúng các hằng số và công thức đang dùng trong mã tại thời điểm cập nhật. Khi thay đổi layout/logic, vui lòng cập nhật lại để đảm bảo đồng bộ.
