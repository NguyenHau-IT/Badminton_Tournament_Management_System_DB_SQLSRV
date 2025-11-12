/**
 * Tournament Rules Page JavaScript
 * Handles TOC navigation and document actions
 */

document.addEventListener('DOMContentLoaded', function() {
    // Initialize AOS
    AOS.init({
        duration: 800,
        once: true
    });

    // Smooth scroll for TOC links
    const tocLinks = document.querySelectorAll('.toc-link');
    tocLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            
            // Remove active class from all links
            tocLinks.forEach(l => l.classList.remove('active'));
            
            // Add active class to clicked link
            this.classList.add('active');
            
            // Scroll to section
            const targetId = this.getAttribute('href');
            const targetSection = document.querySelector(targetId);
            
            if (targetSection) {
                targetSection.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });

    // Update active TOC item on scroll
    const sections = document.querySelectorAll('.rules-section');
    
    const observerOptions = {
        root: null,
        rootMargin: '-20% 0px -70% 0px',
        threshold: 0
    };

    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const id = entry.target.getAttribute('id');
                const correspondingLink = document.querySelector(`.toc-link[href="#${id}"]`);
                
                // Remove active from all
                tocLinks.forEach(link => link.classList.remove('active'));
                
                // Add active to current
                if (correspondingLink) {
                    correspondingLink.classList.add('active');
                }
            }
        });
    }, observerOptions);

    sections.forEach(section => {
        observer.observe(section);
    });

    // Download as PDF
    const downloadPdfBtn = document.getElementById('downloadPdf');
    if (downloadPdfBtn) {
        downloadPdfBtn.addEventListener('click', function() {
            console.log('Downloading rules as PDF...');
            // In production: generate PDF server-side or use library like jsPDF
            window.print();
        });
    }

    // Print rules
    const printBtn = document.getElementById('printRules');
    if (printBtn) {
        printBtn.addEventListener('click', function() {
            window.print();
        });
    }

    // Share rules
    const shareBtn = document.getElementById('shareRules');
    if (shareBtn) {
        shareBtn.addEventListener('click', function() {
            if (navigator.share) {
                navigator.share({
                    title: document.title,
                    text: 'Luật thi đấu giải đấu cầu lông',
                    url: window.location.href
                })
                .then(() => console.log('Share successful'))
                .catch(error => console.log('Share failed:', error));
            } else {
                // Fallback: copy to clipboard
                navigator.clipboard.writeText(window.location.href)
                    .then(() => alert('Đã sao chép link vào clipboard!'))
                    .catch(err => console.error('Copy failed:', err));
            }
        });
    }

    // Highlight current section when scrolling
    window.addEventListener('scroll', function() {
        // Add shadow to sidebar when scrolling
        const sidebar = document.querySelector('.rules-sidebar');
        if (sidebar) {
            if (window.scrollY > 100) {
                sidebar.style.boxShadow = '0 4px 12px rgba(0,0,0,0.15)';
            } else {
                sidebar.style.boxShadow = '0 2px 8px rgba(0,0,0,0.1)';
            }
        }
    });

    // Animate sections on scroll
    if ('IntersectionObserver' in window) {
        const sectionObserver = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.style.opacity = '1';
                    entry.target.style.transform = 'translateY(0)';
                    sectionObserver.unobserve(entry.target);
                }
            });
        }, {
            threshold: 0.1
        });

        sections.forEach(section => {
            section.style.opacity = '0';
            section.style.transform = 'translateY(20px)';
            section.style.transition = 'all 0.6s ease';
            sectionObserver.observe(section);
        });
    }

    // Search within rules (future feature)
    const searchRulesInput = document.getElementById('searchRules');
    if (searchRulesInput) {
        searchRulesInput.addEventListener('input', function() {
            const searchTerm = this.value.toLowerCase().trim();
            highlightSearchResults(searchTerm);
        });
    }

    function highlightSearchResults(term) {
        // In production: implement full-text search with highlighting
        console.log('Searching for:', term);
    }

    // Back to top button
    const backToTopBtn = document.createElement('button');
    backToTopBtn.innerHTML = '<i class="bi bi-arrow-up"></i>';
    backToTopBtn.className = 'btn btn-primary position-fixed bottom-0 end-0 m-4';
    backToTopBtn.style.display = 'none';
    backToTopBtn.style.zIndex = '1000';
    backToTopBtn.style.width = '50px';
    backToTopBtn.style.height = '50px';
    backToTopBtn.style.borderRadius = '50%';
    document.body.appendChild(backToTopBtn);

    window.addEventListener('scroll', function() {
        if (window.scrollY > 300) {
            backToTopBtn.style.display = 'block';
        } else {
            backToTopBtn.style.display = 'none';
        }
    });

    backToTopBtn.addEventListener('click', function() {
        window.scrollTo({
            top: 0,
            behavior: 'smooth'
        });
    });

    // Track reading progress
    let readSections = new Set();
    sections.forEach(section => {
        const id = section.getAttribute('id');
        const sectionObserver = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    readSections.add(id);
                    console.log(`Section read: ${id}. Progress: ${readSections.size}/${sections.length}`);
                    // In production: send to analytics
                }
            });
        }, { threshold: 0.8 });
        
        sectionObserver.observe(section);
    });
});
