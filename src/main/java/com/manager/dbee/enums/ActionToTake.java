package com.manager.dbee.enums;

import java.util.EnumSet;
import java.util.Set;

public enum ActionToTake {
    
	LOG(1),      // 무조건 로그 기록 (기본값)
    NOTIFY(2),   // UI 알림 및 이메일 전송
    BLOCK(4);    // 탐지 엔진 차단

    private final int code;

    ActionToTake(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
    
    // int -> Set<ActionToTake> 변환
    public static Set<ActionToTake> fromInt(int value) {
        Set<ActionToTake> result = EnumSet.noneOf(ActionToTake.class);
        for (ActionToTake action : values()) {
            if ((value & action.getCode()) == action.getCode()) {
                result.add(action);
            }
        }
        return result;
    }

    // Set<ActionToTake> -> int 변환
    public static int toInt(Set<ActionToTake> actions) {
        int result = 0;
        for (ActionToTake action : actions) {
            result |= action.getCode();
        }
        return result;
    }
}
