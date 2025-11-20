# ✅ Phase 1 Implementation Checklist - Database & Backend Foundation

> **Timeline**: Week 1-2 (Nov 17 - Dec 1, 2025)  
> **Goal**: Complete database migrations and backend foundation for Tournament Hub

---

## 📅 WEEK 1: DATABASE FOUNDATION

### Day 1 (Nov 17, 2025) - Database Migrations
- [x] Review current database schema
- [x] Create migration plan document
- [x] Create migration scripts (V1.1, V1.2, V1.3)
- [ ] **EXECUTE NOW**: Backup current database
- [ ] **EXECUTE NOW**: Run V1.1__enhance_tournaments.sql
- [ ] **EXECUTE NOW**: Run V1.2__enhance_users.sql
- [ ] **EXECUTE NOW**: Run V1.3__create_tournament_gallery.sql
- [ ] **VERIFY**: Check all columns added successfully
- [ ] **TEST**: Desktop App still works correctly
- [ ] *Optional*: Run SAMPLE_DATA.sql for testing

**Expected Output**: ✅ Database enhanced with web platform fields

---

### Day 2 (Nov 18, 2025) - JPA Entities Update

#### GiaiDau.java Enhancement
- [ ] Add Jakarta Persistence annotations (@Entity, @Table, @Column)
- [ ] Add new fields with proper mappings:
  - [ ] `moTa` (TEXT)
  - [ ] `diaDiem` (VARCHAR 500)
  - [ ] `tinhThanh` (VARCHAR 100)
  - [ ] `quocGia` (VARCHAR 50)
  - [ ] `trangThai` (VARCHAR 20) - with Enum TrangThaiGiaiDau
  - [ ] `noiBat` (Boolean)
  - [ ] `hinhAnh`, `logo` (VARCHAR 500)
  - [ ] `ngayMoDangKi`, `ngayDongDangKi` (LocalDate)
  - [ ] `soLuongToiDa` (Integer)
  - [ ] `phiThamGia` (BigDecimal)
  - [ ] `giaiThuong` (TEXT)
  - [ ] `dienThoai`, `email`, `website` (VARCHAR)
  - [ ] `capDo`, `theLoai` (VARCHAR 50) - with Enums
  - [ ] `sanThiDau`, `quyDinh` (TEXT)
  - [ ] `luotXem`, `tongDanhGia` (Integer)
  - [ ] `danhGiaTb` (BigDecimal)
- [ ] Add getters/setters for all new fields
- [ ] Add validation annotations (@NotNull, @Size, @Min, @Max)
- [ ] Update constructors
- [ ] Add helper methods (e.g., isRegistrationOpen(), isOngoing())

#### NguoiDung.java Enhancement
- [ ] Add Jakarta Persistence annotations
- [ ] Add new fields:
  - [ ] `email` (VARCHAR 100, UNIQUE)
  - [ ] `dienThoai` (VARCHAR 20)
  - [ ] `vaiTro` (VARCHAR 20) - with Enum VaiTro
  - [ ] `trangThai` (VARCHAR 20) - with Enum TrangThaiUser
  - [ ] `lanDangNhapCuoi` (LocalDateTime)
  - [ ] `anhDaiDien` (VARCHAR 500)
  - [ ] `xacThucEmail` (Boolean)
  - [ ] `maXacThuc`, `maDatLaiMk` (VARCHAR 100)
  - [ ] `ngayHetHanToken` (LocalDateTime)
- [ ] Add getters/setters
- [ ] Add validation annotations
- [ ] Update constructors

#### TournamentGallery.java (NEW)
- [ ] Create new entity class
- [ ] Add fields: id, idGiai, loai, url, tieuDe, moTa, thuTu, thoiGianTao
- [ ] Add @ManyToOne relationship to GiaiDau
- [ ] Add getters/setters
- [ ] Add validation

**Expected Output**: ✅ JPA entities ready for database operations

---

### Day 3 (Nov 19, 2025) - Enums & DTOs

#### Create Enums
- [ ] `TrangThaiGiaiDau.java` (DRAFT, UPCOMING, REGISTRATION, ONGOING, COMPLETED, CANCELLED)
- [ ] `CapDoGiaiDau.java` (PROFESSIONAL, AMATEUR, YOUTH)
- [ ] `TheLoaiGiaiDau.java` (OPEN, INVITATIONAL, LEAGUE)
- [ ] `VaiTro.java` (ADMIN, ORGANIZER, PLAYER, CLIENT)
- [ ] `TrangThaiUser.java` (ACTIVE, SUSPENDED, DELETED)
- [ ] `LoaiMedia.java` (IMAGE, VIDEO, DOCUMENT)

#### Create DTOs (Data Transfer Objects)
- [ ] `TournamentDTO.java` - For list views
  - [ ] Basic info (id, name, dates, location, status, featured)
  - [ ] Display fields (image, viewCount, rating)
  - [ ] @Builder annotation
- [ ] `TournamentDetailDTO.java` - For detail page
  - [ ] All tournament info
  - [ ] Nested objects (gallery, organizer info)
- [ ] `TournamentCardDTO.java` - For card components
  - [ ] Minimal info for grid displays
- [ ] `TournamentCalendarEventDTO.java` - For calendar view
  - [ ] title, start, end, status, color
- [ ] `TournamentStatsDTO.java` - For statistics
  - [ ] participant count, match count, etc.
- [ ] `UserDTO.java` - For user info
- [ ] `TournamentGalleryDTO.java` - For media

#### Create Mappers
- [ ] `TournamentMapper.java` (using MapStruct or manual)
  - [ ] toDTO(GiaiDau entity)
  - [ ] toDetailDTO(GiaiDau entity)
  - [ ] toCardDTO(GiaiDau entity)
  - [ ] toCalendarEventDTO(GiaiDau entity)
  - [ ] toEntity(TournamentDTO dto)
- [ ] `UserMapper.java`
- [ ] `GalleryMapper.java`

**Expected Output**: ✅ DTOs and Mappers ready for service layer

---

### Day 4 (Nov 20, 2025) - Repository Layer

#### Create/Update Repositories
- [ ] Update `GiaiDauRepository.java` (if exists) or create new
  - [ ] Extend JpaRepository<GiaiDau, Integer>
  - [ ] Custom queries:
    ```java
    List<GiaiDau> findByTrangThai(TrangThaiGiaiDau status);
    List<GiaiDau> findByNoiBatTrue();
    Page<GiaiDau> findByTinhThanhContaining(String city, Pageable pageable);
    List<GiaiDau> findByNgayBdBetween(LocalDate start, LocalDate end);
    @Query("SELECT g FROM GiaiDau g WHERE g.trangThai = :status ORDER BY g.ngayBd DESC")
    List<GiaiDau> findUpcoming(@Param("status") TrangThaiGiaiDau status);
    ```
  - [ ] Search query (by name, location)
  - [ ] Count by status
  - [ ] Find featured & upcoming

- [ ] Create `TournamentGalleryRepository.java`
  - [ ] findByIdGiai(Integer idGiai)
  - [ ] findByIdGiaiAndLoai(Integer idGiai, LoaiMedia loai)

- [ ] Update `VanDongVienRepository.java` (for future use)
- [ ] Update `CauLacBoRepository.java` (for future use)

**Expected Output**: ✅ Repositories with custom queries ready

---

### Day 5 (Nov 21, 2025) - Service Layer Part 1

#### Update TournamentDataService
- [ ] Inject GiaiDauRepository
- [ ] **Remove** JSON file loading logic
- [ ] Implement database queries:
  - [ ] `getAllTournaments()` → return List<TournamentDTO>
  - [ ] `getTournamentById(Integer id)` → return TournamentDetailDTO
  - [ ] `getFeaturedTournaments()` → return List<TournamentDTO>
  - [ ] `getUpcomingTournaments()` → return List<TournamentDTO>
  - [ ] `getOngoingTournaments()` → return List<TournamentDTO>
  - [ ] `getTournamentsByStatus(String status)` → return List<TournamentDTO>
  - [ ] `getTournamentsByCity(String city)` → return List<TournamentDTO>
  - [ ] `getRecentTournaments(int limit)` → return List<TournamentDTO>
- [ ] Add mapping from Entity to DTO using TournamentMapper
- [ ] Add error handling (throw exceptions if not found)
- [ ] Add logging

#### Create TournamentService (Advanced operations)
- [ ] Inject repositories and mappers
- [ ] Implement methods:
  - [ ] `searchTournaments(String keyword, TournamentSearchCriteria criteria, Pageable pageable)`
  - [ ] `getTournamentStats(Integer id)` → return TournamentStatsDTO
  - [ ] `incrementViewCount(Integer id)`
  - [ ] `getTournamentGallery(Integer id)` → return List<TournamentGalleryDTO>
  - [ ] `getCalendarEvents(int month, int year)` → return List<CalendarEventDTO>
- [ ] Add @Transactional annotations where needed
- [ ] Add validation logic
- [ ] Add caching (@Cacheable) for frequently accessed data

**Expected Output**: ✅ Service layer with business logic ready

---

### Weekend (Nov 22-23, 2025) - Testing & Documentation

#### Unit Tests
- [ ] `GiaiDauRepositoryTest.java`
  - [ ] Test findByTrangThai
  - [ ] Test custom queries
  - [ ] Test pagination
- [ ] `TournamentDataServiceTest.java`
  - [ ] Mock repository
  - [ ] Test getAllTournaments
  - [ ] Test getFeaturedTournaments
  - [ ] Test edge cases (empty list, null values)
- [ ] `TournamentMapperTest.java`
  - [ ] Test entity to DTO conversion
  - [ ] Test null handling

#### Integration Tests
- [ ] `TournamentIntegrationTest.java`
  - [ ] Test with actual database (H2 in-memory)
  - [ ] Test full flow: Repository → Service → DTO

#### Documentation
- [ ] Add JavaDoc to all public methods
- [ ] Update README with database changes
- [ ] Document DTO structures
- [ ] Create sequence diagrams (optional)

**Expected Output**: ✅ Tested and documented backend foundation

---

## 📅 WEEK 2: REST API & FRONTEND INTEGRATION

### Day 6 (Nov 24, 2025) - REST API Controllers

#### Create TournamentApiController
- [ ] Create `@RestController` class
- [ ] Add `@RequestMapping("/api/tournaments")`
- [ ] Implement endpoints:
  ```java
  GET  /api/tournaments                        → List all
  GET  /api/tournaments/{id}                   → Get detail
  GET  /api/tournaments/featured               → Featured list
  GET  /api/tournaments/upcoming               → Upcoming list
  GET  /api/tournaments/live                   → Live/ongoing list
  GET  /api/tournaments/calendar?month=11&year=2025 → Calendar events
  GET  /api/tournaments/search?q=...&city=...  → Search
  POST /api/tournaments/{id}/view              → Increment view
  ```
- [ ] Add request param validation
- [ ] Add response pagination (Page<>)
- [ ] Add proper HTTP status codes
- [ ] Add error handling (@ExceptionHandler)
- [ ] Add CORS configuration

#### Create GlobalExceptionHandler
- [ ] Handle ResourceNotFoundException
- [ ] Handle ValidationException
- [ ] Return proper error response format
- [ ] Add logging

#### Add Swagger/OpenAPI Documentation
- [ ] Add springdoc-openapi dependency
- [ ] Configure Swagger UI
- [ ] Add @Operation, @ApiResponse annotations
- [ ] Add example values

**Expected Output**: ✅ RESTful APIs documented with Swagger

---

### Day 7 (Nov 25, 2025) - API Testing

#### Manual Testing with Postman/Insomnia
- [ ] Create API collection
- [ ] Test all GET endpoints
- [ ] Test pagination
- [ ] Test filtering
- [ ] Test search
- [ ] Test error cases (404, 400)
- [ ] Document API examples

#### Automated Testing
- [ ] `TournamentApiControllerTest.java`
  - [ ] @WebMvcTest
  - [ ] Mock service layer
  - [ ] Test all endpoints
  - [ ] Test response formats
  - [ ] Test error handling

**Expected Output**: ✅ APIs fully tested and working

---

### Day 8-9 (Nov 26-27, 2025) - Frontend Integration

#### Update TournamentController (Web MVC)
- [ ] Inject TournamentService (not DataService)
- [ ] Update `tournamentHome()`:
  - [ ] Fetch data from service
  - [ ] Add error handling
  - [ ] Add model attributes
- [ ] Update `tournamentList()`:
  - [ ] Add pagination support
  - [ ] Add filter parameters
  - [ ] Add search functionality
- [ ] Update `tournamentDetail()`:
  - [ ] Fetch full details
  - [ ] Fetch gallery
  - [ ] Fetch participants/matches (if available)
  - [ ] Increment view count
- [ ] Add `tournamentCalendar()` method
- [ ] Add `tournamentLive()` method

#### Update Templates with Real Data
- [ ] `tournament-home.html`:
  - [ ] Connect to actual data
  - [ ] Remove mock data
  - [ ] Add loading states
  - [ ] Add error messages
- [ ] `tournament-list.html`:
  - [ ] Connect to pagination
  - [ ] Connect to filters
  - [ ] Add sort options
- [ ] `tournament-detail.html`:
  - [ ] Connect to detail data
  - [ ] Add gallery display
  - [ ] Add tabs (if needed)

#### Add JavaScript for Dynamic Features
- [ ] Create `tournament-home.js`:
  - [ ] Quick search functionality
  - [ ] Filter chip interactions
- [ ] Create `tournament-list.js`:
  - [ ] AJAX pagination
  - [ ] Filter sidebar interactions
  - [ ] Search with debouncing
- [ ] Create `tournament-calendar.js`:
  - [ ] FullCalendar initialization
  - [ ] Fetch events from API
  - [ ] Event click handlers

**Expected Output**: ✅ Frontend connected to backend

---

### Day 10 (Nov 28, 2025) - Styling & Polish

#### CSS Enhancements
- [ ] Complete `tournament-home.css`
- [ ] Complete `tournament-list.css`
- [ ] Complete `tournament-detail.css`
- [ ] Add responsive breakpoints
- [ ] Add animations
- [ ] Test on different screen sizes

#### UI/UX Improvements
- [ ] Loading spinners
- [ ] Empty states (no tournaments found)
- [ ] Error states
- [ ] Success messages
- [ ] Hover effects
- [ ] Transitions

**Expected Output**: ✅ Polished UI ready for demo

---

### Day 11-12 (Nov 29-30, 2025) - End-to-End Testing

#### Cross-browser Testing
- [ ] Chrome
- [ ] Firefox
- [ ] Edge
- [ ] Safari (if available)

#### Device Testing
- [ ] Desktop (1920x1080)
- [ ] Laptop (1366x768)
- [ ] Tablet (768px)
- [ ] Mobile (375px)

#### Feature Testing
- [ ] Tournament discovery flow
- [ ] Search and filter
- [ ] Calendar view
- [ ] Detail page
- [ ] Navigation
- [ ] Loading performance

#### Bug Fixing
- [ ] Create bug list
- [ ] Prioritize (Critical → Low)
- [ ] Fix critical bugs
- [ ] Fix high priority bugs
- [ ] Document known issues

**Expected Output**: ✅ Stable, tested tournament hub

---

### Weekend Milestone (Dec 1, 2025) - Phase 1 Complete! 🎉

#### Deliverables Checklist
- [ ] ✅ Database migrated successfully
- [ ] ✅ JPA Entities with new fields
- [ ] ✅ DTOs and Mappers working
- [ ] ✅ Repository layer with custom queries
- [ ] ✅ Service layer with business logic
- [ ] ✅ REST APIs documented and tested
- [ ] ✅ Web MVC controllers updated
- [ ] ✅ Frontend templates connected
- [ ] ✅ JavaScript interactions working
- [ ] ✅ Responsive CSS styling complete
- [ ] ✅ Cross-browser tested
- [ ] ✅ Desktop App still working (IMPORTANT!)

#### Demo Preparation
- [ ] Prepare demo script
- [ ] Create demo data (tournaments, users)
- [ ] Practice demo flow
- [ ] Screenshot key features
- [ ] Record demo video (optional)

#### Documentation
- [ ] Update README.md
- [ ] API documentation complete
- [ ] Database schema documented
- [ ] User guide (basic)

---

## 🎯 SUCCESS CRITERIA

Phase 1 is considered COMPLETE when:

1. ✅ **Database**: All migrations executed, Desktop App compatible
2. ✅ **Backend**: APIs returning correct data, proper error handling
3. ✅ **Frontend**: All tournament pages functional, responsive design
4. ✅ **Performance**: Page load < 3 seconds, API response < 500ms
5. ✅ **Quality**: No critical bugs, tested on major browsers
6. ✅ **Documentation**: Code documented, API docs available

---

## 📊 PROGRESS TRACKING

Update this section daily:

```
Week 1 Progress: [====      ] 40%
- Day 1: ✅ Complete
- Day 2: 🚧 In Progress
- Day 3: ⏳ Pending
- Day 4: ⏳ Pending
- Day 5: ⏳ Pending

Week 2 Progress: [          ] 0%
- Day 6: ⏳ Pending
- Day 7: ⏳ Pending
- Day 8: ⏳ Pending
- Day 9: ⏳ Pending
- Day 10: ⏳ Pending
```

---

## 🚀 READY TO START?

**Current Status**: Day 1 - Database Migrations Ready  
**Next Action**: Execute migration scripts!

### Immediate Next Steps:
1. [ ] Open H2 Console or your SQL client
2. [ ] Backup current database
3. [ ] Execute V1.1__enhance_tournaments.sql
4. [ ] Verify execution successful
5. [ ] Execute V1.2__enhance_users.sql
6. [ ] Execute V1.3__create_tournament_gallery.sql
7. [ ] Run verification queries
8. [ ] Test Desktop App
9. [ ] Mark Day 1 as ✅ Complete

**Let's begin! 💪🏸**
