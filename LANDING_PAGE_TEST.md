# 🎯 Hướng dẫn Test Landing Page

## ✅ Đã hoàn thành

### 1. **Templates** (7/7 sections)
- ✅ `main-home/main-home.html` - Main layout
- ✅ `main-home/sections/hero.html` - Hero section
- ✅ `main-home/sections/features.html` - Features grid
- ✅ `main-home/sections/stats.html` - Stats counters
- ✅ `main-home/sections/app-showcase.html` - App showcase
- ✅ `main-home/sections/tournament-preview.html` - Tournament cards
- ✅ `main-home/sections/testimonials.html` - User testimonials
- ✅ `main-home/sections/cta.html` - Call-to-action

### 2. **Styling**
- ✅ `css/pages/main-home.css` - Landing page styles (1000+ lines)
- ✅ CSS cho tất cả 7 sections
- ✅ Responsive design (mobile/tablet/desktop)
- ✅ Animations & hover effects

### 3. **JavaScript**
- ✅ `js/pages/main-home.js` - Landing page interactions (400+ lines)
- ✅ Counter animation
- ✅ Scroll animations (AOS)
- ✅ Card interactions
- ✅ Live badge effects
- ✅ Parallax effects
- ✅ Download tracking

### 4. **Controller**
- ✅ `HomeController.java` - Routing cho `/` và `/home`
- ✅ Model attributes cho stats
- ✅ SEO metadata

### 5. **Libraries**
- ✅ AOS (Animate On Scroll) 2.3.4 - Added to base.html
- ✅ Bootstrap 5.3.3
- ✅ Bootstrap Icons 1.11.3
- ✅ jQuery 3.7.1

---

## 🚀 Cách chạy để test

### Option 1: Từ VS Code
1. Mở Terminal (Ctrl + `)
2. Chạy lệnh:
```powershell
mvn spring-boot:run
```

### Option 2: Từ IDE (IntelliJ/Eclipse)
1. Mở file `BadmintonTournamentManagementSystemApplication.java`
2. Click Run hoặc Ctrl+Shift+F10

### Option 3: Build JAR và chạy
```powershell
mvn clean package
java -jar target/btms-2.0.0.jar
```

---

## 🌐 Truy cập Landing Page

Sau khi server khởi động thành công, mở trình duyệt và truy cập:

```
http://localhost:8080/
```

hoặc

```
http://localhost:8080/home
```

---

## ✨ Các tính năng cần test

### Hero Section
- [ ] Video background (nếu có)
- [ ] Gradient text animation
- [ ] CTA buttons hover
- [ ] Floating badges animation
- [ ] Scroll indicator bounce
- [ ] Social proof với avatars

### Features Section
- [ ] 6 feature cards hiển thị đúng
- [ ] "Phổ biến nhất" ribbon trên card Remote Control
- [ ] Card hover lift effect
- [ ] Featured card gradient background

### Stats Section
- [ ] Counter animation khi scroll vào view
- [ ] 4 stats: 500 tournaments, 10K players, 150 clubs, 25K matches
- [ ] Growth badge (+35%)
- [ ] Gradient background với pattern

### App Showcase
- [ ] Desktop screenshot
- [ ] Mobile screenshot (overlay)
- [ ] System requirements table
- [ ] Download button
- [ ] Version info (v1.0.0)

### Tournament Preview
- [ ] 3 tournament cards
- [ ] LIVE badge pulse animation
- [ ] Upcoming badge (yellow)
- [ ] Registration badge (green)
- [ ] Card hover scale effect
- [ ] Image zoom on hover

### Testimonials
- [ ] 6 testimonial cards
- [ ] Featured card (card giữa) với ribbon
- [ ] Rating stars
- [ ] Avatar + author info
- [ ] Overall rating 4.8/5 từ 250+ reviews

### CTA Section
- [ ] Gradient background
- [ ] Decorative shapes
- [ ] 2 CTA buttons
- [ ] Feature checklist
- [ ] Ripple effect khi click button

### Responsive
- [ ] Mobile (< 768px): Stack layout
- [ ] Tablet (768px - 992px): 2 columns
- [ ] Desktop (> 992px): 3 columns
- [ ] Navigation menu responsive

---

## 🐛 Troubleshooting

### Lỗi: Template not found
**Nguyên nhân:** Thymeleaf không tìm thấy template
**Giải pháp:**
- Kiểm tra đường dẫn trong `HomeController.java` return `"main-home/main-home"`
- Đảm bảo file `main-home.html` nằm trong `src/main/resources/templates/main-home/`

### Lỗi: CSS/JS không load
**Nguyên nhân:** Static resources không được serve
**Giải pháp:**
- Kiểm tra `application.properties` có config static resources
- Đảm bảo CSS/JS files nằm trong `src/main/resources/static/`
- Clear browser cache (Ctrl + Shift + R)

### Lỗi: AOS animations không chạy
**Nguyên nhân:** AOS library chưa được initialize
**Giải pháp:**
- Kiểm tra AOS đã được thêm vào `base.html`
- Mở DevTools Console, xem có lỗi JS không
- Thử disable ad-blocker (một số blocker chặn CDN)

### Lỗi: Counter không animate
**Nguyên nhân:** Intersection Observer không được support hoặc JS lỗi
**Giải pháp:**
- Test trên browser hiện đại (Chrome, Firefox, Edge)
- Mở Console kiểm tra lỗi
- Thử scroll xuống stats section rồi reload page

### Port 8080 đã được sử dụng
**Giải pháp:**
```powershell
# Tìm process đang dùng port 8080
netstat -ano | findstr :8080

# Kill process (thay PID bằng số process ID)
taskkill /PID <PID> /F

# Hoặc đổi port trong application.properties
server.port=8081
```

---

## 📊 Performance Tips

1. **Lazy Loading Images:**
   - Thêm `data-src` attribute thay vì `src`
   - JS sẽ tự động load khi scroll vào view

2. **Optimize Images:**
   - Resize images về kích thước phù hợp
   - Compress với TinyPNG hoặc ImageOptim
   - Dùng WebP format nếu có thể

3. **CDN Caching:**
   - Bootstrap, jQuery, AOS đều load từ CDN
   - Browser sẽ cache lại để tăng tốc

4. **Minify CSS/JS:**
   - Production nên minify CSS/JS
   - Có thể dùng Maven plugin để auto minify

---

## 📝 Notes

- **Main route:** `/` và `/home` đều point đến landing page
- **Health check:** `/health` redirect về home
- **Stats data:** Hardcoded trong `HomeController.java`, có thể thay bằng database query sau
- **Images:** Hiện đang dùng placeholder từ `pravatar.cc` và `picsum.photos`, cần thay bằng ảnh thật
- **Video background:** Hiện chưa có video, cần thêm video vào hero section nếu muốn

---

## 🎨 Customization

### Đổi màu sắc
Edit file `css/common/variables.css`:
```css
--color-primary: #0066ff;  /* Màu chính */
--color-secondary: #ff6b35; /* Màu phụ */
```

### Đổi font
Edit file `layouts/base.html`:
```html
<!-- Thay Google Fonts URL -->
<link href="https://fonts.googleapis.com/css2?family=YOUR_FONT&display=swap" rel="stylesheet">
```

### Thêm/bớt sections
Edit file `main-home/main-home.html`:
```html
<!-- Comment out sections không cần -->
<!-- <div th:replace="~{main-home/sections/stats :: stats}"></div> -->
```

---

## 🔄 Next Steps

Sau khi test xong landing page, có thể:

1. **Tạo Tournament Platform pages** (Option B)
   - Tournament list
   - Tournament detail
   - Live scoreboard
   - Tournament registration

2. **Tạo App Promotion pages** (Option C)
   - Features detail
   - Download page
   - Documentation
   - FAQ

3. **Tạo User System pages** (Option D)
   - Login/Register
   - User profile
   - Dashboard
   - Settings

4. **Integrate real data**
   - Connect stats với database
   - Load real tournaments
   - User testimonials từ DB

5. **Add more features**
   - Search functionality
   - Filters
   - Pagination
   - Social sharing

---

**Created:** November 8, 2025  
**Status:** ✅ Ready for testing  
**Version:** 1.0.0
