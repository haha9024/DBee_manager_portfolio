package com.manager.dbee.dao.log;

import java.util.List;

import com.manager.dbee.dto.Log;
import com.manager.dbee.dto.LogFilter;

public interface LogDAO {

	public List<Log> selectLogs(LogFilter filter);
	
	public int countLogsbyPagination(LogFilter filter);
	
	public int countAllLogs();
	
	/*
	 * public int checkLogTableExists(@Param("schema") String
	 * schema, @Param("tableName") String tableName);
	 */
}
