# 🧹 Cleanup Plan - Immediate Actions

## ✅ Files to DELETE (Không được sử dụng)

### Templates:
```bash
# Lý do: TournamentController return "badmintonTournament/badmintonTournament"
❌ templates/tournament/detail.html
❌ templates/tournament/tournament-detail.html

# Decision: Giữ badmintonTournament/badmintonTournament.html vì đang được dùng
```

### CSS - Merge enhanced files:

**Quyết định:** Giữ `-enhanced` files vì chúng có nhiều features hơn

```bash
# Keep enhanced versions, delete old ones if exists
✅ KEEP: css/badmintonTournament/ranking-enhanced.css
✅ KEEP: css/badmintonTournament/schedule-enhanced.css  
✅ KEEP: css/badmintonTournament/stats-enhanced.css

# HOẶC rename để bỏ -enhanced:
ranking-enhanced.css → ranking.css
schedule-enhanced.css → schedule.css
stats-enhanced.css → stats.css
```

---

## 📝 Standardization Tasks

### 1. Update Controller Returns

**TournamentController.java:**
```java
// Line 91: Hiện tại
return "badmintonTournament/badmintonTournament";

// Đề xuất đổi thành:
return "tournament/detail";  // Nhất quán hơn với /tournament/{id}
```

### 2. Rename Templates (Optional - Phase 2)

```bash
# Option A: Keep current structure
badmintonTournament/badmintonTournament.html → tournament/detail.html

# Option B: Semantic naming
badmintonTournament/badmintonTournament.html → tournament/platform.html
```

### 3. CSS/JS Consistency Check

**Current structure OK:**
```
css/badmintonTournament/
├── badmintonTournament.css          ✅ Main styles
├── ranking-enhanced.css             ✅ Section specific
├── schedule-enhanced.css            ✅ Section specific
└── stats-enhanced.css               ✅ Section specific

js/badmintonTournament/
├── badmintonTournament.js           ✅ Main script
└── tournament-enhanced.js           ✅ Enhanced features
```

---

## 🔄 Routing Convention

### Current Routes:
```
/tournament/list              → tournament/list.html           ✅
/tournament/{id}              → badmintonTournament/badmintonTournament.html  ⚠️
/tournament/search            → tournament/list.html           ✅
/badmintonTournament/badmintonTournament → badmintonTournament/badmintonTournament.html  ⚠️
```

### Recommended Routes:
```
/tournament/list              → tournament/list.html           ✅
/tournament/{id}              → tournament/detail.html         ✅ Nhất quán
/tournament/search            → tournament/list.html           ✅
/tournament/{id}/platform     → tournament/platform.html       ✅ Nếu cần tách
```

---

## 🎯 Priority Actions

### HIGH PRIORITY (Làm ngay):

1. ✅ **Delete unused files:**
   ```bash
   rm templates/tournament/detail.html
   rm templates/tournament/tournament-detail.html
   ```

2. ✅ **Rename enhanced CSS (remove -enhanced suffix):**
   ```bash
   mv ranking-enhanced.css → ranking.css
   mv schedule-enhanced.css → schedule.css  
   mv stats-enhanced.css → stats.css
   ```

3. ✅ **Update HTML references:**
   ```html
   <!-- Before -->
   <link href="/css/badmintonTournament/ranking-enhanced.css" rel="stylesheet" />
   
   <!-- After -->
   <link href="/css/badmintonTournament/ranking.css" rel="stylesheet" />
   ```

### MEDIUM PRIORITY (Tuần này):

4. **Move badmintonTournament.html:**
   ```bash
   # Option 1: Semantic
   mv badmintonTournament/badmintonTournament.html → tournament/platform.html
   
   # Option 2: Simple
   mv badmintonTournament/badmintonTournament.html → tournament/detail.html
   ```

5. **Update Controllers:**
   ```java
   // TournamentController.java - Line 91
   return "tournament/detail";
   
   // BadmintonTournamentController.java - Line 9
   return "tournament/platform";  // hoặc "tournament/detail"
   ```

6. **Move sections:**
   ```bash
   mv badmintonTournamentSections/ → tournament/sections/
   ```

### LOW PRIORITY (Tuần sau):

7. **Create common modules:**
   ```bash
   mkdir -p css/common js/common
   # Extract shared variables, utilities
   ```

8. **Update documentation:**
   ```bash
   # Add to README.md:
   - Routing convention
   - Naming convention
   - File organization
   ```

---

## 📊 Impact Assessment

### Breaking Changes:
- ❌ URLs không đổi (safe)
- ⚠️ Template paths đổi (cần update controllers)
- ✅ CSS/JS paths đổi (cần update HTML)

### Testing Checklist:
- [ ] `/tournament/list` still works
- [ ] `/tournament/{id}` still works
- [ ] `/tournament/search` still works
- [ ] `/badmintonTournament/badmintonTournament` still works
- [ ] All CSS loads correctly
- [ ] All JS loads correctly
- [ ] No 404 errors in browser console

---

## 🚀 Execution Order

### Step 1: Backup
```bash
git add .
git commit -m "Backup before cleanup"
git checkout -b cleanup-structure
```

### Step 2: Delete unused files
```bash
# Execute cleanup script
```

### Step 3: Rename enhanced files
```bash
# Execute rename script
```

### Step 4: Update HTML references
```bash
# Find and replace in HTML files
```

### Step 5: Test thoroughly
```bash
mvn clean resources:resources
# Test all routes
```

### Step 6: Commit changes
```bash
git add .
git commit -m "Cleanup: Remove duplicates and standardize naming"
```

---

**Next:** Bạn muốn tôi execute cleanup plan này không?
