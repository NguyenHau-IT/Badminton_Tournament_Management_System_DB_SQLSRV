# 🌐 Cấu trúc Web Platform cho BTMS

> **Mục tiêu kép**: 
> 1. 📱 Quảng bá ứng dụng BTMS Desktop
> 2. 🏸 Xây dựng Tournament Platform cho giải đấu cầu lông

---

## 📁 CẤU TRÚC FOLDER TEMPLATES ĐỀ XUẤT

```
templates/
│
├── 🏠 layouts/                          # Shared layouts
│   ├── base.html                        # Base layout với header/footer
│   ├── header.html                      # ✅ Đã có - Navigation bar
│   ├── footer.html                      # ✅ Đã có - Footer
│   ├── sidebar.html                     # NEW - Sidebar cho admin/user
│   └── meta-tags.html                   # NEW - SEO meta tags fragment
│
├── 🎯 main-home/                        # Landing page - Trang chủ chính
│   ├── main-home.html                   # ✅ Đã có (empty) - Hero + CTA
│   ├── sections/                        # NEW - Landing page sections
│   │   ├── hero.html                    # Hero section với video/animation
│   │   ├── features.html                # Key features showcase
│   │   ├── testimonials.html            # User reviews/testimonials
│   │   ├── stats.html                   # Statistics counter
│   │   ├── pricing.html                 # Pricing plans (nếu có)
│   │   ├── faq.html                     # FAQ section
│   │   └── cta.html                     # Call-to-action section
│   └── components/                      # NEW - Reusable components
│       ├── feature-card.html
│       ├── testimonial-card.html
│       └── stat-counter.html
│
├── 📱 app/                              # BTMS App promotion
│   ├── btms-app.html                    # ✅ Đã có (empty) - App overview
│   ├── features/                        # NEW - Chi tiết tính năng
│   │   ├── multi-court.html             # Quản lý đa sân
│   │   ├── real-time.html               # Real-time updates
│   │   ├── remote-control.html          # Điều khiển từ xa
│   │   ├── tournament-mgmt.html         # Quản lý giải đấu
│   │   └── reporting.html               # Báo cáo & thống kê
│   ├── download-app/
│   │   └── download-app.html            # ✅ Đã có (empty) - Download page
│   ├── learn-more-app/
│   │   └── learn-more-app.html          # ✅ Đã có (empty) - Details
│   ├── screenshots/                     # NEW - App screenshots gallery
│   │   └── gallery.html
│   ├── pricing/                         # NEW - Pricing & plans
│   │   └── pricing.html
│   ├── demo/                            # NEW - Live demo/video
│   │   └── demo.html
│   └── comparison/                      # NEW - So sánh với competitors
│       └── comparison.html
│
├── 🏸 tournament/                       # Tournament Platform - CORE NEW
│   ├── home.html                        # NEW - Tournament platform homepage
│   ├── list.html                        # NEW - Danh sách tất cả giải đấu
│   ├── detail.html                      # NEW - Chi tiết 1 giải đấu
│   ├── calendar.html                    # NEW - Lịch thi đấu dạng calendar
│   ├── live.html                        # NEW - Live matches ongoing
│   ├── browse/                          # NEW - Tìm kiếm & lọc
│   │   ├── search.html                  # Search tournaments
│   │   ├── filter.html                  # Advanced filters
│   │   └── map.html                     # Map view (tournaments by location)
│   ├── registration/                    # NEW - Đăng ký giải đấu
│   │   ├── register-player.html         # Đăng ký cá nhân
│   │   ├── register-team.html           # Đăng ký đội
│   │   ├── register-club.html           # Đăng ký CLB
│   │   └── confirmation.html            # Xác nhận đăng ký
│   ├── results/                         # NEW - Kết quả
│   │   ├── overview.html                # Tổng quan kết quả
│   │   ├── brackets.html                # Bảng đấu (brackets/draws)
│   │   ├── matches.html                 # Danh sách trận đấu
│   │   ├── standings.html               # Bảng xếp hạng
│   │   └── history.html                 # Lịch sử các mùa
│   ├── players/                         # NEW - Quản lý VĐV
│   │   ├── list.html                    # Danh sách VĐV
│   │   ├── profile.html                 # Profile VĐV
│   │   ├── rankings.html                # Bảng xếp hạng VĐV
│   │   └── statistics.html              # Thống kê VĐV
│   ├── clubs/                           # NEW - Quản lý CLB
│   │   ├── list.html                    # Danh sách CLB
│   │   ├── profile.html                 # Profile CLB
│   │   └── members.html                 # Thành viên CLB
│   └── categories/                      # NEW - Nội dung thi đấu
│       ├── singles.html                 # Đơn nam/nữ
│       ├── doubles.html                 # Đôi nam/nữ/mixed
│       └── age-groups.html              # Nhóm tuổi
│
├── 📰 news/                             # NEW - Tin tức & Sự kiện
│   ├── list.html                        # Danh sách bài viết
│   ├── detail.html                      # Chi tiết bài viết
│   ├── categories.html                  # Chuyên mục
│   ├── featured.html                    # Bài nổi bật
│   └── archive.html                     # Lưu trữ
│
├── 📊 statistics/                       # NEW - Thống kê & Phân tích
│   ├── overview.html                    # Tổng quan
│   ├── tournament-stats.html            # Thống kê giải đấu
│   ├── player-stats.html                # Thống kê VĐV
│   ├── club-stats.html                  # Thống kê CLB
│   ├── trends.html                      # xu hướng
│   └── leaderboards.html                # Bảng xếp hạng tổng
│
├── 👤 user/                             # NEW - Quản lý người dùng
│   ├── login.html                       # Đăng nhập
│   ├── register.html                    # Đăng ký
│   ├── profile.html                     # Hồ sơ cá nhân
│   ├── dashboard.html                   # Dashboard user
│   ├── settings.html                    # Cài đặt
│   ├── notifications.html               # Thông báo
│   └── my-tournaments.html              # Giải đấu của tôi
│
├── 🎮 scoreboard/                       # ✅ Đã có - Remote control
│   └── scoreboard.html                  # Live scoreboard view
│
├── 📌 pin/                              # ✅ Đã có - PIN access
│   └── pin-entry.html                   # PIN entry form
│
├── 📄 about-us/                         # ✅ Đã có - About
│   ├── about-us.html                    # About page
│   ├── team.html                        # NEW - Team members
│   ├── vision.html                      # NEW - Vision & Mission
│   └── contact.html                     # NEW - Contact form
│
├── 📚 resources/                        # NEW - Tài nguyên
│   ├── rules.html                       # Luật thi đấu BWF
│   ├── guides.html                      # Hướng dẫn
│   ├── tutorials.html                   # Video tutorials
│   ├── downloads.html                   # Downloads (forms, etc.)
│   └── api-docs.html                    # API documentation
│
├── 🎨 gallery/                          # NEW - Thư viện ảnh
│   ├── photos.html                      # Photo gallery
│   ├── videos.html                      # Video gallery
│   └── highlights.html                  # Highlights
│
├── 🛒 shop/                             # NEW - Shop (optional)
│   ├── products.html                    # Sản phẩm (vợt, giày, áo...)
│   ├── cart.html                        # Giỏ hàng
│   └── checkout.html                    # Thanh toán
│
├── 🎫 events/                           # NEW - Sự kiện
│   ├── upcoming.html                    # Sự kiện sắp tới
│   ├── past.html                        # Sự kiện đã qua
│   └── register.html                    # Đăng ký sự kiện
│
├── 🏆 hall-of-fame/                     # NEW - Vinh danh
│   ├── champions.html                   # Nhà vô địch
│   ├── records.html                     # Kỷ lục
│   └── legends.html                     # Huyền thoại
│
├── 📱 mobile/                           # NEW - Mobile-specific views
│   ├── home.html                        # Mobile home
│   └── menu.html                        # Mobile menu
│
└── ⚠️ error/                            # NEW - Error pages
    ├── 404.html                         # Not found
    ├── 500.html                         # Server error
    └── maintenance.html                 # Maintenance mode
```

---

## 🎨 CẤU TRÚC CSS/JS ĐỀ XUẤT

```
static/
│
├── css/
│   ├── common/                          # Shared styles
│   │   ├── variables.css                # CSS variables (colors, spacing)
│   │   ├── reset.css                    # CSS reset
│   │   ├── typography.css               # Font styles
│   │   └── utilities.css                # Utility classes
│   ├── components/                      # Component styles
│   │   ├── buttons.css
│   │   ├── cards.css
│   │   ├── forms.css
│   │   ├── modals.css
│   │   ├── tables.css
│   │   └── badges.css
│   ├── layouts/
│   │   ├── header.css
│   │   ├── footer.css
│   │   └── sidebar.css
│   ├── pages/                           # Page-specific styles
│   │   ├── main-home.css
│   │   ├── tournament-list.css
│   │   ├── tournament-detail.css
│   │   ├── player-profile.css
│   │   └── scoreboard.css
│   └── themes/                          # Theme variations
│       ├── light.css
│       └── dark.css
│
└── js/
    ├── common/                          # Shared scripts
    │   ├── app.js                       # Main app logic
    │   ├── utils.js                     # Utility functions
    │   └── api.js                       # API calls
    ├── components/                      # Component scripts
    │   ├── modal.js
    │   ├── dropdown.js
    │   ├── carousel.js
    │   └── countdown.js
    ├── pages/                           # Page-specific scripts
    │   ├── tournament-list.js
    │   ├── tournament-detail.js
    │   ├── live-scoreboard.js
    │   └── registration.js
    └── vendor/                          # Third-party libraries
        ├── chart.min.js
        ├── fullcalendar.min.js
        └── leaflet.min.js
```

---

## 💡 Ý TƯỞNG SÁNG TẠO CHO WEB PLATFORM

### 🎯 **A. Landing Page (Main Home)**

#### **1. Hero Section - Tạo ấn tượng mạnh**
```
💎 Ý tưởng:
- Video background: Clip cầu lông chuyên nghiệp
- Animated text: "Quản lý giải đấu chuyên nghiệp" với typing effect
- Dual CTA buttons:
  [Tải ứng dụng BTMS] [Khám phá giải đấu →]
- Floating elements: Icons cầu lông bay lên
```

#### **2. Interactive Features Showcase**
```
💎 Ý tưởng:
- Tab switching giữa các tính năng
- Live demo embeded (video/gif)
- Hover effects với 3D tilt
- Counter animation (số lượng giải, VĐV, trận đấu)
```

#### **3. Social Proof**
```
💎 Ý tưởng:
- Testimonials carousel với ảnh thật
- Logo các CLB/giải đấu đã sử dụng
- Rating stars animation
- Success stories
```

---

### 🏸 **B. Tournament Platform - Core Features**

#### **1. Tournament Discovery**
```
💎 Ý tưởng:
- Grid/List view toggle
- Advanced filters:
  ✓ Địa điểm (Map integration)
  ✓ Thời gian (Calendar picker)
  ✓ Nội dung (Singles/Doubles/Mixed)
  ✓ Độ tuổi (Age groups)
  ✓ Trình độ (Beginner/Intermediate/Advanced)
  ✓ Giá vé
- Sort options: Newest, Popular, Upcoming, Prize money
- Quick search với autocomplete
```

#### **2. Tournament Detail Page - Rich Content**
```
💎 Sections:
┌─────────────────────────────────────────┐
│ 📸 Cover Image + Video                  │
├─────────────────────────────────────────┤
│ 📋 Quick Info (Date, Location, Fee)    │
├─────────────────────────────────────────┤
│ 📝 Description                          │
├─────────────────────────────────────────┤
│ 🏆 Categories & Prizes                  │
├─────────────────────────────────────────┤
│ 📅 Schedule (Timeline/Calendar view)    │
├─────────────────────────────────────────┤
│ 👥 Registered Players/Teams             │
├─────────────────────────────────────────┤
│ 🎯 Brackets/Draws (Interactive)         │
├─────────────────────────────────────────┤
│ 📊 Live Results (Real-time SSE)         │
├─────────────────────────────────────────┤
│ 📍 Venue Map (Google Maps embed)        │
├─────────────────────────────────────────┤
│ 📷 Photo Gallery                        │
├─────────────────────────────────────────┤
│ 💬 Comments & Reviews                   │
├─────────────────────────────────────────┤
│ 🔗 Share (Social media)                 │
└─────────────────────────────────────────┘

💎 Interactive elements:
- Countdown timer to tournament start
- Live badge cho giải đang diễn ra
- Registration button với status (Open/Closed/Full)
- Favorite/Bookmark button
- Share to social media
```

#### **3. Live Match View - Đỉnh cao**
```
💎 Ý tưởng:
- Multiple court view (grid layout)
- Court selector tabs
- Real-time score updates (SSE)
- Match status badges (LIVE, Upcoming, Finished)
- Animated score changes
- Sound effects (optional)
- Full-screen mode
- Match timeline/history
- Live chat (optional)
```

#### **4. Interactive Brackets/Draws**
```
💎 Ý tưởng:
- SVG-based bracket visualization
- Zoom in/out controls
- Click to see match details
- Animated progression
- Print-friendly version
- Export as image/PDF
- Responsive mobile view (horizontal scroll)
```

---

### 📱 **C. BTMS App Promotion**

#### **1. Features Showcase - Storytelling**
```
💎 Ý tưởng:
- Scroll-triggered animations
- Split-screen design (Image + Text)
- GIF demos cho mỗi feature
- "Before vs After" comparison
- Feature comparison table
```

#### **2. Screenshots Gallery - Professional**
```
💎 Ý tưởng:
- Device mockups (Desktop/Tablet/Mobile)
- Carousel with thumbnails
- Lightbox zoom
- Categorized by features
- Video walkthrough embeds
```

#### **3. Download Page - Conversion focused**
```
💎 Ý tưởng:
- Platform detection (Windows 10/11)
- Download buttons với version info
- System requirements checklist
- Installation guide video
- FAQ accordion
- Quick start guide download
- Release notes link
```

---

### 👤 **D. User Experience Features**

#### **1. Dashboard - Personalized**
```
💎 Sections:
- My Tournaments (Registered/Watching)
- Upcoming Matches
- Recent Results
- Notifications
- Quick actions (Register, Browse, Profile)
- Favorite players/clubs
- Statistics overview
```

#### **2. Registration Flow - Smooth**
```
💎 Steps:
1. Select tournament
2. Choose category
3. Select registration type (Individual/Team/Club)
4. Fill form (với validation real-time)
5. Upload documents (if required)
6. Review & confirm
7. Payment (if applicable)
8. Confirmation email + QR code
```

#### **3. Player Profile - Rich Data**
```
💎 Sections:
- Profile photo + cover
- Bio & Info
- Statistics (Matches played, Win rate, etc.)
- Tournament history
- Achievements/Badges
- Recent matches
- Rankings
- Photos/Videos
- Follow button
```

---

### 📊 **E. Statistics & Analytics**

#### **1. Interactive Charts**
```
💎 Ý tưởng:
- Chart.js/D3.js visualizations
- Filters by time period
- Compare players/clubs
- Export data
- Share charts
```

#### **2. Leaderboards**
```
💎 Ý tưởng:
- Real-time rankings
- Multiple categories
- Filter by region/age/gender
- Search player
- Podium animation
- Rising star highlights
```

---

### 🎨 **F. Design & UX Enhancements**

#### **1. Theme System**
```
💎 Ý tưởng:
- Light/Dark mode toggle
- Theme customization
- Save preference in localStorage
- Smooth transitions
```

#### **2. Animations**
```
💎 Ý tưởng:
- Page transitions
- Scroll animations (AOS library)
- Hover effects
- Loading skeletons
- Micro-interactions
```

#### **3. Accessibility**
```
💎 Best practices:
- Semantic HTML
- ARIA labels
- Keyboard navigation
- Screen reader support
- Color contrast (WCAG AA)
- Focus indicators
```

---

### 🚀 **G. Advanced Features (Future)**

#### **1. Live Streaming**
```
💎 Ý tưởng:
- Video embeds (YouTube/Vimeo)
- Multi-camera angles
- Picture-in-picture
- Chat integration
```

#### **2. Mobile App**
```
💎 Ý tưởng:
- Progressive Web App (PWA)
- Push notifications
- Offline mode
- Install prompt
```

#### **3. Gamification**
```
💎 Ý tưởng:
- Badges/Achievements
- Points system
- Leaderboards
- Challenges
- Rewards program
```

#### **4. Social Features**
```
💎 Ý tưởng:
- Follow players/clubs
- News feed
- Share results
- Comments & reactions
- Photo/video uploads
```

#### **5. AI Features**
```
💎 Ý tưởng:
- Match predictions
- Player recommendations
- Smart search
- Chatbot support
```

---

## 📋 PRIORITIZATION - Roadmap

### **Sprint 1: Foundation (Week 1-2)**
```
✅ Priority 1:
- layouts/ (base, header, footer)
- main-home/ (hero, features, cta)
- app/ (btms-app overview, download)
- tournament/ (home, list, detail - basic)
```

### **Sprint 2: Core Platform (Week 3-4)**
```
✅ Priority 2:
- tournament/ (calendar, live, results)
- players/ (list, profile)
- clubs/ (list, profile)
- user/ (login, register, dashboard)
```

### **Sprint 3: Enhancement (Week 5-6)**
```
✅ Priority 3:
- tournament/ (registration flow)
- tournament/results/ (brackets, standings)
- statistics/ (overview, leaderboards)
- news/ (list, detail)
```

### **Sprint 4: Polish (Week 7-8)**
```
✅ Priority 4:
- gallery/ (photos, videos)
- resources/ (guides, rules)
- hall-of-fame/
- Mobile optimization
- Performance tuning
- SEO optimization
```

---

## 🎯 KẾT LUẬN

### **Hai hướng phát triển song song:**

#### **1. App Promotion (Landing Page)**
```
Mục tiêu: Convert visitors → App downloads
- Professional design
- Clear value proposition
- Strong CTAs
- Social proof
- Easy download flow
```

#### **2. Tournament Platform**
```
Mục tiêu: Create ecosystem for badminton community
- Discover tournaments
- Register & participate
- Follow live matches
- View results & rankings
- Connect with players/clubs
```

### **Success Metrics:**
```
📈 App promotion:
- Download count
- Page views
- Conversion rate
- Time on site

📈 Tournament platform:
- User registrations
- Tournament listings
- Active users
- Match views
- Engagement rate
```

---

**Bạn muốn bắt đầu implement phần nào trước?** 🚀
1. Landing page (main-home)
2. Tournament list & detail
3. App promotion pages
4. User system (login/register)

Tôi sẽ giúp code chi tiết cho phần bạn chọn! 💪
