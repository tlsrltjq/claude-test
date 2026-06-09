/**
 * date-utils.js — 연/월/일 분리 날짜 입력 자동 탭
 *
 * 사용법:
 *   <span class="date-split [date-split-sm]">
 *     <input type="text" class="date-seg date-seg-y" maxlength="4" placeholder="YYYY" inputmode="numeric" autocomplete="off">
 *     <span class="date-sep">.</span>
 *     <input type="text" class="date-seg date-seg-m" maxlength="2" placeholder="MM" inputmode="numeric" autocomplete="off">
 *     <span class="date-sep">.</span>
 *     <input type="text" class="date-seg date-seg-d" maxlength="2" placeholder="DD" inputmode="numeric" autocomplete="off">
 *     <input type="hidden" name="fieldName" value="YYYY-MM-DD">
 *   </span>
 *
 * initDateSplits(root?) — root 하위의 모든 .date-split을 초기화
 * DOMContentLoaded 시 자동 실행
 */
function initDateSplits(root) {
  (root || document).querySelectorAll('.date-split').forEach(function(wrap) {
    if (wrap.dataset.dsInit) return; // 중복 초기화 방지
    wrap.dataset.dsInit = '1';

    var y = wrap.querySelector('.date-seg-y');
    var m = wrap.querySelector('.date-seg-m');
    var d = wrap.querySelector('.date-seg-d');
    var hidden = wrap.querySelector('input[type="hidden"]');

    // hidden 값(YYYY-MM-DD)으로 초기 표시
    if (hidden && hidden.value) {
      var parts = hidden.value.split('-');
      if (parts.length === 3) {
        if (y) y.value = parts[0];
        if (m) m.value = parts[1];
        if (d) d.value = parts[2];
      }
    }

    function sync() {
      if (!hidden) return;
      var yv = y ? y.value.trim() : '';
      var mv = m ? m.value.trim() : '';
      var dv = d ? d.value.trim() : '';
      hidden.value = (yv && mv && dv)
        ? yv + '-' + mv.padStart(2, '0') + '-' + dv.padStart(2, '0')
        : '';
      hidden.dispatchEvent(new Event('change', { bubbles: true }));
    }

    function makeKd(field, next) {
      return function(e) {
        // 숫자 외 문자 입력 차단 (백스페이스·Delete·방향키·Tab 허용)
        if (e.key.length === 1 && !/\d/.test(e.key)) {
          e.preventDefault();
          return;
        }
        // 필드가 꽉 찬 상태에서 추가 숫자 → 다음 필드로 이동
        if (e.key.length === 1 && /\d/.test(e.key) &&
            field.value.length >= parseInt(field.getAttribute('maxlength') || '4') && next) {
          e.preventDefault();
          next.focus();
          next.value = e.key;
          sync();
        }
      };
    }

    function makeInput(field) {
      return function() {
        field.value = field.value.replace(/\D/g, '')
          .slice(0, parseInt(field.getAttribute('maxlength') || '4'));
        sync();
      };
    }

    function lastKd(e) {
      if (e.key.length === 1 && !/\d/.test(e.key)) e.preventDefault();
    }

    if (y) { y.addEventListener('keydown', makeKd(y, m)); y.addEventListener('input', makeInput(y)); }
    if (m) { m.addEventListener('keydown', makeKd(m, d)); m.addEventListener('input', makeInput(m)); }
    if (d) { d.addEventListener('keydown', lastKd); d.addEventListener('input', makeInput(d)); }
  });
}

document.addEventListener('DOMContentLoaded', function() { initDateSplits(); });

/**
 * JS로 date-split의 값을 설정할 때 사용.
 * @param {Element} wrapOrEl - .date-split 래퍼이거나 그 내부 자식 요소
 * @param {string} dateStr   - 'YYYY-MM-DD' 형식 (비우려면 '' 전달)
 */
function setDateSplitValue(wrapOrEl, dateStr) {
  if (!wrapOrEl) return;
  var wrap = wrapOrEl.classList && wrapOrEl.classList.contains('date-split')
    ? wrapOrEl
    : wrapOrEl.closest ? wrapOrEl.closest('.date-split') : null;
  if (!wrap) return;
  var hidden = wrap.querySelector('input[type="hidden"]');
  var y = wrap.querySelector('.date-seg-y');
  var m = wrap.querySelector('.date-seg-m');
  var d = wrap.querySelector('.date-seg-d');
  if (hidden) hidden.value = dateStr || '';
  if (dateStr && dateStr.includes('-')) {
    var parts = dateStr.split('-');
    if (y) y.value = parts[0] || '';
    if (m) m.value = parts[1] || '';
    if (d) d.value = parts[2] || '';
  } else {
    if (y) y.value = '';
    if (m) m.value = '';
    if (d) d.value = '';
  }
}
