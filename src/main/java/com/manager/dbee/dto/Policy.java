package com.manager.dbee.dto;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.manager.dbee.enums.DangerLevel;
import com.manager.dbee.enums.DetectType;
import com.manager.dbee.enums.Status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Policy {

	private String p_id;
	private String name;
	private Status status;
	private String description;
	private String created_by;
	private String updated_by;
	private Integer base_sec;
	private Integer base_count;
	private String src_ip_start;
	private String src_ip_end;
	private String dst_ip_start;
	private String dst_ip_end;
	private Integer dst_port_start;
	private Integer dst_port_end;
	private String payload_content1;
	private String payload_content2;
	private String payload_content3;
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	private Timestamp created_at;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	private Timestamp updated_at;
	private DetectType policy_type;
	private DangerLevel threat_level;
	private String action_to_take;
	//private Set<ActionToTake> action_to_take;
	private Integer block_duration;
	
	public boolean containsSrcIp(String ipStr) {
	    long ip = ipToLong(ipStr);
	    return ip >= ipToLong(this.src_ip_start) && ip <= ipToLong(this.src_ip_end);
	}

	private long ipToLong(String ipStr) {
	    String[] parts = ipStr.split("\\.");
	    long result = 0;
	    for (int i = 0; i < 4; i++) {
	        result |= (Long.parseLong(parts[i]) << (24 - (8 * i)));
	    }
	    return result;
	}
}
