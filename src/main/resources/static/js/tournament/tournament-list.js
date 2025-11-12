/**
 * Tournament List Page JavaScript
 * Handles animations and card interactions
 */

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
});
