# 📁 Sample Data Files

Thư mục này chứa các file dữ liệu mẫu dạng JSON để phát triển và testing.

## 📄 tournaments.json

File chứa dữ liệu mẫu các giải đấu cầu lông.

### Cấu trúc dữ liệu:
```json
{
  "id": 1,                           // ID duy nhất
  "name": "Tên giải (Tiếng Việt)",  
  "nameEn": "Tournament Name (English)",
  "startDate": "YYYY-MM-DD",         // Ngày bắt đầu
  "endDate": "YYYY-MM-DD",           // Ngày kết thúc
  "location": "Địa điểm (Tiếng Việt)",
  "locationEn": "Location (English)",
  "status": "ongoing|upcoming|registration|completed",
  "participants": 256,               // Số VĐV
  "prize": "500,000,000 VNĐ",       // Giải thưởng
  "image": "/icons/tournaments/...", // Đường dẫn hình
  "category": "professional|amateur|club|youth|corporate|veteran|open",
  "description": "Mô tả ngắn",
  "registrationDeadline": "YYYY-MM-DD",
  "featured": true|false,            // Hiển thị trên landing page
  "winner": "Tên người thắng (nếu completed)",
  "runnerUp": "Tên á quân (nếu completed)"
}
```

### Status values:
- `ongoing` - Đang diễn ra (hiển thị badge LIVE màu đỏ)
- `registration` - Đang nhận đăng ký (badge xanh lá)
- `upcoming` - Sắp diễn ra (badge xanh dương)
- `completed` - Đã kết thúc (badge xám)

### Category values:
- `professional` - Chuyên nghiệp
- `amateur` - Nghiệp dư
- `club` - Câu lạc bộ
- `youth` - Trẻ (U19, U15, etc.)
- `corporate` - Doanh nghiệp
- `veteran` - Cựu vận động viên
- `open` - Mở rộng

### Featured tournaments:
Các giải có `featured: true` sẽ hiển thị trên landing page section "Tournament Preview".

## 🔧 Sử dụng

### Load data trong Service:
```java
@Service
public class TournamentDataService {
    private List<Map<String, Object>> tournaments;
    
    public TournamentDataService(ObjectMapper objectMapper) {
        ClassPathResource resource = new ClassPathResource("data/tournaments.json");
        tournaments = objectMapper.readValue(resource.getInputStream(), 
            new TypeReference<List<Map<String, Object>>>() {});
    }
}
```

### Thêm giải đấu mới:
1. Mở file `tournaments.json`
2. Copy một object hiện có
3. Thay đổi `id` (unique), `name`, `dates`, v.v.
4. Tạo SVG image mới trong `static/icons/tournaments/`
5. Set `featured: true` nếu muốn hiển thị trên landing page
6. Restart server

### Ví dụ thêm giải mới:
```json
{
  "id": 9,
  "name": "Giải Cầu Lông Học Sinh 2025",
  "nameEn": "Student Badminton Tournament 2025",
  "startDate": "2025-04-15",
  "endDate": "2025-04-18",
  "location": "Trường THPT Nguyễn Huệ, Hà Nội",
  "locationEn": "Nguyen Hue High School, Hanoi",
  "status": "registration",
  "participants": 150,
  "prize": "30,000,000 VNĐ",
  "image": "/icons/tournaments/student-tournament.svg",
  "category": "youth",
  "description": "Giải đấu dành cho học sinh THPT toàn Hà Nội",
  "registrationDeadline": "2025-04-01",
  "featured": true
}
```

## 📊 Migration sang Database

Khi sẵn sàng chuyển sang SQL Server:

### Bước 1: Tạo table
```sql
CREATE TABLE tournaments (
    id INT PRIMARY KEY IDENTITY(1,1),
    name NVARCHAR(255) NOT NULL,
    name_en NVARCHAR(255),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    location NVARCHAR(500),
    location_en NVARCHAR(500),
    status VARCHAR(20) NOT NULL,
    participants INT,
    prize NVARCHAR(100),
    image_url NVARCHAR(500),
    category VARCHAR(50),
    description NVARCHAR(MAX),
    registration_deadline DATE,
    featured BIT DEFAULT 0,
    winner NVARCHAR(255),
    runner_up NVARCHAR(255),
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE()
);
```

### Bước 2: Import data
```sql
-- Script để import từ JSON vào SQL
-- Có thể dùng Java CommandLineRunner hoặc SQL script
```

### Bước 3: Update Service
```java
// Thay JSON loading bằng JPA Repository
@Autowired
private TournamentRepository tournamentRepository;

public List<Tournament> getFeaturedTournaments() {
    return tournamentRepository.findByFeaturedTrue();
}
```

## 🎯 Best Practices

1. **ID**: Luôn unique, tăng dần
2. **Dates**: Format ISO 8601 (YYYY-MM-DD)
3. **Status**: Lowercase, consistent values
4. **Images**: Luôn có fallback (default.svg)
5. **Featured**: Giới hạn 4-6 giải để landing page đẹp
6. **Description**: Ngắn gọn, dưới 150 ký tự

## 📝 Notes

- File này dùng cho **development & testing** only
- Production nên dùng database (SQL Server)
- Backup file này trước khi modify
- Validate JSON format sau khi edit (jsonlint.com)

---
**Last updated:** 2024-11-10  
**Current tournaments:** 8  
**Featured tournaments:** 4
