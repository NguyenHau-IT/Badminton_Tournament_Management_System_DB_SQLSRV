'use strict';

$(function () {
  let currentIndex = 0;

  const $digits = $('.pin-digit');
  const $submitBtn = $('#submitBtn');
  const $errorMessage = $('#errorMessage');
  const $loading = $('#loading');
  const $form = $('#pinForm');

  // ===== Helpers =====
  function getPin() {
    return $digits.map(function () { return $(this).val(); }).get().join('');
  }

  function updateSubmitButton() {
    const pin = getPin();
    $submitBtn.prop('disabled', pin.length !== 4);
  }

  function focusIndex(i) {
    currentIndex = Math.max(0, Math.min(3, i));
    $digits.eq(currentIndex).trigger('focus');
  }

  function showError(msg) {
    $errorMessage.text(msg).removeClass('d-none');
  }

  function hideError() {
    $errorMessage.addClass('d-none');
  }

  function showLoading() {
    $loading.removeClass('d-none');
    $submitBtn.addClass('d-none');
  }

  function hideLoading() {
    $loading.addClass('d-none');
    $submitBtn.removeClass('d-none');
  }

  function clearAll() {
    $digits.each(function () {
      $(this).val('').removeClass('filled');
    });
    focusIndex(0);
    updateSubmitButton();
    hideError();
  }

  function inputDigit(d) {
    if (currentIndex < 4) {
      const $cur = $digits.eq(currentIndex);
      $cur.val(String(d)).addClass('filled');
      if (currentIndex < 3) focusIndex(currentIndex + 1);
      updateSubmitButton();
      hideError();
    }
  }

  function backspace() {
    const $cur = $digits.eq(currentIndex);
    if ($cur.val()) {
      $cur.val('').removeClass('filled');
    } else if (currentIndex > 0) {
      focusIndex(currentIndex - 1);
      $digits.eq(currentIndex).val('').removeClass('filled');
    }
    updateSubmitButton();
    hideError();
  }

  // ===== Inputs events =====
  $digits.on('input', function () {
    const $t = $(this);
    const index = $digits.index($t);
    const value = ($t.val() || '').replace(/[^0-9]/g, '');
    $t.val(value);

    if (value) {
      $t.addClass('filled');
      if (index < 3) focusIndex(index + 1);
    } else {
      $t.removeClass('filled');
    }

    updateSubmitButton();
    hideError();
  });

  $digits.on('keydown', function (e) {
    const index = $digits.index(this);

    if (e.key === 'Backspace' && !$(this).val() && index > 0) {
      focusIndex(index - 1);
    } else if (e.key === 'ArrowLeft' && index > 0) {
      focusIndex(index - 1);
    } else if (e.key === 'ArrowRight' && index < 3) {
      focusIndex(index + 1);
    } else if (e.key === 'Enter') {
      e.preventDefault();
      $form.trigger('submit');
    }
  });

  $digits.on('focus', function () {
    currentIndex = $digits.index(this);
  });

  $digits.on('paste', function (e) {
    e.preventDefault();
    const data = (e.originalEvent.clipboardData || window.clipboardData)
      .getData('text')
      .replace(/[^0-9]/g, '')
      .slice(0, 4);

    for (let i = 0; i < data.length && i < 4; i++) {
      const $d = $digits.eq(i);
      $d.val(data[i]).addClass('filled');
    }
    if (data.length > 0) {
      const nextIndex = Math.min(data.length, 3);
      focusIndex(nextIndex);
    }
    updateSubmitButton();
    hideError();
  });

  // ===== Keypad (event delegation) =====
  $('#keypad').on('click', 'button', function () {
    const $btn = $(this);
    const digit = $btn.data('digit');
    const action = $btn.data('action');

    if (digit !== undefined) {
      inputDigit(digit);
    } else if (action === 'clear') {
      clearAll();
    } else if (action === 'backspace') {
      backspace();
    }
  });

  // ===== Submit =====
  $form.on('submit', function (e) {
    e.preventDefault();
    const pin = getPin();

    if (pin.length !== 4) {
      showError('Vui lòng nhập đủ 4 chữ số');
      return;
    }

    showLoading();
    hideError();

    $.ajax({
      url: `/api/court/${pin}/status`,
      method: 'GET',
    })
      .done(function () {
        window.location.href = `/scoreboard/${pin}`;
      })
      .fail(function (jqXHR) {
        if (jqXHR.status === 404) {
          showError('Mã PIN không tồn tại. Vui lòng kiểm tra lại.');
        } else {
          showError('Có lỗi xảy ra. Vui lòng thử lại.');
        }
        hideLoading();
      });
  });

  // ===== Access info (URL + QR) =====
  function initializeAccessInfo() {
    const currentUrl = window.location.origin + '/pin';
    const $urlBox = $('#currentUrl .url-text');
    $urlBox.text(currentUrl);
    loadQRCode(currentUrl);
  }

  // Global copy function (đang được gọi từ HTML onclick)
  window.copyUrl = function () {
    const currentUrl = window.location.origin + '/pin';
    navigator.clipboard.writeText(currentUrl).then(function () {
      const $btn = $('#currentUrl button');
      $btn.removeClass('btn-primary').addClass('btn-success').html('<i class="bi bi-check2"></i> Copied!');
      setTimeout(function () {
        $btn.removeClass('btn-success').addClass('btn-primary').html('<i class="bi bi-clipboard"></i> Copy');
      }, 1800);
    }).catch(function (err) {
      console.error('Failed to copy URL:', err);
      alert('Không thể copy URL. Vui lòng copy thủ công.');
    });
  };

  function loadQRCode(url) {
    // Tải lib QR bằng jQuery
    $.getScript('https://cdn.jsdelivr.net/npm/qrcode@1.5.3/build/qrcode.min.js')
      .done(function () {
        try {
          const $qr = $('#qrCode').empty();
          const canvas = document.createElement('canvas');
          // global QRCode do lib cung cấp
          QRCode.toCanvas(
            canvas,
            url,
            { width: 120, height: 120, margin: 2, color: { dark: '#000000', light: '#FFFFFF' } },
            function (error) {
              if (error) {
                console.error('QR code generation failed:', error);
                $qr.html('<div class="small text-body-secondary text-center">' + url + '</div>');
              } else {
                $qr.append(canvas);
              }
            }
          );
        } catch (e) {
          console.error('QR code generation error:', e);
        }
      })
      .fail(function () {
        console.log('QR code library not loaded, using fallback');
      });
  }

  // ===== Init =====
  focusIndex(0);
  updateSubmitButton();
  initializeAccessInfo();
});
