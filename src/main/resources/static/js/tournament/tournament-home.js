/**
 * Tournament Home Page JavaScript
 * Handles search, filters, and interactive features
 */

document.addEventListener('DOMContentLoaded', function() {
    
    // Initialize components
    initQuickSearch();
    initFilterChips();
    initStatsCountUp();
    initLiveIndicators();
    initTooltips();
    
    /**
     * Quick Search Functionality
     */
    function initQuickSearch() {
        const searchInput = document.getElementById('quickSearch');
        if (!searchInput) return;
        
        let searchTimeout;
        
        searchInput.addEventListener('input', function(e) {
            clearTimeout(searchTimeout);
            const query = e.target.value.trim();
            
            if (query.length < 2) return;
            
            // Debounce search
            searchTimeout = setTimeout(() => {
                performSearch(query);
            }, 500);
        });
        
        // Handle Enter key
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                const query = e.target.value.trim();
                if (query.length >= 2) {
                    window.location.href = `/tournaments/list?search=${encodeURIComponent(query)}`;
                }
            }
        });
    }
    
    /**
     * Perform search and redirect
     */
    function performSearch(query) {
        // Show loading indicator (optional)
        console.log('Searching for:', query);
        
        // Redirect to list page with search query
        window.location.href = `/tournaments/list?search=${encodeURIComponent(query)}`;
    }
    
    /**
     * Filter Chips Interaction
     */
    function initFilterChips() {
        const filterChips = document.querySelectorAll('.filter-chip');
        
        filterChips.forEach(chip => {
            chip.addEventListener('click', function(e) {
                // Add visual feedback
                this.style.transform = 'scale(0.95)';
                setTimeout(() => {
                    this.style.transform = '';
                }, 100);
            });
        });
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
