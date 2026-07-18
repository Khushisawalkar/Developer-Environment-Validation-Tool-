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
    public void analyzeWorkspaceDetails(com.nexus.model.WorkspaceProfile profile) {
        File dir = new File(profile.getProjectPath());
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }

        // Project Type & Language
        boolean hasPom = new File(dir, "pom.xml").exists();
        boolean hasGradle = new File(dir, "build.gradle").exists();
        boolean hasPackageJson = new File(dir, "package.json").exists();
        
        if (hasPom) {
            profile.setProjectType("Maven");
            profile.setPrimaryLanguage("Java");
        } else if (hasGradle) {
            profile.setProjectType("Gradle");
            profile.setPrimaryLanguage("Java");
        } else if (hasPackageJson) {
            profile.setProjectType("npm");
            profile.setPrimaryLanguage("Node.js");
        } else {
            profile.setProjectType("Unknown");
            profile.setPrimaryLanguage("Unknown");
        }

        // Dependencies
        List<String> deps = new ArrayList<>();
        if (hasPom) {
            try {
                String pomContent = Files.readString(Path.of(dir.getAbsolutePath(), "pom.xml")).toLowerCase();
                if (pomContent.contains("postgresql")) deps.add("PostgreSQL");
                if (pomContent.contains("mysql")) deps.add("MySQL");
                if (pomContent.contains("spring-boot")) deps.add("Spring Boot");
                if (pomContent.contains("react")) deps.add("React");
            } catch (Exception ignored) {}
        }
        if (hasPackageJson) {
            try {
                String pkgContent = Files.readString(Path.of(dir.getAbsolutePath(), "package.json")).toLowerCase();
                if (pkgContent.contains("express")) deps.add("Express");
                if (pkgContent.contains("react")) deps.add("React");
                if (pkgContent.contains("postgresql") || pkgContent.contains("pg")) deps.add("PostgreSQL");
                if (pkgContent.contains("mysql")) deps.add("MySQL");
            } catch (Exception ignored) {}
        }
        profile.setKeyDependencies(deps);

        // Git Info
        if (new File(dir, ".git").exists()) {
            profile.setGitBranch(runCommand(dir, "git", "rev-parse", "--abbrev-ref", "HEAD"));
            String status = runCommand(dir, "git", "status", "-s");
            profile.setGitStatus(status != null && !status.trim().isEmpty() ? "Uncommitted Changes" : "Clean");
        } else {
            profile.setGitBranch("Not a Git repo");
            profile.setGitStatus("-");
        }

        // Last Modified
        long lastModified = dir.lastModified();
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.lastModified() > lastModified) {
                    lastModified = file.lastModified();
                }
            }
        }
        profile.setLastModified(lastModified);
    }

    private String runCommand(File dir, String... command) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(dir);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            process.waitFor(2, java.util.concurrent.TimeUnit.SECONDS);
            return output.toString().trim();
        } catch (Exception e) {
            return "Error";
        }
    }
}
