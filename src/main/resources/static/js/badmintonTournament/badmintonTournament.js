// Dropdown menu for tournament status
document.addEventListener('DOMContentLoaded', function () {
    console.log('Tournament dropdown script loaded');
    const dropdownBtn = document.getElementById('tournamentDropdownBtn');
    const dropdownMenu = document.getElementById('tournamentDropdownMenu');
    const dropdownLabel = document.getElementById('dropdownLabel');
    const tabContents = document.querySelectorAll('.tournament-tab-content');
    // Hiển thị mặc định tất cả
    tabContents.forEach(c => c.style.display = '');
    // Dropdown toggle
    if (dropdownBtn && dropdownMenu) {
        dropdownBtn.addEventListener('click', function (e) {
            e.stopPropagation();
            const expanded = dropdownBtn.getAttribute('aria-expanded') === 'true';
            dropdownBtn.setAttribute('aria-expanded', !expanded);
            dropdownMenu.style.display = expanded ? 'none' : 'block';
        });
        // Dropdown item click
        dropdownMenu.querySelectorAll('a').forEach(item => {
            item.addEventListener('click', function (e) {
                e.preventDefault();
                const tab = this.getAttribute('data-tab');
                // Đổi label
                dropdownLabel.textContent = this.textContent;
                // Ẩn/hiện các tab content
                if (tab === 'all') {
                    tabContents.forEach(c => c.style.display = '');
                } else {
                    tabContents.forEach(c => {
                        if (c.id === 'tab-' + tab) c.style.display = '';
                        else c.style.display = 'none';
                    });
                }
                dropdownMenu.style.display = 'none';
                dropdownBtn.setAttribute('aria-expanded', 'false');
            });
        });
        // Đóng dropdown khi click ngoài
        document.addEventListener('click', function () {
            dropdownMenu.style.display = 'none';
            dropdownBtn.setAttribute('aria-expanded', 'false');
        });
    }
});

// Dropdown for badminton intro
const introBtn = document.getElementById('introToggleBtn');
const introContent = document.getElementById('introContent');
const introArrow = document.getElementById('introArrow');
if (introBtn && introContent && introArrow) {
    introBtn.addEventListener('click', function () {
        const expanded = introBtn.getAttribute('aria-expanded') === 'true';
        introBtn.setAttribute('aria-expanded', !expanded);
        introContent.classList.toggle('show');
        introContent.classList.toggle('hide');
        introArrow.textContent = expanded ? '►' : '▼';
    });
}