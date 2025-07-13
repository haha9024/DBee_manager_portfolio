package com.manager.dbee.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.manager.dbee.dto.Log;
import com.manager.dbee.dto.LogFilter;
import com.manager.dbee.service.LogService;

@Controller
public class LogController {

	private final LogService logService;
	
	
	public LogController(LogService logService) {
		this.logService = logService;
	}

	@GetMapping("/logs")
	public String getListLogs(LogFilter filter, 
			@PageableDefault(size=20, sort="detected_dt", direction = Sort.Direction.DESC)Pageable pageable, 
			Model model) {
		
		// 서비스 호출: 필터, 페이징 정보 넘김
		Page<Log> page = logService.getLogs(filter, pageable);
		
		// 뷰에 표시할 데이터 바인딩
		model.addAttribute("page", page);
		model.addAttribute("filter", filter);
		
		// 리턴 뷰 이름(Thymeleaf: logs.html)
		return "logs";
	}
	
	// 상세보기 클릭용
	  //@GetMapping("/{logNo}")
	  //public String viewLog(
	    //@PathVariable Long logNo,
	    //Model model
	  //) {
	    //Log detail = logService.getLogById(logNo);
	    //model.addAttribute("log", detail);
	    //return "logs/detail";
	  //}
}
