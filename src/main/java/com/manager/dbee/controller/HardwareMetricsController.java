package com.manager.dbee.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.manager.dbee.dto.HardwareMetricsDto;
import com.manager.dbee.service.HardwareMetricsService;

@RestController
@RequestMapping("/dashboard2/metrics")
public class HardwareMetricsController {
    private final HardwareMetricsService svc;
    public HardwareMetricsController(HardwareMetricsService svc) {
        this.svc = svc;
    }

    @GetMapping("/hardware")
    public List<HardwareMetricsDto> hardware() {
        return svc.getRecent();
    }
}
