package com.manager.dbee.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.manager.dbee.dao.log.LogChartDAO;
import com.manager.dbee.dto.ChartFilterDto;
import com.manager.dbee.dto.TableFilterDto;
import com.manager.dbee.exception.NoDataFoundException;
import com.manager.dbee.exception.TableNotFoundException;

@Service
@Transactional(readOnly = true)
public class ChartService {

	@Autowired
	private LogChartDAO logChartDAO;

	public ChartService(LogChartDAO logChartDAO) {
		this.logChartDAO = logChartDAO;
	}

	/*
	 * public List<Map<String, Object>> getTopCountryCntAtDate(
	 * 	LocalDate selectedDate, int startHour, int endHour) { 
	 * 		ChartFilterDto dto = new ChartFilterDto(); 
	 * 		dto.setDate(selectedDate.toString());
	 * 		dto.setStartHour(startHour); 
	 * 		dto.setEndHour(endHour); 
	 * 		dto.setTopN(10); // 기본값
	 * 		return getTopChartByFilter("countryTop10", dto); 
	 * }
	 */

	public List<Map<String, Object>> getTopChartByFilter(String chartId, ChartFilterDto dto) {

		LocalDate date = LocalDate.parse(dto.getDate());
		int startHour = dto.getStartHour();
		int endHour = dto.getEndHour();

		// 공통 검증
		validateDateAndTime(date, startHour, endHour);

		// 현재 시간이 오늘이라면 종료시간 제한
		if (date.equals(LocalDate.now())) {
			endHour = Math.min(endHour, LocalDateTime.now().getHour());
			dto.setEndHour(endHour); // 💡 잊지 말고 DTO에도 반영
		}

		dto.buildDateRange(); // 반드시 필요!
		
		// 테이블명 확인 후 params 준비
		Map<String, Object> params = resolveAndValidateTableName(date);
		params.put("startDate", dto.getStartDate());
		params.put("endDate", dto.getEndDate());
		params.put("topN", dto.getTopN());
		
		List<Map<String, Object>> rawIpList = logChartDAO.selectTopSrcIpCounts(params);
		

		System.out.println(
				"▶ DTO startDate type/value: " + dto.getStartDate() + " (" + dto.getStartDate().getClass() + ")");
		System.out.println("▶ DTO endDate type/value: " + dto.getEndDate() + " (" + dto.getEndDate().getClass() + ")");


		//params.put("startDate", dto.getStartDate()); // ✅ String으로 들어가야 MyBatis에서 인식 가능
		//params.put("endDate", dto.getEndDate());
		//params.put("topN", dto.getTopN());


		switch (chartId) {
		//case "countryTop10":
			//return logChartDAO.selectTopSrcIpCounts(params);
		case "p_idTop10":
			return logChartDAO.selectTopPid(params);
		case "policyTypeStats":
			return logChartDAO.selectPolicyTypeStats(params);
		case "ThreatLevelStats":
			return logChartDAO.selectThreatLevelStats(params);
		// case "portTop10": return logChartDAO.selectTopPorts(params);
		case "HourlyStats":
			List<Map<String, Object>> rawData = logChartDAO.selectHourlyStats(params);
			
			// 1. DB에서 온 데이터를 "01"->건수 형태로 매핑
			Map<String, Integer> countMap = rawData.stream()
			    .collect(Collectors.toMap(
			        entry -> (String) entry.get("hour"),
			        entry -> ((Number) entry.get("cnt")).intValue()
			    ));
			
			return generateHourlyChartData(
					LocalDateTime.parse(dto.getStartDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
					LocalDateTime.parse(dto.getEndDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
					hourStr -> {
						Map<String, Object> m = new HashMap<>();
						m.put("cnt", countMap.getOrDefault(hourStr, 0));
						return m;
					}
				);
			
		case "HourlyPolicyStats":
			List<Map<String, Object>> rawData2 = logChartDAO.selectHourlyPolicyStats(params);
			
			// 결과 예시: [{hour=15, policy_type=BEHAVIOR, cnt=3}, {hour=15, policy_type=PATTERN, cnt=2}, ...]
			
			// 시간(hour) -> 정책 유형(BEHAVIOR / PATTERN) -> 건수(cnt)
			Map<String, Map<String, Integer>> reshaped = new LinkedHashMap<>();
			
			for(Map<String, Object> row : rawData2) {
				String hour = String.valueOf(row.get("hour")); // 예 : "15"
				String policyType = String.valueOf(row.get("policy_type")); // 예: "BEHAVIOR", "PATTERN"
				int count = ((Number)row.get("cnt")).intValue();
				
				
				reshaped.computeIfAbsent(hour, k -> new HashMap<>()).put(policyType, count);
			}
			
			return generateHourlyChartData(
			        LocalDateTime.parse(dto.getStartDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
			        LocalDateTime.parse(dto.getEndDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
			        hourStr -> {
			            Map<String, Integer> typeCounts = reshaped.getOrDefault(hourStr, new HashMap<>());
			            Map<String, Object> m = new HashMap<>();
			            m.put("BEHAVIOR", typeCounts.getOrDefault("BEHAVIOR", 0));
			            m.put("PATTERN", typeCounts.getOrDefault("PATTERN", 0));
			            return m;
			        }
			    );

		case "HourlyThreatStats":
			List<Map<String, Object>> rawData3 = logChartDAO.selectHourlyThreatStats(params);
			Map<String, Map<String, Integer>> reshaped2 = new LinkedHashMap<>();
			
			for(Map<String, Object> row : rawData3) {
				String hour = String.valueOf(row.get("hour"));
				String threatLevel = String.valueOf(row.get("threat_level"));
				int count = ((Number)row.get("cnt")).intValue();
				
				reshaped2.computeIfAbsent(hour, k -> new HashMap<>()).put(threatLevel, count);
			}
			
			return generateHourlyChartData(
					LocalDateTime.parse(dto.getStartDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
					LocalDateTime.parse(dto.getEndDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
					hourStr -> {
						Map<String, Integer> typeCounts = reshaped2.getOrDefault(hourStr, new HashMap<>());
						Map<String, Object> map = new HashMap<>();
						map.put("LOW", typeCounts.getOrDefault("LOW", 0));
						map.put("MEDIUM", typeCounts.getOrDefault("MEDIUM", 0));
						map.put("HIGH", typeCounts.getOrDefault("HIGH", 0));
						map.put("CRITICAL", typeCounts.getOrDefault("CRITICAL", 0));
						return map;
					}
					);
		case "srcIp_top10":	
			//return getTopTableByFilter("srcIp_top10", new TableFilterDto(dto.getDate(), dto.isRealtime(), dto.getTopN()));
			
			// 1) TableFilterDto 인스턴스 생성
			TableFilterDto tableFilter = new TableFilterDto();
			
		    // 2) ChartFilterDto(dto)의 값 복사
		    tableFilter.setDate(dto.getDate());
		    tableFilter.setRealtime(dto.isRealtime());
		    tableFilter.setTopN(dto.getTopN());
		    
		    // 3) startDate/endDate 세팅
		    tableFilter.buildRange();
		    
		    // 4) 기존 TableService 로직 재사용
		    return getTopTableByFilter("srcIp_top10", tableFilter);
		default:
			throw new IllegalArgumentException("지원하지 않는 차트 ID: " + chartId);
		}
	}

	
	private void validateDateAndTime(LocalDate selectedDate, int startHour, int endHour) {
		LocalDate today = LocalDate.now();
		int currentHour = LocalDateTime.now().getHour();

		if (selectedDate.isAfter(today)) {
			throw new IllegalArgumentException("오늘 날짜 이후의 데이터는 조회할 수 없습니다.");
		}
		if (startHour > endHour) {
			throw new IllegalArgumentException("시작 시각은 종료 시각보다 이후일 수 없습니다.");
		}
		if (selectedDate.equals(today) && endHour > currentHour) {
			throw new IllegalArgumentException("현재 시각 이후의 데이터는 조회할 수 없습니다.");
		}
	}

	private Map<String, Object> resolveAndValidateTableName(LocalDate date) {
		String tableName = "log_" + date.format(DateTimeFormatter.BASIC_ISO_DATE);
	
	    try {
	        int exists = logChartDAO.checkLogTableExists("IPS_Log", tableName);
	        if (exists == 0) {
	            // 단순히 “없는 테이블”이라면 단일 생성자 쓰기
	            throw new TableNotFoundException(
	                "로그 테이블 '" + tableName + "'을(를) 찾을 수 없습니다."
	            );
	        }
	    } catch (DataAccessException dae) {
	        // DB 에러 등 하위 예외 보존하고 싶으면 cause 전달
	        throw new TableNotFoundException(
	            "로그 테이블 존재 여부 확인 중 DB 오류: " + tableName, dae);
	    }
	    
	    // 디버깅용
	    System.out.println("<테이블명 확인용>> resolve tableName = " + tableName);
	    
	    // 변경: mutable HashMap 사용
	    Map<String, Object> params = new HashMap<>();
	    params.put("tableName", tableName);
	    
	  
	   
	    return params;
	}
	
	/*
	 * 시간대별 데이터를 채워서 누락 없는 시간대 리스트를 리턴한다
	 * @Param start 시작 시간 (LocalDateTime)
	 * @Param end 종료 시간 (LocalDateTime)
	 * @Param valueMapper 각 시간대에 맞는 Map 데이터를 구성하는 함수
	 * @return 시간대별 차트 데이터 리스트
	 */
	private List<Map<String, Object>> generateHourlyChartData(
			LocalDateTime start, LocalDateTime end,
			Function<String, Map<String, Object>> valueMapper){
	
		DateTimeFormatter hourFormatter = DateTimeFormatter.ofPattern("HH");
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		
		String dateStr = start.format(dateFormatter);
		List<Map<String, Object>> result = new ArrayList<>();
		
		for(LocalDateTime t = start.withMinute(0).withSecond(0); !t.isAfter(end); t=t.plusHours(1)) {
			String hourStr = t.format(hourFormatter);
			
			Map<String, Object> item = new HashMap<>();
			item.put("hour", hourStr);
			item.put("date", dateStr);
			
			// 추가 데이터 넣기
			item.putAll(valueMapper.apply(hourStr));
			
			result.add(item);
		}
		return result;
	}
	
	public List<Map<String, Object>> getAvgChartByFilter(String chartId, ChartFilterDto dto){
	    
		// 1) buildDateRange 로 이미 dto.startDate/ dto.endDate 가 "yyyy-MM-dd HH:mm:ss" 형태로 세팅됨
		// 예전) ❌ dto.getDate() 에 의존하여 단일 날짜를 파싱
	    // dto.buildDateRange();
	    
	    
	    // 2) 날짜 부분만 잘라서(예: "2025-06-27") 기간 전체 log 테이블 리스트 생성
	    String startDay = dto.getStartDate().substring(0, 10);
	    String endDay = dto.getEndDate().substring(0, 10);
	    
	    
	    // 3) 기간 내 모든 날짜의 테이블명 리스트
	    List<String> allTables = generateTableNamesForRange(startDay, endDay);
	    //List<String> tableNames = generateTableNamesForRange(startDay, endDay);
	    
	    
	    // 4) 실제 존재하는 테이블만 필터링
	    List<String> existings = allTables.stream()
	            .filter(tbl -> logChartDAO.checkLogTableExists("IPS_Log", tbl) > 0)
	            .collect(Collectors.toList());
	    
	    // 5) 만약 아예 없으면, 빈 평균(0)도 보여주려면 아래처럼 정책 전체 0 채움 로직을 추가하거나,
        //    지금처럼 빈 리스트를 리턴해도 좋습니다.
        if (existings.isEmpty()) {
            return Collections.emptyList();
        }
	    
	    // 3) 모든 테이블을 UNION 하기 위한 파라미터 구성
	    Map<String, Object> params = new HashMap<>();
	    params.put("tableNames", existings);	// 변경점: 복수 테이블
	    params.put("startDate", dto.getStartDate());
	    params.put("endDate", dto.getEndDate());
	    params.put("topN", dto.getTopN());
	    
		System.out.println(
				"▶ DTO startDate type/value: " + dto.getStartDate() + " (" + dto.getStartDate().getClass() + ")");
		System.out.println("▶ DTO endDate type/value: " + dto.getEndDate() + " (" + dto.getEndDate().getClass() + ")");	
	    
	    // 4) MyBatis ⟨foreach collection="tableNames"⟩ 매퍼 호출
	    if ("TopPolicyAvgViolations".equals(chartId)) {
	    	return logChartDAO.selectTopPolicyAvgViolations(params);
	    }
	    
	    
	    throw new IllegalArgumentException("지원하지 않는 평균 차트입니다: " + chartId);
	    
		
	    // 현재 시간이 오늘이라면 종료시간 제한

		
	}
	
	// 기간에 맞는 로그테이블 이름 찾기
	private List<String> generateTableNamesForRange(String startDate, String endDate) {
	    List<String> tableNames = new ArrayList<>();
	    
	    // startDate ~ endDate 범위에 해당하는 날짜 리스트 생성
	    LocalDate start = LocalDate.parse(startDate);
	    LocalDate end = LocalDate.parse(endDate);
	    while (!start.isAfter(end)) {
	        tableNames.add("log_" + start.format(DateTimeFormatter.BASIC_ISO_DATE));
	        start = start.plusDays(1);
	    }

	    return tableNames;
	}
	
	public List<Map<String, Object>> getTopTableByFilter(
			String tableId, 
			TableFilterDto filter)
	{
		
		// 1) LocalDate 형으로 바꾸기 
		LocalDate reqDate;
		try {
			reqDate = LocalDate.parse(filter.getDate());
		    // (선택) 로그나 디버깅을 위해 파싱 결과 출력
	        // log.debug("Parsed date: {}", reqDate);
		} catch (DateTimeParseException e) {
			
			// e.getParsedString() → 파싱 시도했던 원본 문자열
			// e.getErrorIndex()  → 에러가 발생한 인덱스 위치
			String detail = String.format("입력값: '%s', 에러위치= '%d', 원본예외메시지= '%s'",
					e.getParsedString(), e.getErrorIndex(), e.getMessage());
			
			throw new IllegalArgumentException("date 파라미터 형식은 yyyy-MM-dd 이어야 합니다: " + filter.getDate(), e);
		}
		
				
		// 2) 위에서 만든 reqDate로 테이블명 검증 (없으면 TableNotFoundException 던짐)
		Map<String, Object> params = resolveAndValidateTableName(reqDate);
		System.out.printf(">> Using tableName = %s%n", params.get("tableName"));

		
		// 3) 파라미터 세팅 (DTO의 startDate/endDate(strings)을 그대로 사용)
		params.put("startDate", filter.getStartDate());
		params.put("endDate", filter.getEndDate());
		params.put("topN", filter.getTopN());
		
		
		// tableId와 realtime 플래그에 따라 DAO 호출
		switch(tableId) {
		case "srcIp_top10":
			List<Map<String, Object>> data = filter.isRealtime() 
					? logChartDAO.selectTopSrcIpRealTime(params)
					: logChartDAO.selectTopSrcIpCounts3(params);
			
			if (data.isEmpty()) {
				throw new NoDataFoundException();
			}
			
			return data;
			
		default:
			throw new IllegalArgumentException("지원하지 않는 테이블: " + tableId);
		}
		
		/*int checkLogTableExists(@Param("schema") String schema, @Param("tableName") String tableName);*/
	}
}
