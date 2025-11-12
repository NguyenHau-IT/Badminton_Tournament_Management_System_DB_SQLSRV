/**
 * Tournament Participants Page JavaScript
 * Search, filter, and participant interactions
 */

document.addEventListener('DOMContentLoaded', function() {
    // Initialize AOS
    AOS.init({
        duration: 800,
        once: true
    });

    let allParticipants = [];
    let filteredParticipants = [];

    // Load participants data
    const participantCards = document.querySelectorAll('.participant-card');
    participantCards.forEach(card => {
        allParticipants.push({
            element: card,
            name: card.querySelector('.participant-name')?.textContent.toLowerCase(),
            country: card.querySelector('.participant-country')?.textContent.toLowerCase(),
            category: card.querySelector('.category-badge')?.textContent.toLowerCase()
        });
    });

    filteredParticipants = [...allParticipants];

    // Search functionality
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.addEventListener('input', function() {
            const searchTerm = this.value.toLowerCase().trim();
            filterParticipants(searchTerm);
        });

        // Clear search button
        const clearBtn = document.createElement('button');
        clearBtn.innerHTML = '<i class="bi bi-x-circle"></i>';
        clearBtn.className = 'btn btn-link position-absolute end-0 top-50 translate-middle-y';
        clearBtn.style.display = 'none';
        searchInput.parentElement.appendChild(clearBtn);

        searchInput.addEventListener('input', function() {
            clearBtn.style.display = this.value ? 'block' : 'none';
        });

        clearBtn.addEventListener('click', function() {
            searchInput.value = '';
            clearBtn.style.display = 'none';
            filterParticipants('');
        });
    }

    function filterParticipants(searchTerm) {
        let visibleCount = 0;

        allParticipants.forEach(participant => {
            const matchesSearch = !searchTerm || 
                participant.name.includes(searchTerm) || 
                participant.country.includes(searchTerm);

            const matchesCategory = !activeCategory || 
                participant.category.includes(activeCategory.toLowerCase());

            if (matchesSearch && matchesCategory) {
                participant.element.style.display = 'block';
                visibleCount++;
            } else {
                participant.element.style.display = 'none';
            }
        });

        // Show/hide empty state
        updateEmptyState(visibleCount);

        // Update results count
        updateResultsCount(visibleCount);
    }

    // Category filter buttons
    let activeCategory = '';
    const filterButtons = document.querySelectorAll('.filter-btn');
    
    filterButtons.forEach(btn => {
        btn.addEventListener('click', function() {
            // Toggle active state
            filterButtons.forEach(b => b.classList.remove('active'));
            this.classList.add('active');

            // Get category
            activeCategory = this.dataset.category || '';

            // Apply filter
            const searchTerm = searchInput?.value.toLowerCase().trim() || '';
            filterParticipants(searchTerm);

            console.log('Filter applied:', activeCategory || 'All');
        });
    });

    function updateEmptyState(count) {
        let emptyState = document.querySelector('.no-participants');
        if (count === 0 && !emptyState) {
            emptyState = document.createElement('div');
            emptyState.className = 'no-participants';
            emptyState.innerHTML = `
                <i class="bi bi-person-x"></i>
                <h3>Không tìm thấy người chơi</h3>
                <p>Thử tìm kiếm với từ khóa khác hoặc thay đổi bộ lọc</p>
            `;
            document.querySelector('.participants-grid').parentElement.appendChild(emptyState);
        } else if (count > 0 && emptyState) {
            emptyState.remove();
        }
    }

    function updateResultsCount(count) {
        let countElement = document.getElementById('resultsCount');
        if (!countElement) {
            countElement = document.createElement('div');
            countElement.id = 'resultsCount';
            countElement.className = 'text-muted mb-3';
            document.querySelector('.participants-grid').before(countElement);
        }
        countElement.textContent = `Hiển thị ${count} / ${allParticipants.length} người chơi`;
    }

    // Participant card click
    participantCards.forEach(card => {
        card.addEventListener('click', function(e) {
            // Don't trigger if clicking a button
            if (e.target.closest('.btn')) return;

            const participantId = this.dataset.participantId;
            console.log('Participant clicked:', participantId);
            // In production: navigate to participant profile or show modal
        });
    });

    // View profile buttons
    const profileButtons = document.querySelectorAll('.btn-view-profile');
    profileButtons.forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.stopPropagation();
            const participantId = this.closest('.participant-card').dataset.participantId;
            console.log('View profile:', participantId);
            // In production: navigate to profile page
        });
    });

    // View matches buttons
    const matchesButtons = document.querySelectorAll('.btn-view-matches');
    matchesButtons.forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.stopPropagation();
            const participantId = this.closest('.participant-card').dataset.participantId;
            console.log('View matches:', participantId);
            // In production: navigate to matches page or show modal
        });
    });

    // Animate cards on scroll
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

        participantCards.forEach(card => {
            card.style.opacity = '0';
            card.style.transform = 'translateY(20px)';
            card.style.transition = 'all 0.5s ease';
            cardObserver.observe(card);
        });
    }

    // Export participants list
    const exportBtn = document.getElementById('exportParticipants');
    if (exportBtn) {
        exportBtn.addEventListener('click', function() {
            console.log('Exporting participants...');
            // In production: generate and download CSV/PDF
            alert('Đang xuất danh sách người chơi...');
        });
    }

    // Initialize results count
    updateResultsCount(allParticipants.length);
});
