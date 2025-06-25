package com.manager.dbee.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ChartFilterDto {

	private String date; // yyyy-MM-dd 형식
	private int startHour;
	private int endHour;
	private int topN = 10; // 기본값 10
	private int interval = 1; // 기본값 1시간
	private String sortBy = "count"; // 정렬기준, 기본 count

	private String startDate; // yyyy-MM-dd HH:mm:ss
    private String endDate;

    // 날짜와 시간 기반으로 startDate, endDate 세팅하는 메서드 예)
    public void buildDateRange() {
        LocalDateTime start = LocalDateTime.parse(date + "T00:00:00")
            .withHour(startHour).withMinute(0).withSecond(0);
        LocalDateTime end = LocalDateTime.parse(date + "T00:00:00")
            .withHour(endHour).withMinute(59).withSecond(59);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.startDate = start.format(formatter);
        this.endDate = end.format(formatter);
    }


	// 추후 확장: 차트의 시간 간격 단위 (ex. 1시간, 2시간, 3시간 단위 차트)
	// 나중에 관리자가 직접 선택하게 한다
	// private int intervalHour = 1;
}
