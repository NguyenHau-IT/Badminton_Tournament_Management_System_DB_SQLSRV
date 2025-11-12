/**
 * Tournament Calendar Page JavaScript
 * FullCalendar initialization and event handling
 */

document.addEventListener('DOMContentLoaded', function() {
    // Initialize AOS
    AOS.init({
        duration: 800,
        once: true
    });

    // Initialize FullCalendar
    const calendarEl = document.getElementById('calendar');
    
    if (calendarEl) {
        const calendar = new FullCalendar.Calendar(calendarEl, {
            initialView: 'dayGridMonth',
            locale: 'vi',
            headerToolbar: {
                left: 'prev,next today',
                center: 'title',
                right: 'dayGridMonth,dayGridWeek,listWeek'
            },
            buttonText: {
                today: 'Hôm nay',
                month: 'Tháng',
                week: 'Tuần',
                list: 'Danh sách'
            },
            events: [
                {
                    title: 'Giải Cầu Lông Mở Rộng TP.HCM 2024',
                    start: '2024-03-15',
                    end: '2024-03-20',
                    color: '#dc3545',
                    extendedProps: {
                        status: 'ongoing',
                        location: 'Nhà thi đấu Phú Thọ, TP.HCM'
                    }
                },
                {
                    title: 'Giải Vô Địch Cầu Lông Quốc Gia',
                    start: '2024-04-01',
                    end: '2024-04-07',
                    color: '#28a745',
                    extendedProps: {
                        status: 'registration',
                        location: 'Cung thể thao Quần Ngựa, Hà Nội'
                    }
                },
                {
                    title: 'Giải Cầu Lông Câu Lạc Bộ Miền Bắc',
                    start: '2024-05-10',
                    end: '2024-05-15',
                    color: '#007bff',
                    extendedProps: {
                        status: 'upcoming',
                        location: 'Nhà thi đấu Trịnh Hoài Đức, Hà Nội'
                    }
                },
                {
                    title: 'Giải Cầu Lông Thanh Niên Toàn Quốc',
                    start: '2024-06-20',
                    end: '2024-06-25',
                    color: '#007bff',
                    extendedProps: {
                        status: 'upcoming',
                        location: 'Nhà thi đấu Võ Trường Toản, Đà Nẵng'
                    }
                },
                {
                    title: 'Giải Cầu Lông Mùa Xuân 2024',
                    start: '2024-02-01',
                    end: '2024-02-05',
                    color: '#6c757d',
                    extendedProps: {
                        status: 'completed',
                        location: 'Nhà thi đấu Phú Thọ, TP.HCM'
                    }
                }
            ],
            eventClick: function(info) {
                showEventDetails(info.event);
            },
            eventMouseEnter: function(info) {
                info.el.style.cursor = 'pointer';
                info.el.style.opacity = '0.8';
            },
            eventMouseLeave: function(info) {
                info.el.style.opacity = '1';
            },
            dayCellDidMount: function(info) {
                // Add custom styling to weekend days
                if (info.date.getDay() === 0 || info.date.getDay() === 6) {
                    info.el.style.backgroundColor = 'rgba(0, 0, 0, 0.02)';
                }
            }
        });

        calendar.render();

        // View change tracking
        calendar.on('viewDidMount', function(info) {
            console.log('Calendar view changed to:', info.view.type);
            // In production: send to analytics
        });
    }

    // Show event details in modal (future feature)
    function showEventDetails(event) {
        const details = {
            title: event.title,
            start: event.start,
            end: event.end,
            location: event.extendedProps.location,
            status: event.extendedProps.status
        };
        console.log('Event clicked:', details);
        // In production: show modal with event details
        alert(`${event.title}\n${event.extendedProps.location}`);
    }

    // Upcoming events animation
    const upcomingItems = document.querySelectorAll('.upcoming-item');
    if ('IntersectionObserver' in window) {
        const itemObserver = new IntersectionObserver((entries) => {
            entries.forEach((entry, index) => {
                if (entry.isIntersecting) {
                    setTimeout(() => {
                        entry.target.style.opacity = '1';
                        entry.target.style.transform = 'translateX(0)';
                    }, index * 100);
                    itemObserver.unobserve(entry.target);
                }
            });
        });

        upcomingItems.forEach(item => {
            item.style.opacity = '0';
            item.style.transform = 'translateX(-20px)';
            item.style.transition = 'all 0.5s ease';
            itemObserver.observe(item);
        });
    }
});
