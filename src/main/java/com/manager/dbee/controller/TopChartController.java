package com.manager.dbee.controller;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.manager.dbee.dto.ChartFilterDto;
import com.manager.dbee.service.ChartService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class TopChartController {

	private final ChartService chartService;
	
	/*
	 * [기본 차트] 오늘 날짜 기준, 0시부터 현재 시각까지의 Top N 차트들 첫 로딩 시 호출됨
	 * 페이지 첫 로딩 시 호출됨, 서비스에서 차트 아이디로 분기 보여줌
	 */
	@GetMapping("top/{chartId}/default")
    public ResponseEntity<?> getDefaultChart(@PathVariable("chartId") String chartId) {
        try {
            // 어제 날짜 기준 기본 필터 생성
            LocalDate yesterday = LocalDate.now().minusDays(4);
            ChartFilterDto dto = new ChartFilterDto();
            dto.setDate(yesterday.toString());
            dto.setStartHour(0);
            dto.setEndHour(23);
            
            System.out.println("▶ 차트 요청 chartId: " + chartId);
 
            List<Map<String, Object>> result = chartService.getTopChartByFilter(chartId, dto);
            
            // 에러 점검용 
            System.out.println("차트 결과: " + result);  // 여기에서 null인지 확인
            if(result == null) {
            	result = Collections.emptyList();
            }

            return ResponseEntity.ok(result);
            
        } catch (IllegalStateException e) {
            return ResponseEntity.ok(Collections.emptyList()); // 테이블 없으면 빈 리스트로 처리
        } catch (Exception e) {
           
        	// 문제 가능성 있음!
        	// return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류");
        	
        	// 그래서 바꿈
        	return ResponseEntity.ok(Collections.emptyList());
        }
    }
	
	// [필터 설정 차트] 날짜 선택, 시간대 선택해서 설정에 따라 top N 차트들 보여줌
	@PostMapping("top/{chartId}/data")
	public ResponseEntity<?> getTopNChartWithFilter(
			@PathVariable String chartId,
			@RequestBody ChartFilterDto filterDto) {
		
		try {
			List<Map<String, Object>> result = chartService.getTopChartByFilter(chartId, filterDto);
			return ResponseEntity.ok(result);
		} catch(IllegalStateException e) {
			return ResponseEntity
					.status(HttpStatus.BAD_REQUEST)
					.body(e.getMessage());	// 예: "해당 날짜의 로그 기록이 없습니다."
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity
					.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("로그 기록이 없습니다.");
					/*body("서버 오류 발생: "+e.getMessage());*/
		}
		
	}
	
}
