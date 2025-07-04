package com.manager.dbee.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.manager.dbee.dto.TableFilterDto;
import com.manager.dbee.service.ChartService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j 
@RestController
@RequestMapping("/dashboard2")
@RequiredArgsConstructor
public class TopTableController {

	private final ChartService chartService;
	
	@GetMapping("top/table/{tableId}/default")
	public ResponseEntity<?> getDefaultTable(
			@PathVariable("tableId") String tableId,
			@ModelAttribute TableFilterDto filter	// 이 한 줄로 date, realtime, topN 바인딩
			)
	{
		
	       // 로그 찍기 (IDE 콘솔에서 확인)
        log.info("▶ getDefaultTable: date={} realtime={} topN={}",
                 filter.getDate(), filter.isRealtime(), filter.getTopN());

		
		LocalDate defaultDate = LocalDate.now().minusDays(12);
		if(filter.getDate() == null || filter.getDate().isBlank()) {
			filter.setDate(defaultDate.toString());
		}
		
		// realtime 자동 세팅(오늘이면 true)
		LocalDate req = LocalDate.parse(filter.getDate());
		filter.setRealtime(req.equals(LocalDate.now()));
		
		// dto로 startDate, endDate 계산
		filter.buildRange();
		
		try {
			// 1) 서비스 호출: tableId와 filter에 따라 분기 처리
			List<Map<String, Object>> rows = chartService.getTopTableByFilter(tableId, filter);
			
			// 2) 데이터가 단 1건도 없으면
			if(rows.isEmpty()) {
				return ResponseEntity.ok(Map.of("message", "데이터가 없습니다"));
			}
			
			// 3) 정상 응답: date, realtime, date 함께 반환
			Map<String, Object> payload = Map.of(
					"date", filter.getDate(),
					"realtime", filter.isRealtime(),
					"topN", filter.getTopN(),
					"data", rows
					);
			
			return ResponseEntity.ok(payload);
		
		} catch (IllegalStateException e) {
			// 로그테이블 자체가 없으면 서비스에서 던진 예외
			return ResponseEntity
					.status(HttpStatus.BAD_REQUEST)
					.body(e.getMessage()); 
		}
	}	
}
