package com.manager.dbee.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RealTimeChartResponse {

	  private String chartId;
	  private Integer count = 0;		// count 기반 차트용(공격/차단 건수)
	  private Double ratio;		// 비율 기반 차트용(차단 비율)
	  private String startTime;
	  private String endTime;
	  
	  // 0708 추가: 동적 임계치
	  private double threshold;
}
