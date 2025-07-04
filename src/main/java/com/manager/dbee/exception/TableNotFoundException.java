package com.manager.dbee.exception;

public class TableNotFoundException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;  // 이 줄을 추가!
	
	public TableNotFoundException(String message) {
		super(message);
	}
	
    public TableNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public TableNotFoundException() {
        super("요청하신 테이블을 찾을 수 없습니다.");
    }
}
