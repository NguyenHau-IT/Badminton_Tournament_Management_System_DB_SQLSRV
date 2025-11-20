/**
 * Tournament List Page JavaScript
 * Handles animations and card interactions
 */

// ========== GLOBAL FUNCTIONS (Must be defined before DOMContentLoaded) ==========

/**
 * Open Quick View Modal
 * Load tournament details and display in modal
 * @param {number} tournamentId - The tournament ID to display
 */
window.openQuickView = async function(tournamentId) {
    console.log('🔍 Opening quick view for tournament:', tournamentId);
    
    // Get modal elements
    const modalEl = document.getElementById('quickViewModal');
    if (!modalEl) {
        console.error('❌ Quick View Modal element not found!');
        return;
    }
    
    const modal = new bootstrap.Modal(modalEl);
    const loadingEl = document.getElementById('quickViewLoading');
    const contentEl = document.getElementById('quickViewContent');
    const errorEl = document.getElementById('quickViewError');
    
    // Show modal and loading state
    modal.show();
    if (loadingEl) loadingEl.style.display = 'block';
    if (contentEl) contentEl.style.display = 'none';
    if (errorEl) errorEl.style.display = 'none';
    
    try {
        // Fetch tournament details from API
        const response = await fetch(`/api/tournaments/${tournamentId}`);
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const tournament = await response.json();
        console.log('✅ Tournament data loaded:', tournament);
        
        // Populate modal with data
        populateQuickView(tournament);
        
        // Show content
        if (loadingEl) loadingEl.style.display = 'none';
        if (contentEl) contentEl.style.display = 'block';
        
    } catch (error) {
        console.error('❌ Error loading tournament:', error);
        
        // Show error state
        if (loadingEl) loadingEl.style.display = 'none';
        if (errorEl) errorEl.style.display = 'block';
    }
};

/**
 * Populate Quick View Modal with tournament data
 * @param {Object} tournament - Tournament data object
 */
function populateQuickView(tournament) {
    // Status badge mapping
    const statusMap = {
        'ongoing': { text: 'Đang diễn ra', class: 'bg-danger' },
        'registration': { text: 'Đang đăng ký', class: 'bg-success' },
        'upcoming': { text: 'Sắp diễn ra', class: 'bg-primary' },
        'completed': { text: 'Đã kết thúc', class: 'bg-secondary' }
    };
    
    const status = statusMap[tournament.trangThai] || { text: tournament.trangThai, class: 'bg-info' };
    
    // Format dates
    const startDate = tournament.ngayBatDau 
        ? new Date(tournament.ngayBatDau).toLocaleDateString('vi-VN', { 
            year: 'numeric', month: 'long', day: 'numeric' 
          })
        : 'Chưa xác định';
        
    const endDate = tournament.ngayKetThuc 
        ? new Date(tournament.ngayKetThuc).toLocaleDateString('vi-VN', { 
            year: 'numeric', month: 'long', day: 'numeric' 
          })
        : 'Chưa xác định';
    
    const deadline = tournament.hanDangKy 
        ? new Date(tournament.hanDangKy).toLocaleDateString('vi-VN', { 
            year: 'numeric', month: 'long', day: 'numeric' 
          })
        : 'Chưa có thông tin';
    
    // Format price
    const price = tournament.phiThamGia 
        ? new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(tournament.phiThamGia)
        : 'Miễn phí';
    
    // Update header
    const qvImage = document.getElementById('qvTournamentImage');
    const qvStatus = document.getElementById('qvStatus');
    const qvName = document.getElementById('qvTournamentName');
    const qvDate = document.getElementById('qvDate');
    const qvLocation = document.getElementById('qvLocation');
    const qvParticipants = document.getElementById('qvParticipants');
    
    if (qvImage) qvImage.src = tournament.hinhAnh || '/icons/tournaments/default.svg';
    if (qvStatus) {
        qvStatus.textContent = status.text;
        qvStatus.className = `badge ${status.class} mb-2`;
    }
    if (qvName) qvName.textContent = tournament.tenGiai || 'Tournament';
    if (qvDate) qvDate.textContent = `${startDate} - ${endDate}`;
    if (qvLocation) qvLocation.textContent = `${tournament.diaDiem || 'Unknown'}, ${tournament.tinhThanh || ''}`;
    if (qvParticipants) qvParticipants.textContent = `${tournament.soLuongDaDangKy || 0}/${tournament.soLuongToiDa || 'N/A'} người`;
    
    // Update description
    const qvDescription = document.getElementById('qvDescription');
    if (qvDescription) qvDescription.innerHTML = tournament.moTa || '<em class="text-muted">Chưa có mô tả</em>';
    
    // Update format
    const qvFormat = document.getElementById('qvFormat');
    if (qvFormat) qvFormat.innerHTML = tournament.theThuc || '<em class="text-muted">Chưa có thông tin</em>';
    
    // Update prize
    const qvPrize = document.getElementById('qvPrize');
    if (qvPrize) qvPrize.innerHTML = tournament.giaiThuong || '<em class="text-muted">Chưa công bố</em>';
    
    // Update quick info
    const qvLevel = document.getElementById('qvLevel');
    const qvType = document.getElementById('qvType');
    const qvFee = document.getElementById('qvFee');
    const qvDeadline = document.getElementById('qvDeadline');
    const qvRegistered = document.getElementById('qvRegistered');
    
    if (qvLevel) qvLevel.textContent = tournament.capDo || 'N/A';
    if (qvType) qvType.textContent = tournament.theLoai || 'N/A';
    if (qvFee) qvFee.textContent = price;
    if (qvDeadline) qvDeadline.textContent = deadline;
    if (qvRegistered) qvRegistered.textContent = `${tournament.soLuongDaDangKy || 0}/${tournament.soLuongToiDa || 'N/A'}`;
    
    // Update stats
    const qvViews = document.getElementById('qvViews');
    const qvRating = document.getElementById('qvRating');
    const qvReviews = document.getElementById('qvReviews');
    
    if (qvViews) qvViews.textContent = (tournament.luotXem || 0).toLocaleString('vi-VN');
    if (qvRating) qvRating.textContent = tournament.danhGiaTb ? tournament.danhGiaTb.toFixed(1) : '0.0';
    if (qvReviews) qvReviews.textContent = tournament.tongDanhGia || 0;
    
    // Update action buttons
    const qvViewDetailsBtn = document.getElementById('qvViewDetailsBtn');
    if (qvViewDetailsBtn) qvViewDetailsBtn.href = `/tournaments/${tournament.id}`;
    
    const registerBtn = document.getElementById('qvRegisterBtn');
    if (registerBtn) {
        if (tournament.trangThai === 'registration') {
            registerBtn.disabled = false;
            registerBtn.onclick = () => {
                window.location.href = `/tournaments/${tournament.id}/register`;
            };
        } else {
            registerBtn.disabled = true;
            registerBtn.onclick = null;
        }
    }
}

// ========== MAIN INITIALIZATION ==========

document.addEventListener('DOMContentLoaded', function() {
    // Initialize AOS animations
    AOS.init({
        duration: 800,
        once: true
    });

    // Add hover effect for cards
    const cards = document.querySelectorAll('.tournament-card');
    cards.forEach(card => {
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-5px)';
        });
        
        card.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0)';
        });
    });

    // Filter button interactions
    const filterButtons = document.querySelectorAll('.filter-btn');
    filterButtons.forEach(btn => {
        btn.addEventListener('click', function() {
            // Add loading state
            const originalText = this.innerHTML;
            this.innerHTML = '<i class="bi bi-hourglass-split"></i> Đang tải...';
            this.disabled = true;
            
            // Simulate loading (in production: actual filter request)
            setTimeout(() => {
                this.innerHTML = originalText;
                this.disabled = false;
            }, 500);
        });
    });

    // Lazy load images
    if ('IntersectionObserver' in window) {
        const imageObserver = new IntersectionObserver((entries, observer) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    const img = entry.target;
                    img.src = img.dataset.src || img.src;
                    img.classList.add('loaded');
                    observer.unobserve(img);
                }
            });
        });

        document.querySelectorAll('.tournament-image img').forEach(img => {
            imageObserver.observe(img);
        });
    }

    // Track card clicks for analytics
    cards.forEach(card => {
        card.addEventListener('click', function(e) {
            if (!e.target.closest('.btn')) {
                const tournamentId = this.querySelector('a[href*="/tournaments/"]')?.href.split('/').pop();
                console.log('Tournament card clicked:', tournamentId);
                // In production: send to analytics
            }
        });
    });

    // ========== ADVANCED FILTERS FUNCTIONALITY ==========
    initAdvancedFilters();

    // ========== SORT DROPDOWN FUNCTIONALITY ==========
    initSortDropdown();

    // ========== VIEW TOGGLE FUNCTIONALITY ==========
    initViewToggle();

    // ========== INFINITE SCROLL FUNCTIONALITY ==========
    initInfiniteScroll();
});

/**
 * Initialize View Toggle
 * Xử lý chuyển đổi giữa grid view và list view với localStorage
 */
function initViewToggle() {
    console.log('🎯 Initializing view toggle...');
    
    const toggleButtons = document.querySelectorAll('.btn-view-toggle');
    const container = document.getElementById('tournamentsContainer');
    
    console.log('Toggle buttons found:', toggleButtons.length);
    console.log('Container found:', container ? 'Yes' : 'No');
    
    if (!toggleButtons.length || !container) {
        console.error('❌ View toggle elements not found!');
        console.log('- Buttons:', toggleButtons.length);
        console.log('- Container:', container);
        return;
    }

    // Restore view preference from localStorage
    const savedView = localStorage.getItem('tournament_view_mode') || 'grid';
    console.log('Saved view mode:', savedView);
    setViewMode(savedView);

    // Add click handlers
    toggleButtons.forEach((btn, index) => {
        console.log(`Adding click handler to button ${index}:`, btn.dataset.view);
        
        btn.addEventListener('click', function(e) {
            // Don't prevent default for buttons
            const viewMode = this.dataset.view;
            console.log('🖱️ View toggle clicked:', viewMode);
            
            setViewMode(viewMode);
            localStorage.setItem('tournament_view_mode', viewMode);
        });
    });

    function setViewMode(mode) {
        console.log('📐 Setting view mode to:', mode);
        
        // Update buttons
        toggleButtons.forEach(btn => {
            if (btn.dataset.view === mode) {
                btn.classList.add('active');
                console.log('✅ Activated button:', mode);
            } else {
                btn.classList.remove('active');
            }
        });

        // Update container classes
        if (mode === 'list') {
            container.classList.remove('grid-view');
            container.classList.add('list-view');
            console.log('📋 Switched to list view');
        } else {
            container.classList.remove('list-view');
            container.classList.add('grid-view');
            console.log('📱 Switched to grid view');
        }
        
        console.log('Container classes:', container.className);
    }

    console.log('✅ View toggle initialized successfully');
}

/**
 * Initialize Advanced Filters Panel
 * Xử lý collapsible, multi-select, date range, price slider, apply/reset
 */
function initAdvancedFilters() {
    const filterHeader = document.getElementById('filterHeader');
    const filterBody = document.getElementById('filterBody');
    const toggleBtn = document.getElementById('toggleFilterBtn');
    const toggleIcon = document.getElementById('toggleFilterIcon');
    const toggleText = document.getElementById('toggleFilterText');
    const filterForm = document.getElementById('advancedFilterForm');
    const applyBtn = document.getElementById('applyFiltersBtn');
    const resetBtn = document.getElementById('resetFiltersBtn');
    const filterCountBadge = document.getElementById('filterCountBadge');

    if (!filterForm) {
        console.log('Advanced filter form not found');
        return;
    }

    // Toggle Filter Panel Icon & Text (Bootstrap handles collapse)
    if (filterBody && toggleBtn && toggleIcon && toggleText) {
        filterBody.addEventListener('shown.bs.collapse', function() {
            toggleIcon.classList.remove('bi-chevron-down');
            toggleIcon.classList.add('bi-chevron-up');
            toggleText.textContent = 'Thu gọn';
            console.log('Filter panel expanded');
        });

        filterBody.addEventListener('hidden.bs.collapse', function() {
            toggleIcon.classList.remove('bi-chevron-up');
            toggleIcon.classList.add('bi-chevron-down');
            toggleText.textContent = 'Mở rộng';
            console.log('Filter panel collapsed');
        });

        // Set initial state
        if (filterBody.classList.contains('show')) {
            toggleIcon.classList.add('bi-chevron-up');
            toggleText.textContent = 'Thu gọn';
        }
    }

    // Price Range Sliders - Real-time update
    const priceMinFilter = document.getElementById('priceMinFilter');
    const priceMaxFilter = document.getElementById('priceMaxFilter');
    const priceMinLabel = document.getElementById('priceMinLabel');
    const priceMaxLabel = document.getElementById('priceMaxLabel');
    const priceRangeDisplay = document.getElementById('priceRangeDisplay');

    function updatePriceDisplay() {
        let minValue = parseInt(priceMinFilter.value);
        let maxValue = parseInt(priceMaxFilter.value);

        // Đảm bảo min <= max
        if (minValue > maxValue) {
            [minValue, maxValue] = [maxValue, minValue];
            priceMinFilter.value = minValue;
            priceMaxFilter.value = maxValue;
        }

        // Format tiền VNĐ
        const minFormatted = minValue.toLocaleString('vi-VN') + 'đ';
        const maxFormatted = maxValue.toLocaleString('vi-VN') + 'đ';

        priceMinLabel.textContent = minFormatted;
        priceMaxLabel.textContent = maxFormatted;
        priceRangeDisplay.textContent = `${minFormatted} - ${maxFormatted}`;
    }

    if (priceMinFilter && priceMaxFilter) {
        priceMinFilter.addEventListener('input', updatePriceDisplay);
        priceMaxFilter.addEventListener('input', updatePriceDisplay);
        updatePriceDisplay(); // Initial display
    }

    // Count Active Filters - Đếm số filters đang được chọn
    function updateFilterCount() {
        let count = 0;

        // Count Status filters
        const statusFilter = document.getElementById('statusFilter');
        count += statusFilter.selectedOptions.length;

        // Count Province filter
        const provinceFilter = document.getElementById('provinceFilter');
        if (provinceFilter.value) count++;

        // Count Level filters
        const levelFilter = document.getElementById('levelFilter');
        count += levelFilter.selectedOptions.length;

        // Count Type filters
        const typeFilter = document.getElementById('typeFilter');
        count += typeFilter.selectedOptions.length;

        // Count Date filters
        const dateFrom = document.getElementById('dateFromFilter');
        const dateTo = document.getElementById('dateToFilter');
        if (dateFrom.value) count++;
        if (dateTo.value) count++;

        // Count Price filters (nếu khác default)
        const priceMin = parseInt(priceMinFilter.value);
        const priceMax = parseInt(priceMaxFilter.value);
        if (priceMin > 0 || priceMax < 2000000) count++;

        // Update badge
        if (count > 0) {
            filterCountBadge.textContent = count;
            filterCountBadge.style.display = 'inline-flex';
        } else {
            filterCountBadge.style.display = 'none';
        }

        return count;
    }

    // Listen to all filter changes
    const allFilters = filterForm.querySelectorAll('select, input[type="date"], input[type="range"]');
    allFilters.forEach(filter => {
        filter.addEventListener('change', updateFilterCount);
    });

    // Apply Filters - Submit form
    filterForm.addEventListener('submit', function(e) {
        e.preventDefault();

        // Collect filter values
        const filters = {
            status: Array.from(document.getElementById('statusFilter').selectedOptions).map(opt => opt.value),
            province: document.getElementById('provinceFilter').value,
            level: Array.from(document.getElementById('levelFilter').selectedOptions).map(opt => opt.value),
            type: Array.from(document.getElementById('typeFilter').selectedOptions).map(opt => opt.value),
            dateFrom: document.getElementById('dateFromFilter').value,
            dateTo: document.getElementById('dateToFilter').value,
            priceMin: priceMinFilter.value,
            priceMax: priceMaxFilter.value
        };

        console.log('Applying filters:', filters);

        // Build URL with query params
        const params = new URLSearchParams();
        
        if (filters.status.length > 0) params.append('status', filters.status.join(','));
        if (filters.province) params.append('province', filters.province);
        if (filters.level.length > 0) params.append('level', filters.level.join(','));
        if (filters.type.length > 0) params.append('type', filters.type.join(','));
        if (filters.dateFrom) params.append('dateFrom', filters.dateFrom);
        if (filters.dateTo) params.append('dateTo', filters.dateTo);
        if (filters.priceMin != 0) params.append('priceMin', filters.priceMin);
        if (filters.priceMax != 2000000) params.append('priceMax', filters.priceMax);

        // Add loading state
        applyBtn.classList.add('loading');
        applyBtn.disabled = true;

        // Navigate với query params
        window.location.href = `/tournaments/list?${params.toString()}`;
    });

    // Reset Filters - Clear all selections
    resetBtn.addEventListener('click', function() {
        // Reset all selects
        document.getElementById('statusFilter').selectedIndex = -1;
        document.getElementById('provinceFilter').selectedIndex = 0;
        document.getElementById('levelFilter').selectedIndex = -1;
        document.getElementById('typeFilter').selectedIndex = -1;

        // Reset dates
        document.getElementById('dateFromFilter').value = '';
        document.getElementById('dateToFilter').value = '';

        // Reset price sliders
        priceMinFilter.value = 0;
        priceMaxFilter.value = 2000000;
        updatePriceDisplay();

        // Update count
        updateFilterCount();

        // Navigate to base URL
        setTimeout(() => {
            window.location.href = '/tournaments/list';
        }, 300);
    });

    // Initial count
    updateFilterCount();

    // Restore filters from URL (nếu có query params)
    restoreFiltersFromURL();
}

/**
 * Restore filter values từ URL query parameters
 */
function restoreFiltersFromURL() {
    const params = new URLSearchParams(window.location.search);

    // Restore Status
    const statusParam = params.get('status');
    if (statusParam) {
        const statusFilter = document.getElementById('statusFilter');
        const statusValues = statusParam.split(',');
        Array.from(statusFilter.options).forEach(option => {
            if (statusValues.includes(option.value)) {
                option.selected = true;
            }
        });
    }

    // Restore Province
    const provinceParam = params.get('province');
    if (provinceParam) {
        document.getElementById('provinceFilter').value = provinceParam;
    }

    // Restore Level
    const levelParam = params.get('level');
    if (levelParam) {
        const levelFilter = document.getElementById('levelFilter');
        const levelValues = levelParam.split(',');
        Array.from(levelFilter.options).forEach(option => {
            if (levelValues.includes(option.value)) {
                option.selected = true;
            }
        });
    }

    // Restore Type
    const typeParam = params.get('type');
    if (typeParam) {
        const typeFilter = document.getElementById('typeFilter');
        const typeValues = typeParam.split(',');
        Array.from(typeFilter.options).forEach(option => {
            if (typeValues.includes(option.value)) {
                option.selected = true;
            }
        });
    }

    // Restore Dates
    const dateFrom = params.get('dateFrom');
    const dateTo = params.get('dateTo');
    if (dateFrom) document.getElementById('dateFromFilter').value = dateFrom;
    if (dateTo) document.getElementById('dateToFilter').value = dateTo;

    // Restore Price Range
    const priceMin = params.get('priceMin');
    const priceMax = params.get('priceMax');
    if (priceMin) document.getElementById('priceMinFilter').value = priceMin;
    if (priceMax) document.getElementById('priceMaxFilter').value = priceMax;

    // Trigger price display update
    const priceMinFilter = document.getElementById('priceMinFilter');
    const priceMaxFilter = document.getElementById('priceMaxFilter');
    if (priceMinFilter && priceMaxFilter) {
        priceMinFilter.dispatchEvent(new Event('input'));
    }
}

/**
 * Initialize Sort Dropdown
 * Xử lý sorting với icons, active state, và smooth transitions
 */
function initSortDropdown() {
    const sortOptions = document.querySelectorAll('.sort-option');
    const sortSelectedText = document.getElementById('sortSelectedText');

    if (!sortOptions.length) return;

    // Map sort values to display text
    const sortTextMap = {
        'newest': 'Mới nhất',
        'most-viewed': 'Xem nhiều nhất',
        'highest-rated': 'Đánh giá cao nhất',
        'price-low': 'Phí thấp đến cao',
        'price-high': 'Phí cao đến thấp'
    };

    sortOptions.forEach(option => {
        option.addEventListener('click', function(e) {
            e.preventDefault();

            const sortValue = this.getAttribute('data-sort');
            const sortText = sortTextMap[sortValue];
            
            console.log(`🔄 Sort option clicked: ${sortValue}`);

            // Remove active from all options
            sortOptions.forEach(opt => opt.classList.remove('active'));

            // Add active to clicked option
            this.classList.add('active');

            // Update button text
            if (sortSelectedText) {
                sortSelectedText.textContent = sortText;
                console.log(`✅ Updated button text to: ${sortText}`);
            }

            // Get current URL params
            const params = new URLSearchParams(window.location.search);
            const currentSort = params.get('sort');
            
            // Only navigate if sort value actually changed
            if (currentSort !== sortValue) {
                params.set('sort', sortValue);
                console.log(`🔄 Navigating with sort: ${sortValue}`);
                window.location.href = `/tournaments/list?${params.toString()}`;
            } else {
                console.log(`ℹ️ Already sorted by: ${sortValue}`);
            }
        });
    });

    // Restore sort from URL
    const params = new URLSearchParams(window.location.search);
    const currentSort = params.get('sort') || 'newest';

    sortOptions.forEach(option => {
        if (option.getAttribute('data-sort') === currentSort) {
            option.classList.add('active');
            sortSelectedText.textContent = sortTextMap[currentSort];
        }
    });
}

/**
 * Initialize Infinite Scroll
 * Load more tournaments as user scrolls down
 */
function initInfiniteScroll() {
    console.log('🔄 Initializing infinite scroll...');
    
    const container = document.getElementById('tournamentsContainer');
    if (!container) {
        console.error('❌ Tournament container not found!');
        return;
    }

    // State management
    let currentPage = 1; // Page 0 already loaded (initial render)
    let isLoading = false;
    let hasMore = true;

    // Create loading indicator
    const loadingIndicator = createLoadingIndicator();
    
    // Get current filters from URL
    function getCurrentFilters() {
        const params = new URLSearchParams(window.location.search);
        return {
            status: params.get('status') || '',
            category: params.get('category') || '',
            province: params.get('province') || '',
            level: params.get('level') || '',
            type: params.get('type') || '',
            dateFrom: params.get('dateFrom') || '',
            dateTo: params.get('dateTo') || '',
            priceMin: params.get('priceMin') || '',
            priceMax: params.get('priceMax') || '',
            sort: params.get('sort') || 'newest'
        };
    }

    // Load more tournaments from API
    async function loadMoreTournaments() {
        if (isLoading || !hasMore) {
            console.log('⏸️ Skip loading - isLoading:', isLoading, 'hasMore:', hasMore);
            return;
        }

        isLoading = true;
        showLoading();
        console.log(`📥 Loading page ${currentPage}...`);

        try {
            const filters = getCurrentFilters();
            const queryParams = new URLSearchParams({
                page: currentPage,
                size: 12,
                ...filters
            });

            const response = await fetch(`/api/tournaments/list?${queryParams}`);
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            console.log('✅ Loaded data:', data);

            if (data.tournaments && data.tournaments.length > 0) {
                // Render new tournaments
                renderTournaments(data.tournaments);
                currentPage++;
                hasMore = data.hasMore;
                
                console.log(`✅ Loaded ${data.tournaments.length} tournaments. HasMore: ${hasMore}`);
            } else {
                hasMore = false;
                console.log('📭 No more tournaments to load');
            }

        } catch (error) {
            console.error('❌ Error loading tournaments:', error);
            showError();
        } finally {
            isLoading = false;
            hideLoading();
        }
    }

    // Render tournaments to container
    function renderTournaments(tournaments) {
        const currentView = localStorage.getItem('tournament-view') || 'grid';
        
        tournaments.forEach(tournament => {
            const card = createTournamentCard(tournament, currentView);
            container.appendChild(card);
        });

        // Re-initialize AOS for new elements
        if (typeof AOS !== 'undefined') {
            AOS.refresh();
        }
    }

    // Create tournament card element
    function createTournamentCard(tournament, viewMode) {
        const card = document.createElement('div');
        card.className = viewMode === 'grid' 
            ? 'col-md-6 col-lg-4 mb-4' 
            : 'col-12 mb-3';
        card.setAttribute('data-aos', 'fade-up');

        // Status badge mapping
        const statusMap = {
            'ongoing': { text: 'Đang diễn ra', class: 'bg-danger' },
            'registration': { text: 'Đang đăng ký', class: 'bg-success' },
            'upcoming': { text: 'Sắp diễn ra', class: 'bg-primary' },
            'completed': { text: 'Đã kết thúc', class: 'bg-secondary' }
        };
        
        const status = statusMap[tournament.trangThai] || { text: tournament.trangThai, class: 'bg-info' };

        // Format date
        const startDate = tournament.ngayBatDau ? new Date(tournament.ngayBatDau).toLocaleDateString('vi-VN') : 'TBA';
        
        // Format price
        const price = tournament.phiThamGia 
            ? new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(tournament.phiThamGia)
            : 'Miễn phí';

        const imageUrl = tournament.hinhAnh && tournament.hinhAnh.trim() !== '' 
            ? tournament.hinhAnh 
            : '/icons/tournaments/default.svg';
        
        card.innerHTML = `
            <div class="tournament-card ${viewMode === 'list' ? 'list-view' : ''}">
                <div class="tournament-image" style="${!tournament.hinhAnh || tournament.hinhAnh.trim() === '' ? 'background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);' : ''}">
                    ${tournament.hinhAnh && tournament.hinhAnh.trim() !== '' ? `<img src="${imageUrl}" 
                         alt="${tournament.tenGiai || 'Tournament'}"
                         loading="lazy"
                         onerror="this.style.display='none'; this.parentElement.style.background='linear-gradient(135deg, #667eea 0%, #764ba2 100%)';">` : ''}
                    <button class="btn btn-light btn-quick-view rounded-pill shadow" 
                            onclick="openQuickView(${tournament.id})">
                        <i class="bi bi-eye"></i> Xem nhanh
                    </button>
                    <span class="status-badge ${status.class}">${status.text}</span>
                </div>
                <div class="tournament-content">
                    <h5 class="tournament-title">${tournament.tenGiai || 'Untitled Tournament'}</h5>
                    <div class="tournament-info">
                        <div class="info-item">
                            <i class="bi bi-calendar-event"></i>
                            <span>${startDate}</span>
                        </div>
                        <div class="info-item">
                            <i class="bi bi-geo-alt"></i>
                            <span>${tournament.tinhThanh || 'Unknown'}</span>
                        </div>
                        <div class="info-item">
                            <i class="bi bi-cash"></i>
                            <span>${price}</span>
                        </div>
                    </div>
                    <div class="tournament-stats">
                        <span title="Lượt xem">
                            <i class="bi bi-eye"></i> ${tournament.luotXem || 0}
                        </span>
                        <span title="Đánh giá">
                            <i class="bi bi-star-fill text-warning"></i> 
                            ${tournament.danhGiaTb ? tournament.danhGiaTb.toFixed(1) : '0.0'}
                        </span>
                    </div>
                    <div class="tournament-footer">
                        <span class="tournament-category">${tournament.capDo || 'Category'}</span>
                        <a href="/tournaments/${tournament.id}" class="btn btn-primary btn-sm">
                            Chi tiết <i class="bi bi-arrow-right"></i>
                        </a>
                    </div>
                </div>
            </div>
        `;

        return card;
    }

    // Infinite scroll observer
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting && hasMore && !isLoading) {
                console.log('👁️ Scroll trigger detected');
                loadMoreTournaments();
            }
        });
    }, {
        root: null,
        rootMargin: '200px', // Trigger 200px before reaching bottom
        threshold: 0.1
    });

    // Observe the loading indicator
    observer.observe(loadingIndicator);

    // Helper functions
    function createLoadingIndicator() {
        const indicator = document.createElement('div');
        indicator.id = 'infiniteScrollIndicator';
        indicator.className = 'text-center py-4';
        indicator.innerHTML = `
            <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">Loading...</span>
            </div>
            <p class="mt-2 text-muted">Đang tải thêm giải đấu...</p>
        `;
        indicator.style.display = 'none';
        
        // Insert after container
        container.parentNode.insertBefore(indicator, container.nextSibling);
        return indicator;
    }

    function showLoading() {
        loadingIndicator.style.display = 'block';
    }

    function hideLoading() {
        loadingIndicator.style.display = 'none';
    }

    function showError() {
        const errorDiv = document.createElement('div');
        errorDiv.className = 'alert alert-warning text-center';
        errorDiv.innerHTML = `
            <i class="bi bi-exclamation-triangle"></i>
            Không thể tải thêm giải đấu. Vui lòng thử lại sau.
        `;
        loadingIndicator.replaceWith(errorDiv);
        
        setTimeout(() => {
            errorDiv.remove();
            container.parentNode.insertBefore(loadingIndicator, container.nextSibling);
        }, 3000);
    }

    console.log('✅ Infinite scroll initialized');
}

/**
 * Initialize Quick View Modal behaviors
 */
function initQuickViewModal() {
    const modalEl = document.getElementById('quickViewModal');
    
    if (!modalEl) {
        console.warn('⚠️ Quick View Modal not found');
        return;
    }
    
    // Add keyboard navigation
    modalEl.addEventListener('shown.bs.modal', function() {
        console.log('📖 Quick View Modal opened');
        
        // Focus on close button for accessibility
        const closeBtn = modalEl.querySelector('.btn-close');
        if (closeBtn) {
            closeBtn.focus();
        }
    });
    
    // Clean up on close
    modalEl.addEventListener('hidden.bs.modal', function() {
        console.log('📕 Quick View Modal closed');
        
        // Reset modal content
        const contentEl = document.getElementById('quickViewContent');
        const loadingEl = document.getElementById('quickViewLoading');
        const errorEl = document.getElementById('quickViewError');
        
        if (contentEl) contentEl.style.display = 'none';
        if (loadingEl) loadingEl.style.display = 'block';
        if (errorEl) errorEl.style.display = 'none';
    });
    
    console.log('✅ Quick View Modal initialized');
}

// Initialize Quick View Modal on DOM ready
document.addEventListener('DOMContentLoaded', function() {
    initQuickViewModal();
});