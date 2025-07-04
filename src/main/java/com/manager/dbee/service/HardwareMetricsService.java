package com.manager.dbee.service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.manager.dbee.dto.HardwareMetricsDto;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;

@Service
public class HardwareMetricsService {
    private final SystemInfo si = new SystemInfo();
    private final CentralProcessor cpu = si.getHardware().getProcessor();
    private final GlobalMemory mem = si.getHardware().getMemory();

    // OSHI 6.x 방식: 이전 틱 배열을 보관
    private long[] prevTicks = cpu.getSystemCpuLoadTicks();

    // 최근 60개(5분치)만 보관
    private final Deque<HardwareMetricsDto> buffer = new LinkedList<>();

    // 5초마다 수집
    @Scheduled(fixedRate = 5000)
    public void collect() {
        long[] currTicks = cpu.getSystemCpuLoadTicks();
        double cpuLoad = cpu.getSystemCpuLoadBetweenTicks(prevTicks);
        prevTicks = currTicks;

        long total = mem.getTotal();
        long used = total - mem.getAvailable();
        String ts = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        // 호스트 이름 얻기 (예외 처리)
        String host;
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            host = "unknown";
        }
        
        synchronized (buffer) {
            buffer.addLast(new HardwareMetricsDto(ts, cpuLoad, total, used, host));
            if (buffer.size() > 60) {
                buffer.removeFirst();
            }
        }
    }

    // REST 컨트롤러에서 꺼내 쓸 메서드
    public List<HardwareMetricsDto> getRecent() {
        synchronized (buffer) {
            return new ArrayList<>(buffer);
        }
    }
}
