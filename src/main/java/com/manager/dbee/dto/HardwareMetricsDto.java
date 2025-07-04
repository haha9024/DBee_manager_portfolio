package com.manager.dbee.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data 
@AllArgsConstructor
public class HardwareMetricsDto {

	private String timestamp;    // "HH:mm:ss"
	private double cpuLoad;      // 0.0 ~ 1.0
	private long totalMemory;    // bytes
	private long usedMemory;     // bytes
	private String host;
}
