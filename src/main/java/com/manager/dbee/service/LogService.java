package com.manager.dbee.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;      // ← 요게 핵심
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.manager.dbee.dao.log.LogChartDAO;
import com.manager.dbee.dao.log.LogDAO;
import com.manager.dbee.dto.Log;
import com.manager.dbee.dto.LogFilter;
import com.manager.dbee.exception.TableNotFoundException;


@Service
public class LogService {

	private final LogDAO logDAO;
	
	private final LogChartDAO logChartDAO;
	
	public LogService(LogDAO logDAO, LogChartDAO logChartDAO) {
		this.logDAO = logDAO;
		this.logChartDAO = logChartDAO;
	}
	

    /**
     * 페이징된 로그 조회
     * @param filter  기존 LogFilter (offset/limit 은 여기서 세팅)
     * @param pageable  Spring Data Pageable (page, size, sort 정보)
     * @return Page<Log>
     */
	@Transactional(transactionManager = "logTransactionManager", readOnly = true)
	public Page<Log> getLogs(LogFilter filter, Pageable pageable){
		
		// ==== 0) 날짜/시간 필터 결합 로직 ====
        
		LocalDate today = LocalDate.now();
		LocalTime now = LocalTime.now();
        
		LocalDate dateToUse;
        LocalTime startTime;
        LocalTime endTime;
  
        // 1) 사용할 날짜 결정: user 입력(logDate) → 아니면 오늘자, 입력이 오늘 이후면 에러
        dateToUse = ( filter.getLogDate() != null ) ? filter.getLogDate() : today;
        if(dateToUse.isAfter(today)) {
        	throw new IllegalArgumentException("로그 날짜는 오늘과 오늘 이전만 선택 가능합니다");
        }
        
        // 2) startTime 기본값: 입력 없으면 00:00
        // dateToUse가 오늘인데 입력 시간이 현재 시각보다 미래면 에러
        startTime = filter.getStartTime() != null ? filter.getStartTime() : LocalTime.MIN;
        if (dateToUse.equals(today) && startTime.isAfter(now)) {
            throw new IllegalArgumentException("시작시간은 현재 시각 이전만 선택 가능합니다.");
        }

        
        // 3) endTime 검증, 기본값은 오늘일 경우 현재 시각, 과거 날짜는 23:59:59
        if(filter.getEndTime() != null ) {
        	endTime = filter.getEndTime();
        	
        	// (a) 오늘인데 미래시간 선택 금지
            if (dateToUse.equals(today) && endTime.isAfter(now)) {
                throw new IllegalArgumentException("종료시간은 현재 시각 이전만 선택 가능합니다.");
            }
            
            // (b) 종료시간 < 시작시간 금지
            if (endTime.isBefore(startTime)) {
                throw new IllegalArgumentException("종료시간은 시작시간 이후로 선택해야 합니다.");
            }
        } else {
            // 입력 없을 때 기본: 오늘이면 now, 과거면 23:59:59
            endTime = dateToUse.equals(today)
                ? now.truncatedTo(ChronoUnit.SECONDS)
                : LocalTime.MAX.truncatedTo(ChronoUnit.SECONDS);
        }
        
        // 4) DTO 에 LocalDateTime 결합
        filter.setStartDate(dateToUse.atTime(startTime));
        filter.setEndDate  (dateToUse.atTime(endTime));
        

        // ==== 1) 테이블명 결정 (log_YYYYMMDD) ====
        if (!StringUtils.hasText(filter.getTableName())) {
            String suffix = dateToUse.format(DateTimeFormatter.BASIC_ISO_DATE);
            String tblName = "log_" + suffix;
            if (logChartDAO.checkLogTableExists("IPS_Log", tblName) == 0) {
                throw new TableNotFoundException(
                    "로그 테이블 '" + tblName + "'을(를) 찾을 수 없습니다."
                );
            }
            filter.setTableName(tblName);
        }

		
		// 1) Pageable 에서 offset/limit 계산
		int offset = (int)pageable.getOffset();
		int limit = pageable.getPageSize();
		
        filter.setOffset(offset);
        filter.setLimit(limit);
		
        // 2) 실제 데이터 조회
        List<Log> content = logDAO.selectLogs(filter);
        
        // 3) 전체 건수 조회
        long total = logDAO.countLogsbyPagination(filter);
        
        // 4) PageImpl 로 감싸서 반환
		return new PageImpl<>(content, pageable, total);
		
		//return logDAO.selectLogs(filter);
	}
}
