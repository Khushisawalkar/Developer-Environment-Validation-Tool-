package com.nexus.service;

import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;

@Service
public class SystemHealthService {
    
    private final OperatingSystemMXBean osBean;
    
    public SystemHealthService() {
        this.osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    }
    
    public double getCpuLoad() {
        // Returns a value between 0.0 and 1.0
        double load = osBean.getCpuLoad();
        if (load < 0) return 0.0;
        return load * 100; // Return as percentage
    }
    
    public double getMemoryUsage() {
        long totalMemory = osBean.getTotalMemorySize();
        long freeMemory = osBean.getFreeMemorySize();
        long usedMemory = totalMemory - freeMemory;
        
        return ((double) usedMemory / totalMemory) * 100; // Return as percentage
    }
}
