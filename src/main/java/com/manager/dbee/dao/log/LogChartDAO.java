package com.manager.dbee.dao.log;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LogChartDAO {

	// 0619 로그 테이블 존재하나 체크
	// 반환값 0: 해당 테이블 없음
	int checkLogTableExists(@Param("schema") String schema, @Param("tableName") String tableName);
	
	

	// 0618 왜 Map<>에 개수 뽑아야 하는데 객체가 들어가나? 
	// MyBatis가 SQL에서 가져온 각 컬럼의 데이터 타입을 미리 알 수 없음-> 결과를 문자열, 객체로 자동 매핑
	
	// 정적 통계-> 특정 날짜(단일 날짜)의 공격 국가 아이피 top 10
	public List<Map<String, Object>> selectTopSrcIpCounts(Map<String, Object> params);
	
	public List<Map<String, Object>> selectTopSrcIpCounts2(Map<String, Object> params);
	
	// 동적 통계-> 선택한 날짜 범위의 공격 국가 아이피 top 10(1일, 3일, 1주일->오늘 제외하고)
	public List<Map<String, Object>> selectTopCountryCountsByTables(Map<String, Object> params);
	
	// 정적 통계 -> 특정 날짜(단일 날짜)의 p_id 위반 top 10
	public List<Map<String, Object>> selectTopPid(Map<String, Object> params);
	
	// 특정 날짜 로그의 src_ip가 정책 범위 내에 있는지 매칭된 데이터 가져오기
    List<Map<String, Object>> selectLogWithPolicyByDate(Map<String, Object> params);
    
    // 정적 통계 -> 특정 날짜 로그의 정책 타입 위반 비율
    List<Map<String, Object>> selectPolicyTypeStats(Map<String, Object> params);
	
    // 정적 통계 -> 특정 날짜 로그의 위험도 위반 비율
    List<Map<String, Object>>selectThreatLevelStats(Map<String, Object> params);
    
    // 정적 통계 -> 특정 날짜 로그의 시간대별 탐지 수 (1시간 단위)
    List<Map<String, Object>> selectHourlyStats(Map<String, Object> params);
    
    // 정적 통계 -> 특정 날짜 로그의 시간대별 정책 타입 탐지 분포 (1시간 단위)
    List<Map<String, Object>> selectHourlyPolicyStats(Map<String, Object> params);
    
    // 정적 통계 -> 특정 날짜 로그의 시간대별 위험도 탐지 분포 (1시간 단위)
    List<Map<String, Object>> selectHourlyThreatStats(Map<String, Object> params);
    
    // 동적? 통계 -> 기간별 로그의 정책별 평균 위반 수 top 10 (기본값 일주일)
    List<Map<String, Object>> selectTopPolicyAvgViolations(Map<String, Object> params);
    
    // 오늘 + 실시간 모드: 최근 1시간 간의 공격자 IP TopN
    List<Map<String, Object>> selectTopSrcIpRealTime(Map<String, Object> params);
    
    List<Map<String, Object>> selectTopSrcIpCounts3(Map<String, Object> params);
}


