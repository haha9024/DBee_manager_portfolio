package com.manager.dbee.enums;

public enum Status {
	ACTIVE, INACTIVE;
	
	public static boolean contains(Status value) {
	    for (Status s : values()) {
	        if (s == value) return true;
	    }
	    return false;
	}

}
