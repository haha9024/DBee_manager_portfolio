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

// 0623 추가
// Chart 인스턴스 저장용 전역 객체
const chartIds = ["countryTop10", "p_idTop10"];
window.myCharts = {};

// 기본 차트 로딩
document.addEventListener("DOMContentLoaded", function(){
	//loadDefaultChart("countryTop10");
	loadDefaultChart("p_idTop10"); // 이런 식으로 수많은 기본 차트들 loadDefaultChart()로 호출
});


// fetch 후 렌더링 함수 호출
function loadDefaultChart(chartId){
	fetch(`/dashboard/top/${chartId}/default`)
	.then(response=>response.json())
	.then(data=>{
		console.log("서버 응답 데이터: ", data);
		
		if (!Array.isArray(data)) {
		        console.error("❌ 서버 응답이 배열이 아님:", data);
		        return;
		    }
		renderChart(chartId, data);
	})
	.catch(error=>{
		console.error("기본 차트 로딩 실패", error);
		showChartError(chartId, "차트 로딩 실패"); // 사용자에게 보여주기 위한 용도
	});
}

function needsCountryConversion(chartId){
	return chartId === "countryTop10";
}



// 필터 차트 로딩
function loadFilteredChart(chartId){
	const date = document.getElementById(`filter-date-${chartId}`).value;
	const startHour = parseInt(document.getElementById(`start-hour-${chartId}`).value);
	const endHour = parseInt(document.getElementById(`end-hour-${chartId}`).value);
	
	/*const topN = parseInt(document.getElementById(`top-n-${chartId}`).value) || 10;*/
	const topNElement = document.getElementById(`top-n-${chartId}`);
	const topN = topNElement ? parseInt(topNElement.value) || 10 : 10;
	
	const filterDto = {
		date : date,
		startHour: startHour,
		endHour: endHour,
		topN: topN
	};
		
	console.log("filterDto 전송:", filterDto);
	
	fetch(`/dashboard/top/${chartId}/data`, {
		method: "POST",
		headers: {
			"Content-Type": "application/json"
		},
		body: JSON.stringify(filterDto)
	})
	.then(response => {
		if(!response.ok){
			return response.text().then(msg => { throw new Error(msg); });
		}
		return response.json();
	})
	.then(data => {
		console.log("loadFilteredChart 호출됨");
		renderChart(chartId, data);
	})
	.catch(error=>{
		console.error("필터 차트 로딩 실패", error);
		const chartArea = document.getElementById(`${chartId}-chart`);
		// chartArea.innerHTML = `<p style="color:red;">${error.message}</p>`;
		
		// 차트 인스턴스가 이미 있다면 제거
		if (window.myCharts[chartId]) {
			window.myCharts[chartId].destory();
			delete window.myCharts[chartId];
		}
		
		// 에러 메시지 파싱
		let userMessage = "⚠️ 알 수 없는 오류가 발생했습니다.";
		if(error.messge.includes("로그 기록이 없습니다")){
			userMessage = "🚫 해당 날짜의 로그 테이블이 존재하지 않습니다.";
		}
	
		// 사용자에게 친절한 메시지 출력
		chartArea.innerHTML = `
			<div class="text-center text-muted">
				<i class="fas fa-exclamation-triangle fa-2x mb-2"></i>
				<p>${error.message.includes("로그 기록이 없습니다")? 
					"해당 날짜의 로그 테이블이 존재하지 않습니다.":error.message}</p>
			</div>
		`;
	});
			
}

// 차트 렌더링
async function renderChart(chartId, data) {
	const chartArea = document.getElementById(`${chartId}-chart`);
	chartArea.innerHTML = ""; // 기존 내용 제거
	
	// 배열 여부 확인
	if(!Array.isArray(data)){
		console.error("서버 응답이 배열이 아님: ", data);
		chartArea.innerHTML = "<p> ⚠️ 차트를 그릴 수 없습니다. 서버 오류가 발생했거나 데이터가 없습니다. </p>"
	}
	
	if (data.length === 0){
		chartArea.innerHTML = "<p>데이터 없음</p>";
		return;
	}
	

	// 차트 데이터 구성
	//const labels = data.map(item => chartId === "countryTop10" ? item.country : item.src_ip); 
	
	let labels;
	switch(chartId){
		case "countryTop10":
			labels = data.map(item => item.country);	// 이미 서버에서 country 제공
			break;
		case "p_idTop10":
			labels = data.map(item => item.p_id);
			break;
		default:
			labels = data.map(item => item.src_ip); // 이게 바로 fallback
	}
	const counts = data.map(item => item.cnt);
	
	// 순위별 색상 설정
	const backgroundColors = data.map((_, index) => {
		if (index < 3) return 'rgba(255, 99, 132, 0.6)';		// Top 1~3: 빨강
		else if (index < 7) return 'rgba(255, 206, 86, 0.6)'; 	// Top 4~7: 노랑
 		else return 'rgba(75, 192, 192, 0.6)';					// 나머지: 초록
	});
	
	const borderColors = backgroundColors.map(color => color.replace('0.6', '1')); // 진한 외곽선
	
	// 캔버스 요소 동적 추가(chart.js는 canvas 필요), chart.js로 차트 그리기
	const canvas = document.createElement("canvas");
	chartArea.appendChild(canvas);
	
	// 기존 차트 제거
	if(window.myCharts[chartId]){
		window.myCharts[chartId].destroy();
	}
	
	// 차트 렌더링 시작
	
	const maxCount = Math.max(...counts);
	const suggestedMax = maxCount < 5 ? 5 : undefined; // 너무 작으면 5까지 보이게 유도
	new Chart(canvas, {
		type:'bar',
		data:{
			labels : labels,	// 국가명 리스트
			datasets: [{
				label: '탐지 건수',
				data: counts,
				backgroundColor: backgroundColors,
				borderColor: borderColors,
				borderWidth: 1
			}] 
		},
		options: {
			indexAxis : 'y',	// 핵심 포인트: 가로 막대 설정
			responsive: true,
			scales:{
				y:{
					beginAtZero:true,
					suggestedMax : suggestedMax, // 눈금 동적 유도
					ticks:{
						autoSkip:false	// Y축 라벨 모두 표시
					}
				}
			},
			plugins:{
				legend:{
					display: false // 막대마다 색이 다르니 범례는 생략
				}
			}
		}
	});
	
}



