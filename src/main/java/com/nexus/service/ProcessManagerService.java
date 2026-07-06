package com.nexus.service;

import com.nexus.model.SystemProcess;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class ProcessManagerService {

    public CompletableFuture<List<SystemProcess>> getActiveProcesses() {
        return CompletableFuture.supplyAsync(() -> {
            Map<Long, Long> memoryMap = getMemoryMap();
            Set<Long> hungPids = getHungProcesses();
            Instant now = Instant.now();

            return ProcessHandle.allProcesses()
                    .filter(ph -> ph.info().command().isPresent())
                    .map(ph -> {
                        long pid = ph.pid();
                        String cmd = ph.info().command().orElse("Unknown");
                        String name = cmd.substring(cmd.lastIndexOf("\\") + 1);
                        String user = ph.info().user().orElse("Unknown");
                        String cmdLine = ph.info().commandLine().orElse("");
                        
                        long memoryBytes = memoryMap.getOrDefault(pid, 0L);
                        String memoryStr = formatMemory(memoryBytes);
                        
                        String category = categorizeProcess(name);
                        String safeness = determineSafeness(category);
                        boolean isHung = hungPids.contains(pid);
                        
                        String uptimeStr = "Unknown";
                        if (ph.info().startInstant().isPresent()) {
                            Duration uptime = Duration.between(ph.info().startInstant().get(), now);
                            if (uptime.toHours() > 0) {
                                uptimeStr = uptime.toHours() + "h " + (uptime.toMinutesPart()) + "m";
                            } else {
                                uptimeStr = uptime.toMinutesPart() + "m";
                            }
                        }
                        
                        return new SystemProcess(pid, name, user, cmdLine, memoryBytes, memoryStr, category, safeness, isHung, uptimeStr);
                    })
                    .sorted((p1, p2) -> Long.compare(p2.memoryBytes(), p1.memoryBytes()))
                    .collect(Collectors.toList());
        });
    }

    public boolean killProcess(long pid) {
        Optional<ProcessHandle> processHandle = ProcessHandle.of(pid);
        if (processHandle.isPresent()) {
            return processHandle.get().destroyForcibly();
        }
        return false;
    }

    private Map<Long, Long> getMemoryMap() {
        Map<Long, Long> map = new HashMap<>();
        try {
            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "tasklist /FO CSV /NH");
            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\",\"");
                    if (parts.length >= 5) {
                        try {
                            long pid = Long.parseLong(parts[1].replace("\"", ""));
                            String memStr = parts[4].replace("\"", "").replace(" K", "").replace(",", "").trim();
                            long memoryBytes = Long.parseLong(memStr) * 1024L;
                            map.put(pid, memoryBytes);
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    private Set<Long> getHungProcesses() {
        Set<Long> set = new HashSet<>();
        try {
            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "tasklist /FI \"STATUS eq NOT RESPONDING\" /FO CSV /NH");
            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("INFO: No tasks are running")) continue;
                    String[] parts = line.split("\",\"");
                    if (parts.length >= 2) {
                        try {
                            long pid = Long.parseLong(parts[1].replace("\"", ""));
                            set.add(pid);
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
        } catch (Exception ignored) {}
        return set;
    }

    private String formatMemory(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return (bytes / 1024) + " KB";
        if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)) + " MB";
        return String.format("%.2f GB", (double) bytes / (1024 * 1024 * 1024));
    }

    private String categorizeProcess(String name) {
        String lower = name.toLowerCase();
        if (lower.contains("chrome") || lower.contains("msedge") || lower.contains("firefox")) return "Browser";
        if (lower.contains("java") || lower.contains("node") || lower.contains("python") || lower.contains("idea") || lower.contains("code")) return "Dev Tool";
        if (lower.contains("svchost") || lower.contains("explorer") || lower.contains("csrss") || lower.contains("winlogon") 
            || lower.contains("lsass") || lower.contains("wininit") || lower.contains("smss") || lower.contains("services")
            || lower.contains("registry") || lower.contains("system")) return "System Critical";
        return "Background";
    }

    private String determineSafeness(String category) {
        return switch (category) {
            case "System Critical" -> "Unsafe";
            case "Browser", "Dev Tool" -> "Safe";
            default -> "Warning";
        };
    }
}
