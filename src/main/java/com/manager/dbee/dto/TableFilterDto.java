package com.manager.dbee.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TableFilterDto {
	private String date;
	private boolean realtime; // 오늘 전체 vs 최근 1시간
	private int topN = 10;
	
	// 실제 쿼리용 범위
	private String startDate;	// yyyy-MM-dd HH:mm:ss
	private String endDate;
	
	public void buildRange() {
		LocalDate req = LocalDate.parse(date);
		LocalDate today = LocalDate.now();
		
		DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		
		if(req.equals(today) && realtime) {
			// 실시간 모드: 최근 1시간
			LocalDateTime end = LocalDateTime.now();
			LocalDateTime start = end.minusHours(1);
			this.startDate = start.format(fmt);
			this.endDate = end.format(fmt);
		}
		else {
			// 전체 모드(오늘 전체 혹은 과거 날짜 전체)
			LocalDateTime start = req.atStartOfDay();
			LocalDateTime end = req.atTime(23, 59, 59);
			this.startDate = start.format(fmt);
			this.endDate = end.format(fmt);
		}
	}
}
