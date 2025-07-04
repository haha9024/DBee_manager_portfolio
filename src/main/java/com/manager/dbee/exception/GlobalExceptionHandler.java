package com.manager.dbee.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GlobalExceptionHandler {
	
	// 1) JSON 응답이 필요한 API 예외에는 @ResponseBody 또는 ResponseEntity 사용
	// 404: DB에 테이블이 없을 때
	@ExceptionHandler(RuntimeException.class)
	@ResponseBody
	public ResponseEntity<Map<String,String>> handleTableNotFound(TableNotFoundException ex){
		return ResponseEntity
				.status(HttpStatus.NOT_FOUND)
				.body(Map.of("error", ex.getMessage()));
	}
	
	// 204: 쿼리는 정상 동작했으나 결과가 없을 때
	@ExceptionHandler(NoDataFoundException.class)
	@ResponseBody
	public ResponseEntity<Map<String,String>> handleNoDataFound(NoDataFoundException ex){
		return ResponseEntity
				.status(HttpStatus.NO_CONTENT)
				.body(Map.of("error", ex.getMessage()));
	}
	
    // 400: 클라이언트 파라미터 오류(IllegalArgumentException 등)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String,String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity
            .badRequest()
            .body(Map.of("error", ex.getMessage()));
    }
	
	// 2) 뷰 반환이 필요한 “잘못된 URL” 예외
	// @ExceptionHandler(NoHandlerFoundException.class)
	/*
	 * public ModelAndView handleResourceNotFound(NoHandlerFoundException ex) {
	 * 
	 * }
	 */
}
