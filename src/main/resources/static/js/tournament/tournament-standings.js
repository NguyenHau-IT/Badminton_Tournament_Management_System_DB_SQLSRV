/**
 * Tournament Standings Page JavaScript
 * Handles table interactions and animations
 */

document.addEventListener('DOMContentLoaded', function() {
    // Initialize AOS
    AOS.init({
        duration: 800,
        once: true
    });

    // Animate podium places on load
    const podiumPlaces = document.querySelectorAll('.podium-place');
    podiumPlaces.forEach((place, index) => {
        place.style.opacity = '0';
        place.style.transform = 'translateY(50px)';
        
        setTimeout(() => {
            place.style.transition = 'all 0.8s ease';
            place.style.opacity = '1';
            place.style.transform = 'translateY(0)';
        }, index * 200);
    });

    // Add confetti effect for gold medal (visual enhancement)
    const goldMedal = document.querySelector('.place-1');
    if (goldMedal) {
        goldMedal.addEventListener('click', function() {
            console.log('Champion clicked! 🎉');
            // In production: trigger confetti animation
            // Example: confetti.start();
        });
    }

    // Animate table rows on scroll
    const tableRows = document.querySelectorAll('.table tbody tr');
    if ('IntersectionObserver' in window) {
        const rowObserver = new IntersectionObserver((entries) => {
            entries.forEach((entry, index) => {
                if (entry.isIntersecting) {
                    setTimeout(() => {
                        entry.target.style.opacity = '1';
                        entry.target.style.transform = 'translateX(0)';
                    }, index * 50);
                    rowObserver.unobserve(entry.target);
                }
            });
        }, {
            threshold: 0.1
        });

        tableRows.forEach(row => {
            row.style.opacity = '0';
            row.style.transform = 'translateX(-20px)';
            row.style.transition = 'all 0.5s ease';
            rowObserver.observe(row);
        });
    }

    // Tab change tracking
    const tabs = document.querySelectorAll('.nav-tabs .nav-link');
    tabs.forEach(tab => {
        tab.addEventListener('shown.bs.tab', function(e) {
            const category = e.target.textContent.trim();
            console.log('Standings category changed to:', category);
            // In production: send to analytics
        });
    });

    // Row click for player details
    tableRows.forEach(row => {
        row.style.cursor = 'pointer';
        row.addEventListener('click', function() {
            const playerName = this.querySelector('.player-name-cell')?.textContent;
            console.log('Player clicked:', playerName);
            // In production: navigate to player profile or show modal
        });
    });

    // Sort table functionality
    const tableHeaders = document.querySelectorAll('.table thead th[data-sort]');
    tableHeaders.forEach(header => {
        header.style.cursor = 'pointer';
        header.addEventListener('click', function() {
            const sortBy = this.dataset.sort;
            sortTable(sortBy);
        });
    });

    function sortTable(column) {
        console.log('Sorting by:', column);
        // In production: implement actual sorting logic
        // Example: fetch sorted data from API or sort DOM elements
    }

    // Download standings as PDF/CSV
    const downloadBtn = document.getElementById('downloadStandings');
    if (downloadBtn) {
        downloadBtn.addEventListener('click', function() {
            const format = this.dataset.format || 'pdf';
            console.log('Downloading standings as:', format);
            // In production: generate and download file
            alert('Đang tải xuống bảng xếp hạng...');
        });
    }

    // Real-time standings update (if applicable)
    let updateInterval;
    const isLiveTournament = document.querySelector('[data-live="true"]');
    
    if (isLiveTournament) {
        updateInterval = setInterval(updateStandings, 30000); // Update every 30 seconds
        console.log('Live standings updates enabled');
    }

    function updateStandings() {
        console.log('Updating standings...');
        // In production: fetch latest standings from API
        // Example: fetch('/api/tournaments/{id}/standings')
        //   .then(response => response.json())
        //   .then(data => refreshStandingsTable(data));
    }

    // Cleanup interval on page unload
    window.addEventListener('beforeunload', function() {
        if (updateInterval) {
            clearInterval(updateInterval);
        }
    });

    // Highlight current user's rank (if logged in)
    const currentUserId = document.querySelector('[data-current-user]')?.dataset.currentUser;
    if (currentUserId) {
        const userRow = document.querySelector(`tr[data-user-id="${currentUserId}"]`);
        if (userRow) {
            userRow.style.background = 'rgba(102, 126, 234, 0.1)';
            userRow.style.borderLeft = '4px solid #667eea';
            
            // Scroll to user's position
            setTimeout(() => {
                userRow.scrollIntoView({
                    behavior: 'smooth',
                    block: 'center'
                });
            }, 1000);
        }
    }
});
