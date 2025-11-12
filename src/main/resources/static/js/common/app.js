/**
 * BTMS - Main Application JavaScript
 * Common functionality across all pages
 */

(function() {
    'use strict';
    
    // Main App Object
    const BTMS = {
        // Configuration
        config: {
            apiBaseUrl: '/api',
            sseTimeout: 30000,
            animationDuration: 300
        },
        
        // Initialize
        init: function() {
            this.setupNavigation();
            this.setupScrollEffects();
            this.setupDropdowns();
            this.setupTooltips();
            this.setupModals();
            console.log('✅ BTMS App initialized');
        },
        
        // Navigation Active State
        setupNavigation: function() {
            const currentPath = window.location.pathname;
            const navLinks = document.querySelectorAll('.nav-link');
            
            navLinks.forEach(link => {
                const href = link.getAttribute('href');
                if (href && currentPath.startsWith(href) && href !== '/') {
                    link.classList.add('active');
                }
            });
        },
        
        // Scroll Effects
        setupScrollEffects: function() {
            let lastScroll = 0;
            const header = document.querySelector('.header');
            
            window.addEventListener('scroll', () => {
                const currentScroll = window.pageYOffset;
                
                // Add shadow on scroll
                if (currentScroll > 10) {
                    header?.classList.add('shadow');
                } else {
                    header?.classList.remove('shadow');
                }
                
                // Hide/show header on scroll (optional)
                // Uncomment if you want auto-hide behavior
                /*
                if (currentScroll > lastScroll && currentScroll > 100) {
                    header?.style.transform = 'translateY(-100%)';
                } else {
                    header?.style.transform = 'translateY(0)';
                }
                */
                
                lastScroll = currentScroll;
            });
        },
        
        // Bootstrap Dropdowns
        setupDropdowns: function() {
            const dropdownElementList = [].slice.call(
                document.querySelectorAll('[data-bs-toggle="dropdown"]')
            );
            
            dropdownElementList.map(function (dropdownToggleEl) {
                return new bootstrap.Dropdown(dropdownToggleEl);
            });
        },
        
        // Bootstrap Tooltips
        setupTooltips: function() {
            const tooltipTriggerList = [].slice.call(
                document.querySelectorAll('[data-bs-toggle="tooltip"]')
            );
            
            tooltipTriggerList.map(function (tooltipTriggerEl) {
                return new bootstrap.Tooltip(tooltipTriggerEl);
            });
        },
        
        // Bootstrap Modals
        setupModals: function() {
            const modalElementList = [].slice.call(
                document.querySelectorAll('.modal')
            );
            
            modalElementList.map(function (modalEl) {
                return new bootstrap.Modal(modalEl);
            });
        },
        
        // Smooth Scroll to Element
        scrollTo: function(elementId, offset = 80) {
            const element = document.getElementById(elementId);
            if (element) {
                const top = element.offsetTop - offset;
                window.scrollTo({
                    top: top,
                    behavior: 'smooth'
                });
            }
        },
        
        // Show Alert
        showAlert: function(message, type = 'info', duration = 3000) {
            const alertHtml = `
                <div class="alert alert-${type} alert-dismissible fade show position-fixed top-0 start-50 translate-middle-x mt-3" 
                     role="alert" style="z-index: 9999; min-width: 300px;">
                    ${message}
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            `;
            
            document.body.insertAdjacentHTML('beforeend', alertHtml);
            
            // Auto dismiss
            if (duration > 0) {
                setTimeout(() => {
                    const alerts = document.querySelectorAll('.alert');
                    alerts[alerts.length - 1]?.remove();
                }, duration);
            }
        },
        
        // Loading Spinner
        showLoading: function(targetElement) {
            const spinner = `
                <div class="text-center py-5 loading-spinner">
                    <div class="spinner-border text-primary" role="status">
                        <span class="visually-hidden">Loading...</span>
                    </div>
                </div>
            `;
            
            if (targetElement) {
                targetElement.innerHTML = spinner;
            }
        },
        
        // Format Date
        formatDate: function(dateString, format = 'dd/MM/yyyy') {
            const date = new Date(dateString);
            const day = String(date.getDate()).padStart(2, '0');
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const year = date.getFullYear();
            
            return format
                .replace('dd', day)
                .replace('MM', month)
                .replace('yyyy', year);
        },
        
        // Format Number
        formatNumber: function(num) {
            return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
        }
    };
    
    // Initialize when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', () => BTMS.init());
    } else {
        BTMS.init();
    }
    
    // Expose to global scope
    window.BTMS = BTMS;
    
})();
