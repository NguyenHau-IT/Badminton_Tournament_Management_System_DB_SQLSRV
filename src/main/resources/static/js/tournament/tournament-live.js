/**
 * Tournament Live Page JavaScript
 * Real-time score updates and chat functionality
 */

document.addEventListener('DOMContentLoaded', function() {
    let updateInterval;
    let websocket = null;

    // Initialize AOS
    AOS.init({
        duration: 800,
        once: true
    });

    // Simulate real-time score updates (every 5 seconds)
    updateInterval = setInterval(updateLiveScores, 5000);

    function updateLiveScores() {
        console.log('Updating live scores...');
        // In production: fetch from WebSocket or API
        // Example WebSocket connection:
        // connectWebSocket();
        
        // Simulate score change
        const currentSets = document.querySelectorAll('.current-set');
        currentSets.forEach(set => {
            // Random update for demo (remove in production)
            const currentScore = parseInt(set.textContent);
            if (Math.random() > 0.7 && currentScore < 30) {
                animateScoreChange(set, currentScore + 1);
            }
        });
    }

    function animateScoreChange(element, newScore) {
        element.style.transform = 'scale(1.3)';
        element.style.color = '#ffd700';
        
        setTimeout(() => {
            element.textContent = newScore;
            setTimeout(() => {
                element.style.transform = 'scale(1)';
            }, 200);
        }, 200);
    }

    // WebSocket connection (for production)
    function connectWebSocket() {
        const matchId = document.querySelector('[data-match-id]')?.dataset.matchId;
        if (!matchId) return;

        // Example WebSocket URL (adjust for your backend)
        // const wsUrl = `ws://localhost:8080/ws/match/${matchId}`;
        // websocket = new WebSocket(wsUrl);

        // websocket.onmessage = function(event) {
        //     const data = JSON.parse(event.data);
        //     updateScoreDisplay(data);
        // };

        // websocket.onerror = function(error) {
        //     console.error('WebSocket error:', error);
        // };

        // websocket.onclose = function() {
        //     console.log('WebSocket disconnected, reconnecting...');
        //     setTimeout(connectWebSocket, 5000);
        // };
    }

    function updateScoreDisplay(data) {
        // Update player 1 score
        if (data.player1CurrentSet !== undefined) {
            const player1CurrentSet = document.querySelector('.player-row:first-child .current-set');
            animateScoreChange(player1CurrentSet, data.player1CurrentSet);
        }

        // Update player 2 score
        if (data.player2CurrentSet !== undefined) {
            const player2CurrentSet = document.querySelector('.player-row:last-child .current-set');
            animateScoreChange(player2CurrentSet, data.player2CurrentSet);
        }

        // Update serving indicator
        updateServingIndicator(data.serving);

        // Update stats
        if (data.stats) {
            updateStats(data.stats);
        }
    }

    function updateServingIndicator(servingPlayer) {
        document.querySelectorAll('.player-row').forEach((row, index) => {
            if (index + 1 === servingPlayer) {
                row.classList.add('serving');
            } else {
                row.classList.remove('serving');
            }
        });
    }

    function updateStats(stats) {
        // Update winner shots
        document.querySelector('[data-stat="winners-left"]').textContent = stats.player1Winners || 0;
        document.querySelector('[data-stat="winners-right"]').textContent = stats.player2Winners || 0;

        // Update unforced errors
        document.querySelector('[data-stat="errors-left"]').textContent = stats.player1Errors || 0;
        document.querySelector('[data-stat="errors-right"]').textContent = stats.player2Errors || 0;

        // Update rally length
        document.querySelector('[data-stat="rally"]').textContent = stats.longestRally || 0;
    }

    // Chat functionality
    const chatInput = document.getElementById('chatInput');
    const chatSend = document.getElementById('chatSend');
    const chatMessages = document.querySelector('.chat-messages');

    if (chatSend) {
        chatSend.addEventListener('click', sendMessage);
    }

    if (chatInput) {
        chatInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                sendMessage();
            }
        });
    }

    function sendMessage() {
        const message = chatInput.value.trim();
        if (!message) return;

        // In production: send to WebSocket or API
        addChatMessage('Bạn', message, new Date());

        chatInput.value = '';
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }

    function addChatMessage(author, text, time) {
        const messageDiv = document.createElement('div');
        messageDiv.className = 'chat-message';
        messageDiv.innerHTML = `
            <div class="message-author">${author}</div>
            <div class="message-text">${text}</div>
            <div class="message-time">${formatTime(time)}</div>
        `;
        
        chatMessages.appendChild(messageDiv);
        
        // Animate new message
        messageDiv.style.opacity = '0';
        messageDiv.style.transform = 'translateY(10px)';
        setTimeout(() => {
            messageDiv.style.transition = 'all 0.3s ease';
            messageDiv.style.opacity = '1';
            messageDiv.style.transform = 'translateY(0)';
        }, 10);
    }

    function formatTime(date) {
        return date.toLocaleTimeString('vi-VN', {
            hour: '2-digit',
            minute: '2-digit'
        });
    }

    // Fullscreen toggle
    const fullscreenBtn = document.getElementById('fullscreenBtn');
    if (fullscreenBtn) {
        fullscreenBtn.addEventListener('click', function() {
            if (!document.fullscreenElement) {
                document.documentElement.requestFullscreen();
                this.innerHTML = '<i class="bi bi-fullscreen-exit"></i> Thoát toàn màn hình';
            } else {
                document.exitFullscreen();
                this.innerHTML = '<i class="bi bi-arrows-fullscreen"></i> Toàn màn hình';
            }
        });
    }

    // Cleanup on page unload
    window.addEventListener('beforeunload', function() {
        if (updateInterval) {
            clearInterval(updateInterval);
        }
        if (websocket) {
            websocket.close();
        }
    });

    // Sound notifications for score changes
    function playScoreSound() {
        // In production: play actual sound file
        // const audio = new Audio('/sounds/score-update.mp3');
        // audio.play();
        console.log('Score sound played');
    }
});
