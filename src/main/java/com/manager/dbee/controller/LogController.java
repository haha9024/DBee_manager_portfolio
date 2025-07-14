package com.manager.dbee.controller;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
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
		
		// 0) targetPorts 문자열 → List<Integer> 파싱
		// StringUtils.hasText(String) : boolean, 매개변수: 입력된 문자열, 반환형: 공백도 아닌 텍스트가 있으면 true
		if(StringUtils.hasText(filter.getTargetPorts())) {
			
			// 1) rawParts: ["3306", " 9090", "750"] 와 같은 String 배열 
			// String.split(String regex),  
			// 매개변수: 구분자 정규식(",", 콤마), 반환형: String[] (원본 문자열을 콤마 기준으로 나눈 배열)
			String[] rawParts = filter.getTargetPorts().split(",");
			
			
			// 2) 스트림으로 변환 → Stream<String>, Arrays.stream(T[] array)  
			// 매개변수: String[] 배열, 반환형: Stream<String> (배열 요소를 순회할 수 있는 스트림)
			Stream<String> stream = Arrays.stream(rawParts);
			
			
			// 3) 앞뒤 공백 제거 → Stream<String> (예: "9090")
			// Stream<T>.map(Function<? super T,? extends R> mapper)
			// 매개변수: 요소 변환 함수 (여기선 String trim()), 반환형: Stream<String> (trim 적용된 새 스트림)
			Stream<String> trimmed = stream.map(String::trim);
			
			
			// 4) 빈 문자열 걸러내기 → Stream<String> (빈("")인 요소 삭제)
			// Stream<T>.filter(Predicate<? super T> predicate)
			// 매개변수: 불리언 검사 함수, 반환형: Stream<String> (조건(true)인 요소만 남긴 스트림)
			Stream<String> nonEmpty = trimmed.filter(s -> !s.isEmpty());
			
			
			// 5) 숫자로 변환 → Stream<Integer>
			// Stream<T>.map(Function<? super T,? extends R> mapper)
			// 매개변수: String → Integer 변환 함수, 반환형: Stream<Integer>
			Stream<Integer> intStream = nonEmpty.map(Integer::valueOf);
			
			
			// 6) 리스트로 수집 → List<Integer>
			// Stream<T>.collect(Collector<? super T,A,R> collector)
			// Collectors.toList(): 반환형: Collector<T,?,List<T>> 최종 반환: List<Integer> (파싱된 포트 번호들)
			List<Integer> ports = intStream.collect(Collectors.toList());
			
			
			// 7) filter 객체에 세팅
			// List<Integer>를 DTO의 필드에 저장해서, MyBatis XML의 <foreach>에서 그대로 사용 가능
			filter.setTargetPortList(ports);
			
			
			// 위 코드의 아주 간략한 버전
			/*
			 * if (StringUtils.hasText(filter.getTargetPorts())) { 
			 * List<Integer> ports = Arrays.stream(filter.getTargetPorts().split(","))
			 * 		.map(String::trim)
			 * 		.filter(s -> !s.isEmpty()) .map(Integer::valueOf)
			 * 		.collect(Collectors.toList()); 
			 * 
			 * 	filter.setTargetPortList(ports); 
			 * }
			 */
		}
		
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
