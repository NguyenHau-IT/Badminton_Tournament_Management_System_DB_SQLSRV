# 📚 Documentation Index - BTMS Web Platform Development

> **Central hub** for all documentation related to BTMS Web Platform development

---

## 🎯 START HERE

### New to the project?
1. **Read first**: [GETTING_STARTED.md](GETTING_STARTED.md) - Quick start guide
2. **Understand scope**: [LO_TRINH_PHAT_TRIEN_WEB_PLATFORM.md](LO_TRINH_PHAT_TRIEN_WEB_PLATFORM.md) - Full roadmap
3. **Database setup**: [DATABASE_ENHANCEMENT_PLAN.md](DATABASE_ENHANCEMENT_PLAN.md) - Schema changes

### Ready to start Phase 1?
4. **Daily checklist**: [PHASE_1_CHECKLIST.md](PHASE_1_CHECKLIST.md) - 2-week detailed tasks
5. **Migration guide**: [../database/migrations/README.md](../database/migrations/README.md) - Execute migrations

---

## 📁 DOCUMENTATION STRUCTURE

### 🗺️ Planning & Strategy Documents

| Document | Purpose | Status | Priority |
|----------|---------|--------|----------|
| **[LO_TRINH_PHAT_TRIEN_WEB_PLATFORM.md](LO_TRINH_PHAT_TRIEN_WEB_PLATFORM.md)** | Lộ trình phát triển 7 phases, timeline, priorities | ✅ Complete | 🔴 Must Read |
| **[GETTING_STARTED.md](GETTING_STARTED.md)** | Quick start guide for Phase 1 | ✅ Complete | 🔴 Must Read |
| **[WEB_PLATFORM_STRUCTURE.md](WEB_PLATFORM_STRUCTURE.md)** | Cấu trúc templates và components | ✅ Complete | 🟡 Reference |

---

### 🗄️ Database Documents

| Document | Purpose | Status | Priority |
|----------|---------|--------|----------|
| **[DATABASE_ENHANCEMENT_PLAN.md](DATABASE_ENHANCEMENT_PLAN.md)** | Database migration strategy, new fields, tables | ✅ Complete | 🔴 Must Read |
| **[../database/migrations/README.md](../database/migrations/README.md)** | Migration execution guide, troubleshooting | ✅ Complete | 🔴 Must Read |
| **[../database/script.sql](../database/script.sql)** | Original database schema | ✅ Existing | 🟢 Reference |

#### Migration Scripts
| Script | Purpose | Status |
|--------|---------|--------|
| `V1.1__enhance_tournaments.sql` | Add web fields to GIAI_DAU | ✅ Ready |
| `V1.2__enhance_users.sql` | Add web fields to NGUOI_DUNG | ✅ Ready |
| `V1.3__create_tournament_gallery.sql` | Create TOURNAMENT_GALLERY table | ✅ Ready |
| `ROLLBACK_V1.sql` | Rollback Phase 1 migrations | ✅ Ready |
| `SAMPLE_DATA.sql` | Insert test tournament data | ✅ Ready |

---

### ✅ Implementation Checklists

| Document | Purpose | Timeline | Status |
|----------|---------|----------|--------|
| **[PHASE_1_CHECKLIST.md](PHASE_1_CHECKLIST.md)** | Day-by-day tasks for Phase 1 (DB & Backend) | Week 1-2 | 🚧 Active |
| **PHASE_2_CHECKLIST.md** | Landing Page & App Promotion | Week 3-4 | ⏳ Pending |
| **PHASE_3_CHECKLIST.md** | Player & Club Management | Week 5-6 | ⏳ Pending |
| **PHASE_4_CHECKLIST.md** | Authentication & Authorization | Week 7-8 | ⏳ Pending |
| **PHASE_5_CHECKLIST.md** | Analytics & Statistics | Week 9 | ⏳ Pending |
| **PHASE_6_CHECKLIST.md** | News & Content Management | Week 10 | ⏳ Pending |
| **PHASE_7_CHECKLIST.md** | Admin Panel & Advanced Features | Week 11-13 | ⏳ Pending |

---

### 🔧 Technical Documentation

| Document | Purpose | Status | Priority |
|----------|---------|--------|----------|
| **[BAO_CAO_CONG_NGHE_VA_TINH_NANG_v2.md](BAO_CAO_CONG_NGHE_VA_TINH_NANG_v2.md)** | Tech stack, architecture, features (Vietnamese) | ✅ Complete | 🟡 Reference |
| **[API_DOCUMENTATION.md](API_DOCUMENTATION.md)** | REST API & SSE documentation | ✅ Existing | 🟡 Reference |
| **[CONG_THUC_TONG_QUAT_SO_DO_THI_DAU.md](CONG_THUC_TONG_QUAT_SO_DO_THI_DAU.md)** | Tournament bracket formulas | ✅ Existing | 🟢 Reference |
| **[LUAT_THI_DAU_CAU_LONG_BWF.md](LUAT_THI_DAU_CAU_LONG_BWF.md)** | BWF badminton rules | ✅ Existing | 🟢 Reference |
| **[SETTINGS.md](SETTINGS.md)** | Application settings reference | ✅ Existing | 🟢 Reference |

---

### 📖 User Guides

| Document | Purpose | Status | Priority |
|----------|---------|--------|----------|
| **[HUONG_DAN_SU_DUNG.md](HUONG_DAN_SU_DUNG.md)** | User manual for Desktop App | ✅ Existing | 🟢 Reference |
| **WEB_USER_GUIDE.md** | User guide for Web Platform | ⏳ Todo | 🟡 Future |

---

## 🎯 QUICK NAVIGATION BY ROLE

### 👨‍💻 For Developers (Starting Phase 1)

**Must Read** (in order):
1. [GETTING_STARTED.md](GETTING_STARTED.md) - 10 min read
2. [DATABASE_ENHANCEMENT_PLAN.md](DATABASE_ENHANCEMENT_PLAN.md) - 20 min read
3. [../database/migrations/README.md](../database/migrations/README.md) - 10 min read
4. [PHASE_1_CHECKLIST.md](PHASE_1_CHECKLIST.md) - Reference daily

**Day 1 Tasks**:
- Execute: `V1.1`, `V1.2`, `V1.3` migration scripts
- Verify: Database changes successful
- Test: Desktop App still works

---

### 🏗️ For Architects / Tech Leads

**Overview Documents**:
- [LO_TRINH_PHAT_TRIEN_WEB_PLATFORM.md](LO_TRINH_PHAT_TRIEN_WEB_PLATFORM.md) - Full roadmap
- [DATABASE_ENHANCEMENT_PLAN.md](DATABASE_ENHANCEMENT_PLAN.md) - Database strategy
- [BAO_CAO_CONG_NGHE_VA_TINH_NANG_v2.md](BAO_CAO_CONG_NGHE_VA_TINH_NANG_v2.md) - Architecture

**Decision Points**:
- API design patterns
- Database migration approach
- Frontend-backend integration strategy

---

### 🎨 For Frontend Developers

**Will need** (Later phases):
- [WEB_PLATFORM_STRUCTURE.md](WEB_PLATFORM_STRUCTURE.md) - Templates structure
- Template files in `src/main/resources/templates/`
- CSS files in `src/main/resources/static/css/`
- JavaScript files in `src/main/resources/static/js/`

**Not needed yet**: Wait for Phase 1 backend completion

---

### 📊 For Project Managers

**Tracking Documents**:
- [LO_TRINH_PHAT_TRIEN_WEB_PLATFORM.md](LO_TRINH_PHAT_TRIEN_WEB_PLATFORM.md) - Phases, timeline, deliverables
- [PHASE_1_CHECKLIST.md](PHASE_1_CHECKLIST.md) - Week-by-week progress
- Progress section at bottom of checklist

**Milestones**:
- Week 2: Database & Services complete
- Week 4: Landing page & App hub
- Week 6: Player & Club management
- Week 8: Authentication system
- Week 13: Full platform launch

---

## 📅 CURRENT STATUS

### Active Phase
**Phase 1: Database & Backend Foundation**
- **Timeline**: Nov 17 - Dec 1, 2025 (2 weeks)
- **Status**: 🚧 In Progress - Day 1
- **Progress**: 8% (1/12 days)
- **Next Milestone**: Database migrations complete

### Completed Tasks
- ✅ Project analysis
- ✅ Roadmap creation
- ✅ Database enhancement plan
- ✅ Migration scripts ready
- ✅ Documentation complete

### Current Focus
- 🔄 Execute database migrations
- 🔄 Verify Desktop App compatibility
- ⏳ Update JPA entities (upcoming)

### Upcoming (This Week)
- Day 2: JPA Entities update
- Day 3: DTOs & Mappers
- Day 4: Repository layer
- Day 5: Service layer

---

## 🔍 FIND DOCUMENTATION BY TOPIC

### Database
- Schema design → [DATABASE_ENHANCEMENT_PLAN.md](DATABASE_ENHANCEMENT_PLAN.md)
- Migration execution → [../database/migrations/README.md](../database/migrations/README.md)
- Original schema → [../database/script.sql](../database/script.sql)

### Backend Development
- Entities & models → [DATABASE_ENHANCEMENT_PLAN.md](DATABASE_ENHANCEMENT_PLAN.md) (JPA section)
- Services → [PHASE_1_CHECKLIST.md](PHASE_1_CHECKLIST.md) (Day 5)
- REST APIs → [PHASE_1_CHECKLIST.md](PHASE_1_CHECKLIST.md) (Day 6-7)
- Existing API → [API_DOCUMENTATION.md](API_DOCUMENTATION.md)

### Frontend Development
- Templates structure → [WEB_PLATFORM_STRUCTURE.md](WEB_PLATFORM_STRUCTURE.md)
- Integration → [PHASE_1_CHECKLIST.md](PHASE_1_CHECKLIST.md) (Day 8-9)
- Styling → [PHASE_1_CHECKLIST.md](PHASE_1_CHECKLIST.md) (Day 10)

### Testing
- Unit tests → [PHASE_1_CHECKLIST.md](PHASE_1_CHECKLIST.md) (Weekend 1)
- Integration tests → [PHASE_1_CHECKLIST.md](PHASE_1_CHECKLIST.md) (Day 7)
- E2E tests → [PHASE_1_CHECKLIST.md](PHASE_1_CHECKLIST.md) (Day 11-12)

### Business Logic
- Tournament rules → [LUAT_THI_DAU_CAU_LONG_BWF.md](LUAT_THI_DAU_CAU_LONG_BWF.md)
- Bracket formulas → [CONG_THUC_TONG_QUAT_SO_DO_THI_DAU.md](CONG_THUC_TONG_QUAT_SO_DO_THI_DAU.md)

---

## 📝 DOCUMENTATION STANDARDS

### File Naming
- Use SCREAMING_SNAKE_CASE for documentation: `DATABASE_ENHANCEMENT_PLAN.md`
- Use descriptive names: `PHASE_1_CHECKLIST.md` not `P1.md`
- Include version if applicable: `v2`, `v3`

### Content Structure
- Start with overview and purpose
- Use clear headings (H2 ##, H3 ###)
- Include table of contents for long docs
- Add checklists for actionable items
- Include examples and code snippets
- End with next steps or summary

### Maintenance
- Update status when completed: ✅, 🚧, ⏳
- Add date of last update
- Keep index up to date
- Archive old versions

---

## 🔄 DOCUMENT LIFECYCLE

### Planning Phase (✅ Complete)
- [x] LO_TRINH_PHAT_TRIEN_WEB_PLATFORM.md
- [x] DATABASE_ENHANCEMENT_PLAN.md
- [x] GETTING_STARTED.md

### Active Development (🚧 Current)
- [x] PHASE_1_CHECKLIST.md
- [ ] Code comments and JavaDoc
- [ ] API documentation updates

### Future Phases (⏳ Upcoming)
- [ ] PHASE_2_CHECKLIST.md (Week 3-4)
- [ ] PHASE_3_CHECKLIST.md (Week 5-6)
- [ ] WEB_USER_GUIDE.md (Week 13)
- [ ] DEPLOYMENT_GUIDE.md (Week 13)

---

## 🆘 HELP & SUPPORT

### Common Issues
1. **Can't find what I need?**
   - Check this index
   - Search in relevant section above
   - Check GETTING_STARTED.md

2. **Migration failed?**
   - See [../database/migrations/README.md](../database/migrations/README.md) → Troubleshooting

3. **Desktop App broke?**
   - See [GETTING_STARTED.md](GETTING_STARTED.md) → Troubleshooting
   - Check rollback scripts

4. **Unclear about next steps?**
   - See [PHASE_1_CHECKLIST.md](PHASE_1_CHECKLIST.md)
   - Current day's tasks

### Get Help
- Check relevant documentation first
- Review checklists for context
- Gather error messages and logs
- Ask specific questions

---

## 📊 PROGRESS OVERVIEW

```
Overall Project Progress: [==        ] 20%

Phase 1 (Database & Backend)  [==        ] 8%   ← YOU ARE HERE
Phase 2 (Landing Page)        [          ] 0%
Phase 3 (Players & Clubs)     [          ] 0%
Phase 4 (Authentication)      [          ] 0%
Phase 5 (Analytics)           [          ] 0%
Phase 6 (News & Content)      [          ] 0%
Phase 7 (Admin Panel)         [          ] 0%
```

**Current Sprint**: Week 1 of Phase 1  
**Next Milestone**: Dec 1, 2025 - Phase 1 Complete

---

## 🎯 QUICK ACTIONS

### I want to...

**...start Phase 1 development**
→ Read [GETTING_STARTED.md](GETTING_STARTED.md) and follow 5-step Quick Start

**...understand the full roadmap**
→ Read [LO_TRINH_PHAT_TRIEN_WEB_PLATFORM.md](LO_TRINH_PHAT_TRIEN_WEB_PLATFORM.md)

**...execute database migrations**
→ Follow [../database/migrations/README.md](../database/migrations/README.md)

**...see today's tasks**
→ Check [PHASE_1_CHECKLIST.md](PHASE_1_CHECKLIST.md) → Day 1 section

**...understand database changes**
→ Read [DATABASE_ENHANCEMENT_PLAN.md](DATABASE_ENHANCEMENT_PLAN.md)

**...check API documentation**
→ See [API_DOCUMENTATION.md](API_DOCUMENTATION.md)

---

## 📅 LAST UPDATED

- **Date**: November 17, 2025
- **Phase**: Phase 1 - Day 1
- **Status**: Database migrations ready
- **Next Review**: November 24, 2025 (after Week 1)

---

**Happy Coding! 🚀🏸**
