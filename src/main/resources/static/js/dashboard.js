/**
 * 
 */

// 0701 추가
/**
 * 차트 로딩 에러 UI
 * @param {string} chartId — e.g. "p_idTop10"
 * @param {string} message — 서버·클라이언트 에러 메시지
 */
function showChartError(chartId, message) {
	const chartArea = document.getElementById(`${chartId}-chart`);
	if (!chartArea) return;
	chartArea.innerHTML = `
    <div class="text-center text-danger fw-bold">
      <i class="fas fa-exclamation-triangle fa-2x mb-2"></i>
      <p>${message}</p>
    </div>`;
}


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

// 0623 추가
// Chart 인스턴스 저장용 전역 객체
const chartIds = ["countryTop10", "p_idTop10", "policyTypeStats", "ThreatLevelStats",
	"HourlyStats", "HourlyPolicyStats", "HourlyThreatStats", "TopPolicyAvgViolations", "srcIp_top10"];
const averageChartIds = ["TopPolicyAvgViolations"];
window.myCharts = {};

// 기본 차트, 테이블 로딩
document.addEventListener("DOMContentLoaded", function() {
	//loadDefaultChart("countryTop10");
	loadDefaultChart("p_idTop10"); // 이런 식으로 수많은 기본 차트들 loadDefaultChart()로 호출
	loadDefaultChart("policyTypeStats");
	loadDefaultChart("ThreatLevelStats");
	loadDefaultChart("HourlyStats");
	loadDefaultChart("HourlyPolicyStats");
	loadDefaultChart("HourlyThreatStats");
	loadDefaultChart("TopPolicyAvgViolations");
	loadDefaultChart("srcIp_top10");
});


// fetch 후 렌더링 함수 호출
function loadDefaultChart(chartId) {

	// 평균 차트 여부 분기
	const url = isAverageChart(chartId)
		? `/dashboard2/average/${chartId}/default` : `/dashboard2/top/${chartId}/default`;

	console.log(`[debug] 🔄 loadDefaultChart called: ${chartId} fetching URL: ${url}`);

	fetch(url)
		.then(response => {
			if (!response.ok) {
				return response.text().then(msg => { throw new Error(msg); });
			}
			return response.json();
		})
		.then(payload => {
			console.log(`[debug] ✅ 응답 도착 (${chartId}):`, payload);
			console.log("서버 응답 데이터: ", payload);


			if (isAverageChart(chartId)) {

				if (!Array.isArray(payload.data)) {
					console.error(`❌ ${chartId}: 평균 차트용 데이터가 배열이 아님:`, payload);
					return;
				}

				// 평균 차트 렌더링	
				drawAverageChart(chartId, payload.data, payload.startDate, payload.endDate);
			}
			else {

				// 일반 차트용 payload 자체가 배열
				if (!Array.isArray(payload)) {
					console.error(`❌ ${chartId}: 일반 차트용 데이터가 배열이 아님:`, payload);
					return;
				}

				// (기존 date 로직 유지가 필요하면 이곳에 추가)
				const chartsWithDate = ["HourlyStats", "HourlyPolicyStats", "HourlyThreatStats"];
				const date = chartsWithDate.includes(chartId) && payload.length > 0
					? payload[0].date
					: null;

				console.log(`[debug] 📤 렌더링 시도 (${chartId}): date=${date}`);


				renderChart(chartId, payload, date);	// 일반 top 차트 렌더링
			}
		})
		.catch(error => {
			console.error(`❌ ${chartId}: 차트 로딩 실패`, error);
			showChartError(chartId, "차트 로딩 실패"); // 사용자에게 오류 표시
		})
}


function needsCountryConversion(chartId) {
	return chartId === "countryTop10";
}

// 0628 추가
// 평균 차트인지 확인함
function isAverageChart(chartId) {
	const keywords = ["Avg", "avg", "Average", "average"];
	return keywords.some(keyword => chartId.includes(keyword));
}


// 필터 차트 로딩
function loadFilteredChart(chartId) {
	const date = document.getElementById(`filter-date-${chartId}`).value;
	const startHour = parseInt(document.getElementById(`start-hour-${chartId}`).value);
	const endHour = parseInt(document.getElementById(`end-hour-${chartId}`).value);

	/*const topN = parseInt(document.getElementById(`top-n-${chartId}`).value) || 10;*/
	const topNElement = document.getElementById(`top-n-${chartId}`);
	const topN = topNElement ? parseInt(topNElement.value) || 10 : 10;

	const filterDto = {
		date: date,
		startHour: startHour,
		endHour: endHour,
		topN: topN
	};

	console.log("filterDto 전송:", filterDto);

	fetch(`/dashboard2/top/${chartId}/data`, {
		method: "POST",
		headers: {
			"Content-Type": "application/json"
		},
		body: JSON.stringify(filterDto)
	})
		.then(response => {
			if (!response.ok) {
				return response.text().then(msg => { throw new Error(msg); });
			}
			return response.json();
		})
		.then(data => {
			console.log("loadFilteredChart 호출됨");
			renderChart(chartId, data, filterDto?.date);
		})
		.catch(error => {
			console.error("필터 차트 로딩 실패", error);
			const chartArea = document.getElementById(`${chartId}-chart`);
			// chartArea.innerHTML = `<p style="color:red;">${error.message}</p>`;

			// 차트 인스턴스가 이미 있다면 제거
			/*if (window.myCharts[chartId]) {
				window.myCharts[chartId].destroy();
				delete window.myCharts[chartId];
			}*/


			const msg = error.message?.includes("로그") ? error.message : "알 수 없는 오류 발생";
			chartArea.innerHTML = `
			<div class="text-center text-danger fw-bold">
			<i class="fas fa-exclamation-triangle fa-2x mb-2"></i>
				<p>${msg}</p>
			</div>`;
		});

}

// 0701 추가
// 로그 테이블만 로드
function loadDefaultTable(tableId) {
	const url = `/dashboard2/top/table/${tableId}/default`;
	console.log(`[debug] 🔄 loadDefaultTable called: ${tableId} fetching URL: ${url}`);
	genericFetchJson(url)
		.then(payload => {
			console.log(`[debug] ✅ 응답 도착 (${tableId}):`, payload);
			console.log("서버 응답 데이터: ", payload.data);
			renderTable(tableId, payload.data);
		})
		.catch(error => showTableError(tableId, error.message));
}


// 0701 추가
/**
 * @returns Promise<any> — JSON 파싱 결과
 */
function genericFetchJson(url) {
	console.log(`[debug] 🔄 genericFetchJson: url=${url}`);
	return fetch(url)
		.then(async response => {
			const body = await response.json();
			if (!response.ok) {
				// 서버가 { error: "…" } 형태로 응답한다고 가정
				throw new Error(body.error || "알 수 없는 오류");
			}
			return body;
		});
}


// 차트 렌더링
async function renderChart(chartId, data, date = null) {
	console.log(`[debug] 📊 renderChart 호출됨: ${chartId}`);
	const chartArea = document.getElementById(`${chartId}-chart`);
	chartArea.innerHTML = ""; // 기존 내용 제거

	if (window.myCharts[chartId]) {
		window.myCharts[chartId].destroy();
		delete window.myCharts[chartId];
	}

	// 배열 여부 확인
	if (!Array.isArray(data)) {
		console.error("서버 응답이 배열이 아님: ", data);
		chartArea.innerHTML = "<p> ⚠️ 차트를 그릴 수 없습니다. 서버 오류가 발생했거나 데이터가 없습니다. </p>"
	}

	if (data.length === 0) {
		chartArea.innerHTML = "<p>데이터 없음</p>";
		return;
	}


	// 차트 종류 분기
	switch (chartId) {
		case "policyTypeStats":	// 도넛 차트 예시, 정책 타입별 탐지 건수와 비율
			renderDoughnutChart(chartId, data);
			break;
		case "ThreatLevelStats": // 도넛 차트 예시, 위험도별 탐지 건수와 비율
			renderPieChart(chartId, data);
			break;
		case "HourlyStats": 	// 선형 차트 예시, 시간대별 공격 패킷 탐지 수 추이
			renderLineChart(chartId, data, date);
			break;
		case "HourlyPolicyStats":
			renderStackedBarChart(chartId, data, date);
			break;
		case "HourlyThreatStats":
			console.log(`[debug] 🎯 HourlyThreatStats 렌더링 시작`);
			renderStackedBarChart(chartId, data, date);
			break;
		default:
			renderBarChart(chartId, data);
	}

	// 막대 차트 렌더링 함수
	function renderBarChart(chartId, data) {

		// 캔버스 요소 동적 추가(chart.js는 canvas 필요), chart.js로 차트 그리기
		const chartArea = document.getElementById(`${chartId}-chart`);
		const canvas = document.createElement("canvas");
		chartArea.innerHTML = ''; // 꼭 초기화
		chartArea.appendChild(canvas);

		let labels;
		switch (chartId) {
			case "countryTop10":
				labels = data.map(item => item.country);	// 이미 서버에서 country 제공
				break;
			case "p_idTop10":
				labels = data.map(item => item.p_id);
				break;
			default:
				labels = data.map(item => item.src_ip); // 이게 바로 fallback
		}
		const counts = data.map(item => item.cnt ?? item.total_cnt ?? 0);

		// 순위별 색상 설정
		const backgroundColors = data.map((_, index) => {
			if (index < 3) return 'rgba(255, 99, 132, 0.6)';		// Top 1~3: 빨강
			else if (index < 7) return 'rgba(255, 206, 86, 0.6)'; 	// Top 4~7: 노랑
			else return 'rgba(75, 192, 192, 0.6)';					// 나머지: 초록
		});

		const borderColors = backgroundColors.map(color => color.replace('0.6', '1')); // 진한 외곽선


		// 기존 차트 제거
		if (window.myCharts[chartId]) {
			window.myCharts[chartId].destroy();
		}

		// 차트 렌더링 시작
		const maxCount = Math.max(...counts);
		const suggestedMax = maxCount < 5 ? 5 : undefined; // 너무 작으면 5까지 보이게 유도
		new Chart(canvas, {
			type: 'bar',
			data: {
				labels: labels,	// 국가명 리스트
				datasets: [{
					label: '탐지 건수',
					data: counts,
					backgroundColor: backgroundColors,
					borderColor: borderColors,
					borderWidth: 1
				}]
			},
			options: {
				indexAxis: 'y',	// 핵심 포인트: 가로 막대 설정
				responsive: true,
				maintainAspectRatio: false,
				scales: {
					y: {
						beginAtZero: true,
						suggestedMax: suggestedMax, // 눈금 동적 유도
						ticks: {
							autoSkip: false	// Y축 라벨 모두 표시
						}
					}
				},
				plugins: {
					legend: {
						display: false // 막대마다 색이 다르니 범례는 생략
					}
				}
			}
		});
	}

	// 도넛 차트 렌더링 함수(policyTypeStats 용)
	function renderDoughnutChart(chartId, data) {
		const chartArea = document.getElementById(`${chartId}-chart`);
		const canvas = document.createElement("canvas");
		chartArea.innerHTML = "";
		chartArea.appendChild(canvas);

		let labels = [];

		switch (chartId) {
			case "policyTypeStats":
				labels = data.map(item => item.policy_type);
				break;
			default:
				labels = data.map(item => item.label);	// 대체 경로
		}

		//const labels = data.map(item => item.policy_type);
		const counts = data.map(item => item.cnt);
		const total = counts.reduce((sum, val) => sum + val, 0); // 총합 계산, reduce는 배열을 하나의 값으로 줄임

		const backgroundColors = ['rgba(255, 99, 132, 0.6)', 'rgba(255, 206, 86, 0.6)',
			'rgba(75, 192, 192, 0.6)', '#4BC0C0'];

		if (window.myCharts[chartId]) {
			window.myCharts[chartId].destroy();
		}

		window.myCharts[chartId] = new Chart(canvas.getContext('2d'), {
			type: 'doughnut',
			data: {
				labels: labels,
				datasets: [{
					data: counts,
					backgroundColor: backgroundColors.slice(0, labels.length),
					hoverOffset: 30
				}]
			},
			options: {
				responsive: true,
				plugins: {
					legend: {
						position: 'bottom'
					},
					// 콜백: 툴팁을 그릴 때마다 label(context) 함수를 chart.js가 자동으로 호출해서 툴팁 문자열 받아옴
					tooltip: {
						enabled: true,
						callbacks: {
							label: function(context) {
								const label = context.label || '';
								const value = context.parsed;
								const percentage = ((value / total) * 100).toFixed(1); // 소수점 1자리
								return `${label}:${value}건 (${percentage}%)`;
							}
						}
					}
				}
			}
		});
	}

	// 파이 차트 렌더링 함수 (ThreatLevelStats용)
	function renderPieChart(chartId, data) {
		const chartArea = document.getElementById(`${chartId}-chart`);
		const canvas = document.createElement("canvas");
		chartArea.innerHTML = "";
		chartArea.appendChild(canvas);

		let labels = [];

		switch (chartId) {
			case "ThreatLevelStats":
				labels = data.map(item => item.threat_level);
				break;
			default:
				labels = data.map(item => item.label); // fallback(대체경로)	
		}

		const counts = data.map(item => item.cnt);
		const total = counts.reduce((sum, val) => sum + val, 0);
		const backgroundColors = {
			LOW: "#A8E6CF",       // 파스텔 그린
			MEDIUM: "#FFF9C4",    // 파스텔 노랑
			HIGH: "#FFDAB9",      // 파스텔 오렌지
			CRITICAL: "#FFCDD2"   // 파스텔 레드
		};

		const pieColors = labels.map(label => backgroundColors[label] || "#CCCCCC"); // fallback 회색

		if (window.myCharts[chartId]) {
			window.myCharts[chartId].destroy();
		}

		window.myCharts[chartId] = new Chart(canvas.getContext('2d'), {
			type: 'pie',
			data: {
				labels: labels,
				datasets: [{
					data: counts,
					backgroundColor: pieColors,
					hoverOffset: 20
				}]
			},
			options: {
				responsive: true,
				plugins: {
					legend: {
						position: 'bottom'
					},
					tooltip: {
						callbacks: {
							label: function(context) {
								const label = context.label || '';
								const value = context.parsed;
								const percentage = ((value / total) * 100).toFixed(1);
								return `${label}: ${value}건 (${percentage}%)`;
							}
						}
					}
				}
			}
		});

	}


	// 선형 차트 렌더링 함수
	function renderLineChart(chartId, data, date = null) {
		const chartArea = document.getElementById(`${chartId}-chart`);
		const canvas = document.createElement("canvas");
		chartArea.innerHTML = "";
		chartArea.appendChild(canvas);

		let labels = [];

		switch (chartId) {
			case "HourlyStats":
				labels = data.map(item => `${item.hour}시`);	// 시간대별 라벨
				break;
			default:
				labels = data.map(item => item.label); // fallback(대체경로)	
		}

		const counts = data.map(item => item.cnt);
		const titleText = date ? `📅 ${date}` : '시간대별 공격 추이';
		//const maxCount = Math.max(...counts);
		//const suggestedMax = maxCount < 5 ? 5 : undefined; 

		if (window.myCharts[chartId]) {
			window.myCharts[chartId].destroy();
		}

		window.myCharts[chartId] = new Chart(canvas.getContext('2d'), {
			type: 'line',
			data: {
				labels: labels,
				datasets: [{
					label: '시간대별 탐지 수',
					data: counts,
					fill: false,
					borderColor: 'rgba(75, 192, 192, 1)',
					tension: 0.2,
					pointBackgroundColor: 'rgba(75, 192, 192, 1)',
					pointRadius: 4,
				}]
			},
			options: {
				responsive: true,
				plugins: {
					title: {
						display: true,
						text: titleText,
						align: 'end',
						position: 'top',
						font: {
							size: 14,
							weight: 'normal'
						},
						padding: {
							top: 0,
							bottom: 10
						}
					},
					legend: { position: 'top' },
					tooltip: {
						callbacks: {
							label: function(context) {
								const label = context.dataset.label || '';
								return `${context.label} 탐지 건수: ${context.parsed.y}건`;
							}
						}
					}
				},
				scales: {
					y: {
						beginAtZero: true,
						/*suggestedMax: suggestedMax,*/
						title: {
							display: true,
							text: '탐지 건수'
						}
					},
					x: {
						title: {
							display: true,
							text: '시간'
						},
						ticks: {
							autoSkip: false,
							maxRotation: 45,
							minRotation: 30
						}
					}
				}
			}

		});
	}

	// 누적 막대 차트 렌더링 함수
	function renderStackedBarChart(chartId, data, date = null) {
		console.log(`[debug] renderStackedBarChart called with id=${chartId}`);

		const chartArea = document.getElementById(`${chartId}-chart`);

		// chartArea 유효한지 디버깅용 검사 코드
		if (!chartArea) {
			console.error(`❌ chartArea not found for chartId: ${chartId}`);
			return;
		}

		const canvas = document.createElement("canvas");
		chartArea.innerHTML = "";
		chartArea.appendChild(canvas);	//"canvas"넣으면 노드가 아니라 문자열이라 타입 에러 일어남


		// X축 라벨
		let labels = [];

		switch (chartId) {
			case "HourlyPolicyStats":
			case "HourlyThreatStats":
				labels = data.map(item => `${item.hour}시`);
				break;
			default:
				labels = data.map(item => item.label); // fallback(대체경로)	
		}

		// 공통: hour, date 제외한 키 추출
		//const chartKeys = Object.keys(data[0]).filter(k=> k !== "hour" && k !== "date");
		let chartKeys = [];

		if (chartId === "HourlyThreatStats") {
			// 위험도 순서대로 고정
			const orderedThreatLevels = ["LOW", "MEDIUM", "HIGH", "CRITICAL"];
			chartKeys = orderedThreatLevels.filter(key => Object.keys(data[0]).includes(key));
		} else {
			//기존 처리 방식
			chartKeys = Object.keys(data[0]).filter(k => k !== "hour" && k !== "date");
		}

		// 색상 결정 함수: chartId에 따라
		const getColor = (label) => {
			if (chartId === "HourlyPolicyStats") {
				return label === "BEHAVIOR"
					? 'rgba(75, 192, 192, 0.6)'
					: 'rgba(255, 159, 64, 0.6)';
			} else if (chartId === "HourlyThreatStats") {
				const threatColors = {
					LOW: 'rgba(75, 192, 192, 0.6)',     // 초록
					MEDIUM: 'rgba(255, 193, 7, 0.6)',  // 노랑
					HIGH: 'rgba(255, 159, 64, 0.6)',    // 주황
					CRITICAL: 'rgba(255, 99, 132, 0.6)'	 // 빨강
				};
				return threatColors[label] || 'rgba(200,200,200,0.6)'; // fallback 회색
			} else {
				return 'rgba(150,150,150,0.6)';
			}
		}

		// datasets 생성
		const datasets = chartKeys.map(key => ({
			label: key,
			data: data.map(item => item[key]),
			backgroundColor: getColor(key),
			borderColor: getColor(key).replace('0.6', '1'),
			borderWidth: 1
		}));

		// 디버깅용 코드
		console.log("chartKeys:", chartKeys);
		console.log("샘플 데이터:", data[0]);
		console.log("datasets 확인:".datasets);

		const titleText = date ? `📅 ${date}` : '시간대별 탐지방식 분포';

		if (window.myCharts[chartId]) {
			window.myCharts[chartId].destroy();
		}

		window.myCharts[chartId] = new Chart(canvas.getContext('2d'), {

			type: 'bar',
			data: {
				labels: labels,
				datasets: datasets
			},
			options: {
				responsive: true,
				plugins: {
					title: {
						display: true,
						text: titleText,
						align: 'end',
						font: {
							size: 14,
							weight: 'normal'
						},
						padding: {
							top: 0,
							bottom: 10
						}
					},
					tooltip: {
						mode: 'index',
						intersect: false,
						callbacks: {
							label: function(context) {
								const label = context.dataset.label || '';
								const currentValue = context.parsed.y;

								// 전체 합계 계산 (해당 index의 모든 dataset 합)
								const total = context.chart.data.datasets
									.map(ds => ds.data[context.dataIndex])
									.reduce((a, b) => a + b, 0);

								const percent = total > 0 ? ((currentValue / total) * 100).toFixed(1) : 0;

								return `${label}: ${currentValue}건 (${percent}%)`;
							}
						}

					},
					legend: {
						position: 'top'
					}
				},
				scales: {
					x: {
						stacked: true,
						title: {
							display: true,
							text: '시간'
						},
						ticks: {
							autoSkip: false,
							maxRotation: 45,
							minRotation: 30
						},
					},
					y: {
						stacked: true,
						beginAtZero: true,
						title: {
							display: true,
							text: '탐지 건수'
						}
					}
				}
			}
		});


	}

}

// 평균 차트 렌더링
async function drawAverageChart(chartId, data, startDate, endDate) {
	console.log(`[debug] 📊 drawAverageChart 호출됨: ${chartId}`);

	const containerId = `${chartId}-chart`;
	const chartArea = document.getElementById(containerId);
	chartArea.innerHTML = ""; // 기존 내용 제거
	//const canvas = document.createElement("canvas");
	//chartArea.appendChild(canvas);

	// 1) 응답 포맷 확인: data[i] === { p_id: "...", name: "...", avg_violation: "12.34" }

	/*if (window.myCharts[chartId]) {
		window.myCharts[chartId].destroy();
		delete window.myCharts[chartId];
	}*/


	// 1) DTO 범위 표시 (지난 7일 예시)
	const rangeEl = document.getElementById(`${chartId}-range`);
	if (rangeEl) {
		// yyyy-MM-dd만 잘라서 표시
		const fmt = s => s.slice(0, 10);
		rangeEl.textContent = `${fmt(startDate)} ~ ${fmt(endDate)}`;
	}


	// 2) 유효성 검사 -> 배열 여부 확인
	if (!Array.isArray(data)) {
		console.error("서버 응답이 배열이 아님: ", data);
		chartArea.innerHTML = "<p> ⚠️ 차트를 그릴 수 없습니다. 서버 오류가 발생했거나 데이터가 없습니다. </p>"
		return;		// 반드시 함수 종료
	}

	if (data.length === 0) {
		chartArea.innerHTML = "<p>데이터 없음</p>";
		return;
	}

	// 차트 종류 분기
	switch (chartId) {
		case "TopPolicyAvgViolations":
			renderHorizontalBarChart(containerId, data, {
				labelField: "p_id",
				valueField: "avg_violation",
				datasetLabel: "일평균 위반 건수",
				highlightTop: 3
			});
			console.log(`[디버깅용] 차트 아이디(TopPolicyAvgViolations) - ${containerId}`);
			break;
		default:
			chartArea.innerHTML = "<p> 지원하지 않는 평균 차트입니다. </p>"
	}



}

/**
 * 가로 막대 차트 전용 렌더러
 * @param {string} containerId  — "<chartId>-chart" 같은 DIV ID
 * @param {Array<Object>} data — {p_id, name, avg_violation, ...} 배열
 * @param {Object} opts
 *   @property {string} labelField   — y축 레이블 필드 이름 (예: "p_id")
 *   @property {string} valueField   — x축 값 필드 이름 (예: "avg_violation")
 *   @property {string} datasetLabel — 레전드/툴팁에 표시할 라벨 (예: "일평균 위반 건수")
 *   @property {number} highlightTop — 상위 N개 강조 색 (기본 3)
 */
function renderHorizontalBarChart(containerId, data, opts) {

	console.log(`[debug] 📊 renderHorizontalBarChart 호출됨: ${containerId}`);

	const {
		labelField,
		valueField,
		datasetLabel,
		highlightTop
	} = opts;


	const container = document.getElementById(containerId);
	container.innerHTML = "";
	const canvas = document.createElement("canvas");
	container.appendChild(canvas);


	// y축 레이블: p_id
	const labels = data.map(item => item[labelField]);
	// x축 값: avg_violation
	const values = data.map(item => item[valueField]);


	// 데이터 최대값 + 10% 여유
	const maxVal = Math.max(...values);
	const suggestedMax = Math.ceil(maxVal * 1.1);


	// 색상: 상위 highlightTop 은 강조색, 나머진 기본색
	const bgColors = values.map((_, i) => {
		if (i < 3) return 'rgba(255, 99, 132, 0.6)';  // Top1~3
		else if (i < 7) return 'rgba(255, 206, 86, 0.6)';  // Top4~7
		else return 'rgba(75, 192, 192, 0.6)';  // 나머지
	});
	const borderColors = bgColors.map(c => c.replace('0.6', '1'));

	// 기존 차트가 있으면 삭제
	if (window.myCharts[containerId]) {
		window.myCharts[containerId].destroy();
	}


	window.myCharts[containerId] = new Chart(canvas.getContext('2d'), {
		type: 'bar',
		data: {
			labels: labels,
			datasets: [{
				label: datasetLabel,
				data: values,
				backgroundColor: bgColors,
				borderColor: borderColors,
				borderWidth: 1
			}]
		},
		options: {
			indexAxis: 'y',       // 가로 막대
			responsive: true,
			maintainAspectRatio: false,	// 컨테이너 크기에 따라 자유롭게 그리기
			scales: {
				x: {
					beginAtZero: true,
					suggestedMax,            // 동적 눈금 상한 
					title: {
						display: true,
						text: datasetLabel
					}
				},
				y: {
					beginAtZero: true,
					title: {
						display: true,
						text: '정책 ID'
					},
					ticks: {
						autoSkip: false // Y축 눈금 다 표시
					}
				}
			},
			plugins: {
				legend: { display: false },  // 범례 숨김
				tooltip: {
					callbacks: {
						// 툴팁에 "P001 (Login Policy): 5.43건" 형태로 표시
						label: context => {
							const idx = context.dataIndex;
							const item = data[idx];
							return `${item.p_id} (${item.name}): ${item.avg_violation}건`;
						}
					}
				}
			}
		}
	});
}


// 0701 테이블 렌더링 함수 추가
/**
 * @param {string} tableId
 * @param {Array<Object>} rows
 */
function renderTable(tableId, rows) {
	const tbody = document.getElementById(`${tableId}-tbody`);
/*	if (!container) {
		console.log(`[debug] 📤 렌더링 시도 (${tableId}) 행 (${rows})`);
		return;

	}*/
	
	if (!tbody) {
	  console.log(`[debug] no target tbody: ${tableId}-tbody`);
	  return;
	}

	// ② 기존 내용 지우기
	tbody.innerHTML = "";
	
	// ③ 데이터 없으면 빈 메시지
	if (!Array.isArray(rows) || rows.length === 0) {
		//container.innerHTML = "<p>데이터가 없습니다</p>";
		tbody.innerHTML = `<tr><td colspan="2" class="text-center">데이터가 없습니다</td></tr>`;
		return;
	}

	// 예: <table> 생성
/*	let html = "<table class='table'><thead><tr>";
	Object.keys(rows[0]).forEach(col => {
		html += `<th>${col}</th>`;
	});
	html += "</tr></thead><tbody>";
	rows.forEach(row => {
		html += "<tr>";
		Object.values(row).forEach(val => {
			html += `<td>${val}</td>`;
		});
		html += "</tr>";
	});
	html += "</tbody></table>";
	container.innerHTML = html;*/
	
	// 한 행씩 추가
/*	rows.forEach(r => {
	  const tr = document.createElement("tr");
	  Object.values(r).forEach(val => {
	    const td = document.createElement("td");
	    td.textContent = val;
	    tr.appendChild(td);
	  });
	  tbody.appendChild(tr);
	});*/
	
	rows.forEach(r => {
	    const tr = document.createElement("tr");

	    // 1) src_ip
	    let td = document.createElement("td");
	    td.textContent = r.src_ip; tr.appendChild(td);

	    // 2) total_cnt
	    td = document.createElement("td");
	    td.textContent = r.total_cnt; tr.appendChild(td);

	    // 3) last_seen
	    td = document.createElement("td");
	    td.textContent = r.last_seen; tr.appendChild(td);

	    // 4) policies
	    td = document.createElement("td");
	    td.textContent = r.policies; tr.appendChild(td);

	    tbody.appendChild(tr);
	  });
}

/**
 * 에러 발생 시 테이블 영역에 메시지 출력
 * @param {string} tableId
 * @param {string} message
 */
function showTableError(tableId, message) {
	const container = document.getElementById(`${tableId}-table`);
	if (!container) return;

	container.innerHTML = `
    <div class="p-3 text-center text-danger">
      데이터 로드 중 오류가 발생했습니다.<br>
      (${message})
    </div>
  `;
}

