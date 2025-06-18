package com.manager.dbee.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.manager.dbee.enums.DangerLevel;
import com.manager.dbee.enums.DetectType;
import com.manager.dbee.enums.Status;

@RestController
@RequestMapping("/enums")
public class EnumController {

    @GetMapping("/status")
    public Status[] getStatus() {
        return Status.values();
    }

    @GetMapping("/dangerlevel")
    public DangerLevel[] getDangerLevels() {
        return DangerLevel.values();
    }

    @GetMapping("/detecttype")
    public DetectType[] getDetectTypes() {
        return DetectType.values();
    }
}
