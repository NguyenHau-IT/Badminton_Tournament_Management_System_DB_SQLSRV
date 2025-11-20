/**
 * Tournament Home Page JavaScript
 * Handles search, filters, and interactive features
 */

document.addEventListener('DOMContentLoaded', function() {
    
    // Initialize components (theo thứ tự ưu tiên)
    initLoadingStates();           // 1. Show skeleton nếu chưa có data
    initAOSEnhancements();         // 2. Setup animations
    initLiveTournamentsCarousel(); // 3. Initialize carousel
    initQuickSearch();             // 4. Search functionality
    initFilterChips();             // 5. Filter interactions
    initStatsCountUp();            // 6. Animated counters
    initLiveIndicators();          // 7. Live badges
    initTooltips();                // 8. Tooltips
    
    /**
     * Initialize Live Tournaments Carousel
     * Configure Swiper.js for live tournaments section
     */
    function initLiveTournamentsCarousel() {
        // Check if Swiper is available
        if (typeof Swiper === 'undefined') {
            console.warn('Swiper library not loaded');
            return;
        }
        
        // Check if carousel container exists
        const carouselContainer = document.querySelector('.live-tournaments-swiper');
        if (!carouselContainer) {
            console.log('Live tournaments carousel not found on this page');
            return;
        }
        
        // Initialize Swiper
        const swiper = new Swiper('.live-tournaments-swiper', {
            // Display settings
            slidesPerView: 1,
            spaceBetween: 25,
            
            // Loop settings
            loop: true,
            loopAdditionalSlides: 2,
            
            // Autoplay settings
            autoplay: {
                delay: 5000,
                disableOnInteraction: false,
                pauseOnMouseEnter: true
            },
            
            // Speed and effects
            speed: 600,
            effect: 'slide',
            
            // Navigation
            navigation: {
                nextEl: '.swiper-button-next',
                prevEl: '.swiper-button-prev',
            },
            
            // Pagination
            pagination: {
                el: '.swiper-pagination',
                clickable: true,
                dynamicBullets: false
            },
            
            // Responsive breakpoints
            breakpoints: {
                // Mobile (< 576px): 1 slide
                0: {
                    slidesPerView: 1,
                    spaceBetween: 20
                },
                // Tablet (>= 768px): 2 slides
                768: {
                    slidesPerView: 2,
                    spaceBetween: 25
                },
                // Desktop (>= 1024px): 3 slides
                1024: {
                    slidesPerView: 3,
                    spaceBetween: 25
                }
            },
            
            // Accessibility
            a11y: {
                prevSlideMessage: 'Giải đấu trước',
                nextSlideMessage: 'Giải đấu tiếp theo',
                paginationBulletMessage: 'Đi đến giải đấu {{index}}'
            },
            
            // Events
            on: {
                init: function() {
                    console.log('Live tournaments carousel initialized with ' + this.slides.length + ' slides');
                },
                slideChange: function() {
                    // Optional: Track slide changes for analytics
                }
            }
        });
        
        // Store swiper instance for potential later use
        window.liveTournamentsSwiper = swiper;
    }
    
    /**
     * AOS Animation Enhancements
     * Add custom delays and effects for staggered animations
     */
    function initAOSEnhancements() {
        // Add staggered delays to stat cards
        const statCards = document.querySelectorAll('.stat-card');
        statCards.forEach((card, index) => {
            card.setAttribute('data-aos-delay', index * 100); // 0, 100, 200, 300ms
        });
        
        // Add staggered delays to featured tournament cards
        const featuredCards = document.querySelectorAll('.featured-card');
        featuredCards.forEach((card, index) => {
            card.setAttribute('data-aos-delay', index * 150); // 0, 150, 300, 450ms
        });
        
        // Add staggered delays to navigation cards
        const navCards = document.querySelectorAll('.nav-card');
        navCards.forEach((card, index) => {
            card.setAttribute('data-aos-delay', index * 100);
        });
        
        // Add staggered delays to category cards
        const categoryCards = document.querySelectorAll('.category-card');
        categoryCards.forEach((card, index) => {
            card.setAttribute('data-aos-delay', index * 80); // Faster for more cards
        });
        
        // Refresh AOS to apply new attributes
        if (typeof AOS !== 'undefined') {
            AOS.refresh();
            console.log('AOS animations enhanced with staggered delays');
        }
    }
    
    /**
     * Quick Search Functionality with Autocomplete
     * Tính năng tìm kiếm nhanh với gợi ý tự động
     */
    function initQuickSearch() {
        const searchInput = document.getElementById('quickSearch');
        const searchClear = document.getElementById('searchClear');
        const dropdown = document.getElementById('autocompleteDropdown');
        const loading = document.getElementById('autocompleteLoading');
        const results = document.getElementById('autocompleteResults');
        
        if (!searchInput || !dropdown) return;
        
        let debounceTimer; // Timer để debounce
        let currentFocus = -1; // Index của item đang được focus
        let searchCache = {}; // Cache kết quả tìm kiếm
        
        // Xử lý khi nhập text vào search box
        searchInput.addEventListener('input', (e) => {
            const query = e.target.value.trim();
            
            // Hiện/ẩn nút clear
            searchClear.style.display = query.length > 0 ? 'block' : 'none';
            
            // Xóa debounce timer cũ
            clearTimeout(debounceTimer);
            
            if (query.length < 2) {
                dropdown.style.display = 'none';
                return;
            }
            
            // Debounce 300ms để tránh gọi API liên tục
            debounceTimer = setTimeout(() => {
                performAutocompleteSearch(query);
            }, 300);
        });
        
        // Xử lý keyboard navigation (Arrow Up/Down, Enter, Escape)
        searchInput.addEventListener('keydown', (e) => {
            const items = results.querySelectorAll('.autocomplete-item');
            
            if (e.key === 'ArrowDown') {
                e.preventDefault();
                currentFocus++;
                if (currentFocus >= items.length) currentFocus = 0;
                setActiveItem(items);
            } else if (e.key === 'ArrowUp') {
                e.preventDefault();
                currentFocus--;
                if (currentFocus < 0) currentFocus = items.length - 1;
                setActiveItem(items);
            } else if (e.key === 'Enter') {
                e.preventDefault();
                if (currentFocus > -1 && items[currentFocus]) {
                    // Click vào item đang active
                    items[currentFocus].click();
                } else {
                    // Tìm kiếm thông thường
                    const query = searchInput.value.trim();
                    if (query.length >= 2) {
                        window.location.href = `/tournaments/list?search=${encodeURIComponent(query)}`;
                    }
                }
            } else if (e.key === 'Escape') {
                dropdown.style.display = 'none';
                currentFocus = -1;
            }
        });
        
        // Xử lý nút clear (xóa text và đóng dropdown)
        searchClear.addEventListener('click', () => {
            searchInput.value = '';
            searchClear.style.display = 'none';
            dropdown.style.display = 'none';
            searchInput.focus();
        });
        
        // Đóng dropdown khi click bên ngoài
        document.addEventListener('click', (e) => {
            if (!searchInput.contains(e.target) && !dropdown.contains(e.target)) {
                dropdown.style.display = 'none';
            }
        });
        
        /**
         * Thực hiện tìm kiếm autocomplete
         */
        function performAutocompleteSearch(query) {
            // Kiểm tra cache trước để tránh gọi API trùng lặp
            if (searchCache[query]) {
                displayResults(searchCache[query]);
                return;
            }
            
            // Hiển thị dropdown và loading state
            dropdown.style.display = 'block';
            loading.style.display = 'flex';
            results.innerHTML = '';
            
            // Gọi API autocomplete
            fetch(`/api/tournaments/autocomplete?q=${encodeURIComponent(query)}&limit=5`)
                .then(response => response.json())
                .then(data => {
                    loading.style.display = 'none';
                    searchCache[query] = data; // Lưu vào cache
                    displayResults(data);
                })
                .catch(error => {
                    console.error('Search error:', error);
                    loading.style.display = 'none';
                    // Fallback: sử dụng mock data để demo
                    displayMockResults(query);
                });
        }
        
        /**
         * Hiển thị kết quả tìm kiếm trong dropdown
         */
        function displayResults(tournaments) {
            results.innerHTML = '';
            currentFocus = -1;
            
            // Nếu không có kết quả, hiển thị thông báo
            if (!tournaments || tournaments.length === 0) {
                results.innerHTML = `
                    <div class="autocomplete-no-results">
                        <i class="bi bi-search"></i>
                        <p>Không tìm thấy giải đấu nào</p>
                    </div>
                `;
                return;
            }
            
            // Tạo HTML cho mỗi tournament
            tournaments.forEach(tournament => {
                const item = document.createElement('a');
                item.href = `/tournaments/${tournament.id}`;
                item.className = 'autocomplete-item';
                
                // Format status text
                const statusText = getStatusText(tournament.trangThai);
                const statusClass = tournament.trangThai;
                
                item.innerHTML = `
                    <img src="${tournament.hinhAnh || '/images/tournament-placeholder.jpg'}" 
                         alt="${tournament.tenGiai}" 
                         class="autocomplete-thumbnail">
                    <div class="autocomplete-content">
                        <div class="autocomplete-title">${tournament.tenGiai}</div>
                        <div class="autocomplete-meta">
                            <span><i class="bi bi-geo-alt"></i> ${tournament.tinhThanh}</span>
                            <span><i class="bi bi-calendar"></i> ${tournament.ngayBatDau}</span>
                        </div>
                    </div>
                    <span class="autocomplete-status ${statusClass}">${statusText}</span>
                `;
                
                results.appendChild(item);
            });
        }
        
        /**
         * Hiển thị mock results khi API chưa có (để demo)
         */
        function displayMockResults(query) {
            const mockData = [
                {
                    id: 1,
                    tenGiai: `Giải Cầu lông ${query}`,
                    tinhThanh: 'TP.HCM',
                    ngayBatDau: '15/03/2025',
                    trangThai: 'ongoing',
                    hinhAnh: '/images/tournament-1.jpg'
                },
                {
                    id: 2,
                    tenGiai: `Giải Vô địch ${query}`,
                    tinhThanh: 'Hà Nội',
                    ngayBatDau: '20/03/2025',
                    trangThai: 'registration',
                    hinhAnh: '/images/tournament-2.jpg'
                }
            ];
            displayResults(mockData);
        }
        
        /**
         * Set active class cho item đang focus (keyboard navigation)
         */
        function setActiveItem(items) {
            // Xóa active class khỏi tất cả items
            items.forEach(item => item.classList.remove('active'));
            
            // Thêm active class vào item hiện tại
            if (currentFocus >= 0 && currentFocus < items.length) {
                items[currentFocus].classList.add('active');
                // Scroll item vào view nếu bị che
                items[currentFocus].scrollIntoView({ block: 'nearest', behavior: 'smooth' });
            }
        }
        
        /**
         * Convert status code sang text hiển thị
         */
        function getStatusText(status) {
            const statusMap = {
                'ongoing': 'ĐANG DIỄN RA',
                'upcoming': 'SẮP DIỄN RA',
                'registration': 'MỞ ĐĂNG KÝ',
                'completed': 'ĐÃ KẾT THÚC'
            };
            return statusMap[status] || 'KHÁC';
        }
    }
    
    /**
     * Perform search and redirect (legacy function)
     */
    function performSearch(query) {
        window.location.href = `/tournaments/list?search=${encodeURIComponent(query)}`;
    }
    
    /**
     * Filter Chips Interaction with Active States
     * Xử lý tương tác filter chip với trạng thái active
     */
    function initFilterChips() {
        const filterChips = document.querySelectorAll('.filter-chip');
        
        // Kiểm tra URL parameters để set active state ban đầu
        checkActiveFilterFromURL();
        
        filterChips.forEach(chip => {
            chip.addEventListener('click', function(e) {
                // Lưu filter value vào sessionStorage
                const filterValue = this.getAttribute('data-filter');
                if (filterValue) {
                    sessionStorage.setItem('activeFilter', filterValue);
                }
                
                // Visual feedback - Scale effect khi click
                this.style.transform = 'scale(0.95)';
                setTimeout(() => {
                    this.style.transform = '';
                }, 100);
            });
            
            // Hover effect - Làm nổi bật chip khi hover
            chip.addEventListener('mouseenter', function() {
                if (!this.classList.contains('active')) {
                    this.style.transform = 'translateY(-3px)';
                }
            });
            
            chip.addEventListener('mouseleave', function() {
                if (!this.classList.contains('active')) {
                    this.style.transform = '';
                }
            });
        });
    }
    
    /**
     * Check Active Filter from URL Parameters
     * Kiểm tra URL để set active state cho filter chip tương ứng
     */
    function checkActiveFilterFromURL() {
        // Lấy URL parameters
        const urlParams = new URLSearchParams(window.location.search);
        const statusParam = urlParams.get('status');
        
        // Lấy từ sessionStorage nếu không có trong URL
        const savedFilter = sessionStorage.getItem('activeFilter');
        const activeFilter = statusParam || savedFilter;
        
        if (activeFilter) {
            // Tìm và active chip tương ứng
            const filterChips = document.querySelectorAll('.filter-chip');
            filterChips.forEach(chip => {
                const filterValue = chip.getAttribute('data-filter');
                if (filterValue === activeFilter) {
                    chip.classList.add('active');
                    console.log(`Filter "${activeFilter}" is active`);
                } else {
                    chip.classList.remove('active');
                }
            });
        }
    }
    
    /**
     * Set Active Filter (có thể gọi từ bên ngoài)
     * @param {string} filterValue - Giá trị filter: 'ongoing', 'upcoming', 'registration'
     */
    function setActiveFilter(filterValue) {
        const filterChips = document.querySelectorAll('.filter-chip');
        
        filterChips.forEach(chip => {
            const chipFilter = chip.getAttribute('data-filter');
            
            if (chipFilter === filterValue) {
                // Add active class với animation
                chip.classList.add('active');
                // Lưu vào sessionStorage
                sessionStorage.setItem('activeFilter', filterValue);
            } else {
                chip.classList.remove('active');
            }
        });
    }
    
    /**
     * Clear All Active Filters
     * Xóa tất cả active states
     */
    function clearActiveFilters() {
        const filterChips = document.querySelectorAll('.filter-chip');
        filterChips.forEach(chip => chip.classList.remove('active'));
        sessionStorage.removeItem('activeFilter');
        console.log('All filters cleared');
    }
    
    /**
     * Animated Counter for Stats
     */
    function initStatsCountUp() {
        const statValues = document.querySelectorAll('.stat-value');
        
        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    animateCounter(entry.target);
                    observer.unobserve(entry.target);
                }
            });
        }, {
            threshold: 0.5
        });
        
        statValues.forEach(stat => observer.observe(stat));
    }
    
    /**
     * Animate number counter
     */
    function animateCounter(element) {
        const target = parseInt(element.textContent.replace(/,/g, ''));
        if (isNaN(target)) return;
        
        const duration = 2000; // 2 seconds
        const increment = target / (duration / 16); // 60 FPS
        let current = 0;
        
        const timer = setInterval(() => {
            current += increment;
            if (current >= target) {
                element.textContent = formatNumber(target);
                clearInterval(timer);
            } else {
                element.textContent = formatNumber(Math.floor(current));
            }
        }, 16);
    }
    
    /**
     * Format number with thousands separator
     */
    function formatNumber(num) {
        return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
    }
    
    /**
     * Live Indicators Animation
     */
    function initLiveIndicators() {
        const liveIndicators = document.querySelectorAll('.live-indicator');
        
        liveIndicators.forEach(indicator => {
            // Add random delay to create staggered effect
            const delay = Math.random() * 0.5;
            indicator.style.animationDelay = `${delay}s`;
        });
    }
    
    /**
     * Initialize Bootstrap Tooltips
     */
    function initTooltips() {
        const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]');
        if (tooltipTriggerList.length > 0 && typeof bootstrap !== 'undefined') {
            const tooltipList = [...tooltipTriggerList].map(tooltipTriggerEl => 
                new bootstrap.Tooltip(tooltipTriggerEl)
            );
        }
    }
    
    /**
     * Featured Card Hover Effects
     */
    const featuredCards = document.querySelectorAll('.featured-card');
    featuredCards.forEach(card => {
        card.addEventListener('mouseenter', function() {
            this.style.zIndex = '10';
        });
        
        card.addEventListener('mouseleave', function() {
            this.style.zIndex = '1';
        });
    });
    
    /**
     * Live Card Pulse Effect
     */
    const liveCards = document.querySelectorAll('.live-card');
    liveCards.forEach(card => {
        card.addEventListener('mouseenter', function() {
            const pulse = this.querySelector('.live-pulse');
            if (pulse) {
                pulse.style.animationPlayState = 'paused';
            }
        });
        
        card.addEventListener('mouseleave', function() {
            const pulse = this.querySelector('.live-pulse');
            if (pulse) {
                pulse.style.animationPlayState = 'running';
            }
        });
    });
    
    /**
     * Category Card Click Analytics (Optional)
     */
    const categoryCards = document.querySelectorAll('.category-card');
    categoryCards.forEach(card => {
        card.addEventListener('click', function(e) {
            const category = this.querySelector('.category-title').textContent;
            console.log('Category clicked:', category);
            
            // You can send analytics data here
            // Example: gtag('event', 'category_click', { category: category });
        });
    });
    
    /**
     * Nav Card Ripple Effect
     */
    const navCards = document.querySelectorAll('.nav-card');
    navCards.forEach(card => {
        card.addEventListener('click', function(e) {
            // Create ripple effect
            const ripple = document.createElement('span');
            ripple.classList.add('ripple');
            
            const rect = this.getBoundingClientRect();
            const x = e.clientX - rect.left;
            const y = e.clientY - rect.top;
            
            ripple.style.left = x + 'px';
            ripple.style.top = y + 'px';
            
            this.appendChild(ripple);
            
            setTimeout(() => {
                ripple.remove();
            }, 600);
        });
    });
    
    /**
     * Smooth Scroll for Anchor Links
     */
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function(e) {
            const href = this.getAttribute('href');
            if (href !== '#') {
                e.preventDefault();
                const target = document.querySelector(href);
                if (target) {
                    target.scrollIntoView({
                        behavior: 'smooth',
                        block: 'start'
                    });
                }
            }
        });
    });
    
    /**
     * Keyboard Navigation Support
     */
    document.addEventListener('keydown', function(e) {
        // Ctrl/Cmd + K to focus search
        if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
            e.preventDefault();
            const searchInput = document.getElementById('quickSearch');
            if (searchInput) {
                searchInput.focus();
            }
        }
    });
    
    /**
     * CTA Button Interaction
     */
    const ctaButtons = document.querySelectorAll('.cta-buttons .btn');
    ctaButtons.forEach(button => {
        button.addEventListener('mouseenter', function() {
            // Add pulse effect
            this.style.animation = 'pulse 0.5s';
        });
        
        button.addEventListener('animationend', function() {
            this.style.animation = '';
        });
    });
    
    /**
     * Handle Window Resize for Responsive Adjustments
     */
    let resizeTimeout;
    window.addEventListener('resize', function() {
        clearTimeout(resizeTimeout);
        resizeTimeout = setTimeout(() => {
            adjustLayoutForScreenSize();
        }, 250);
    });
    
    /**
     * Adjust layout based on screen size
     */
    function adjustLayoutForScreenSize() {
        const isMobile = window.innerWidth < 768;
        
        // Adjust grid gaps or other responsive features
        if (isMobile) {
            console.log('Mobile layout active');
        } else {
            console.log('Desktop layout active');
        }
    }
    
    /**
     * Lazy Loading for Images (if not using browser native)
     */
    function initLazyLoading() {
        const images = document.querySelectorAll('img[data-src]');
        
        const imageObserver = new IntersectionObserver((entries, observer) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    const img = entry.target;
                    img.src = img.dataset.src;
                    img.removeAttribute('data-src');
                    imageObserver.unobserve(img);
                }
            });
        });
        
        images.forEach(img => imageObserver.observe(img));
    }
    
    // Initialize lazy loading if needed
    if (document.querySelectorAll('img[data-src]').length > 0) {
        initLazyLoading();
    }
    
    /**
     * Performance Monitoring (Optional)
     */
    if ('performance' in window) {
        window.addEventListener('load', function() {
            const perfData = performance.timing;
            const pageLoadTime = perfData.loadEventEnd - perfData.navigationStart;
            console.log('Page load time:', pageLoadTime, 'ms');
        });
    }
    
    /**
     * Add Dynamic Greeting Based on Time
     */
    function addTimeBasedGreeting() {
        const hour = new Date().getHours();
        let greeting = 'Chào mừng đến với';
        
        if (hour < 12) {
            greeting = 'Chào buổi sáng! Khám phá';
        } else if (hour < 18) {
            greeting = 'Chào buổi chiều! Tham gia';
        } else {
            greeting = 'Chào buổi tối! Theo dõi';
        }
        
        // You can update hero subtitle with greeting
        // const heroSubtitle = document.querySelector('.hero-subtitle');
        // if (heroSubtitle) {
        //     heroSubtitle.textContent = greeting + ' các giải đấu cầu lông chuyên nghiệp';
        // }
    }
    
    // Call initial functions
    adjustLayoutForScreenSize();
    addTimeBasedGreeting();
    
    console.log('Tournament Home page initialized successfully');
});

/**
 * Custom Ripple Effect CSS (add to stylesheet or inline)
 */
const style = document.createElement('style');
style.textContent = `
    .nav-card {
        position: relative;
        overflow: hidden;
    }
    
    .ripple {
        position: absolute;
        border-radius: 50%;
        background: rgba(102, 126, 234, 0.3);
        transform: scale(0);
        animation: ripple-animation 0.6s ease-out;
        pointer-events: none;
    }
    
    @keyframes ripple-animation {
        to {
            transform: scale(4);
            opacity: 0;
        }
    }
`;
document.head.appendChild(style);

    /**
     * ============================================
     * LOADING STATES - Skeleton Loaders
     * Hiển thị skeleton placeholders khi đang tải data
     * ============================================
     */
    
    /**
     * Initialize Loading States
     * Tự động show/hide skeleton dựa vào data có sẵn hay không
     */
    function initLoadingStates() {
        // Kiểm tra xem có data không
        const statsCards = document.querySelectorAll('.stat-card');
        const featuredCards = document.querySelectorAll('.featured-card');
        const liveCarousel = document.querySelector('.live-tournaments-swiper');
        
        // Nếu không có data, show skeleton
        if (statsCards.length === 0) {
            showStatsSkeletons();
        }
        
        if (featuredCards.length === 0) {
            showFeaturedSkeletons();
        }
        
        if (!liveCarousel || liveCarousel.querySelectorAll('.swiper-slide').length === 0) {
            showLiveSkeletons();
        }
    }
    
    /**
     * Show Stats Skeletons
     * Tạo 4 skeleton cards cho stats section
     */
    function showStatsSkeletons() {
        const statsGrid = document.querySelector('.stats-grid');
        if (!statsGrid) return;
        
        // Tạo loading container
        const loadingContainer = document.createElement('div');
        loadingContainer.className = 'loading-container stats-loading';
        loadingContainer.setAttribute('data-skeleton', 'stats');
        
        // Tạo 4 skeleton cards
        for (let i = 0; i < 4; i++) {
            const skeleton = document.createElement('div');
            skeleton.className = 'skeleton skeleton-stat';
            loadingContainer.appendChild(skeleton);
        }
        
        // Insert trước stats-grid
        statsGrid.parentNode.insertBefore(loadingContainer, statsGrid);
        statsGrid.classList.add('loading'); // Ẩn grid thật
        
        console.log('Stats skeletons displayed');
    }
    
    /**
     * Show Featured Skeletons
     * Tạo 4 skeleton cards cho featured tournaments
     */
    function showFeaturedSkeletons() {
        const featuredGrid = document.querySelector('.featured-grid');
        if (!featuredGrid) return;
        
        const loadingContainer = document.createElement('div');
        loadingContainer.className = 'loading-container featured-loading';
        loadingContainer.setAttribute('data-skeleton', 'featured');
        
        // Tạo 4 skeleton cards
        for (let i = 0; i < 4; i++) {
            const skeleton = document.createElement('div');
            skeleton.className = 'skeleton skeleton-featured';
            loadingContainer.appendChild(skeleton);
        }
        
        featuredGrid.parentNode.insertBefore(loadingContainer, featuredGrid);
        featuredGrid.classList.add('loading');
        
        console.log('Featured skeletons displayed');
    }
    
    /**
     * Show Live Tournament Skeletons
     * Tạo 3 skeleton cards cho live carousel
     */
    function showLiveSkeletons() {
        const liveSection = document.querySelector('.live-section .container');
        if (!liveSection) return;
        
        const loadingContainer = document.createElement('div');
        loadingContainer.className = 'loading-container live-loading';
        loadingContainer.setAttribute('data-skeleton', 'live');
        
        // Tạo 3 skeleton cards (responsive sẽ show 1-3 tùy màn hình)
        for (let i = 0; i < 3; i++) {
            const skeleton = document.createElement('div');
            skeleton.className = 'skeleton skeleton-live';
            loadingContainer.appendChild(skeleton);
        }
        
        // Tìm carousel container
        const carousel = document.querySelector('.live-tournaments-swiper');
        if (carousel) {
            carousel.parentNode.insertBefore(loadingContainer, carousel);
            carousel.classList.add('loading');
        }
        
        console.log('Live skeletons displayed');
    }
    
    /**
     * Hide All Skeletons
     * Remove tất cả skeleton và show real content
     * Gọi function này sau khi data đã load xong
     */
    function hideAllSkeletons() {
        // Remove tất cả skeleton containers
        const skeletons = document.querySelectorAll('[data-skeleton]');
        skeletons.forEach(skeleton => {
            skeleton.style.opacity = '0';
            skeleton.style.transition = 'opacity 0.3s ease';
            
            // Remove sau animation
            setTimeout(() => {
                skeleton.remove();
            }, 300);
        });
        
        // Show real content với fade-in
        const statsGrid = document.querySelector('.stats-grid');
        const featuredGrid = document.querySelector('.featured-grid');
        const liveCarousel = document.querySelector('.live-tournaments-swiper');
        
        if (statsGrid) {
            statsGrid.classList.remove('loading');
        }
        
        if (featuredGrid) {
            featuredGrid.classList.remove('loading');
        }
        
        if (liveCarousel) {
            liveCarousel.classList.remove('loading');
        }
        
        console.log('All skeletons hidden, real content displayed');
    }
    
    /**
     * Simulate Data Loading (Demo Purpose)
     * Trong production, function này sẽ được gọi sau AJAX/Fetch success
     */
    function simulateDataLoading() {
        // Show skeletons ngay lập tức
        initLoadingStates();
        
        // Giả lập loading 2 giây, sau đó hide skeleton
        setTimeout(() => {
            hideAllSkeletons();
        }, 2000);
    }
    
    // Auto-run loading states khi page load
    // Comment out dòng dưới nếu có data thật từ server
    // simulateDataLoading();
    
