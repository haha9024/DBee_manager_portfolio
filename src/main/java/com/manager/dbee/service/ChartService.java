package com.manager.dbee.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.manager.dbee.dao.log.LogChartDAO;
import com.manager.dbee.dto.ChartFilterDto;

@Service
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
		// case "portTop10": return logChartDAO.selectTopPorts(params);
		// case "hourTop10": return logChartDAO.selectTopHours(params);
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
		String tableName = "log_" + date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		// String tableName = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

		int exists = logChartDAO.checkLogTableExists("IPS_Log", tableName);
		if (exists == 0) {
			throw new IllegalStateException("해당 날짜의 로그 기록이 없습니다.");
		}
		
		Map<String, Object> params = new HashMap<>();
		params.put("tableName", tableName);
		return params;
	}
	
}
