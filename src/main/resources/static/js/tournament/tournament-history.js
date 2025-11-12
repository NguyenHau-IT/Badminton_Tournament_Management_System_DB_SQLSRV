/**
 * Tournament History Page JavaScript
 * Timeline animations and filtering
 */

document.addEventListener('DOMContentLoaded', function() {
    // Initialize AOS
    AOS.init({
        duration: 800,
        once: true
    });

    // Year filter functionality
    const yearButtons = document.querySelectorAll('.year-btn');
    let activeYear = 'all';

    yearButtons.forEach(btn => {
        btn.addEventListener('click', function() {
            // Remove active from all buttons
            yearButtons.forEach(b => b.classList.remove('active'));
            
            // Add active to clicked button
            this.classList.add('active');
            
            // Get selected year
            activeYear = this.dataset.year;
            
            // Filter timeline items
            filterByYear(activeYear);
            
            console.log('Filtering by year:', activeYear);
        });
    });

    function filterByYear(year) {
        const timelineItems = document.querySelectorAll('.timeline-item');
        
        timelineItems.forEach(item => {
            const itemYear = item.dataset.year;
            
            if (year === 'all' || itemYear === year) {
                item.style.display = 'grid';
                // Animate item appearance
                setTimeout(() => {
                    item.style.opacity = '1';
                    item.style.transform = 'translateY(0)';
                }, 100);
            } else {
                item.style.opacity = '0';
                item.style.transform = 'translateY(20px)';
                setTimeout(() => {
                    item.style.display = 'none';
                }, 300);
            }
        });
    }

    // Animate timeline items on scroll
    const timelineItems = document.querySelectorAll('.timeline-item');
    
    if ('IntersectionObserver' in window) {
        const itemObserver = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.style.opacity = '1';
                    entry.target.style.transform = 'translateY(0)';
                    itemObserver.unobserve(entry.target);
                }
            });
        }, {
            threshold: 0.2
        });

        timelineItems.forEach(item => {
            item.style.opacity = '0';
            item.style.transform = 'translateY(30px)';
            item.style.transition = 'all 0.6s ease';
            itemObserver.observe(item);
        });
    }

    // Edition card click
    const editionCards = document.querySelectorAll('.edition-card');
    editionCards.forEach(card => {
        card.addEventListener('click', function() {
            const editionId = this.dataset.editionId;
            console.log('Edition clicked:', editionId);
            // In production: navigate to detailed edition page or show modal
        });
    });

    // Champion card click
    const championCards = document.querySelectorAll('.champion-card');
    championCards.forEach(card => {
        card.addEventListener('click', function() {
            const championId = this.dataset.championId;
            console.log('Champion clicked:', championId);
            // In production: navigate to player profile
        });
    });

    // Timeline dot animation
    const timelineDots = document.querySelectorAll('.timeline-dot');
    timelineDots.forEach(dot => {
        dot.addEventListener('mouseenter', function() {
            this.style.background = 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)';
            this.querySelector('.timeline-year').style.color = 'white';
        });

        dot.addEventListener('mouseleave', function() {
            this.style.background = 'white';
            this.querySelector('.timeline-year').style.color = '#667eea';
        });
    });

    // Export history data
    const exportBtn = document.getElementById('exportHistory');
    if (exportBtn) {
        exportBtn.addEventListener('click', function() {
            console.log('Exporting tournament history...');
            // In production: generate and download PDF/CSV
            alert('Đang xuất lịch sử giải đấu...');
        });
    }

    // Search in history
    const searchInput = document.getElementById('searchHistory');
    if (searchInput) {
        searchInput.addEventListener('input', function() {
            const searchTerm = this.value.toLowerCase().trim();
            searchHistory(searchTerm);
        });
    }

    function searchHistory(term) {
        if (!term) {
            // Show all items
            timelineItems.forEach(item => {
                item.style.display = 'grid';
            });
            return;
        }

        timelineItems.forEach(item => {
            const text = item.textContent.toLowerCase();
            if (text.includes(term)) {
                item.style.display = 'grid';
            } else {
                item.style.display = 'none';
            }
        });

        console.log('Searching for:', term);
    }

    // Compare editions (future feature)
    const compareBtn = document.getElementById('compareEditions');
    if (compareBtn) {
        compareBtn.addEventListener('click', function() {
            console.log('Opening edition comparison tool...');
            // In production: show comparison modal
        });
    }

    // Stats animation on scroll
    const statNumbers = document.querySelectorAll('.stat-number');
    
    if ('IntersectionObserver' in window) {
        const statsObserver = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    const target = parseInt(entry.target.textContent);
                    animateCounter(entry.target, target);
                    statsObserver.unobserve(entry.target);
                }
            });
        }, {
            threshold: 0.5
        });

        statNumbers.forEach(stat => {
            statsObserver.observe(stat);
        });
    }

    function animateCounter(element, target) {
        let current = 0;
        const increment = target / 50;
        const duration = 1500;
        const stepTime = duration / 50;

        const timer = setInterval(() => {
            current += increment;
            if (current >= target) {
                element.textContent = target;
                clearInterval(timer);
            } else {
                element.textContent = Math.floor(current);
            }
        }, stepTime);
    }

    // Trophy animation
    const trophyIcons = document.querySelectorAll('.trophy-icon');
    setInterval(() => {
        trophyIcons.forEach(icon => {
            icon.style.transform = 'rotate(15deg)';
            setTimeout(() => {
                icon.style.transform = 'rotate(-15deg)';
                setTimeout(() => {
                    icon.style.transform = 'rotate(0deg)';
                }, 200);
            }, 200);
        });
    }, 3000);

    // Initialize with all years
    filterByYear('all');
});
