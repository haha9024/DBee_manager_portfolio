package com.manager.dbee.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 최근 N분 또는 오늘 전체 차단 비율 결과를 담는 DTO
 */
@Data
@AllArgsConstructor
public class BlockRatioResponse {
    private int    blockCnt;    // 차단 건수
    private int    totalCnt;    // 전체 건수
    private double blockRatio;  // 차단 비율 %
}

