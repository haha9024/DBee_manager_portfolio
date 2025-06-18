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

			submenu.style.display = submenu.style.display === 'none' || submenu.style.display === ''
				? 'block'
				: 'none';
		});
	}

});


// 행 추가 버튼 이벤트
document.getElementById("addRowBtn").addEventListener("click", () => {
	const tbody = document.getElementById("policyTableBody");
	const lastRow = tbody.lastElementChild;

	// 필수 항목 확인
	const p_id = lastRow.querySelector('[data-field="p_id"]').innerText.trim();
	const name = lastRow.querySelector('[data-field="name"]').innerText.trim();
	const actionValues = Array.from(lastRow.querySelectorAll('input[name="action_to_take"]:checked')).map(cb => cb.value);

	if (!p_id || !name || actionValues.length === 0) {
		alert("마지막 행에 정책 ID, 이름, 조치가 모두 입력되어야 새로운 행을 추가할 수 있습니다.");
		return;
	}

	// 새로운 행 추가
	const newRow = cloneRowWithSelects(lastRow);

	tbody.appendChild(newRow);
});


// 행 삭제 버튼 이벤트
document.getElementById("removeRowBtn").addEventListener("click", () => {
	const tbody = document.getElementById("policyTableBody");
	const selectedRow = tbody.querySelector("tr.selected");

	if (!selectedRow) {
		alert("삭제할 행을 선택해주세요.");
		return;
	}

	const policyId = selectedRow.querySelector('[data-field="p_id"]').innerText.trim() || "해당 정책";

	if (!confirm(`${policyId} 정책 등록을 정말 취소하시겠습니까?`)) {
		return;
	}

	tbody.removeChild(selectedRow);
});


// 행 클릭시 선택 표시
document.querySelector("#policyTableBody").addEventListener("click", (e) => {
	const tr = e.target.closest("tr");
	if (!tr) return;

	document.querySelectorAll("#policyTableBody tr").forEach(row => row.classList.remove("selected"));
	tr.classList.add("selected");
});



// 정책 제출 버튼 이벤트
document.getElementById("submitBtn").addEventListener("click", () => {
	const rows = document.querySelectorAll("#policyTableBody tr");
	const policies = [];

	const numberFields = ["base_sec", "base_count", "dst_port_start", "dst_port_end"];

	// ⚠️ 전체 유효성 통과 여부
	let validationPassed = true;

	for (const row of rows) {
		const data = {};

		try {
			// 1. data-field td 값 수집 + 숫자 변환
			row.querySelectorAll("[data-field]").forEach(td => {
				const key = td.dataset.field;
				let value = td.innerText.trim();

				if (numberFields.includes(key)) {
					value = Number(value);
					if (isNaN(value)) {
						alert(`${key}는 숫자여야 합니다.`);
						throw new Error(`${key} is not a number`);
					}
				}

				data[key] = value;
			});


			// 2. 체크박스 값 수집 (중요!)
			const actionCheckboxes = row.querySelectorAll('input[name="action_to_take"]:checked');
			const actionValues = [];
			actionCheckboxes.forEach(checkbox => {
				actionValues.push(checkbox.value);
			});
			if (actionValues.length === 0) {
				alert("조치(action_to_take) 항목을 한 개 이상 선택해주세요.");
				return;
			}
			data.action_to_take = actionValues.join(",");


			// 3. Select 태그 값 수집
			const selects = {
				status: row.querySelector('select[name="status"]'),
				policy_type: row.querySelector('select[name="policy_type"]'),
				threat_level: row.querySelector('select[name="threat_level"]'),
			};

			for (const key in selects) {
				const el = selects[key];
				if (!el) continue;		// el이 없으면 건너뜀

				const val = el.value;
				if (!val) {
					alert(`${key} 항목을 선택해주세요.`);
					el.focus();
					return;
				}
				data[key] = val;

			}


			// 4. 필수값 체크
			if (!data.p_id || !data.name || !data.action_to_take) {
				alert("정책 ID와 이름과 조치는 필수입니다.");
				return;
			}

			policies.push(data); // 유효한 데이터만 추가
		
		} catch (e) {

			alert("입력 오류 : " + e.message);
			validationPassed = false;
			break; // 한 번이라도 에러가 있으면 반복 중단
		}

	}


	// 에러가 있으면 서버 전송 X
	if (!validationPassed) return;

	// 서버 전송
	fetch("/registerpolicy", {
		method: "POST",
		headers: { "Content-Type": "application/json" },
		body: JSON.stringify(policies)
	})
		.then(res => res.text())
		.then(result => {
			if (result === "SUCCESS") {
				alert("정책 등록 성공!");
				location.reload();
			} else {
				alert("오류 발생: " + result);
			}
		});
});


// 06.14 addRowBtn클릭 이벤트를 해결할 함수 추가
// 06.16 수정(ui가 아쉽다)
// 기존에 드롭다운이 뜨지 않는 문제가 있었다
function cloneRowWithSelects(templateRow) {
	const newRow = templateRow.cloneNode(true);

	// 1. <td data-field> 초기화
	newRow.querySelectorAll("[data-field]").forEach(td => {
		
		// select-cell은 건너뛰고, 나머지만 비워줌
		if(!td.classList.contains("select-cell")){
			td.innerText = "";	
		}
	});

	// 2. 체크박스 초기화
	newRow.querySelectorAll('input[name="action_to_take"]').forEach(cb => cb.checked = false);

	// 3. select 필드 초기화 및 재생성
	const optionsMap = {
		status: ["ACTIVE", "INACTIVE"],
		policy_type: ["PATTERN", "BEHAVIOR"],
		threat_level: ["LOW", "MEDIUM", "HIGH", "CRITICAL"]
	};

	for (const field in optionsMap) {
		const td = newRow.querySelector(`td[data-field="${field}"]`);
		if (!td) continue;
		
		// 기존 select 제거
		//td.innerHTML = "";

		
		// select-cell 클래스 유지
		td.className = "select-cell";
		td.innerHTML = ""; // 기존 내용 초기화

				
		// select 생성
		const select = document.createElement("select");
		select.name = field;
		select.className = "real-select"; //  CSS에서 form-select, real-select 모두 쓰니 추가

		// 기본 선택 옵션
		const defaultOption = document.createElement("option");
		defaultOption.value = "";
		defaultOption.textContent = "선택";
		select.appendChild(defaultOption);

		// 나머지 옵션 추가
		optionsMap[field].forEach(value => {
			const option = document.createElement("option");
			option.value = value;
			option.textContent = value;
			select.appendChild(option);
		});

		td.appendChild(select);
		
		// 예전 버전
		//td.innerHTML = ""; // 기존 select 제거
		//td.appendChild(select);
	}

	return newRow;
}

