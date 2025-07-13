/**
 * 
 */

// 1) 함수 정의부: initGauge / updateGauge
function initGauge({ containerId, title, color, max = 100 }) {
  const chart = echarts.init(document.getElementById(containerId));
  chart.setOption({
    tooltip: {
      show: true,
      formatter: params => {
        // params.data.time 은 updateGauge에서 채워줍니다
        return `${title}<br/>${params.value}건<br/>기간: ${params.data.time}`;
      }
    },
    title: {
      text: title,
      left: 'center',
      top: '5%',
      textStyle: { fontSize: 14, color: '#333' }
    },
    series: [{
      type: 'gauge',
      startAngle: 180, endAngle: 0,
      radius: '90%', center: ['50%', '65%'],
      min: 0, max: max,
      pointer: { show: false },
      axisLine: {
        lineStyle: {
          width: 20,
          color: [
			[1, '#d9d9d9']
		  ]
        }
      },
      splitLine:   { show: false },
      axisTick:    { show: false },
      axisLabel:   { show: false },
      detail: {
        valueAnimation: true,
        formatter: '{value}건',
        fontSize: 18,
        offsetCenter: [0, '-20%'],
        color: '#333'
      },
      data: [{ value: 0, time: '' }]
    }]
  });
  return chart;
}

async function updateGauge(chart, cfg, minutes = 60) {
  const res = await fetch(`/api/charts/realtime/${cfg.chartId}?minutes=${minutes}`);
  if (!res.ok) return;
  
  const data = await res.json();
  // config.valueKey 에 따라 적절한 값 추출
  const rawValue = data[cfg.valueKey];
  // 차트에 표시할 값
  const displayValue = +rawValue;
  // 최대값 갱신 (예: count 차트는 점점 커질 수 있고, ratio 차트는 max=100 고정)
  const newMax  = cfg.max || Math.max(chart.getOption().series[0].max, displayValue);
  const ratio   = displayValue / newMax;

  chart.setOption({
    series: [{
      max: newMax,
	  detail : { formatter: () => cfg.formatter(displayValue) },
      data: [{ value: displayValue, time: `${data.startTime} ~ ${data.endTime}` }],
      axisLine: {
        lineStyle: {
          color: [
            [ratio, cfg.color],
            [1, '#d9d9d9']
          ]
        }
      }
    }]
  });
}

// 2) 설정(config) 배열
const gaugeConfigs = [
  {
    containerId: 'chart-attackCounts',
    chartId:     'attackCounts',
    title:       '실시간 공격 건수 (최근 1시간)',
    color:       'rgba(255,77,79,0.9)',
    max:         100,
	// 응답 JSON에서 읽어올 값의 키
	valueKey:    'count',
	// 단위 포맷터 (숫자 뒤에 붙일 단위)
	formatter:   v => `${v}건`
  },
  {
    containerId: 'chart-blockCounts',
    chartId:     'blockCounts',
    title:       '실시간 차단 건수 (최근 1시간)',
    color:       'rgba(24,144,255,0.9)',
    max:         100,
	valueKey:    'count',
	formatter:   v => `${v}건`
  },
  // 필요하면 여기 새 객체를 하나만 추가하세요!
  {
    containerId: 'chart-blockRatio',
    chartId:     'blockRatio',
    title:       '실시간 차단 비율 (최근 1시간)',
    color:       'rgba(82,196,26,0.9)',
    max:         100,
    valueKey:    'ratio',
    formatter:   v => `${v.toFixed(1)}%`
  }
];

// 3) DOMContentLoaded 에서 초기화 + 주기 갱신
document.addEventListener('DOMContentLoaded', () => {
  const DEFAULT_MINUTES  = 60;
  const REFRESH_INTERVAL = 5_000; // 5초

  gaugeConfigs.forEach(cfg => {
    const chart = initGauge(cfg);
    // 즉시 업데이트
    updateGauge(chart, cfg, DEFAULT_MINUTES);
    // 주기 갱신
    setInterval(() => updateGauge(chart, cfg, DEFAULT_MINUTES),
                REFRESH_INTERVAL);
  });
});
