/**
 * 
 */

// 정책 메뉴 토글
document.addEventListener('DOMContentLoaded', function() {
	//const policyMenuLink = document.getElementById('policy-menu-link');
	const toggleBtn = document.getElementById('policy-toggle-button');
	const submenu = document.getElementById('policy-submenu');

	// ▼ 아이콘 눌렀을 때 하위 메뉴 토글
	if (toggleBtn && submenu) {
		toggleBtn.addEventListener('click', function(e) {
			e.preventDefault();
			e.stopPropagation(); // 링크 이동 방지 + 이벤트 중단
			/* submenu.style.display = submenu.style.display === 'none' ? 'block' : 'none'; */

			// 토글 처리
			submenu.style.display = submenu.style.display === 'none' || submenu.style.display === ''
				? 'block'
				: 'none';
		});
	}

});


// 검색 AJAX 처리
$('#searchForm').submit(function(e) {
  e.preventDefault();
  const submitBtn = $(this).find('button[type="submit"]');
  submitBtn.prop('disabled', true).text('검색 중...');

  const actionList = $('input[name="action_to_take_list"]:checked')
    .map(function () { return $(this).val(); }).get();

  const threatList = $('input[name="threat_levels"]:checked')
    .map(function() { return $(this).val(); }).get();

  const data = {
    p_id: $('input[name="p_id"]').val(),
    name: $('input[name="name"]').val(),
    status: $('select[name="status"]').val(),
    policy_type: $('select[name="policy_type"]').val(),
    min_base_sec: $('input[name="min_base_sec"]').val(),
    max_base_sec: $('input[name="max_base_sec"]').val(),
    action_to_take_list: actionList.length > 0 ? actionList : null,
    threat_levels: threatList.length > 0 ? threatList : null,
 	  // 추가(0609)		 
 	  min_base_count: $('input[name="min_base_count"]').val(),
    max_base_count: $('input[name="max_base_count"]').val()

  };

  $.ajax({
    url: '/readpolicy2/search',
    type: 'POST',
    contentType: 'application/json',
    data: JSON.stringify(data),
    success: function(policies) {
      const tbody = $('#policyTableBody');
      tbody.empty();

      if (!policies || policies.length === 0) {
        tbody.append('<tr><td colspan="22">검색 결과가 없습니다.</td></tr>');
        return;
      }

      policies.forEach(function(policy) {
        tbody.append(`
          <tr>
            <td>${policy.p_id || ''}</td>
            <td>${policy.name || ''}</td>
            <td>${policy.status || ''}</td>
            <td>${policy.description || ''}</td>
            <td>${policy.created_by || ''}</td>
            <td>${policy.updated_by || ''}</td>
            <td>${policy.base_sec || ''}</td>
            <td>${policy.base_count || ''}</td>
            <td>${policy.src_ip_start || ''}</td>
            <td>${policy.src_ip_end || ''}</td>
            <td>${policy.dst_ip_start || ''}</td>
            <td>${policy.dst_ip_end || ''}</td>
            <td>${policy.dst_port_start || ''}</td>
            <td>${policy.dst_port_end || ''}</td>
            <td>${policy.payload_content1 || ''}</td>
            <td>${policy.payload_content2 || ''}</td>
            <td>${policy.payload_content3 || ''}</td>
            <td>${policy.policy_type || ''}</td>
            <td>${policy.threat_level || ''}</td>
            <td>${policy.action_to_take || ''}</td>
            <td>${policy.created_at || ''}</td>
            <td>${policy.updated_at || ''}</td>
          </tr>
        `);
      });
    },
    error: function(err) {
      console.error('에러 발생:', err);
      alert('검색 중 오류가 발생했습니다.');
    },
    complete: function() {
      submitBtn.prop('disabled', false).text('검색');
    }
  });
});


// 위협 / 조치 토글 처리
 $(document).ready(function() {
   $('#toggleAction').click(function(e) {
   	e.stopPropagation(); 	/* 추가 */
     $('#actionBox').toggle();
   });

   $('#toggleThreat').click(function(e) {
   	e.stopPropagation();
     $('#threatBox').toggle();	 /* 추가 */
   });
   
   // 외부 클릭 시 닫히도록
   $(document).click(function(e) {
     $('#actionBox, #threatBox').hide();
 });

});
















