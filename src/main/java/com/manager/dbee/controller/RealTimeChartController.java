package com.manager.dbee.controller;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.manager.dbee.dto.BlockRatioResponse;
import com.manager.dbee.dto.RealTimeChartResponse;
import com.manager.dbee.service.RealTimeChartService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/charts/realtime")
@RequiredArgsConstructor
public class RealTimeChartController {

	private static final DateTimeFormatter HH_MM = 
		    DateTimeFormatter.ofPattern("HH:mm");
	
	private final RealTimeChartService realtimeChartService;

	@GetMapping("/{chartId}")
	public ResponseEntity<RealTimeChartResponse> getRealtimeChart(@PathVariable("chartId") String chartId,
			@RequestParam(name = "minutes", defaultValue = "60") int minutes) {

		log.debug("RealtimeChart: chartId={}, minutes={}", chartId, minutes);
		
		// 1) minutes 범위 직접 검증
		if (minutes < 1 || minutes > 1440) {
			throw new IllegalArgumentException("minutes 값은 1~1440 사이여야 합니다.");
		}
		
		Integer count = null;
		Double ratio = null;

		// 2) 차트 아이디 검증 및 분기
		switch (chartId) {
		case "attackCounts":
			count = realtimeChartService.getRecentCount(minutes);
			break;
		case "blockCounts":
			count = realtimeChartService.getBlockRecentCount(minutes);
			break;
		case "blockRatio":
			BlockRatioResponse br = realtimeChartService.getBlockRatioLastMinutes(minutes);
			ratio = br.getBlockRatio();
			break;
		default:
			// return ResponseEntity.badRequest().body(Map.of("error", "지원하지 않는 실시간 차트: " +
			// chartId));
			throw new IllegalArgumentException("지원하지 않는 실시간 차트: " + chartId);
		}

        // 3) 동적 임계치 계산 (지난 30일 기준)
        double threshold = realtimeChartService.computeThresholdLastNDays(30);
		
        
        // 4) 시간 범위
		LocalDateTime end = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
		LocalDateTime start = end.minusMinutes(minutes);
		String startTime = start.format(HH_MM);
		String endTime = end.format(HH_MM);
        
		// 5) DTO에 담아서 반환
		RealTimeChartResponse body = new RealTimeChartResponse(chartId, count, ratio, startTime, endTime, threshold);

		return ResponseEntity.ok(body);
	}

}
