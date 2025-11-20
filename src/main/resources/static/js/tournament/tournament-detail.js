/**
 * Tournament Detail Page JavaScript
 * Handles countdown, social share, parallax, tabs, and animations
 */

// ========== GLOBAL FUNCTIONS (Must be global for onclick attributes) ==========

/**
 * Share on Facebook
 */
window.shareFacebook = function() {
    const url = encodeURIComponent(window.location.href);
    const title = document.querySelector('.hero-title')?.textContent || 'Giải đấu cầu lông';
    window.open(
        `https://www.facebook.com/sharer/sharer.php?u=${url}`,
        'facebook-share',
        'width=600,height=400'
    );
};

/**
 * Share on Twitter
 */
window.shareTwitter = function() {
    const url = encodeURIComponent(window.location.href);
    const title = document.querySelector('.hero-title')?.textContent || 'Giải đấu cầu lông';
    const text = encodeURIComponent(`${title} - Đăng ký ngay!`);
    window.open(
        `https://twitter.com/intent/tweet?url=${url}&text=${text}`,
        'twitter-share',
        'width=600,height=400'
    );
};

/**
 * Share on Zalo
 */
window.shareZalo = function() {
    const url = encodeURIComponent(window.location.href);
    window.open(
        `https://sp.zalo.me/share_inline?url=${url}`,
        'zalo-share',
        'width=600,height=400'
    );
};

/**
 * Copy link to clipboard
 */
window.copyLink = function() {
    const url = window.location.href;
    
    // Copy to clipboard
    if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(url).then(() => {
            showCopySuccess();
        }).catch(err => {
            console.error('Failed to copy:', err);
            // Fallback method
            fallbackCopyTextToClipboard(url);
        });
    } else {
        // Fallback for older browsers
        fallbackCopyTextToClipboard(url);
    }
};

/**
 * Fallback copy method for older browsers
 */
function fallbackCopyTextToClipboard(text) {
    const textArea = document.createElement('textarea');
    textArea.value = text;
    textArea.style.position = 'fixed';
    textArea.style.left = '-999999px';
    document.body.appendChild(textArea);
    textArea.focus();
    textArea.select();
    
    try {
        const successful = document.execCommand('copy');
        if (successful) {
            showCopySuccess();
        }
    } catch (err) {
        console.error('Fallback copy failed:', err);
    }
    
    document.body.removeChild(textArea);
}

/**
 * Show copy success message
 */
function showCopySuccess() {
    const successDiv = document.createElement('div');
    successDiv.className = 'copy-success';
    successDiv.innerHTML = '<i class="bi bi-check-circle"></i> Đã sao chép liên kết!';
    
    const shareCard = document.querySelector('.social-share-card');
    if (shareCard) {
        shareCard.appendChild(successDiv);
        
        // Remove after 3 seconds
        setTimeout(() => {
            successDiv.remove();
        }, 3000);
    }
}

// ========== DOM READY INITIALIZATION ==========

document.addEventListener('DOMContentLoaded', function() {
    // Initialize countdown timer
    initCountdownTimer();
    
    // Initialize parallax effect
    initParallaxEffect();
    // Initialize AOS
    AOS.init({
        duration: 800,
        once: true
    });

    // Smooth scroll for anchor links
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            const target = document.querySelector(this.getAttribute('href'));
            if (target) {
                target.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });

    // Tab change tracking
    const tabs = document.querySelectorAll('.nav-tabs .nav-link');
    tabs.forEach(tab => {
        tab.addEventListener('shown.bs.tab', function(e) {
            const tabName = e.target.textContent.trim();
            console.log('Tab changed to:', tabName);
            // In production: send to analytics
        });
    });

    // Registration button click tracking
    const registerBtn = document.querySelector('a[href*="/register"]');
    if (registerBtn) {
        registerBtn.addEventListener('click', function(e) {
            const tournamentId = this.href.split('/').slice(-2)[0];
            console.log('Registration started for tournament:', tournamentId);
            // In production: send to analytics
        });
    }

    // Info card animations on scroll
    const infoCards = document.querySelectorAll('.info-card');
    if ('IntersectionObserver' in window) {
        const cardObserver = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.style.opacity = '0';
                    entry.target.style.transform = 'translateY(20px)';
                    setTimeout(() => {
                        entry.target.style.transition = 'all 0.5s ease';
                        entry.target.style.opacity = '1';
                        entry.target.style.transform = 'translateY(0)';
                    }, 100);
                    cardObserver.unobserve(entry.target);
                }
            });
        });

        infoCards.forEach(card => cardObserver.observe(card));
    }

});

// ========== COUNTDOWN TIMER ==========

/**
 * Initialize countdown timer
 */
function initCountdownTimer() {
    const countdownElement = document.getElementById('countdownTimer');
    if (!countdownElement) return;

    const deadline = countdownElement.dataset.deadline;
    if (!deadline) return;

    // Parse deadline (format: yyyy-MM-ddTHH:mm:ss)
    const deadlineDate = new Date(deadline);
    
    // Update countdown every second
    const countdownInterval = setInterval(() => {
        const now = new Date().getTime();
        const distance = deadlineDate - now;

        // Check if expired
        if (distance < 0) {
            clearInterval(countdownInterval);
            countdownElement.innerHTML = '<div class="countdown-label text-center"><i class="bi bi-exclamation-circle"></i> Đã hết hạn đăng ký</div>';
            return;
        }

        // Calculate time units
        const days = Math.floor(distance / (1000 * 60 * 60 * 24));
        const hours = Math.floor((distance % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
        const minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60));
        const seconds = Math.floor((distance % (1000 * 60)) / 1000);

        // Update DOM
        updateCountdownValue('days', days);
        updateCountdownValue('hours', hours);
        updateCountdownValue('minutes', minutes);
        updateCountdownValue('seconds', seconds);
    }, 1000);
}

/**
 * Update countdown value with animation
 */
function updateCountdownValue(elementId, value) {
    const element = document.getElementById(elementId);
    if (!element) return;

    const formattedValue = value.toString().padStart(2, '0');
    
    // Only update if value changed (prevents unnecessary DOM updates)
    if (element.textContent !== formattedValue) {
        element.textContent = formattedValue;
        
        // Add flash animation
        element.style.animation = 'none';
        setTimeout(() => {
            element.style.animation = 'flash 0.3s ease';
        }, 10);
    }
}

// ========== PARALLAX EFFECT ==========

/**
 * Initialize parallax scrolling effect
 */
function initParallaxEffect() {
    const parallaxBg = document.querySelector('.parallax-bg');
    if (!parallaxBg) return;

    let ticking = false;

    window.addEventListener('scroll', () => {
        if (!ticking) {
            window.requestAnimationFrame(() => {
                const scrolled = window.pageYOffset;
                const heroHeight = document.querySelector('.hero-section')?.offsetHeight || 600;
                
                // Only apply parallax if within hero section
                if (scrolled <= heroHeight) {
                    const parallaxSpeed = 0.5;
                    parallaxBg.style.transform = `translate3d(0, ${scrolled * parallaxSpeed}px, 0)`;
                }
                
                ticking = false;
            });
            
            ticking = true;
        }
    });
}

// ========== FLASH ANIMATION (for countdown updates) ==========

// Add CSS animation dynamically
const style = document.createElement('style');
style.textContent = `
    @keyframes flash {
        0%, 100% {
            opacity: 1;
        }
        50% {
            opacity: 0.6;
            transform: scale(1.1);
        }
    }
`;
document.head.appendChild(style);
