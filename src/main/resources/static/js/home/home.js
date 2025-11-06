(function () {
    const slides = Array.from(document.querySelectorAll('.slide'));
    if (!slides.length) return;

    let currentIndex = 0;
    let animating = false;
    let touchStartY = 0;
    const enableWheel = () => window.innerWidth > 768;

    const updateSlides = (index) => {
        if (index === currentIndex || index < 0 || index >= slides.length) return;
        animating = true;
        currentIndex = index;
        slides.forEach((slide, idx) => slide.classList.toggle('active', idx === currentIndex));
        slides[currentIndex].scrollIntoView({ behavior: 'smooth' });
        document.body.classList.toggle('footer-visible', currentIndex === slides.length - 1);
        setTimeout(() => {
            animating = false;
        }, 1000);
    };

    const handleWheel = (event) => {
        if (!enableWheel()) return;
        event.preventDefault();
        if (animating) return;
        const delta = event.deltaY;
        if (Math.abs(delta) < 15) return;
        const nextIndex = delta > 0 ? currentIndex + 1 : currentIndex - 1;
        updateSlides(Math.max(0, Math.min(slides.length - 1, nextIndex)));
    };

    const handleKey = (event) => {
        if (!enableWheel() || animating) return;
        if (["ArrowDown", "PageDown", " ", "Enter"].includes(event.key)) {
            event.preventDefault();
            updateSlides(Math.min(slides.length - 1, currentIndex + 1));
        } else if (["ArrowUp", "PageUp"].includes(event.key)) {
            event.preventDefault();
            updateSlides(Math.max(0, currentIndex - 1));
        }
    };

    const handleTouchStart = (event) => {
        if (!enableWheel()) return;
        touchStartY = event.changedTouches[0].clientY;
    };

    const handleTouchEnd = (event) => {
        if (!enableWheel() || animating) return;
        const endY = event.changedTouches[0].clientY;
        const diff = touchStartY - endY;
        if (Math.abs(diff) < 40) return;
        updateSlides(diff > 0 ? currentIndex + 1 : currentIndex - 1);
    };

    document.addEventListener('wheel', handleWheel, { passive: false });
    document.addEventListener('keydown', handleKey, { passive: false });
    document.addEventListener('touchstart', handleTouchStart, { passive: true });
    document.addEventListener('touchend', handleTouchEnd, { passive: true });

    document.querySelectorAll('.landing-header nav a').forEach((link, idx) => {
        link.addEventListener('click', (event) => {
            event.preventDefault();
            updateSlides(idx);
        });
    });

    // Support anchors to slides (e.g. <a href="#slide-2">)
    document.querySelectorAll('a[href^="#slide-"]').forEach((link) => {
        link.addEventListener('click', (event) => {
            event.preventDefault();
            const targetId = link.getAttribute('href').slice(1); // remove '#'
            const idx = slides.findIndex(s => s.id === targetId);
            if (idx >= 0) updateSlides(idx);
        });
    });

    slides[0].classList.add('active');
    slides[0].scrollIntoView();
    document.body.classList.toggle('footer-visible', false);
})();