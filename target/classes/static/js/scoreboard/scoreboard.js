// ======= Config =======
let refreshTimer = null;
let usingSSE = false;
const API_BASE = '';

// ======= Helpers UI =======
function showToast(id, msg) {
  if (msg) $('#' + id + 'Msg').text(msg);
  const t = new bootstrap.Toast(document.getElementById(id), { delay: 1800 });
  t.show();
}

function setLoading(loading) {
  $('.btn-change, #btnRefresh, #btnReset, #autoRefreshSwitch').prop('disabled', loading);
}

// ======= Render Core =======
function renderScores(source) {
  if (source && Array.isArray(source.score)) {
    $('#scoreA').text(source.score[0]);
    $('#scoreB').text(source.score[1]);

    // Lịch sử ván + header/footer
    renderGamesHistory(source);
    updateGameInfo(source);

    // ---- TÊN ĐỘI / VĐV (tạm set, đánh đơn sẽ sắp theo R/L ở dưới) ----
    if (Array.isArray(source.names)) {
      const nameA = source.names[0] || 'Đội A';
      const nameB = source.names[1] || 'Đội B';

      if ($('#teamAName').length) $('#teamAName').text(nameA);
      if ($('#teamBName').length) $('#teamBName').text(nameB);

      if (nameA.includes('-')) {
        const [a1, a2] = nameA.split('-').map(n => n.trim());
        $('#nameA').text(a1 || ''); $('#nameA2').text(a2 || '');
      } else {
        $('#nameA').text(nameA); $('#nameA2').text('');
      }

      if (nameB.includes('-')) {
        const [b1, b2] = nameB.split('-').map(n => n.trim());
        $('#nameB').text(b1 || ''); $('#nameB2').text(b2 || '');
      } else {
        $('#nameB').text(nameB); $('#nameB2').text('');
      }
    } else {
      $('#nameA').text('Đội A'); $('#nameA2').text('VĐV 2');
      $('#nameB').text('Đội B'); $('#nameB2').text('VĐV 2');
    }

    // ---- CẬP NHẬT R/L + SẮP TÊN ĐƠN THEO VỊ TRÍ ----
    updateTeamServePositions(source);

    // Điều kiện thắng ván
    checkGameWinCondition(source);

  } else if (source && typeof source.teamAScore === 'number') {
    // Dữ liệu kiểu cũ
    $('#scoreA').text(source.teamAScore);
    $('#scoreB').text(source.teamBScore);
    updateGamesHistoryDisplay(source);
  }
}

// Hiển thị lịch sử các ván đã thi đấu
function renderGamesHistory(source) {
  if (!source || !Array.isArray(source.games) || !source.bestOf) return;

  const gamesA = source.games[0] || 0;
  const gamesB = source.games[1] || 0;
  const bestOf = source.bestOf;
  const currentGame = source.gameNumber || 1;

  $('#gamesContainerA').empty();
  $('#gamesContainerB').empty();

  for (let i = 1; i <= bestOf; i++) {
    const make = (won, side) => {
      const $el = $('<div>').addClass('game-item').text(i);
      if (i < currentGame) {
        $el.addClass(won ? 'won' : 'lost');
      } else if (i === currentGame) {
        $el.addClass('current');
      } else {
        $el.addClass('future');
      }
      $(side).append($el);
    };
    make(i <= gamesA, '#gamesContainerA');
    make(i <= gamesB, '#gamesContainerB');
  }

  updateGamesHistoryDisplay(source);
}

// Cập nhật thông tin ván hiện tại (header + footer)
function updateGameInfo(source) {
  if (!source) return;

  const currentGame = source.gameNumber || 1;
  const bestOf = source.bestOf || 3;
  const server = (typeof source.server === 'number') ? source.server : 0;
  const names = source.names || ['Đội A', 'Đội B'];

  // Header
  $('#currentGameNumber').text(currentGame);
  $('#bestOfNumber').text(bestOf);
  $('#serverName').text(names[server] || 'Đội A');

  // Server indicator màu sắc
  if (server === 0) {
    $('#serverIndicator').removeClass('bg-secondary').addClass('bg-success');
  } else {
    $('#serverIndicator').removeClass('bg-success').addClass('bg-secondary');
  }

  // Footer (nếu có)
  $('#currentGameNumberFooter').text(currentGame);
  $('#bestOfNumberFooter').text(bestOf);
}

/* ======= Đặt tên VĐV đánh đơn theo R/L =======
 * A: R -> #nameA2, L -> #nameA
 * B: R -> #nameB,  L -> #nameB2
 * Nếu tên có '-' thì coi là đôi: tách hiển thị bình thường.
 */
function applySinglesNamePlacement(posA, posB, names) {
  const rawA = (names && names[0]) ? names[0].trim() : ($('#nameA').text().trim() || 'Đội A');
  const rawB = (names && names[1]) ? names[1].trim() : ($('#nameB').text().trim() || 'Đội B');

  const isSinglesA = !rawA.includes('-');
  const isSinglesB = !rawB.includes('-');

  // A
  if (isSinglesA) {
    if (posA === 'R') { $('#nameA2').text(rawA); $('#nameA').text(''); }
    else { $('#nameA').text(rawA); $('#nameA2').text(''); }
  } else {
    const [a1, a2] = rawA.split('-').map(s => s.trim());
    $('#nameA').text(a1 || ''); $('#nameA2').text(a2 || '');
  }

  // B
  if (isSinglesB) {
    if (posB === 'R') { $('#nameB').text(rawB); $('#nameB2').text(''); }
    else { $('#nameB2').text(rawB); $('#nameB').text(''); }
  } else {
    const [b1, b2] = rawB.split('-').map(s => s.trim());
    $('#nameB').text(b1 || ''); $('#nameB2').text(b2 || '');
  }
}

/** 
 * ======= R/L theo bên đang GIAO CẦU (server) =======
 * - server: 0 = A, 1 = B (nếu thiếu -> mặc định A)
 * - Vị trí = R nếu điểm của BÊN GIAO là chẵn, L nếu lẻ
 * - Bên nhận luôn đối diện (đảo R/L)
 * - Đồng thời sắp tên VĐV đánh đơn vào đúng ô theo yêu cầu.
 */
function updateTeamServePositions(source) {
  if (!source || !Array.isArray(source.score)) return;

  const server = (typeof source.server === 'number') ? source.server : 0;
  const scoreA = source.score[0] || 0;
  const scoreB = source.score[1] || 0;

  // ---- Badge lớn (header) theo server ----
  const serverScore = server === 0 ? scoreA : scoreB;
  const serverPos = (serverScore % 2 === 0) ? 'R' : 'L';
  $('#servePosition').text(serverPos);

  const $indicator = $('#servePositionIndicator').removeClass('bg-info bg-warning');
  (serverPos === 'R') ? $indicator.addClass('bg-info') : $indicator.addClass('bg-warning');

  // ---- R/L của TỪNG đội theo điểm của đội đó (không đảo theo server) ----
  const posA = (scoreA % 2 === 0) ? 'R' : 'L';
  const posB = (scoreB % 2 === 0) ? 'R' : 'L';

  // Badge phần “Vị trí giao cầu”
  $('#servePositionA').text(posA);
  $('#servePositionB').text(posB);

  // (Nếu layout có) badge top/bottom
  $('#servePositionA-top, #servePositionA-bottom').text(posA);
  $('#servePositionB-top, #servePositionB-bottom').text(posB);

  // ---- Đặt tên đánh đơn theo vị trí đúng yêu cầu ----
  applySinglesNamePlacement(posA, posB, source.names);

  // Hiệu ứng nhẹ
  $('.serve-position-badge').addClass('pulse-animation');
  setTimeout(() => $('.serve-position-badge').removeClass('pulse-animation'), 800);
}

// Wrapper tương thích chỗ gọi cũ
function updateServePosition(source, server) {
  if (typeof server === 'number') source.server = server;
  updateTeamServePositions(source);
}

// ======= Names / Scrolling =======
function addScrollEffectIfNeeded(selector, name) {
  const $element = $(selector);
  const $wrapper = $element.closest('.team-name-wrapper');
  $element.removeClass('team-name-scroll');

  const charTooLong = (name || '').length > 15;
  requestAnimationFrame(() => {
    const overflow = $element[0] && $wrapper[0] && ($element[0].scrollWidth > $wrapper[0].clientWidth + 8);
    if (charTooLong || overflow) $element.addClass('team-name-scroll');
  });
}

// ======= Combined Score (nếu dùng) =======
function updateCombined() {
  const a = parseInt($('#scoreA').text(), 10) || 0;
  const b = parseInt($('#scoreB').text(), 10) || 0;
  const $el = $('#scoreCombined');
  if ($el.length) $el.text(`${a}-${b}`);
}

// ======= Games History Display =======
function updateGamesHistoryDisplay(source) {
  if (!source || !Array.isArray(source.games) || !source.bestOf) return;

  const bestOf = source.bestOf;
  const currentGame = source.gameNumber || 1;
  const $gamesHistory = $('#gamesHistory');
  if (!$gamesHistory.length) return;

  let historyHTML = '';

  // Set 1 (đang đấu hoặc đã xong)
  if (currentGame >= 1) {
    let set1Score = '';
    let set1Class = 'bg-warning text-dark';

    if (currentGame > 1) {
      if (source.gameScores && source.gameScores[0] && source.gameScores[0][0] >= 0) {
        const scores = source.gameScores[0];
        set1Score = ` ${scores[0]}-${scores[1]}`;
        set1Class = 'bg-success';
      }
    } else {
      const scoreA = source.score ? source.score[0] : 0;
      const scoreB = source.score ? source.score[1] : 0;
      set1Score = ` ${scoreA}-${scoreB}`;
    }

    historyHTML += `<div class="game-row"><span class="badge ${set1Class}">Set 1${set1Score}</span></div>`;
  }

  // Các set đã hoàn thành từ Set 2 đến trước set hiện tại
  for (let i = 2; i < currentGame; i++) {
    let gameScore = '';
    if (source.gameScores && source.gameScores[i - 1] && source.gameScores[i - 1][0] >= 0) {
      const scores = source.gameScores[i - 1];
      gameScore = ` ${scores[0]}-${scores[1]}`;
    }
    historyHTML += `<div class="game-row"><span class="badge bg-success">Set ${i}${gameScore}</span></div>`;
  }

  // Set hiện tại (nếu >= 2)
  if (currentGame >= 2 && currentGame <= bestOf) {
    const scoreA = source.score ? source.score[0] : 0;
    const scoreB = source.score ? source.score[1] : 0;
    historyHTML += `<div class="game-row"><span class="badge bg-warning text-dark">Set ${currentGame} ${scoreA}-${scoreB}</span></div>`;
  }

  // Divider + tổng ván thắng
  if (historyHTML) {
    const gamesA = source.games ? source.games[0] : 0;
    const gamesB = source.games ? source.games[1] : 0;

    historyHTML += `
      <div class="game-divider"></div>
      <hr class="my-2">
      <div class="game-row">
        <span class="badge bg-primary">${gamesA}-${gamesB}</span>
      </div>`;
  }

  $gamesHistory.html(historyHTML || '<div class="text-muted">Chưa có ván nào</div>');
}

// ======= Routing Helpers =======
function getPinCodeFromUrl() {
  const parts = window.location.pathname.split('/');
  const i = parts.indexOf('scoreboard');
  if (i !== -1 && i + 1 < parts.length) {
    const pin = parts[i + 1];
    if (/^\d{4}$/.test(pin)) return pin;
  }
  return null;
}

// ======= API =======
function loadScore(showOk = false) {
  setLoading(true);
  hideNextGameSuggestion();

  const pin = getPinCodeFromUrl();
  const endpoint = pin ? `/api/court/${pin}/sync` : '/api/scoreboard/sync';
  return $.getJSON(API_BASE + endpoint)
    .done((data) => { renderScores(data); if (showOk) showToast('toastOk'); })
    .fail((xhr) => {
      const msg = xhr.responseText || xhr.statusText || 'Không tải được điểm.';
      showToast('toastError', 'Lỗi: ' + msg + ' (Status: ' + xhr.status + ')');
    })
    .always(() => setLoading(false));
}

function changeScore(action) {
  setLoading(true);
  hideNextGameSuggestion();

  const pin = getPinCodeFromUrl();
  const endpoint = pin ? `/api/court/${pin}/${action}` : `/api/scoreboard/${action}`;
  $.ajax({ url: API_BASE + endpoint, method: 'POST', timeout: 10000 })
    .done((data) => {
      if (!usingSSE) return loadScore(true);
      showToast('toastOk');

      // Sau khi server trả về snapshot, cập nhật R/L đúng theo bên đang giao
      if (data) updateTeamServePositions(data);
    })
    .fail((xhr) => {
      const msg = xhr.responseText || xhr.statusText || 'Không thực hiện được hành động.';
      showToast('toastError', 'Lỗi: ' + msg + ' (Status: ' + xhr.status + ')');
    })
    .always(() => setLoading(false));
}

function confirmReset() {
  if (confirm('Bạn có chắc chắn muốn đặt lại điểm và vị trí giao cầu về mặc định?\n\nHành động này sẽ:\n• Đặt lại điểm về 0-0\n• Đặt lại vị trí giao cầu về R-L\n• Xóa lịch sử các ván đã thi đấu\n\nBạn có muốn tiếp tục?')) {
    resetScore();
  }
}

function confirmNextGame() {
  if (confirm('Bạn có chắc chắn muốn chuyển sang ván tiếp theo?\n\nHành động này sẽ:\n• Đặt lại điểm về 0-0\n• Giữ nguyên vị trí giao cầu R-L\n• Cập nhật số ván đã thi đấu\n\nBạn có muốn tiếp tục?')) {
    nextGame();
  }
}

function confirmSwapEnds() {
  if (confirm('Bạn có chắc chắn muốn đổi sân?\n\nHành động này sẽ:\n• Đảo vị trí giao cầu R-L của cả hai đội\n• Giữ nguyên điểm số hiện tại\n• Giữ nguyên đội đang giao cầu\n\nBạn có muốn tiếp tục?')) {
    swapEnds();
  }
}

function confirmChangeServer() {
  if (confirm('Bạn có chắc chắn muốn đổi giao cầu?\n\nHành động này sẽ:\n• Thay đổi đội nào đang giao cầu\n• Giữ nguyên điểm số hiện tại\n• Giữ nguyên vị trí giao cầu R-L\n\nBạn có muốn tiếp tục?')) {
    changeServer();
  }
}

function resetScore() {
  hideNextGameSuggestion();

  const pin = getPinCodeFromUrl();
  const endpoint = pin ? `/api/court/${pin}/reset` : '/api/scoreboard/reset';
  $.ajax({ url: API_BASE + endpoint, method: 'POST' })
    .done(() => {
      if (!usingSSE) return loadScore(true);
      showToast('toastOk');
      setTimeout(() => { loadScore(); }, 100);
    })
    .fail(() => {
      $('#scoreA, #scoreB').text('0');
      updateCombined();
      showToast('toastOk');

      // Về mặc định
      $('#servePosition').text('R');
      $('#servePositionIndicator').removeClass('bg-info bg-warning').addClass('bg-info');
      $('#servePositionA').text('R');
      $('#servePositionB').text('L');

      if (typeof updateCourtPositions === 'function') {
        updateCourtPositions('R', 'L');
      }

      $('#nameA').text('Đội A');
      $('#nameA2').text('VĐV 2');
      $('#nameB').text('Đội B');
      $('#nameB2').text('VĐV 2');
    });
}

// ======= Change Server =======
function changeServer() {
  hideNextGameSuggestion();

  const pin = getPinCodeFromUrl();
  const endpoint = pin ? `/api/court/${pin}/change-server` : '/api/scoreboard/change-server';
  $.ajax({ url: API_BASE + endpoint, method: 'POST' })
    .done(() => {
      if (!usingSSE) return loadScore(true);
      showToast('toastOk', 'Đã đổi giao cầu!');
      setTimeout(() => { loadScore(); }, 100);
    })
    .fail((xhr) => {
      const msg = xhr.responseText || xhr.statusText || 'Không thể đổi giao cầu.';
      showToast('toastError', 'Lỗi: ' + msg);
    });
}

// ======= Swap Ends =======
function swapEnds() {
  hideNextGameSuggestion();

  const pin = getPinCodeFromUrl();
  const endpoint = pin ? `/api/court/${pin}/swap` : '/api/scoreboard/swap';
  $.ajax({ url: API_BASE + endpoint, method: 'POST' })
    .done(() => {
      if (!usingSSE) return loadScore(true);
      showToast('toastOk', 'Đã đổi sân!');
      setTimeout(() => { loadScore(); }, 100);
    })
    .fail((xhr) => {
      const msg = xhr.responseText || xhr.statusText || 'Không thể đổi sân.';
      showToast('toastError', 'Lỗi: ' + msg);
    });
}

// ======= Next Game =======
function nextGame() {
  hideNextGameSuggestion();

  const pin = getPinCodeFromUrl();
  const endpoint = pin ? `/api/court/${pin}/next` : '/api/scoreboard/next';
  $.ajax({ url: API_BASE + endpoint, method: 'POST' })
    .done(() => {
      if (!usingSSE) return loadScore(true);
      showToast('toastOk', 'Đã chuyển sang ván tiếp theo!');
      setTimeout(() => { loadScore(); }, 100);
    })
    .fail((xhr) => {
      const msg = xhr.responseText || xhr.statusText || 'Không thể chuyển ván.';
      showToast('toastError', 'Lỗi: ' + msg);
    });
}

// ======= Check Game Win Condition =======
function checkGameWinCondition(source) {
  if (!source || !Array.isArray(source.score) || !source.bestOf) return;

  const scoreA = source.score[0] || 0;
  const scoreB = source.score[1] || 0;
  const currentGame = source.gameNumber || 1;
  const bestOf = source.bestOf || 3;
  const gamesA = source.games ? source.games[0] : 0;
  const gamesB = source.games ? source.games[1] : 0;

  const matchEnded = (gamesA > bestOf / 2) || (gamesB > bestOf / 2);
  if (matchEnded) {
    showMatchEndedMessage(gamesA, gamesB);
    hideNextGameSuggestion();
    return;
  }

  const isLastGame = (currentGame >= bestOf);
  let isGameWon = false;

  if (scoreA >= 21 || scoreB >= 21) {
    if (scoreA >= 21 && scoreA - scoreB >= 2) isGameWon = true;
    else if (scoreB >= 21 && scoreB - scoreA >= 2) isGameWon = true;
    else if (scoreA === 30 || scoreB === 30) isGameWon = true;
    else if (scoreA >= 20 && scoreB >= 20) {
      if (Math.abs(scoreA - scoreB) >= 2) isGameWon = true;
    }
  }

  if (isGameWon) {
    showNextGameSuggestion();
    if (currentGame < bestOf && !matchEnded && !isLastGame) {
      setTimeout(() => {
        if (source.gameNumber === currentGame && !matchEnded)
          { 
            autoNextGame();
            autoSwapEnds();
          }
      }, 3000);
    }``
  } else {
    hideNextGameSuggestion();
  }
}

// Hiển thị gợi ý chuyển ván
function showNextGameSuggestion() {
  if (!$('#toastNextGame').length) {
    const toastHTML = `
      <div id="toastNextGame" class="toast align-items-center text-bg-info border-0" role="alert" aria-live="assertive" aria-atomic="true">
        <div class="d-flex">
          <div class="toast-body">
            <i class="bi bi-arrow-right-circle me-2" aria-hidden="true"></i>
            <strong>Ván đã kết thúc!</strong><br>
            <small>Nhấn "Ván tiếp theo" hoặc chờ 3 giây để tự động chuyển và đổi sân.</small>
          </div>
          <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Đóng"></button>
        </div>
      </div>`;
    $('.toast-container').append(toastHTML);
  }
  const toast = new bootstrap.Toast(document.getElementById('toastNextGame'), { delay: 8000 });
  toast.show();
}

function hideNextGameSuggestion() {
  $('#toastNextGame').remove();
  $('#toastMatchEnded').remove();
}

function showMatchEndedMessage(gamesA, gamesB) {
  if (!$('#toastMatchEnded').length) {
    const winner = gamesA > gamesB ? 'Đội A' : 'Đội B';
    const toastHTML = `
      <div id="toastMatchEnded" class="toast align-items-center text-bg-success border-0" role="alert" aria-live="assertive" aria-atomic="true">
        <div class="d-flex">
          <div class="toast-body">
            <i class="bi bi-trophy-fill me-2" aria-hidden="true"></i>
            <strong>Trận đấu kết thúc!</strong><br>
            <small>${winner} đã thắng với tỷ số ${gamesA}-${gamesB}</small>
          </div>
          <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Đóng"></button>
        </div>
      </div>`;
    $('.toast-container').append(toastHTML);
  }
  const toast = new bootstrap.Toast(document.getElementById('toastMatchEnded'), { delay: 10000 });
  toast.show();
}

function autoNextGame() {
  hideNextGameSuggestion();
  const pin = getPinCodeFromUrl();
  const endpoint = pin ? `/api/court/${pin}/next` : '/api/scoreboard/next';
  $.ajax({ url: API_BASE + endpoint, method: 'POST' })
    .done(() => {
      if (!usingSSE) return loadScore(true);
      showToast('toastOk', 'Tự động chuyển sang ván tiếp theo!');
      setTimeout(() => { loadScore(); }, 100);
    })
    .fail((xhr) => {
      console.log('Không thể tự động chuyển ván:', xhr.statusText);
    });
}

function autoSwapEnds() {
  hideNextGameSuggestion();
  const pin = getPinCodeFromUrl();
  const endpoint = pin ? `/api/court/${pin}/swap` : '/api/scoreboard/swap';
  $.ajax({ url: API_BASE + endpoint, method: 'POST' })
    .done(() => {
      if (!usingSSE) return loadScore(true);
      showToast('toastOk', 'Đã đổi sân!');
      setTimeout(() => { loadScore(); }, 100);
    })
    .fail((xhr) => {
      const msg = xhr.responseText || xhr.statusText || 'Không thể đổi sân.';
      showToast('toastError', 'Lỗi: ' + msg);
    });
}

// ======= SSE with fallback =======
let esRef = null;
function startSSE() {
  if (!window.EventSource) return false;
  try {
    const pin = getPinCodeFromUrl();
    const endpoint = pin ? `/api/court/${pin}/stream` : '/api/scoreboard/stream';
    const es = new EventSource(API_BASE + endpoint);
    esRef = es;

    usingSSE = true;
    $('#liveBadge').removeClass('d-none');

    let last = 0; const minGap = 80;
    es.addEventListener('init', (e) => { last = performance.now(); renderScores(JSON.parse(e.data)); });
    es.addEventListener('update', (e) => {
      const now = performance.now();
      if (now - last < minGap) return;
      last = now;
      renderScores(JSON.parse(e.data));
    });
    es.onerror = () => {
      usingSSE = false;
      $('#liveBadge').addClass('d-none');
      setupAutoRefresh(true);
      es.close();
    };
    return true;
  } catch {
    return false;
  }
}

function setupAutoRefresh(enable) {
  const $sw = $('#autoRefreshSwitch');
  if (enable) {
    if (!refreshTimer) refreshTimer = setInterval(loadScore, 3000);
    if (!$sw.prop('checked')) $sw.prop('checked', true);
  } else {
    clearInterval(refreshTimer); refreshTimer = null;
    if ($sw.prop('checked')) $sw.prop('checked', false);
  }
}

// ======= Fullscreen =======
function toggleFullscreen() {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen?.();
  } else {
    document.exitFullscreen?.();
  }
}

// ======= Press-and-hold repeat for +/- =======
function bindPressAndHold() {
  const REPEAT_DELAY = 350;
  const REPEAT_INTERVAL = 120;
  let timer1 = null, timer2 = null;

  function clearTimers() { clearTimeout(timer1); clearInterval(timer2); timer1 = timer2 = null; }

  $('.btn-change').each(function () {
    const $btn = $(this);
    const action = $btn.data('action');

    function fireOnce() { changeScore(action); }

    function startRepeat() {
      if (timer1 || timer2) return;
      fireOnce();
      timer1 = setTimeout(() => { timer2 = setInterval(fireOnce, REPEAT_INTERVAL); }, REPEAT_DELAY);
    }

    $btn.on('pointerdown', (e) => { e.preventDefault(); startRepeat(); });
    $btn.on('pointerup pointerleave pointercancel', clearTimers);
    $btn.on('keydown', (e) => { if (e.key === 'Enter' || e.key === ' ') startRepeat(); });
    $btn.on('keyup', clearTimers);
  });
}

// ======= Keyboard shortcuts =======
function bindHotkeys() {
  $(document).on('keydown', (e) => {
    if (['INPUT', 'TEXTAREA'].includes(e.target.tagName)) return;
    if (e.key === '+') { changeScore('increaseA'); return; }
    if (e.key === '-') { changeScore('decreaseA'); return; }
    if (e.key.toLowerCase() === 's') { confirmSwapEnds(); return; }
    if (e.key.toLowerCase() === 'n') { confirmNextGame(); return; }
    if (e.key.toLowerCase() === 'f') { toggleFullscreen(); }
    if (e.key.toLowerCase() === 'g') { confirmChangeServer(); return; }
  });
}

// ======= Init =======
$(function () {
  hideNextGameSuggestion();

  // Mặc định (trước khi có dữ liệu thực)
  $('#servePosition').text('R');
  $('#servePositionIndicator').removeClass('bg-info bg-warning').addClass('bg-info');
  $('#servePositionA').text('R');
  $('#servePositionB').text('L');

  $('#nameA').text('Đội A');
  $('#nameA2').text('VĐV 2');
  $('#nameB').text('Đội B');
  $('#nameB2').text('VĐV 2');

  const ok = startSSE();
  if (!ok) setupAutoRefresh(true); else setupAutoRefresh(false);

  loadScore(); // snapshot đầu

  bindPressAndHold();
  bindHotkeys();

  $('#btnRefresh').on('click', () => loadScore(true));
  $('#btnReset').on('click', confirmReset);
  $('#btnNextGame').on('click', confirmNextGame);
  $('#btnSwap').on('click', confirmSwapEnds);
  $('#btnChangeServer').on('click', confirmChangeServer);
  $('#autoRefreshSwitch').on('change', function () { setupAutoRefresh(this.checked); });
  $('#btnFullscreen').on('click', toggleFullscreen);

  // Re-check marquee khi resize
  let resizeTO = null;
  $(window).on('resize', () => {
    clearTimeout(resizeTO);
    resizeTO = setTimeout(() => {
      ['#nameA', '#nameA2', '#nameB', '#nameB2'].forEach(sel => {
        const txt = $(sel).text() || '';
        if (txt) addScrollEffectIfNeeded(sel, txt);
      });
    }, 150);
  });

  // Đảm bảo đóng SSE khi rời trang
  window.addEventListener('beforeunload', () => {
    try {
      esRef?.close();
      hideNextGameSuggestion();

      $('#servePosition').text('R');
      $('#servePositionIndicator').removeClass('bg-info bg-warning').addClass('bg-info');
      $('#servePositionA').text('R');
      $('#servePositionB').text('L');

      $('#nameA').text('Đội A');
      $('#nameA2').text('VĐV 2');
      $('#nameB').text('Đội B');
      $('#nameB2').text('VĐV 2');
    } catch { }
  });

  // Bind nút quay lại trang nhập mã PIN
  $('#btnBackToPin').on('click', () => {
    if (confirm('Bạn có muốn quay lại trang nhập mã PIN?')) {
      window.location.href = '/pin';
    }
  });

});