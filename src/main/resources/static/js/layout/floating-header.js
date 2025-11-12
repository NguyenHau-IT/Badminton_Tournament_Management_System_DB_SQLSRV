/**
 * Floating Pill Navigation Header JavaScript
 * Handles interactions, dropdowns, and search
 */

document.addEventListener('DOMContentLoaded', function() {
    
    // Initialize components
    initDropdowns();
    initSearch();
    initScrollBehavior();
    initNotifications();
    
    /**
     * Dropdown Menu Management
     */
    function initDropdowns() {
        const navItems = document.querySelectorAll('.nav-item');
        let currentDropdown = null;
        let hideTimeout = null;
        
        // Close all dropdowns
        function closeAllDropdowns() {
            navItems.forEach(item => {
                const dropdown = item.querySelector('.nav-dropdown');
                if (dropdown) {
                    dropdown.style.opacity = '0';
                    dropdown.style.visibility = 'hidden';
                    dropdown.style.pointerEvents = 'none';
                }
            });
            currentDropdown = null;
        }
        
        navItems.forEach(item => {
            const dropdown = item.querySelector('.nav-dropdown');
            if (!dropdown) return;
            
            // Show dropdown on hover
            item.addEventListener('mouseenter', function() {
                clearTimeout(hideTimeout);
                
                // Close other dropdowns first
                if (currentDropdown && currentDropdown !== dropdown) {
                    currentDropdown.style.opacity = '0';
                    currentDropdown.style.visibility = 'hidden';
                    currentDropdown.style.pointerEvents = 'none';
                }
                
                // Open this dropdown
                dropdown.style.opacity = '1';
                dropdown.style.visibility = 'visible';
                dropdown.style.pointerEvents = 'auto';
                currentDropdown = dropdown;
            });
            
            // Hide dropdown with shorter delay
            item.addEventListener('mouseleave', function() {
                hideTimeout = setTimeout(() => {
                    dropdown.style.opacity = '0';
                    dropdown.style.visibility = 'hidden';
                    dropdown.style.pointerEvents = 'none';
                    if (currentDropdown === dropdown) {
                        currentDropdown = null;
                    }
                }, 150);
            });
            
            // Keep dropdown open when hovering on it
            dropdown.addEventListener('mouseenter', function() {
                clearTimeout(hideTimeout);
            });
            
            dropdown.addEventListener('mouseleave', function() {
                hideTimeout = setTimeout(() => {
                    dropdown.style.opacity = '0';
                    dropdown.style.visibility = 'hidden';
                    dropdown.style.pointerEvents = 'none';
                    if (currentDropdown === dropdown) {
                        currentDropdown = null;
                    }
                }, 150);
            });
        });
        
        // Close dropdowns when clicking outside
        document.addEventListener('click', function(e) {
            if (!e.target.closest('.nav-item')) {
                closeAllDropdowns();
            }
        });
    }
    
    /**
     * Header Search Functionality
     */
    function initSearch() {
        const searchInput = document.getElementById('headerSearch');
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
                    window.location.href = `/search?q=${encodeURIComponent(query)}`;
                }
            }
        });
        
        // Handle Escape key to close search
        searchInput.addEventListener('keydown', function(e) {
            if (e.key === 'Escape') {
                this.blur();
            }
        });
    }
    
    /**
     * Perform search (can integrate with API)
     */
    function performSearch(query) {
        console.log('Searching for:', query);
        // TODO: Integrate with search API or redirect
    }
    
    /**
     * Header Scroll Behavior
     */
    function initScrollBehavior() {
        const header = document.querySelector('.floating-header');
        if (!header) return;
        
        let lastScroll = 0;
        let isHeaderVisible = true;
        
        window.addEventListener('scroll', function() {
            const currentScroll = window.pageYOffset;
            
            // Hide header on scroll down, show on scroll up
            if (currentScroll > lastScroll && currentScroll > 100) {
                // Scrolling down
                if (isHeaderVisible) {
                    header.style.transform = 'translateX(-50%) translateY(-100px)';
                    header.style.opacity = '0';
                    isHeaderVisible = false;
                }
            } else {
                // Scrolling up
                if (!isHeaderVisible) {
                    header.style.transform = 'translateX(-50%) translateY(0)';
                    header.style.opacity = '1';
                    isHeaderVisible = true;
                }
            }
            
            lastScroll = currentScroll;
        });
        
        // Add smooth transition
        header.style.transition = 'all 0.3s ease';
    }
    
    /**
     * Notification Management
     */
    function initNotifications() {
        const notificationBtn = document.getElementById('notificationBtn');
        if (!notificationBtn) return;
        
        // Mark notifications as read when dropdown is opened
        notificationBtn.addEventListener('mouseenter', function() {
            const badge = this.querySelector('.notification-badge');
            if (badge) {
                setTimeout(() => {
                    // badge.style.display = 'none';
                    // Keep badge visible or update count via API
                }, 3000);
            }
        });
    }
    
    /**
     * Active Page Highlighting
     */
    function highlightActivePage() {
        const currentPath = window.location.pathname;
        const navItems = document.querySelectorAll('.nav-item');
        
        navItems.forEach(item => {
            const btn = item.querySelector('.nav-btn');
            if (!btn) return;
            
            const onclick = btn.getAttribute('onclick');
            if (onclick && onclick.includes(currentPath)) {
                item.classList.add('current');
            }
        });
    }
    
    /**
     * Keyboard Shortcuts
     */
    document.addEventListener('keydown', function(e) {
        // Ctrl/Cmd + K to focus search
        if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
            e.preventDefault();
            const searchInput = document.getElementById('headerSearch');
            if (searchInput) {
                searchInput.focus();
                // Trigger hover effect on parent
                const navSearch = searchInput.closest('.nav-search');
                if (navSearch) {
                    navSearch.classList.add('active');
                }
            }
        }
        
        // Escape to close search
        if (e.key === 'Escape') {
            const searchInput = document.getElementById('headerSearch');
            if (searchInput && document.activeElement === searchInput) {
                searchInput.blur();
                searchInput.value = '';
            }
        }
    });
    
    /**
     * Smooth Scroll for Anchor Links
     */
    document.querySelectorAll('.dropdown-item[href^="#"]').forEach(anchor => {
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
     * Handle Responsive Behavior
     */
    function handleResponsive() {
        const isMobile = window.innerWidth < 768;
        
        if (isMobile) {
            // Close all dropdowns on mobile when clicking outside
            document.addEventListener('click', function(e) {
                if (!e.target.closest('.nav-item')) {
                    document.querySelectorAll('.nav-dropdown').forEach(dropdown => {
                        dropdown.style.opacity = '0';
                        dropdown.style.visibility = 'hidden';
                    });
                }
            });
        }
    }
    
    /**
     * User Menu Toggle
     */
    const userMenuBtn = document.getElementById('userMenuBtn');
    if (userMenuBtn) {
        userMenuBtn.addEventListener('click', function() {
            const dropdown = this.nextElementSibling;
            if (dropdown && dropdown.classList.contains('nav-dropdown')) {
                const isVisible = dropdown.style.visibility === 'visible';
                dropdown.style.opacity = isVisible ? '0' : '1';
                dropdown.style.visibility = isVisible ? 'hidden' : 'visible';
            }
        });
    }
    
    // Initialize
    highlightActivePage();
    handleResponsive();
    
    // Handle window resize
    let resizeTimeout;
    window.addEventListener('resize', function() {
        clearTimeout(resizeTimeout);
        resizeTimeout = setTimeout(handleResponsive, 250);
    });
    
    console.log('Floating header initialized successfully');
});

/**
 * Utility: Add ripple effect to buttons
 */
document.querySelectorAll('.nav-btn').forEach(btn => {
    btn.addEventListener('click', function(e) {
        const ripple = document.createElement('span');
        ripple.classList.add('ripple-effect');
        
        const rect = this.getBoundingClientRect();
        const size = Math.max(rect.width, rect.height);
        const x = e.clientX - rect.left - size / 2;
        const y = e.clientY - rect.top - size / 2;
        
        ripple.style.width = ripple.style.height = size + 'px';
        ripple.style.left = x + 'px';
        ripple.style.top = y + 'px';
        
        this.appendChild(ripple);
        
        setTimeout(() => {
            ripple.remove();
        }, 600);
    });
});

// Ripple effect CSS (injected)
const rippleStyle = document.createElement('style');
rippleStyle.textContent = `
    .nav-btn {
        position: relative;
        overflow: hidden;
    }
    
    .ripple-effect {
        position: absolute;
        border-radius: 50%;
        background: rgba(255, 107, 53, 0.3);
        transform: scale(0);
        animation: ripple 0.6s ease-out;
        pointer-events: none;
    }
    
    @keyframes ripple {
        to {
            transform: scale(4);
            opacity: 0;
        }
    }
`;
document.head.appendChild(rippleStyle);
