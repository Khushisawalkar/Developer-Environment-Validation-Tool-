package com.nexus.service;

import org.springframework.stereotype.Service;

import java.io.File;
import java.util.concurrent.CompletableFuture;

@Service
public class SystemOptimizerService {

    public CompletableFuture<Boolean> optimizeSystem() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 1. Force Java GC
                System.gc();
                
                // 2. Clear Windows %TEMP% Directory
                String tempPath = System.getenv("TEMP");
                if (tempPath != null) {
                    File tempDir = new File(tempPath);
                    if (tempDir.exists() && tempDir.isDirectory()) {
                        File[] files = tempDir.listFiles();
                        if (files != null) {
                            for (File file : files) {
                                // Safely attempt to delete, ignoring failures (locked files)
                                try {
                                    if (file.isFile()) {
                                        file.delete();
                                    }
                                } catch (Exception ignored) {}
                            }
                        }
                    }
                }
                
                // 3. Flush DNS
                ProcessBuilder pb = new ProcessBuilder("ipconfig", "/flushdns");
                pb.start().waitFor();
                
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });
    }
}
