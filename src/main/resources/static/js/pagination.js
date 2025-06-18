/**
 * 
 */
document.addEventListener('DOMContentLoaded', () => {
  const policies = Array.from(document.querySelectorAll('#existingpolicyTableBody tr')); // 기존 tr 요소들
  const rowsPerPage = 6; // 한 페이지당 정책 개수
  const pagination = document.querySelector('.pagination');
  const pageItems = pagination.querySelectorAll('li.page-item:not(.first):not(.prev):not(.next):not(.last)');

  let currentPage = 1;
  const totalPages = Math.ceil(policies.length / rowsPerPage);

  // 초기 세팅
  function showPage(page) {
    if (page < 1) page = 1;
    if (page > totalPages) page = totalPages;

    currentPage = page;

    // 정책 리스트 렌더링 (show/hide)
    policies.forEach((tr, index) => {
      tr.style.display = (index >= (page - 1) * rowsPerPage && index < page * rowsPerPage) ? '' : 'none';
    });

    // 페이지네이션 active 설정
    pageItems.forEach(li => {
      const pageNum = Number(li.textContent.trim());
      if (pageNum === page) {
        li.classList.add('active');
      } else {
        li.classList.remove('active');
      }
    });

    // prev/next disabled 처리
    pagination.querySelector('.first').classList.toggle('disabled', page === 1);
    pagination.querySelector('.prev').classList.toggle('disabled', page === 1);
    pagination.querySelector('.next').classList.toggle('disabled', page === totalPages);
    pagination.querySelector('.last').classList.toggle('disabled', page === totalPages);
  }

  // 페이지 번호 클릭 이벤트
  pageItems.forEach(li => {
    li.addEventListener('click', () => {
      const pageNum = Number(li.textContent.trim());
      if (!li.classList.contains('active')) {
        showPage(pageNum);
      }
    });
  });

  // 첫 페이지 버튼 클릭
  pagination.querySelector('.first').addEventListener('click', () => {
    if (currentPage !== 1) showPage(1);
  });

  // 이전 페이지 버튼 클릭
  pagination.querySelector('.prev').addEventListener('click', () => {
    if (currentPage > 1) showPage(currentPage - 1);
  });

  // 다음 페이지 버튼 클릭
  pagination.querySelector('.next').addEventListener('click', () => {
    if (currentPage < totalPages) showPage(currentPage + 1);
  });

  // 마지막 페이지 버튼 클릭
  pagination.querySelector('.last').addEventListener('click', () => {
    if (currentPage !== totalPages) showPage(totalPages);
  });

  // 초기 페이지 표시
  showPage(1);
});
