/**
 * Tournament Platform Enhanced JavaScript
 * Handles interactive features for ranking, schedule, and stats sections
 */

// ==============================================
// Ranking Section
// ==============================================
document.addEventListener('DOMContentLoaded', function() {
    // Search functionality
    const searchInput = document.getElementById('searchPlayer');
    if (searchInput) {
        searchInput.addEventListener('input', function(e) {
            const searchTerm = e.target.value.toLowerCase();
            const playerRows = document.querySelectorAll('.rank-item');
            
            playerRows.forEach(row => {
                const playerName = row.querySelector('.player-name strong').textContent.toLowerCase();
                const club = row.querySelector('.player-club').textContent.toLowerCase();
                
                if (playerName.includes(searchTerm) || club.includes(searchTerm)) {
                    row.style.display = '';
                } else {
                    row.style.display = 'none';
                }
            });
        });
    }

    // Filter by tournament
    const filterTournament = document.getElementById('filterTournament');
    if (filterTournament) {
        filterTournament.addEventListener('change', function() {
            console.log('Filter by tournament:', this.value);
            // TODO: Implement tournament filtering
        });
    }

    // Filter by club
    const filterClub = document.getElementById('filterClub');
    if (filterClub) {
        filterClub.addEventListener('change', function() {
            console.log('Filter by club:', this.value);
            // TODO: Implement club filtering
        });
    }
});

// ==============================================
// Schedule Section
// ==============================================
document.addEventListener('DOMContentLoaded', function() {
    // View toggle (List vs Calendar)
    const listViewBtn = document.getElementById('listView');
    const calendarViewBtn = document.getElementById('calendarView');
    const listViewContainer = document.getElementById('scheduleListView');
    const calendarViewContainer = document.getElementById('scheduleCalendarView');

    if (listViewBtn && calendarViewBtn) {
        listViewBtn.addEventListener('click', function() {
            listViewBtn.classList.add('active');
            calendarViewBtn.classList.remove('active');
            listViewContainer.classList.remove('d-none');
            calendarViewContainer.classList.add('d-none');
        });

        calendarViewBtn.addEventListener('click', function() {
            calendarViewBtn.classList.add('active');
            listViewBtn.classList.remove('active');
            calendarViewContainer.classList.remove('d-none');
            listViewContainer.classList.add('d-none');
        });
    }

    // Date filter
    const filterDate = document.getElementById('filterDate');
    if (filterDate) {
        filterDate.addEventListener('change', function() {
            console.log('Filter by date:', this.value);
            // TODO: Implement date filtering
        });
    }

    // Court filter
    const filterCourt = document.getElementById('filterCourt');
    if (filterCourt) {
        filterCourt.addEventListener('change', function() {
            console.log('Filter by court:', this.value);
            // TODO: Implement court filtering
        });
    }

    // Round filter
    const filterRound = document.getElementById('filterRound');
    if (filterRound) {
        filterRound.addEventListener('change', function() {
            console.log('Filter by round:', this.value);
            // TODO: Implement round filtering
        });
    }

    // Match card hover effects
    const matchCards = document.querySelectorAll('.match-card');
    matchCards.forEach(card => {
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-5px)';
        });
        card.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0)';
        });
    });
});

// ==============================================
// Stats Section - Chart.js Integration
// ==============================================
document.addEventListener('DOMContentLoaded', function() {
    // Check if Chart.js is loaded
    if (typeof Chart === 'undefined') {
        console.warn('Chart.js not loaded. Stats charts will not be rendered.');
        return;
    }

    // Pie Chart - Category Distribution
    const categoryChartCanvas = document.getElementById('categoryChart');
    if (categoryChartCanvas) {
        const categoryCtx = categoryChartCanvas.getContext('2d');
        new Chart(categoryCtx, {
            type: 'doughnut',
            data: {
                labels: ['Nam Đơn', 'Nữ Đơn', 'Nam Đôi', 'Nữ Đôi', 'Đôi Nam Nữ'],
                datasets: [{
                    data: [85, 62, 45, 32, 32],
                    backgroundColor: [
                        '#0d47a1',
                        '#e53935',
                        '#ffd600',
                        '#00c853',
                        '#7b1fa2'
                    ],
                    borderWidth: 2,
                    borderColor: '#fff'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    legend: {
                        display: false
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                return context.label + ': ' + context.parsed + ' VĐV';
                            }
                        }
                    }
                }
            }
        });
    }

    // Bar Chart - Top Clubs
    const clubsChartCanvas = document.getElementById('clubsChart');
    if (clubsChartCanvas) {
        const clubsCtx = clubsChartCanvas.getContext('2d');
        new Chart(clubsCtx, {
            type: 'bar',
            data: {
                labels: ['CLB Thiên Long', 'CLB Phượng Hoàng', 'CLB Victory', 'CLB Rồng Xanh', 'CLB Bão Tố', 'CLB Sao Mai', 'CLB Hùng Vương', 'CLB Đại Việt', 'CLB Tiến Phong', 'CLB Ánh Sáng'],
                datasets: [{
                    label: 'Số VĐV',
                    data: [35, 28, 25, 22, 20, 18, 16, 14, 12, 10],
                    backgroundColor: 'rgba(13, 71, 161, 0.8)',
                    borderColor: '#0d47a1',
                    borderWidth: 1,
                    borderRadius: 8
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    legend: {
                        display: false
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            stepSize: 5
                        }
                    }
                }
            }
        });
    }

    // Line Chart - Growth History
    const growthChartCanvas = document.getElementById('growthChart');
    if (growthChartCanvas) {
        const growthCtx = growthChartCanvas.getContext('2d');
        new Chart(growthCtx, {
            type: 'line',
            data: {
                labels: ['2020', '2021', '2022', '2023', '2024', '2025'],
                datasets: [{
                    label: 'Số VĐV',
                    data: [120, 145, 180, 210, 235, 256],
                    borderColor: '#0d47a1',
                    backgroundColor: 'rgba(13, 71, 161, 0.1)',
                    tension: 0.4,
                    fill: true,
                    pointRadius: 6,
                    pointHoverRadius: 8,
                    pointBackgroundColor: '#0d47a1',
                    pointBorderColor: '#fff',
                    pointBorderWidth: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    legend: {
                        display: false
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            stepSize: 50
                        }
                    }
                }
            }
        });
    }

    // Chart filter buttons
    const chartFilterButtons = document.querySelectorAll('.chart-filters .btn');
    chartFilterButtons.forEach(btn => {
        btn.addEventListener('click', function() {
            chartFilterButtons.forEach(b => b.classList.remove('active'));
            this.classList.add('active');
            
            // TODO: Update growth chart based on selected filter
            console.log('Chart filter changed:', this.textContent);
        });
    });
});

// ==============================================
// Animated Counter for Stats
// ==============================================
function animateCounter(element, target, duration = 2000) {
    let current = 0;
    const increment = target / (duration / 16);
    
    const updateCounter = () => {
        current += increment;
        if (current < target) {
            element.textContent = Math.floor(current);
            requestAnimationFrame(updateCounter);
        } else {
            element.textContent = target;
        }
    };
    
    updateCounter();
}

// Intersection Observer for animations
const observeElements = (selector, callback) => {
    const elements = document.querySelectorAll(selector);
    
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                callback(entry.target);
                observer.unobserve(entry.target);
            }
        });
    }, {
        threshold: 0.5
    });
    
    elements.forEach(el => observer.observe(el));
};

// Initialize animations when elements come into view
document.addEventListener('DOMContentLoaded', function() {
    // Animate stats numbers
    observeElements('.stats-number', (element) => {
        const target = parseInt(element.getAttribute('data-target'));
        animateCounter(element, target);
    });

    // Fade in cards
    observeElements('.stats-card, .chart-card, .stats-detail-card', (element) => {
        element.style.opacity = '0';
        element.style.transform = 'translateY(30px)';
        
        setTimeout(() => {
            element.style.transition = 'all 0.6s ease';
            element.style.opacity = '1';
            element.style.transform = 'translateY(0)';
        }, 100);
    });
});

// ==============================================
// Utility Functions
// ==============================================

// Format number with thousand separator
function formatNumber(num) {
    return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}

// Format date
function formatDate(date) {
    const options = { weekday: 'long', year: 'numeric', month: 'numeric', day: 'numeric' };
    return new Date(date).toLocaleDateString('vi-VN', options);
}

// Debounce function for search
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Toast notification (if Bootstrap is available)
function showToast(message, type = 'info') {
    if (typeof bootstrap !== 'undefined') {
        const toastContainer = document.querySelector('.toast-container') || createToastContainer();
        const toastHTML = `
            <div class="toast align-items-center text-white bg-${type} border-0" role="alert" aria-live="assertive" aria-atomic="true">
                <div class="d-flex">
                    <div class="toast-body">${message}</div>
                    <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
                </div>
            </div>
        `;
        toastContainer.insertAdjacentHTML('beforeend', toastHTML);
        const toastElement = toastContainer.lastElementChild;
        const toast = new bootstrap.Toast(toastElement);
        toast.show();
        
        toastElement.addEventListener('hidden.bs.toast', () => {
            toastElement.remove();
        });
    } else {
        console.log(message);
    }
}

function createToastContainer() {
    const container = document.createElement('div');
    container.className = 'toast-container position-fixed bottom-0 end-0 p-3';
    document.body.appendChild(container);
    return container;
}

// Export for use in other scripts
window.TournamentApp = {
    animateCounter,
    formatNumber,
    formatDate,
    debounce,
    showToast
};
