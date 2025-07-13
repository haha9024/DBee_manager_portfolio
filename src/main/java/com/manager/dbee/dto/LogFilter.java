package com.manager.dbee.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import lombok.Data;

/**
 * 로그 조회 필터용 DTO
 */
@Data
public class LogFilter {
    private String tableName;          // 동적 테이블명 (보안 검증 후 세팅)
    private LocalDateTime startDate;   // 탐지 시각 시작
    private LocalDateTime endDate;     // 탐지 시각 종료
    
    //private String logDate;
    private LocalDate logDate; 
    private LocalTime startTime;
    private LocalTime endTime; 

    private String srcIp;
    private String srcIpStart;
    private String srcIpEnd;

    private String targetIp;
    private String targetIpStart;
    private String targetIpEnd;

    private List<String> protocolList;
    private List<String> threatLevelList;

    private String pId;
    private List<String> pIdList;

    private String action;
    private List<String> actionList;   // "PERMIT", "BLOCK"

    private String policyType;

    private String orderBy;            // "ASC" / "DESC"
    
    // 페이지네이션용
    private Integer offset;
    private Integer limit;

    
}
