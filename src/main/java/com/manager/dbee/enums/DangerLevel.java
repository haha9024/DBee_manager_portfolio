package com.manager.dbee.enums;

public enum DangerLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;
	
	public static boolean contains(DangerLevel value) {
	    for (DangerLevel d : values()) {
	        if (d == value) return true;
	    }
	    return false;
	}
}
