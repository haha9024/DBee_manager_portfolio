package com.manager.dbee.enums;

public enum DetectType {
	PATTERN, BEHAVIOR;
	
	public static boolean contains(DetectType value) {
	    for (DetectType d : values()) {
	        if (d == value) return true;
	    }
	    return false;
	}
}
