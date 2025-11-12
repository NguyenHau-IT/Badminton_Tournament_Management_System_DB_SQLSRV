/**
 * Tournament Detail Page JavaScript
 * Handles tabs, smooth scrolling, and animations
 */

document.addEventListener('DOMContentLoaded', function() {
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

    // Share functionality (future feature)
    const shareButtons = document.querySelectorAll('[data-share]');
    shareButtons.forEach(btn => {
        btn.addEventListener('click', function() {
            const platform = this.dataset.share;
            const url = window.location.href;
            const title = document.querySelector('.hero-title').textContent;
            console.log(`Share to ${platform}:`, { url, title });
            // In production: implement actual sharing
        });
    });
});
