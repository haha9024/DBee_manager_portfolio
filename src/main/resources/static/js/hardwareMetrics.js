/**
 * 
 */
// src/main/resources/static/js/hardwareMetrics.js

let hardwareChart;

document.addEventListener("DOMContentLoaded", () => {
	initHardwareChart();
	setInterval(fetchHardwareMetrics, 5000);
});

function initHardwareChart() {
	const container = document.getElementById("hardware-usage-chart");
	const canvas = document.createElement("canvas");
	container.appendChild(canvas);

	hardwareChart = new Chart(canvas, {
		type: 'line',
		data: {
			labels: [],
			datasets: [
				{
					label: 'CPU 사용률',
					data: [],			// 0.0~1.0 범위
					yAxisID: 'y',
					borderWidth: 1,
					fill: false,
					pointRadius: 0,         // 점 없애기
					pointHitRadius: 10,  // 히트 영역 10px
					pointHoverRadius: 5  // 호버 시 5px로 보여 줌
				},
				{
					label: '메모리 사용량 (MB)',
					data: [],			// 0.0~1.0 범위
					yAxisID: 'y',
					borderWidth: 1,
					fill: false,
					pointRadius: 0,         // 점 없애기
					pointHitRadius: 10,
					pointHoverRadius: 5
				}
			]
		},
		options: {
			responsive: true,
			scales: {
				x: {
					type: 'time',
					time: {
						unit: 'minute', // 1분 단위 눈금
						displayFormats: { minute: 'HH:mm' }
					},
					min: Date.now() - 5 * 60 * 1000,
					max: Date.now(),
					ticks: {
						autoSkip: true,
						maxTicksLimit: 6	// 그래프폭에 따라 적절히 줄여 줌
					},
					grid: { display: false }
				},
				y: {
					type: 'linear',
					position: 'left',
					beginAtZero: true,
					max: 1,
					ticks: { callback: v => (v * 100).toFixed(0) + '%' }
				}
			},
			intersect: {
				mode: 'nearest',	// 'nearest' 또는 'index'
				intersect: false	// 교차하지 않아도 툴팁 열기
			},
			plugins: {
				tooltip: {
				   mode: 'nearest',
				   intersect: false,
				   callbacks: {
				     label(ctx) {
				       return `${ctx.dataset.label}: ${(ctx.parsed.y*100).toFixed(1)}%`;
				     }
				   }
				 },
				legend: { position: 'top' }
			}
		}
	});
}

function fetchHardwareMetrics() {
	fetch('/dashboard2/metrics/hardware')
		.then(res => res.json())
		.then(data => {
			/*hardwareChart.data.labels = data.map(d => d.timestamp);
			hardwareChart.data.datasets[0].data = data.map(d => d.cpuLoad);
			hardwareChart.data.datasets[1].data = data.map(d => d.usedMemory);
			hardwareChart.update();*/

			// 1) 데이터 파싱
			// timestamp 문자열("HH:mm:ss")을 Date 객체로 변환
			const cpuData = data.map(d => {
				const [hh, mm, ss] = d.timestamp.split(':').map(Number);
				const dt = new Date();
				dt.setHours(hh, mm, ss, 0);
				return { x: dt, y: d.cpuLoad };
			});
			const memData = data.map(d => {
				const [hh, mm, ss] = d.timestamp.split(':').map(Number);
				const dt = new Date();
				dt.setHours(hh, mm, ss, 0);
				return { x: dt, y: d.usedMemory/d.totalMemory };
			});

			// 2) 데이터 세팅
			hardwareChart.data.datasets[0].data = cpuData;
			hardwareChart.data.datasets[1].data = memData;


			// 3) 축(window) 갱신
			const now = Date.now();
			hardwareChart.options.scales.x.min = now - 5 * 60 * 1000;
			hardwareChart.options.scales.x.max = now;

			// 4) 차트 업데이트
			hardwareChart.update('none');	// 애니메이션 없이 바로 갱신
		})
		.catch(err => console.error("하드웨어 메트릭 로드 실패:", err));
}
