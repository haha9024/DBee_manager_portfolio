package com.manager.dbee.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ConnectionStatDTO {

	private int id;						// 각 행(row)의 고유 식별자
	private LocalDateTime event_time;	// 이 행이 기록된 시점(스냅샷 시각)
	private String db_login_id;			// 실제 DB 계정
	private String app_user_id;			// 웹 애플리케이션(예: 관리자 페이지)에 로그인한 일반 유저 계정(ID)
	private String host;				// processlist.HOST 컬럼과 동일, 클라이언트가 접속해 온 IP:포트 문자열 전체(예: 192.168.1.50:52344)
	private String client_addr;			// 위 host 에서 포트 번호를 뗀 순수 IP 부분만 저장			
	private String command;				// processlist.COMMAND 에 대응하는 값, MySQL 스레드가 어떤 종류의 작업(Query, Sleep, Connect 등)을 하고 있는지
	private String state;				// processlist.STATE 에 대응(현재 수행 중인 상태에 대한 상세 설명), 예: Sending data, Locked, Waiting for tables 등
	private Integer time_sec;			// processlist.TIME 컬럼,  현 상태(또는 명령)를 유지한 시간을 초 단위로 나타냄, 예: state='Query'라면 “그 쿼리를 몇 초째 실행 중인지”
}
