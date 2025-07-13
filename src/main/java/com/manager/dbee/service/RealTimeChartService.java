package com.manager.dbee.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.manager.dbee.dao.log.LogChartDAO;
import com.manager.dbee.dto.BlockRatioResponse;
import com.manager.dbee.exception.TableNotFoundException;

@Service
@Transactional(readOnly = true)
public class RealTimeChartService {

	@Autowired
	private LogChartDAO logChartDAO;
	
	public RealTimeChartService(LogChartDAO logChartDAO) {
		this.logChartDAO = logChartDAO;
	}

	/** 오늘 최근 N분 건수 **/
	public int getRecentCount(int lastMinutes) {
		String tableName = resolveTodayTableName(LocalDate.now());
		return logChartDAO.selectRecentCount(tableName, lastMinutes);
	}
	
    /** 오늘 최근 N분 차단 건수 조회 (blockCounts) */
	public int getBlockRecentCount(int lastMinutes) {
		String tableName = resolveTodayTableName(LocalDate.now());
		return logChartDAO.selectBlockCount(tableName, lastMinutes);
	}
	
	/* 오늘 최근 N분 차단 비율 조회 (BlockRatio) */
	public BlockRatioResponse getBlockRatioLastMinutes(int lastMinutes) {
		String TableName = resolveTodayTableName(LocalDate.now());
		return logChartDAO.selectBlockRatioByLastMinutes(TableName, lastMinutes);
	}
	
	private String resolveTodayTableName(LocalDate date) {
		String tableName = "log_" + date.format(DateTimeFormatter.BASIC_ISO_DATE);

		if (logChartDAO.checkLogTableExists("IPS_Log", tableName) == 0) {
			throw new TableNotFoundException("로그 테이블 '" + tableName + "'을(를) 찾을 수 없습니다.");
		}

		// 디버깅용
		System.out.println("<오늘 날짜 테이블이 있나 확인>> 오늘자 테이블은 = " + tableName);
		
		return tableName;
	}
	
    /** 과거 N일(기본 30일) 통계로 동적 임계치(avg+3σ) 계산 **/
    public double computeThresholdLastNDays(int days) {
        LocalDate today = LocalDate.now();
        List<Double> counts = new ArrayList<>();

        for (int i = 1; i <= days; i++) {
            LocalDate date = today.minusDays(i);
            String table = "log_" + date.format(DateTimeFormatter.BASIC_ISO_DATE);
            int c;
            try {
                // 테이블이 없으면 0건으로 간주
                if (logChartDAO.checkLogTableExists("IPS_Log", table) == 0) {
                    c = 0;
                } else {
                    // 하루 전체 건수: 00:00:00 ~ 23:59:59
                    String start = date.atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    String end   = date.atTime(23,59,59).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    Map<String,Object> params = Map.of(
                        "tableName", table,
                        "startDate", start,
                        "endDate",   end
                    );
                    c = logChartDAO.selectCountByDateRange(params);
                }
            } catch (Exception e) {
                c = 0;
            }
            counts.add((double)c);
        }

        // 평균
        double avg = counts.stream().mapToDouble(d->d).average().orElse(0.0);
        // 모집단 표준편차
        double variance = counts.stream()
            .mapToDouble(d -> (d - avg)*(d - avg))
            .sum() / counts.size();
        double stddev = Math.sqrt(variance);

        return avg + 3 * stddev;
    }

}
