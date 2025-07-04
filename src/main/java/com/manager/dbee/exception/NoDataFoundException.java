package com.manager.dbee.exception;

public class NoDataFoundException extends RuntimeException {
	
	private static final long serialVersionUID = 2L;  // 이 줄을 추가!
	
	public NoDataFoundException(String message) {
		super(message);
	}
	
	public NoDataFoundException() {
		super("데이터가 없습니다");
	}
}
