/**
 * 
 */

function validateForm() {
	const status = document.getElementById('statusSelect').value;
	const level = document.getElementById('threatLevelSelect').value;
	const actions = Array.from(document.querySelectorAll(
		'input[name="action_to_take"]:checked')).map(el => el.value);

	if ((level === 'HIGH' || level === 'CRITICAL') && status === 'ACTIVE') {
		const required = ['LOG', 'ALERT', 'BLOCK'];
		const missing = required.filter(r => !actions.includes(r));
		if (missing.length > 0) {
			alert("HIGH 또는 CRITICAL 위험도에서 ACTIVE 상태일 경우 LOG, ALERT, BLOCK 모두 선택해야 합니다.");
			return false;
		}
	}

	return true;
}



fetch('/enums/status')
	.then(res => res.json())	// res = fetch()로부터 받은 Response 객체, res는 변수 이름일 뿐
	.then(statuses => {
		const select = document.getElementById('statusSelect');
		statuses.forEach(status => {
			const option = document.createElement('option');
			option.value = status;
			option.textContent = status;
			select.appendChild(option);
		});
	});



fetch('/enums/dangerlevel')
	.then(res => res.json())
	.then(dangerlevels => {
		const select = document.getElementById('threatSelect');
		dangerlevels.forEach(level => {
			const option = document.createElement('option');
			option.value = level;
			option.textContent = level;
			select.appendChild(option);
		});
	});


fetch('/enums/detecttype')
	.then(res => res.json())
	.then(statuses => {
		const select = document.getElementById('typeSelect');
		statuses.forEach(types => {
			const option = document.createElement('option');
			option.value = types;
			option.textContent = types;
			select.appendChild(option);
		});
	});


// 0612 추가

document.getElementById("addRowBtn").addEventListener("click", () => {
	const tbody = document.getElementById("policyTableBody");
	const newRow = tbody.rows[0].cloneNode(true);

	// 기존 입력값 초기화
	Array.from(newRow.querySelectorAll("input")).forEach(input => {
		if (input.type === "checkbox") input.checked = false;
		else input.value = "";
	});

	Array.from(newRow.querySelectorAll("select")).forEach(select => {
		select.selectedIndex = 0;
	});

	tbody.appendChild(newRow);
});

// 행 삭제 버튼 처리
document.addEventListener("click", function(e) {
	if (e.target.classList.contains("removeRowBtn")) {
		const row = e.target.closest("tr");
		const tbody = document.getElementById("policyTableBody");
		if (tbody.rows.length > 1) {
			tbody.removeChild(row);
		} else {
			alert("최소 1개의 행은 있어야 합니다.");
		}
	}
});

// 등록 버튼 클릭 시 모든 데이터 JSON으로 묶기
document.getElementById("submitBtn").addEventListener("click", () => {
	const rows = document.querySelectorAll("#policyTableBody tr");
	const policies = [];

	rows.forEach(row => {
		const p_id = row.querySelector('input[name="p_id"]').value.trim();
		const name = row.querySelector('input[name="name"]').value.trim();
		const threat_level = row.querySelector('select[name="threat_level"]').value;
		const status = row.querySelector('select[name="status"]').value;
		const actions = Array.from(row.querySelectorAll('input[name="action_to_take"]:checked'))
			.map(el => el.value);

		// 필수값 검증
		if (!p_id || !name) {
			alert("정책 ID와 이름은 필수입니다.");
			return;
		}

		// LOG는 무조건 포함되게 함
		if (!actions.includes("LOG")) {
			actions.push("LOG");
		}

		policies.push({
			p_id,
			name,
			threat_level,
			status,
			action_to_take: actions.join(",")  // 서버에서 String으로 받는 경우
		});
	});

	// 서버에 전송
	fetch("/registerpolicy", {
		method: "POST",
		headers: {
			"Content-Type": "application/json"
		},
		body: JSON.stringify(policies)
	})
		.then(res => res.text())
		.then(result => {
			if (result === "SUCCESS") {
				alert("정책 등록 성공!");
				location.reload(); // 새로고침
			} else {
				alert("오류 발생: " + result);
			}
		});
});


// 0612 추가
// ➕ 행 추가 버튼
document.getElementById("addRowBtn").addEventListener("click", () => {
	const lastRow = document.querySelector("#policyTableBody tr:last-child");

	const p_id = lastRow.querySelector('input[name="p_id"]').value.trim();
	const name = lastRow.querySelector('input[name="name"]').value.trim();

	if (!p_id || !name) {
		alert("정책을 등록하십시오.\n\n필수 항목: 정책 ID, 이름");
		return;
	}

	const newRow = lastRow.cloneNode(true);

	// 입력 초기화
	newRow.querySelectorAll("input").forEach(input => {
		if (input.type === "checkbox") input.checked = false;
		else input.value = "";
	});

	newRow.querySelectorAll("select").forEach(select => {
		select.selectedIndex = 0;
	});

	document.getElementById("policyTableBody").appendChild(newRow);
});

// ➖ 행 삭제 버튼
document.addEventListener("click", function(e) {
	if (e.target.classList.contains("removeRowBtn")) {
		const row = e.target.closest("tr");
		const p_id = row.querySelector('input[name="p_id"]').value || "미입력";

		if (confirm(`정말 ${p_id} 정책의 등록을 취소하시겠습니까?`)) {
			const tbody = document.getElementById("policyTableBody");
			if (tbody.rows.length > 1) {
				row.remove();
			} else {
				alert("최소 1개의 행은 있어야 합니다.");
			}
		}
	}
});

// ✅ 등록 버튼
document.getElementById("submitBtn").addEventListener("click", () => {
	const rows = document.querySelectorAll("#policyTableBody tr");
	const policies = [];

	for (const row of rows) {
		const p_id = row.querySelector('input[name="p_id"]').value.trim();
		const name = row.querySelector('input[name="name"]').value.trim();
		const status = row.querySelector('select[name="status"]').value;
		const policy_type = row.querySelector('select[name="policy_type"]').value;
		const threat_level = row.querySelector('select[name="threat_level"]').value;

		const actions = Array.from(row.querySelectorAll('input[name="action_to_take"]:checked'))
			.map(el => el.value);

		if (!p_id || !name) {
			alert("정책 ID와 이름은 필수입니다.");
			return;
		}

		if (!actions.includes("LOG")) {
			actions.push("LOG");
		}

		policies.push({
			p_id,
			name,
			status,
			policy_type,
			threat_level,
			action_to_take: actions.join(",")
		});
	}

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
				alert("서버 오류 발생: " + result);
			}
		});
});
