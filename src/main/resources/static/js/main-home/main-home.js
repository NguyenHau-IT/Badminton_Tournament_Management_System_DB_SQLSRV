/**
 * MAIN HOME PAGE JAVASCRIPT
 * FULLPAGE SCROLL SNAP + Animations, counters, and interactive elements
 */

(function() {
    'use strict';

    // ===================================
    // FULLPAGE SCROLL SNAP NAVIGATION
    // ===================================
    const initFullpageScroll = () => {
        const container = document.getElementById('fullpageContainer');
        const sections = container ? container.querySelectorAll('section') : [];
        const indicators = document.querySelectorAll('.scroll-indicator');
        
        if (!container || sections.length === 0) {
            console.log('Fullpage: No container or sections found');
            return;
        }
        
        console.log(`Fullpage: Found ${sections.length} sections`);
        
        let isScrolling = false;
        let currentSection = 0;
        
        // Update active indicator based on scroll position
        const updateActiveIndicator = () => {
            const scrollTop = container.scrollTop;
            const viewportHeight = window.innerHeight;
            const newSection = Math.round(scrollTop / viewportHeight);
            
            if (newSection !== currentSection && newSection >= 0 && newSection < sections.length) {
                currentSection = newSection;
                
                indicators.forEach((indicator, index) => {
                    indicator.classList.toggle('active', index === currentSection);
                });
                
                console.log(`Active section: ${currentSection}`);
            }
        };
        
        // Scroll to specific section
        const scrollToSection = (index) => {
            if (index < 0 || index >= sections.length) return;
            
            const section = sections[index];
            container.scrollTo({
                top: section.offsetTop,
                behavior: 'smooth'
            });
            
            currentSection = index;
        };
        
        // Click on indicators to navigate
        indicators.forEach((indicator, index) => {
            indicator.addEventListener('click', () => {
                console.log(`Navigating to section ${index}`);
                scrollToSection(index);
            });
        });
        
        // Update indicator on scroll
        container.addEventListener('scroll', () => {
            if (!isScrolling) {
                window.requestAnimationFrame(() => {
                    updateActiveIndicator();
                    isScrolling = false;
                });
                isScrolling = true;
            }
        });
        
        // Keyboard navigation (Arrow Up/Down)
        document.addEventListener('keydown', (e) => {
            if (e.key === 'ArrowDown' && currentSection < sections.length - 1) {
                e.preventDefault();
                scrollToSection(currentSection + 1);
            } else if (e.key === 'ArrowUp' && currentSection > 0) {
                e.preventDefault();
                scrollToSection(currentSection - 1);
            }
        });
        
        console.log('Fullpage scroll initialized successfully');
    };

    // ===================================
    // COUNTER ANIMATION
    // ===================================
    const animateCounter = (element, target, duration = 2000) => {
        let start = 0;
        const increment = target / (duration / 16); // 60fps
        
        const updateCounter = () => {
            start += increment;
            if (start < target) {
                element.textContent = Math.floor(start).toLocaleString('vi-VN');
                requestAnimationFrame(updateCounter);
            } else {
                element.textContent = target.toLocaleString('vi-VN');
            }
        };
        
        updateCounter();
    };

    // ===================================
    // INTERSECTION OBSERVER FOR COUNTERS
    // ===================================
    const observeCounters = () => {
        const counters = document.querySelectorAll('[data-count]');
        
        if (counters.length === 0) return;
        
        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting && !entry.target.dataset.animated) {
                    const target = parseInt(entry.target.dataset.count);
                    animateCounter(entry.target, target);
                    entry.target.dataset.animated = 'true';
                }
            });
        }, {
            threshold: 0.5
        });
        
        counters.forEach(counter => observer.observe(counter));
    };

    // ===================================
    // SMOOTH SCROLL TO FEATURES
    // ===================================
    const setupScrollIndicator = () => {
        const scrollBtn = document.querySelector('.scroll-indicator a');
        
        if (scrollBtn) {
            scrollBtn.addEventListener('click', (e) => {
                e.preventDefault();
                const featuresSection = document.querySelector('.features-section');
                
                if (featuresSection) {
                    featuresSection.scrollIntoView({
                        behavior: 'smooth',
                        block: 'start'
                    });
                }
            });
        }
    };

    // ===================================
    // THROTTLE UTILITY FUNCTION
    // ===================================
    const throttle = (func, delay) => {
        let timeoutId;
        let lastExecTime = 0;
        
        return function(...args) {
            const currentTime = Date.now();
            const timeSinceLastExec = currentTime - lastExecTime;
            
            clearTimeout(timeoutId);
            
            if (timeSinceLastExec > delay) {
                lastExecTime = currentTime;
                func.apply(this, args);
            } else {
                timeoutId = setTimeout(() => {
                    lastExecTime = Date.now();
                    func.apply(this, args);
                }, delay - timeSinceLastExec);
            }
        };
    };

    // ===================================
    // PARALLAX EFFECT FOR HERO
    // ===================================
    const setupParallax = () => {
        const hero = document.querySelector('.hero-section');
        
        if (!hero) return;
        
        window.addEventListener('scroll', throttle(() => {
            const scrolled = window.pageYOffset;
            const parallaxElements = hero.querySelectorAll('.hero-content, .hero-image');
            
            parallaxElements.forEach(el => {
                const speed = el.dataset.speed || 0.5;
                el.style.transform = `translateY(${scrolled * speed}px)`;
            });
        }, 16));
    };

    // ===================================
    // TOURNAMENT CARD HOVER EFFECTS
    // ===================================
    const setupTournamentCards = () => {
        const cards = document.querySelectorAll('.tournament-card');
        
        cards.forEach(card => {
            card.addEventListener('mouseenter', function() {
                this.style.transform = 'translateY(-8px) scale(1.02)';
            });
            
            card.addEventListener('mouseleave', function() {
                this.style.transform = 'translateY(0) scale(1)';
            });
        });
    };

    // ===================================
    // FEATURE CARDS STAGGER ANIMATION
    // ===================================
    const setupFeatureCards = () => {
        const featureCards = document.querySelectorAll('.feature-card');
        
        if (featureCards.length === 0) return;
        
        const observer = new IntersectionObserver((entries) => {
            entries.forEach((entry, index) => {
                if (entry.isIntersecting) {
                    setTimeout(() => {
                        entry.target.style.opacity = '1';
                        entry.target.style.transform = 'translateY(0)';
                    }, index * 100);
                }
            });
        }, {
            threshold: 0.2
        });
        
        featureCards.forEach(card => {
            card.style.opacity = '0';
            card.style.transform = 'translateY(20px)';
            card.style.transition = 'all 0.5s ease';
            observer.observe(card);
        });
    };

    // ===================================
    // TESTIMONIALS CARD ANIMATION
    // ===================================
    const setupTestimonials = () => {
        const testimonialCards = document.querySelectorAll('.testimonial-card');
        
        if (testimonialCards.length === 0) return;
        
        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.classList.add('visible');
                }
            });
        }, {
            threshold: 0.3
        });
        
        testimonialCards.forEach(card => observer.observe(card));
    };

    // ===================================
    // ADD TYPING EFFECT TO HERO TITLE
    // ===================================
    const setupTypingEffect = () => {
        const heroTitle = document.querySelector('.hero-title');
        
        if (!heroTitle) return;
        
        const originalText = heroTitle.textContent;
        const gradientSpan = heroTitle.querySelector('.gradient-text');
        
        if (!gradientSpan) return;
        
        const gradientText = gradientSpan.textContent;
        let index = 0;
        
        // Only animate on first visit
        if (sessionStorage.getItem('heroAnimated')) return;
        
        heroTitle.textContent = originalText.replace(gradientText, '');
        gradientSpan.textContent = '';
        heroTitle.appendChild(gradientSpan);
        
        const typeInterval = setInterval(() => {
            if (index < gradientText.length) {
                gradientSpan.textContent += gradientText.charAt(index);
                index++;
            } else {
                clearInterval(typeInterval);
                sessionStorage.setItem('heroAnimated', 'true');
            }
        }, 100);
    };

    // ===================================
    // VIDEO BACKGROUND SETUP
    // ===================================
    const setupVideoBackground = () => {
        const videoContainer = document.querySelector('.hero-video-bg');
        
        if (!videoContainer) return;
        
        // Create video element if data-video attribute exists
        const videoUrl = videoContainer.dataset.video;
        
        if (videoUrl) {
            const video = document.createElement('video');
            video.src = videoUrl;
            video.autoplay = true;
            video.loop = true;
            video.muted = true;
            video.playsInline = true;
            video.className = 'hero-video-bg';
            
            videoContainer.replaceWith(video);
        }
    };

    // ===================================
    // LIVE BADGE ANIMATION
    // ===================================
    const setupLiveBadges = () => {
        const liveBadges = document.querySelectorAll('.tournament-badge.live');
        
        liveBadges.forEach(badge => {
            // Add blinking dot
            const dot = document.createElement('span');
            dot.style.cssText = `
                display: inline-block;
                width: 8px;
                height: 8px;
                background: white;
                border-radius: 50%;
                margin-right: 6px;
                animation: blink 1s infinite;
            `;
            badge.prepend(dot);
        });
        
        // Add blink animation if not exists
        if (!document.querySelector('#blink-animation')) {
            const style = document.createElement('style');
            style.id = 'blink-animation';
            style.textContent = `
                @keyframes blink {
                    0%, 100% { opacity: 1; }
                    50% { opacity: 0.3; }
                }
            `;
            document.head.appendChild(style);
        }
    };

    // ===================================
    // DOWNLOAD BUTTON TRACKING
    // ===================================
    const setupDownloadTracking = () => {
        const downloadButtons = document.querySelectorAll('a[href*="/app/download"]');
        
        downloadButtons.forEach(btn => {
            btn.addEventListener('click', (e) => {
                // Track download event (can be integrated with analytics)
                console.log('Download button clicked:', {
                    section: btn.closest('section')?.className || 'unknown',
                    timestamp: new Date().toISOString()
                });
                
                // Show success message
                if (window.BTMS && window.BTMS.showAlert) {
                    BTMS.showAlert('Đang chuẩn bị tải xuống...', 'info');
                }
            });
        });
    };

    // ===================================
    // LAZY LOAD IMAGES
    // ===================================
    const setupLazyLoading = () => {
        const images = document.querySelectorAll('img[data-src]');
        
        if (images.length === 0) return;
        
        const imageObserver = new IntersectionObserver((entries) => {
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
    };

    // ===================================
    // STATS GROWTH ANIMATION
    // ===================================
    const setupStatsGrowth = () => {
        const growthBadges = document.querySelectorAll('.stat-growth');
        
        if (growthBadges.length === 0) return;
        
        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.style.animation = 'slideInUp 0.5s ease forwards';
                }
            });
        }, {
            threshold: 0.8
        });
        
        growthBadges.forEach(badge => {
            badge.style.opacity = '0';
            badge.style.transform = 'translateY(20px)';
            observer.observe(badge);
        });
        
        // Add animation
        if (!document.querySelector('#slideInUp-animation')) {
            const style = document.createElement('style');
            style.id = 'slideInUp-animation';
            style.textContent = `
                @keyframes slideInUp {
                    to {
                        opacity: 1;
                        transform: translateY(0);
                    }
                }
            `;
            document.head.appendChild(style);
        }
    };

    // ===================================
    // CTA BUTTON RIPPLE EFFECT
    // ===================================
    const setupRippleEffect = () => {
        const buttons = document.querySelectorAll('.cta-buttons .btn');
        
        buttons.forEach(btn => {
            btn.addEventListener('click', function(e) {
                const rect = this.getBoundingClientRect();
                const x = e.clientX - rect.left;
                const y = e.clientY - rect.top;
                
                const ripple = document.createElement('span');
                ripple.style.cssText = `
                    position: absolute;
                    border-radius: 50%;
                    background: rgba(255, 255, 255, 0.6);
                    width: 20px;
                    height: 20px;
                    left: ${x}px;
                    top: ${y}px;
                    transform: translate(-50%, -50%) scale(0);
                    animation: ripple 0.6s ease-out;
                    pointer-events: none;
                `;
                
                this.style.position = 'relative';
                this.style.overflow = 'hidden';
                this.appendChild(ripple);
                
                setTimeout(() => ripple.remove(), 600);
            });
        });
        
        // Add ripple animation
        if (!document.querySelector('#ripple-animation')) {
            const style = document.createElement('style');
            style.id = 'ripple-animation';
            style.textContent = `
                @keyframes ripple {
                    to {
                        transform: translate(-50%, -50%) scale(20);
                        opacity: 0;
                    }
                }
            `;
            document.head.appendChild(style);
        }
    };

    // ===================================
    // DRAG TO SCROLL FEATURES CAROUSEL
    // ===================================
    const setupFeaturesDragScroll = () => {
        const wrapper = document.querySelector('.features-carousel-wrapper');
        const track = document.querySelector('.features-carousel-track');
        
        if (!wrapper || !track) return;
        
        let isDragging = false;
        let startX = 0;
        let scrollLeft = 0;
        let animationPaused = false;
        
        // Get current animation progress
        const getCurrentTransform = () => {
            const style = window.getComputedStyle(track);
            const matrix = new DOMMatrix(style.transform);
            return matrix.m41; // translateX value
        };
        
        // Mouse down - start dragging
        wrapper.addEventListener('mousedown', (e) => {
            isDragging = true;
            wrapper.classList.add('dragging');
            startX = e.pageX;
            scrollLeft = getCurrentTransform();
            
            // Pause animation and store current position
            track.style.animation = 'none';
            track.style.transform = `translateX(${scrollLeft}px)`;
        });
        
        // Mouse move - drag
        wrapper.addEventListener('mousemove', (e) => {
            if (!isDragging) return;
            e.preventDefault();
            
            const x = e.pageX;
            const walk = (x - startX) * 1.5; // Multiply for faster scroll
            const newPosition = scrollLeft + walk;
            
            track.style.transform = `translateX(${newPosition}px)`;
        });
        
        // Mouse up - stop dragging
        const stopDragging = () => {
            if (!isDragging) return;
            
            isDragging = false;
            wrapper.classList.remove('dragging');
            
            // Resume animation from current position
            const currentTransform = getCurrentTransform();
            track.style.animation = '';
            
            // Adjust animation to continue from current position
            const trackWidth = track.scrollWidth / 2; // Half because duplicated
            const normalizedPosition = ((currentTransform % trackWidth) + trackWidth) % trackWidth;
            const progress = (normalizedPosition / trackWidth);
            
            track.style.animationDelay = `-${progress * 50}s`;
        };
        
        wrapper.addEventListener('mouseup', stopDragging);
        wrapper.addEventListener('mouseleave', stopDragging);
        
        // Prevent click when dragging
        wrapper.addEventListener('click', (e) => {
            if (Math.abs(e.pageX - startX) > 5) {
                e.preventDefault();
            }
        });
        
        console.log('Features drag-to-scroll initialized');
    };

    // ===================================
    // INITIALIZE ALL FUNCTIONS
    // ===================================
    const init = () => {
        // NEW: Initialize fullpage scroll navigation
        initFullpageScroll();
        
        // NEW: Initialize features drag-to-scroll
        setupFeaturesDragScroll();
        
        // Core animations
        observeCounters();
        setupScrollIndicator();
        setupParallax();
        
        // Card animations
        setupFeatureCards();
        setupTournamentCards();
        setupTestimonials();
        
        // Visual effects
        setupLiveBadges();
        setupRippleEffect();
        setupStatsGrowth();
        
        // Media
        setupVideoBackground();
        setupLazyLoading();
        
        // Interactions
        setupDownloadTracking();
        
        // Optional typing effect (can be disabled)
        // setupTypingEffect();
        
        console.log('Main Home page with Fullpage Scroll initialized');
    };
    
    // ===================================
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

    // Expose to global scope if needed
    window.MainHome = {
        init,
        initFullpageScroll,
        observeCounters,
        setupParallax,
        setupTypingEffect
    };

})();
