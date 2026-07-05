package com.nexus.service;

import com.nexus.model.SoftwareStatus;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class EnvironmentScannerService {

    private final ProjectAnalyzerService analyzerService;

    public EnvironmentScannerService(ProjectAnalyzerService analyzerService) {
        this.analyzerService = analyzerService;
    }

    public CompletableFuture<List<SoftwareStatus>> scanEnvironment(String projectPath) {
        return CompletableFuture.supplyAsync(() -> {
            // 1. Analyze the project to determine what tools we actually need to scan for
            List<String> requiredTools = analyzerService.analyzeProject(projectPath);
            
            // 2. Build tasks only for required tools
            List<CompletableFuture<SoftwareStatus>> futures = requiredTools.stream()
                .map(this::getCheckForTool)
                .collect(Collectors.toList());

            // 3. Execute all
            return futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());
        });
    }

    private CompletableFuture<SoftwareStatus> getCheckForTool(String tool) {
        return switch (tool) {
            case "Java" -> checkSoftware("Java", "java -version");
            case "Maven" -> checkSoftware("Maven", "mvn -v");
            case "Git" -> checkSoftware("Git", "git --version");
            case "Docker" -> checkSoftware("Docker", "docker -v");
            case "Node.js" -> checkSoftware("Node.js", "node -v");
            case "PostgreSQL" -> checkSoftware("PostgreSQL", "psql -V");
            case "MySQL" -> checkSoftware("MySQL", "mysql -V");
            case "Redis" -> checkSoftware("Redis", "redis-cli -v");
            default -> CompletableFuture.completedFuture(new SoftwareStatus(tool, false, "Unknown tool", "https://google.com/"));
        };
    }

    private CompletableFuture<SoftwareStatus> checkSoftware(String name, String command) {
        return CompletableFuture.supplyAsync(() -> {
            String url = getDownloadUrl(name);
            try {
                // 1. Primary Check: Windows CMD (assumes it's in the PATH variable)
                SoftwareStatus primaryStatus = runCheck(name, "cmd.exe", "/c", command, url, "");
                if (primaryStatus.isInstalled()) return primaryStatus;

                // 2. Fallback Check: Common Windows Installation Paths (if not in PATH)
                String fallbackCmd = getFallbackCommand(name, command);
                if (fallbackCmd != null) {
                    SoftwareStatus fallbackStatus = runCheck(name, "cmd.exe", "/c", fallbackCmd, url, "(Found in Program Files) ");
                    if (fallbackStatus.isInstalled()) return fallbackStatus;
                }

                // 3. Fallback Check: WSL (Windows Subsystem for Linux)
                SoftwareStatus wslStatus = runCheck(name, "wsl", "", command, url, "(WSL) ");
                if (wslStatus.isInstalled()) return wslStatus;

                // If all fail
                return new SoftwareStatus(name, false, "Not Installed or not in PATH", url);
                
            } catch (Exception e) {
                return new SoftwareStatus(name, false, "Not Installed or not in PATH", url);
            }
        });
    }

    private SoftwareStatus runCheck(String name, String shell, String flag, String command, String url, String prefix) {
        try {
            ProcessBuilder builder;
            if (shell.equals("wsl")) {
                // For WSL, we need to split the arguments e.g. ["wsl", "mysql", "-V"]
                String[] parts = command.split(" ");
                String[] pbArgs = new String[parts.length + 1];
                pbArgs[0] = "wsl";
                System.arraycopy(parts, 0, pbArgs, 1, parts.length);
                builder = new ProcessBuilder(pbArgs);
            } else {
                builder = new ProcessBuilder(shell, flag, command);
            }
            
            builder.redirectErrorStream(true); 
            Process process = builder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String firstLine = reader.readLine();
                boolean finished = process.waitFor(2, TimeUnit.SECONDS);
                
                if (finished && process.exitValue() == 0 && firstLine != null && !firstLine.trim().isEmpty() 
                        && !firstLine.contains("is not recognized") && !firstLine.contains("command not found")) {
                    return new SoftwareStatus(name, true, prefix + firstLine.trim(), url);
                } else if (!finished) {
                    process.destroyForcibly();
                }
            }
        } catch (Exception ignored) {
            // Silently ignore execution errors to proceed to the next fallback
        }
        return new SoftwareStatus(name, false, "", url);
    }

    private String getFallbackCommand(String name, String defaultCommand) {
        if (name.equalsIgnoreCase("MySQL")) {
            File mysqlDir = new File("C:\\Program Files\\MySQL");
            if (mysqlDir.exists() && mysqlDir.isDirectory()) {
                File[] subdirs = mysqlDir.listFiles();
                if (subdirs != null) {
                    for (File subdir : subdirs) {
                        if (subdir.getName().startsWith("MySQL Server")) {
                            File bin = new File(subdir, "bin\\mysql.exe");
                            if (bin.exists()) {
                                return "\"" + bin.getAbsolutePath() + "\" " + defaultCommand.split(" ")[1];
                            }
                        }
                    }
                }
            }
        } else if (name.equalsIgnoreCase("PostgreSQL")) {
            File pgDir = new File("C:\\Program Files\\PostgreSQL");
            if (pgDir.exists() && pgDir.isDirectory()) {
                File[] subdirs = pgDir.listFiles();
                if (subdirs != null) {
                    for (File subdir : subdirs) {
                        File bin = new File(subdir, "bin\\psql.exe");
                        if (bin.exists()) {
                            return "\"" + bin.getAbsolutePath() + "\" " + defaultCommand.split(" ")[1];
                        }
                    }
                }
            }
        }
        return null;
    }
    
    private String getDownloadUrl(String name) {
        return switch (name.toLowerCase()) {
            case "java" -> "https://adoptium.net/";
            case "maven" -> "https://maven.apache.org/download.cgi";
            case "git" -> "https://git-scm.com/downloads";
            case "docker" -> "https://www.docker.com/products/docker-desktop/";
            case "node.js" -> "https://nodejs.org/";
            case "postgresql" -> "https://www.postgresql.org/download/windows/";
            case "mysql" -> "https://dev.mysql.com/downloads/installer/";
            case "redis" -> "https://github.com/microsoftarchive/redis/releases";
            default -> "https://google.com/";
        };
    }
}
