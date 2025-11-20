# 🗺️ LỘ TRÌNH PHÁT TRIỂN WEB PLATFORM - BTMS

> **Ngày tạo**: 17/11/2025  
> **Phiên bản**: 1.0  
> **Tác giả**: GitHub Copilot + Nguyen Hau

---

## 📊 PHÂN TÍCH HIỆN TRẠNG DỰ ÁN

### ✅ Đã hoàn thành

#### 1. **Core Desktop Application**
- ✅ Desktop app với Java Swing + FlatLaf
- ✅ Quản lý đa sân (5 sân đồng thời)
- ✅ Real-time scoreboard với SSE
- ✅ PIN-based remote control
- ✅ Database integration (SQL Server + JPA)
- ✅ H2 TCP Server cho remote access

#### 2. **Web Infrastructure**
- ✅ Spring Boot 3.4.0 setup
- ✅ Thymeleaf templating engine
- ✅ Base layouts (header, footer, base.html)
- ✅ Responsive CSS framework
- ✅ Scoreboard web interface
- ✅ PIN entry system

#### 3. **Tournament Hub - Đã có Templates**
- ✅ `tournament-home.html` - Trang chủ hub
- ✅ `tournament-list.html` - Danh sách giải đấu
- ✅ `tournament-detail.html` - Chi tiết giải đấu
- ✅ `tournament-calendar.html` - Lịch giải đấu
- ✅ `tournament-live.html` - Trận đấu trực tiếp
- ✅ `tournament-schedule.html` - Lịch thi đấu
- ✅ `tournament-standings.html` - Bảng xếp hạng
- ✅ `tournament-participants.html` - Danh sách tham gia
- ✅ `tournament-register.html` - Đăng ký giải đấu
- ✅ `tournament-history.html` - Lịch sử
- ✅ `tournament-rules.html` - Luật thi đấu

#### 4. **App Promotion Section**
- ✅ `app/btms-app.html` - Trang giới thiệu app
- ✅ `app/download-app/` - Khu vực tải app
- ✅ `app/learn-more-app/` - Tìm hiểu thêm

#### 5. **Data Services**
- ✅ `TournamentDataService` - Xử lý dữ liệu giải đấu
- ✅ `tournaments.json` - Mock data
- ✅ REST API endpoints cơ bản

### 🚧 Đang phát triển

- 🚧 Controller logic cho các trang tournament
- 🚧 CSS styling cho từng page
- 🚧 JavaScript interactions
- 🚧 Database schema cho tournament data

### ❌ Chưa bắt đầu

- ❌ Landing page hoàn chỉnh (`main-home.html`)
- ❌ User authentication & authorization
- ❌ Player/Club management UI
- ❌ Advanced search & filtering
- ❌ Live match streaming integration
- ❌ Results & brackets visualization
- ❌ Statistics & analytics dashboard
- ❌ News & content management
- ❌ Admin panel

---

## 🎯 MỤC TIÊU VÀ ƯU TIÊN

### Mục tiêu chính của Web Platform

1. **Landing Page** (Priority: MEDIUM)
   - Quảng bá, giới thiệu BTMS Desktop App
   - Showcase tính năng độc đáo
   - CTA rõ ràng để download app

2. **App Hub** (Priority: MEDIUM) 
   - Chi tiết về BTMS app
   - Hướng dẫn sử dụng
   - Khu vực tải xuống
   - FAQ & Support

3. **Tournament Hub** (Priority: **HIGH** ⭐)
   - **Đây là focus hiện tại của bạn**
   - Platform cho các giải đấu cầu lông
   - Khám phá, tìm kiếm giải đấu
   - Đăng ký tham gia
   - Theo dõi lịch thi đấu, kết quả
   - Xếp hạng, thống kê

---

## 📋 LỘ TRÌNH PHÁT TRIỂN CHI TIẾT

Tôi chia thành **7 GIAI ĐOẠN (PHASES)** với các milestones cụ thể:

---

## 🚀 PHASE 1: HOÀN THIỆN TOURNAMENT HUB CORE (2-3 tuần)

> **Mục tiêu**: Đưa Tournament Hub vào hoạt động với đầy đủ tính năng cơ bản

### 📦 Milestone 1.1: Tournament Discovery & Browse (Week 1)

#### Backend Tasks
- [ ] **Database Schema Design**
  - [ ] Tạo/cập nhật entities cho tournaments
  - [ ] Thêm fields: `featured`, `status`, `registrationDeadline`
  - [ ] Relationship với `NoiDung`, `GiaiDau` existing tables
  - [ ] Migration scripts

- [ ] **Service Layer Enhancement**
  - [ ] Mở rộng `TournamentDataService` để query từ DB thay vì JSON
  - [ ] Implement pagination cho tournament list
  - [ ] Filters: status, location, date range, category
  - [ ] Search functionality (theo tên, địa điểm)
  - [ ] Sort options (date, name, popularity)

- [ ] **Controller Completion**
  - [x] `TournamentController.tournamentHome()` - ✅ Đã có
  - [ ] Hoàn thiện `tournamentList()` với pagination
  - [ ] Implement `tournamentDetail()` 
  - [ ] Add query params handling

#### Frontend Tasks
- [ ] **Tournament Home Page Styling**
  - [ ] Hero section với search bar
  - [ ] Stats dashboard cards
  - [ ] Featured tournaments grid (responsive)
  - [ ] Live tournaments carousel
  - [ ] Quick filter chips
  - [ ] Animations (AOS library)

- [ ] **Tournament List Page**
  - [ ] Filter sidebar (collapsible trên mobile)
  - [ ] Tournament cards grid/list view toggle
  - [ ] Pagination controls
  - [ ] Loading states
  - [ ] Empty states
  - [ ] Sort dropdown

- [ ] **Tournament Detail Page**
  - [ ] Tournament header với cover image
  - [ ] Tabs: Overview, Schedule, Participants, Results
  - [ ] Registration CTA button
  - [ ] Share buttons
  - [ ] Breadcrumb navigation

#### Testing
- [ ] Unit tests cho services
- [ ] Integration tests cho controllers
- [ ] Manual testing trên mobile/tablet

---

### 📦 Milestone 1.2: Calendar & Schedule Management (Week 1-2)

#### Backend Tasks
- [ ] **Calendar API Endpoints**
  - [ ] `GET /api/tournaments/calendar?month=11&year=2025`
  - [ ] Return events in FullCalendar format
  - [ ] Filter by status, category

- [ ] **Schedule Service**
  - [ ] Query matches by tournament
  - [ ] Group by date, court
  - [ ] Handle timezone issues

#### Frontend Tasks
- [ ] **Calendar Integration**
  - [ ] FullCalendar.js setup
  - [ ] Custom event rendering với status colors
  - [ ] Event click → navigate to detail
  - [ ] Month/week/day views
  - [ ] Mobile responsive calendar

- [ ] **Schedule Page**
  - [ ] Timeline view cho matches
  - [ ] Court-based filtering
  - [ ] Export to iCal/Google Calendar
  - [ ] Print view

#### Testing
- [ ] Calendar functionality testing
- [ ] Date handling edge cases

---

### 📦 Milestone 1.3: Live Matches & Real-time Updates (Week 2)

#### Backend Tasks
- [ ] **Live Match API**
  - [ ] `GET /api/tournaments/{id}/live-matches`
  - [ ] SSE stream cho live scores
  - [ ] Integration với existing scoreboard system

- [ ] **Match Status Service**
  - [ ] Determine ongoing matches
  - [ ] Recent results
  - [ ] Upcoming matches (next 2 hours)

#### Frontend Tasks
- [ ] **Live Matches Page**
  - [ ] Real-time score cards
  - [ ] SSE connection management
  - [ ] Auto-refresh fallback
  - [ ] Match timeline
  - [ ] "Watch Now" buttons

- [ ] **Live Badge Components**
  - [ ] Pulsing "LIVE" badge
  - [ ] Score ticker
  - [ ] Countdown timers

#### Testing
- [ ] Real-time functionality testing
- [ ] SSE connection stability
- [ ] Fallback mechanism testing

---

### 📦 Milestone 1.4: Tournament Registration (Week 2-3)

#### Backend Tasks
- [ ] **Registration API**
  - [ ] `POST /api/tournaments/{id}/register`
  - [ ] Validation: deadline, capacity, eligibility
  - [ ] Payment integration (optional, để sau)
  - [ ] Confirmation emails

- [ ] **Registration Service**
  - [ ] Check available slots
  - [ ] Handle team vs individual registration
  - [ ] Store participant info
  - [ ] Generate registration codes

#### Frontend Tasks
- [ ] **Registration Form**
  - [ ] Multi-step wizard:
    1. Player/Team info
    2. Category selection
    3. Review & confirm
  - [ ] Form validation
  - [ ] File upload (ID, photos nếu cần)
  - [ ] Payment gateway UI (nếu có)
  - [ ] Confirmation page

- [ ] **Participants Page**
  - [ ] List of registered players/teams
  - [ ] Filter by category
  - [ ] Search participants

#### Testing
- [ ] Registration flow end-to-end
- [ ] Edge cases: full tournament, past deadline

---

### 📦 Milestone 1.5: Results & Standings (Week 3)

#### Backend Tasks
- [ ] **Results API**
  - [ ] `GET /api/tournaments/{id}/results`
  - [ ] `GET /api/tournaments/{id}/standings`
  - [ ] Bracket generation API

- [ ] **Results Service**
  - [ ] Calculate standings from match results
  - [ ] Support different formats: round-robin, knockout, group stage
  - [ ] Points calculation

#### Frontend Tasks
- [ ] **Results Page**
  - [ ] Match results list
  - [ ] Filter by round, category
  - [ ] Score display

- [ ] **Standings Page**
  - [ ] Leaderboard tables
  - [ ] Points breakdown
  - [ ] Charts/graphs (optional)

- [ ] **Bracket Visualization**
  - [ ] Tournament bracket tree (library: bracketsjs hoặc custom SVG)
  - [ ] Interactive navigation
  - [ ] Mobile-friendly

#### Testing
- [ ] Standings calculation accuracy
- [ ] Bracket rendering across devices

---

## 🎨 PHASE 2: LANDING PAGE & APP PROMOTION (1-2 tuần)

> **Mục tiêu**: Tạo landing page ấn tượng và app promotion hub hoàn chỉnh

### 📦 Milestone 2.1: Landing Page Development

#### Backend
- [ ] Home controller với dynamic content
- [ ] CMS-like content management (optional)

#### Frontend
- [ ] **Hero Section**
  - [ ] Full-screen hero với video background (optional)
  - [ ] Main tagline + CTA buttons
  - [ ] Animated elements

- [ ] **Features Showcase**
  - [ ] 3-column feature cards
  - [ ] Icons + descriptions
  - [ ] Scroll animations

- [ ] **Statistics Counter**
  - [ ] Animated counters (tournaments, players, matches)
  - [ ] CountUp.js library

- [ ] **Testimonials**
  - [ ] Carousel/slider với user reviews
  - [ ] Photos + quotes

- [ ] **CTA Section**
  - [ ] Download buttons
  - [ ] "Explore Tournaments" button
  - [ ] Newsletter signup

- [ ] **FAQ Section**
  - [ ] Accordion-style FAQs
  - [ ] Common questions about BTMS

### 📦 Milestone 2.2: App Promotion Hub

- [ ] **Features Deep Dive**
  - [ ] Separate pages for each major feature
  - [ ] Screenshots/videos
  - [ ] Use cases

- [ ] **Download Page**
  - [ ] Platform-specific download buttons (Windows)
  - [ ] System requirements
  - [ ] Installation instructions
  - [ ] Release notes

- [ ] **Learn More / Tutorials**
  - [ ] Video tutorials
  - [ ] Step-by-step guides
  - [ ] User manual (embedded PDF)

- [ ] **Comparison Table**
  - [ ] BTMS vs competitors
  - [ ] Feature comparison matrix

---

## 👥 PHASE 3: PLAYER & CLUB MANAGEMENT (2 tuần)

> **Mục tiêu**: Hệ thống quản lý VĐV và CLB

### 📦 Milestone 3.1: Player Profiles

#### Backend
- [ ] Player API endpoints
- [ ] Profile CRUD operations
- [ ] Stats aggregation

#### Frontend
- [ ] Player list page
- [ ] Player profile page:
  - [ ] Basic info (name, age, club)
  - [ ] Tournament history
  - [ ] Match statistics
  - [ ] Win/loss ratio charts
  - [ ] Rankings

### 📦 Milestone 3.2: Club Management

#### Backend
- [ ] Club API endpoints
- [ ] Club members management

#### Frontend
- [ ] Club list page
- [ ] Club profile page:
  - [ ] Club info
  - [ ] Member roster
  - [ ] Achievements
  - [ ] Upcoming tournaments

---

## 🔐 PHASE 4: USER AUTHENTICATION & ACCOUNTS (1-2 tuần)

> **Mục tiêu**: User login, registration, dashboard cá nhân

### Backend
- [ ] Spring Security setup
- [ ] JWT authentication
- [ ] User roles: ADMIN, ORGANIZER, PLAYER, PUBLIC
- [ ] OAuth2 integration (Google, Facebook) (optional)

### Frontend
- [ ] Login/Register pages
- [ ] User dashboard:
  - [ ] My tournaments (registered, past)
  - [ ] My profile
  - [ ] Notifications
  - [ ] Settings
- [ ] Password reset flow
- [ ] Email verification

---

## 📊 PHASE 5: ANALYTICS & STATISTICS (1 tuần)

> **Mục tiêu**: Thống kê tổng quan và phân tích

### Features
- [ ] Statistics dashboard:
  - [ ] Tournament stats
  - [ ] Player stats
  - [ ] Popular categories
  - [ ] Geographic distribution
- [ ] Charts & graphs (Chart.js)
- [ ] Export reports (CSV, PDF)
- [ ] Leaderboards:
  - [ ] Top players
  - [ ] Top clubs
  - [ ] Most active tournaments

---

## 📰 PHASE 6: NEWS & CONTENT MANAGEMENT (1 tuần)

> **Mục tiêu**: Content platform cho tin tức và bài viết

### Features
- [ ] News list page
- [ ] Article detail page
- [ ] Categories/tags
- [ ] Featured articles
- [ ] Archive
- [ ] Admin: WYSIWYG editor cho content creation

---

## 🛠️ PHASE 7: ADMIN PANEL & ADVANCED FEATURES (2-3 tuần)

> **Mục tiêu**: Admin tools và tính năng nâng cao

### Admin Panel
- [ ] Dashboard với overview metrics
- [ ] Tournament management:
  - [ ] Create/edit/delete tournaments
  - [ ] Manage registrations
  - [ ] Schedule matches
  - [ ] Enter results
- [ ] User management
- [ ] Content management (news, pages)
- [ ] Settings & configuration

### Advanced Features
- [ ] Notifications system (in-app + email)
- [ ] Advanced search (Elasticsearch hoặc full-text search)
- [ ] API documentation (Swagger/OpenAPI)
- [ ] Multi-language support (i18n)
- [ ] Dark mode
- [ ] PWA (Progressive Web App) features
- [ ] Mobile app integration (deep links)

---

## 🗓️ TIMELINE TỔNG HỢP

| Phase | Duration | Priority | Dependencies |
|-------|----------|----------|--------------|
| **Phase 1**: Tournament Hub Core | 2-3 tuần | 🔴 **HIGH** | None |
| **Phase 2**: Landing Page & App | 1-2 tuần | 🟡 MEDIUM | None |
| **Phase 3**: Player & Club | 2 tuần | 🟡 MEDIUM | Phase 1 |
| **Phase 4**: Authentication | 1-2 tuần | 🟢 MEDIUM | None |
| **Phase 5**: Analytics | 1 tuần | 🟢 LOW | Phase 1, 3 |
| **Phase 6**: News & Content | 1 tuần | 🟢 LOW | Phase 4 |
| **Phase 7**: Admin & Advanced | 2-3 tuần | 🟡 MEDIUM | All above |

**Tổng thời gian ước tính**: 10-14 tuần (2.5-3.5 tháng)

---

## 🎯 FOCUS NGAY BÂY GIỜ: PHASE 1 - TOURNAMENT HUB

Dựa trên yêu cầu của bạn, tôi đề xuất **bắt đầu với Phase 1** vì:

1. ✅ Đã có templates sẵn (11 HTML files)
2. ✅ Đã có controller cơ bản
3. ✅ Đã có data service
4. 🎯 Là priority cao nhất của bạn
5. 🎯 Có thể demo được sớm nhất

### Bước tiếp theo đề xuất (tuần này):

#### 🔹 Week 1 - Days 1-2: Database & Backend
1. **Thiết kế database schema cho tournaments**
   - Cập nhật entities (GiaiDau, NoiDung, etc.)
   - Thêm fields cần thiết (status, featured, images)
   - Migration scripts

2. **Hoàn thiện TournamentDataService**
   - Chuyển từ JSON sang DB queries
   - Implement pagination
   - Filters & search

#### 🔹 Week 1 - Days 3-5: Frontend Core Pages
3. **Styling tournament-home.html**
   - CSS cho hero section
   - Stats dashboard cards
   - Featured grid
   - Responsive design

4. **Styling tournament-list.html**
   - Filter sidebar
   - Card grid layout
   - Pagination
   - Search bar

#### 🔹 Week 1 - Weekend: Polish & Test
5. **Testing & refinement**
   - Browser testing
   - Mobile responsive
   - Bug fixes

---

## 🔧 CÔNG NGHỆ & TOOLS ĐỀ XUẤT

### Frontend Libraries
- **AOS** - Scroll animations (đã dùng)
- **FullCalendar** - Calendar view (đã dùng)
- **Chart.js** - Charts & graphs
- **SweetAlert2** - Beautiful alerts
- **DataTables** - Advanced table features (optional)
- **Select2** - Better dropdowns (optional)

### Backend Enhancements
- **Spring Security** - Authentication
- **Spring Data JPA** - Database ORM (đã có)
- **MapStruct** - Entity-DTO mapping
- **Flyway/Liquibase** - Database migrations

### Dev Tools
- **Lombok** - Reduce boilerplate
- **Spring Boot DevTools** - Hot reload (đã có)
- **Swagger** - API documentation

---

## 📝 CONVENTIONS & BEST PRACTICES

### Code Structure
```
src/main/java/com/example/btms/
├── web/
│   ├── controller/
│   │   ├── tournament/
│   │   ├── player/
│   │   ├── club/
│   │   └── api/
│   ├── dto/
│   └── mapper/
├── service/
│   ├── tournament/
│   ├── player/
│   └── club/
├── repository/
├── model/
└── config/
```

### Naming Conventions
- Controllers: `*Controller.java`
- Services: `*Service.java`
- DTOs: `*DTO.java`, `*Request.java`, `*Response.java`
- Templates: kebab-case (`tournament-list.html`)
- CSS/JS: Theo template name
- API endpoints: REST standard (`/api/tournaments/{id}`)

### Git Workflow
- Branch: `feature/tournament-hub`, `feature/landing-page`, etc.
- Commits: Conventional Commits format
  - `feat: add tournament calendar`
  - `fix: resolve pagination issue`
  - `style: update tournament card design`

---

## 🤝 COLLABORATION WORKFLOW

### Từng Milestone
1. **Planning**: Review requirements, thiết kế database/API
2. **Backend First**: Implement services, controllers, tests
3. **Frontend**: HTML, CSS, JavaScript
4. **Integration**: Connect frontend-backend
5. **Testing**: Manual + automated tests
6. **Review**: Code review, bug fixes
7. **Deploy**: Merge to main branch

### Communication
- Tôi sẽ hỗ trợ từng bước cụ thể
- Bạn review và feedback
- Điều chỉnh theo requirements

---

## 📚 TÀI LIỆU THAM KHẢO

1. **Đã có trong project**
   - `WEB_PLATFORM_STRUCTURE.md` - Cấu trúc tổng quan
   - `BAO_CAO_CONG_NGHE_VA_TINH_NANG_v2.md` - Tech stack
   - `API_DOCUMENTATION.md` - API docs

2. **Cần tạo thêm**
   - [ ] Database schema documentation
   - [ ] Component library documentation
   - [ ] Deployment guide
   - [ ] User guide

---

## ✅ ACTION ITEMS - BẮT ĐẦU NGAY

Để bắt đầu Phase 1 - Milestone 1.1, bạn có thể:

1. **Review lộ trình này** - Cho tôi biết phần nào cần điều chỉnh
2. **Xác nhận priorities** - Có đúng Tournament Hub là focus chính?
3. **Database review** - Cùng xem schema hiện tại và plan changes
4. **Pick first task** - Tôi đề xuất bắt đầu với:
   - ✅ Database schema cho tournaments
   - ✅ TournamentDataService enhancements
   - ✅ Styling tournament-home.html

**Bạn muốn bắt đầu với task nào trước? 🚀**

---

## 📞 NEXT STEPS

Hãy cho tôi biết:
1. Lộ trình này có phù hợp không?
2. Có phần nào cần thêm/bớt/thay đổi?
3. Bạn muốn bắt đầu với task cụ thể nào trong Phase 1?

Tôi sẽ đồng hành cùng bạn từng bước để build một web platform tuyệt vời! 💪🏸
