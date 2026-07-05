package com.nexus.service;

import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProjectAnalyzerService {

    public List<String> analyzeProject(String projectPath) {
        List<String> requiredTools = new ArrayList<>();
        
        // Git is universal, always check for version control
        requiredTools.add("Git");
        
        File dir = new File(projectPath);
        if (!dir.exists() || !dir.isDirectory()) {
            return requiredTools;
        }

        boolean hasPom = new File(dir, "pom.xml").exists();
        boolean hasGradle = new File(dir, "build.gradle").exists();
        boolean hasPackageJson = new File(dir, "package.json").exists();
        boolean hasDocker = new File(dir, "docker-compose.yml").exists() || new File(dir, "Dockerfile").exists();

        if (hasPom || hasGradle) {
            requiredTools.add("Java");
            if (hasPom) requiredTools.add("Maven");
            
            // Check for specific databases in pom.xml
            if (hasPom) {
                try {
                    String pomContent = Files.readString(Path.of(dir.getAbsolutePath(), "pom.xml")).toLowerCase();
                    if (pomContent.contains("postgresql")) requiredTools.add("PostgreSQL");
                    if (pomContent.contains("mysql")) requiredTools.add("MySQL");
                    if (pomContent.contains("redis")) requiredTools.add("Redis");
                } catch (Exception ignored) {}
            }
        }
        
        if (hasPackageJson) {
            requiredTools.add("Node.js");
            try {
                String pkgContent = Files.readString(Path.of(dir.getAbsolutePath(), "package.json")).toLowerCase();
                if (pkgContent.contains("postgresql") || pkgContent.contains("pg")) requiredTools.add("PostgreSQL");
                if (pkgContent.contains("mysql")) requiredTools.add("MySQL");
                if (pkgContent.contains("redis")) requiredTools.add("Redis");
            } catch (Exception ignored) {}
        }
        
        if (hasDocker) {
            requiredTools.add("Docker");
        }
        
        return requiredTools.stream().distinct().toList();
    }
}
