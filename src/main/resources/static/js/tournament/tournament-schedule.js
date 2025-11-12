/**
 * Tournament Schedule Page JavaScript
 * Handles live updates and match filtering
 */

document.addEventListener('DOMContentLoaded', function() {
    // Initialize AOS
    AOS.init({
        duration: 800,
        once: true,
        offset: 50
    });

    // Auto-refresh for live matches (every 30 seconds)
    let refreshInterval;
    const liveMatches = document.querySelectorAll('.match-card.live');
    
    if (liveMatches.length > 0) {
        refreshInterval = setInterval(refreshLiveScores, 30000);
        console.log('Auto-refresh enabled for live matches');
    }

    function refreshLiveScores() {
        console.log('Refreshing live scores...');
        // In production: fetch updated scores from API
        // Example: fetch('/api/tournaments/{id}/live-scores')
        //   .then(response => response.json())
        //   .then(data => updateScores(data));
    }

    // Scroll to live matches on page load
    if (liveMatches.length > 0) {
        const firstLiveMatch = liveMatches[0];
        setTimeout(() => {
            firstLiveMatch.scrollIntoView({
                behavior: 'smooth',
                block: 'center'
            });
        }, 500);
    }

    // Match card click tracking
    const matchCards = document.querySelectorAll('.match-card');
    matchCards.forEach(card => {
        card.addEventListener('click', function(e) {
            // Don't trigger if clicking a link or button
            if (e.target.tagName === 'A' || e.target.tagName === 'BUTTON') return;
            
            const matchId = this.dataset.matchId;
            console.log('Match card clicked:', matchId);
            // In production: show match details modal or navigate to match page
        });

        // Add hover effect
        card.addEventListener('mouseenter', function() {
            this.style.borderColor = '#667eea';
        });

        card.addEventListener('mouseleave', function() {
            if (!this.classList.contains('live')) {
                this.style.borderColor = '#e9ecef';
            }
        });
    });

    // Animate match cards on scroll
    if ('IntersectionObserver' in window) {
        const cardObserver = new IntersectionObserver((entries) => {
            entries.forEach((entry, index) => {
                if (entry.isIntersecting) {
                    setTimeout(() => {
                        entry.target.style.opacity = '1';
                        entry.target.style.transform = 'translateY(0)';
                    }, index * 50);
                    cardObserver.unobserve(entry.target);
                }
            });
        }, {
            threshold: 0.1
        });

        matchCards.forEach(card => {
            card.style.opacity = '0';
            card.style.transform = 'translateY(20px)';
            card.style.transition = 'all 0.5s ease';
            cardObserver.observe(card);
        });
    }

    // Day header collapse functionality (future feature)
    const dayHeaders = document.querySelectorAll('.day-header');
    dayHeaders.forEach(header => {
        header.style.cursor = 'pointer';
        header.addEventListener('click', function() {
            const matchList = this.nextElementSibling;
            if (matchList && matchList.classList.contains('match-list')) {
                matchList.style.display = matchList.style.display === 'none' ? 'grid' : 'none';
            }
        });
    });

    // Cleanup interval on page unload
    window.addEventListener('beforeunload', function() {
        if (refreshInterval) {
            clearInterval(refreshInterval);
        }
    });

    // Live status indicator animation
    setInterval(() => {
        const liveStatus = document.querySelectorAll('.status-live');
        liveStatus.forEach(status => {
            status.style.opacity = status.style.opacity === '0.7' ? '1' : '0.7';
        });
    }, 1000);
});
