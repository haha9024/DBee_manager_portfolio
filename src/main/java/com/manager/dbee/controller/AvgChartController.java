package com.manager.dbee.controller;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.manager.dbee.dto.ChartFilterDto;
import com.manager.dbee.service.ChartService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/dashboard2")
@RequiredArgsConstructor
public class AvgChartController {

	private final ChartService chartService;
	
	// 기본 평균 차트
	@GetMapping("/average/{chartId}/default")
	public ResponseEntity<?> getDefaultAverageChart(@PathVariable("chartId") String chartId) {
		 System.out.println("▶▶▶ 평균 컨트롤러 진입: chartId=" + chartId);
		try {
			
			// 오늘(나중에 하자)
			// LocalDate endDate = LocalDate.now();
			
			// 일단 데이터 있는 날짜를 찾아서
			LocalDate endDate = LocalDate.now().minusDays(12);
			LocalDate startDate = endDate.minusDays(6); // 오늘 또는 endDate 포함해서 7일치
			
			ChartFilterDto dto = new ChartFilterDto();
			dto.buildDateRange(startDate, endDate);
			dto.setTopN(10);
			
			//System.out.println("▶ 차트 요청 chartId: " + chartId);
			
			List<Map<String, Object>> data = chartService.getAvgChartByFilter(chartId, dto);
			
			// 에러 점검용
			System.out.println("📊 평균 차트 요청 - chartId: "+ chartId + "차트 결과: " + data);  // 여기에서 null인지 확인
			
			// DTO에 세팅된 범위(startDate, endDate)를 그대로 돌려줍니다.
		       Map<String, Object> payload = new HashMap<>();
		        payload.put("startDate", dto.getStartDate());
		        payload.put("endDate",   dto.getEndDate());
		        payload.put("data",      data);
			
			return ResponseEntity.ok(payload);
			
		} catch (IllegalStateException e) {
			return ResponseEntity.ok(Collections.emptyList()); // 테이블 없으면 빈 리스트로 처리
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.ok(Collections.emptyList()); // 임시 처리
		}
	}
}
