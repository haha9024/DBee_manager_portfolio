package com.manager.dbee.dto;

import java.sql.Timestamp;

import com.manager.dbee.enums.DangerLevel;
import com.manager.dbee.enums.DetectType;
import com.manager.dbee.enums.Protocol;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Log {
	private int log_no;
	private Timestamp detected_dt;
	private String src_ip;
	private Integer src_port;
	private String target_ip;
	private Integer target_port;
	private Protocol protocol;
	private DangerLevel threat_level;
	private String p_id;
	private String action;
	private byte[] payload;
	private DetectType policy_type;
}
