package com.nexus.service;

import com.nexus.model.SystemProcess;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class ProcessManagerService {

    public CompletableFuture<List<SystemProcess>> getActiveProcesses() {
        return CompletableFuture.supplyAsync(() -> {
            return ProcessHandle.allProcesses()
                    .filter(ph -> ph.info().command().isPresent()) // Only processes we can identify
                    .map(ph -> {
                        String cmd = ph.info().command().orElse("Unknown");
                        String name = cmd.substring(cmd.lastIndexOf("\\") + 1); // Get executable name
                        String user = ph.info().user().orElse("Unknown");
                        String cmdLine = ph.info().commandLine().orElse("");
                        return new SystemProcess(ph.pid(), name, user, cmdLine);
                    })
                    .sorted((p1, p2) -> p1.name().compareToIgnoreCase(p2.name())) // Sort alphabetically by name
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
}
