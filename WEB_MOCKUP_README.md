# 🎨 BTMS Web Mockup - Enhanced Design

## 📋 Tổng quan

Bộ mockup HTML/CSS hoàn chỉnh cho **2 hướng phát triển web** của dự án BTMS:
1. **Product Marketing Page** - Trang quảng bá ứng dụng BTMS
2. **Tournament Platform** - Trang web giải đấu cầu lông

---

## 📁 Cấu trúc File

### 1. Product Page (BTMS Marketing)

```
src/main/resources/
├── static/css/product/
│   └── btms-enhanced.css          # CSS đầy đủ cho product page
└── templates/product/
    └── btms.html                   # Template HTML (đã tồn tại, cần update)
```

### 2. Tournament Sections

```
src/main/resources/
├── static/css/badmintonTournament/
│   ├── ranking-enhanced.css       # Styles cho bảng xếp hạng
│   ├── schedule-enhanced.css      # Styles cho lịch thi đấu
│   └── stats-enhanced.css         # Styles cho thống kê
├── static/js/badmintonTournament/
│   └── tournament-enhanced.js     # JavaScript cho interactive features
└── templates/badmintonTournamentSections/
    ├── ranking/
    │   └── ranking-enhanced.html  # Bảng xếp hạng với tabs & filters
    ├── schedule/
    │   └── schedule-enhanced.html # Lịch thi đấu với match cards
    └── stats/
        └── stats-enhanced.html    # Dashboard thống kê với charts
```

---

## 🚀 Cách sử dụng

### Bước 1: Cập nhật Product Page

**File cần chỉnh sửa:** `src/main/resources/templates/product/btms.html`

Thay thế nội dung hiện tại bằng structure mới trong `btms-enhanced.css`. Hoặc thêm link CSS:

```html
<head>
    <!-- Existing CSS -->
    <link th:href="@{/css/product/btms.css}" rel="stylesheet">
    
    <!-- Add enhanced CSS -->
    <link th:href="@{/css/product/btms-enhanced.css}" rel="stylesheet">
</head>
```

### Bước 2: Tích hợp Tournament Sections

**File cần chỉnh sửa:** `src/main/resources/templates/badmintonTournament/badmintonTournament.html`

Thay thế các thẻ `th:replace` bằng versions mới:

```html
<div th:replace="badmintonTournamentSections/ranking/ranking-enhanced.html"></div>
<div th:replace="badmintonTournamentSections/schedule/schedule-enhanced.html"></div>
<div th:replace="badmintonTournamentSections/stats/stats-enhanced.html"></div>
```

Thêm CSS và JavaScript:

```html
<head>
    <!-- CSS -->
    <link th:href="@{/css/badmintonTournament/ranking-enhanced.css}" rel="stylesheet">
    <link th:href="@{/css/badmintonTournament/schedule-enhanced.css}" rel="stylesheet">
    <link th:href="@{/css/badmintonTournament/stats-enhanced.css}" rel="stylesheet">
    
    <!-- Chart.js for stats -->
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
</head>

<body>
    <!-- Content here -->
    
    <!-- JavaScript -->
    <script th:src="@{/js/badmintonTournament/tournament-enhanced.js}"></script>
</body>
```

### Bước 3: Cập nhật Controller (Optional)

Nếu muốn tích hợp dữ liệu thật từ database:

**File:** `src/main/java/com/example/btms/web/controller/tournament/BadmintonTournamentController.java`

```java
@GetMapping("/badmintonTournament/badmintonTournament")
public String showTournamentPage(Model model) {
    // Add ranking data
    List<PlayerRanking> rankings = rankingService.getTopPlayers(10);
    model.addAttribute("rankings", rankings);
    
    // Add schedule data
    List<Match> todayMatches = scheduleService.getTodayMatches();
    model.addAttribute("todayMatches", todayMatches);
    
    // Add statistics
    TournamentStats stats = statsService.getStatistics();
    model.addAttribute("stats", stats);
    
    return "badmintonTournament/badmintonTournament";
}
```

---

## 🎨 Tính năng chính

### Product Page
- ✅ Hero section với animations
- ✅ Tech stack badges
- ✅ Feature cards với icons gradient
- ✅ Screenshots carousel với tabs
- ✅ Use cases grid
- ✅ Download section với GitHub integration
- ✅ FAQ accordion
- ✅ CTA section

### Tournament - Ranking
- ✅ Search & filters (tournament, club)
- ✅ Tabs cho từng nội dung (Singles, Doubles, Mixed)
- ✅ Rank badges (Gold, Silver, Bronze)
- ✅ Win rate indicators
- ✅ Pagination
- ✅ Export to Excel/PDF buttons

### Tournament - Schedule
- ✅ List view / Calendar view toggle
- ✅ Filters (date, court, round)
- ✅ Match cards với 3 trạng thái:
  - 🔴 Live (đang diễn ra)
  - 🔵 Upcoming (sắp diễn ra)
  - 🟢 Finished (đã kết thúc)
- ✅ Player avatars & scores
- ✅ Quick actions (Xem trực tiếp, Chi tiết, Nhắc nhở)

### Tournament - Stats
- ✅ Animated counter cards
- ✅ Pie chart (phân bố theo nội dung)
- ✅ Bar chart (top clubs)
- ✅ Line chart (lịch sử phát triển)
- ✅ Match statistics
- ✅ Player demographics
- ✅ Hot facts section

---

## 🎭 Responsive Design

Tất cả các sections đều **responsive** cho:
- 📱 Mobile (< 576px)
- 📱 Tablet (576px - 992px)
- 💻 Desktop (> 992px)

### Breakpoints:
```css
/* Mobile First */
@media (max-width: 576px) { ... }
@media (max-width: 768px) { ... }
@media (max-width: 992px) { ... }
@media (max-width: 1200px) { ... }
```

---

## 🔧 Dependencies

### CSS Frameworks
- Bootstrap 5.3.3 (đã có trong project)
- Bootstrap Icons 1.11.3 (đã có trong project)

### JavaScript Libraries
- jQuery 3.7.1 (đã có trong project)
- Chart.js 4.4.0 (cần thêm cho stats section)

### Google Fonts
- Inter (cho body text)
- Montserrat (cho headings) - đã có trong project

---

## 🎨 Color Palette

```css
--primary-color: #0d47a1     /* Blue */
--secondary-color: #e53935   /* Red */
--success-color: #00c853     /* Green */
--warning-color: #ffd600     /* Yellow */
--info-color: #00b0ff        /* Light Blue */
--purple-color: #7b1fa2      /* Purple */
--dark-color: #1a237e        /* Dark Blue */
--light-bg: #f5f7fa          /* Light Gray */
```

---

## 📊 Tích hợp Database

### Ranking Data Model
```java
public class PlayerRanking {
    private Integer rank;
    private String name;
    private String club;
    private Integer points;
    private Integer wins;
    private Integer losses;
    private Double winRate;
    private Integer rankChange; // +2, -1, 0
}
```

### Schedule Data Model
```java
public class Match {
    private String matchId;
    private LocalDateTime startTime;
    private String court;
    private String round;
    private String status; // LIVE, UPCOMING, FINISHED
    private String[] playerNames;
    private String[] clubs;
    private int[] scores;
    private String pinCode; // For live viewing
}
```

### Stats Data Model
```java
public class TournamentStats {
    private int totalPlayers;
    private int totalMatches;
    private int totalClubs;
    private BigDecimal prizePool;
    private Map<String, Integer> categoryDistribution;
    private List<ClubStats> topClubs;
    private List<Integer> growthHistory;
}
```

---

## 🚦 Next Steps

### Phase 1: Static Pages (✅ Completed)
- ✅ Create HTML mockups
- ✅ Design CSS styles
- ✅ Add JavaScript interactions

### Phase 2: Controller Integration
1. Create Service classes
2. Connect to database
3. Populate Model attributes
4. Test with real data

### Phase 3: Advanced Features
1. Real-time updates (WebSocket/SSE)
2. User authentication
3. Online registration form
4. Payment integration
5. Email notifications
6. Social sharing

### Phase 4: SEO & Performance
1. Meta tags optimization
2. OpenGraph tags
3. Sitemap.xml
4. Image optimization
5. Lazy loading
6. CDN integration

---

## 📝 Lưu ý quan trọng

### 1. Chart.js CDN
Đảm bảo thêm Chart.js vào template có stats section:
```html
<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
```

### 2. Bootstrap 5
Mockup được thiết kế cho Bootstrap 5.3.3. Nếu dùng version khác, có thể cần điều chỉnh.

### 3. Thymeleaf Fragments
Sử dụng `th:fragment` để tái sử dụng components:
```html
<section th:fragment="ranking">...</section>
```

### 4. Image Paths
Thay thế placeholder images:
```html
<img src="/icons/avatar.png" alt="Player">
<img src="/screenshots/desktop-app.png" alt="App">
```

### 5. Dynamic Data
Mockup hiện tại sử dụng **static data**. Cần replace bằng Thymeleaf expressions:
```html
<!-- Static -->
<strong>Nguyễn Văn A</strong>

<!-- Dynamic -->
<strong th:text="${player.name}">Nguyễn Văn A</strong>
```

---

## 🎯 Demo Pages

Sau khi setup xong, truy cập:

1. **Product Page**: http://localhost:2345/product/btms
2. **Tournament Page**: http://localhost:2345/badmintonTournament/badmintonTournament

---

## 🤝 Contributing

Nếu muốn customize thêm:

1. **Colors**: Edit CSS variables trong `:root`
2. **Fonts**: Thay Google Fonts trong `<head>`
3. **Animations**: Adjust `@keyframes` và `transition`
4. **Responsive**: Modify `@media` queries

---

## 📞 Support

Nếu gặp vấn đề:
1. Check browser console (F12) for JavaScript errors
2. Verify CSS/JS files are loaded (Network tab)
3. Ensure Bootstrap & jQuery are properly included
4. Test on different browsers

---

**Created by:** GitHub Copilot  
**Date:** November 6, 2025  
**Version:** 1.0.0  
**License:** MIT (same as BTMS project)
