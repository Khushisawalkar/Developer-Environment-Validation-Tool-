package com.nexus.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.model.WorkspaceProfile;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class WorkspaceManagerService {

    private final ObjectMapper mapper = new ObjectMapper();
    private final File dataFile;

    public WorkspaceManagerService() {
        String userHome = System.getProperty("user.home");
        File nexusDir = new File(userHome, ".nexus");
        if (!nexusDir.exists()) {
            nexusDir.mkdirs();
        }
        this.dataFile = new File(nexusDir, "profiles.json");
    }

    public List<WorkspaceProfile> getProfiles() {
        if (!dataFile.exists()) {
            return generateDefaultProfiles();
        }
        try {
            return mapper.readValue(dataFile, new TypeReference<List<WorkspaceProfile>>() {});
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void saveProfiles(List<WorkspaceProfile> profiles) {
        try {
            mapper.writeValue(dataFile, profiles);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public CompletableFuture<Void> launchWorkspace(WorkspaceProfile profile) {
        return CompletableFuture.runAsync(() -> {
            try {
                // Launch IDE
                if (profile.getIdeCommand() != null && !profile.getIdeCommand().isEmpty()) {
                    ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", profile.getIdeCommand() + " .");
                    pb.directory(new File(profile.getProjectPath()));
                    pb.start();
                }

                // Launch URLs
                if (profile.getBrowserUrls() != null) {
                    for (String url : profile.getBrowserUrls()) {
                        new ProcessBuilder("cmd.exe", "/c", "start " + url).start();
                    }
                }
                
                // Launch Terminal (example opening cmd in the project dir)
                ProcessBuilder term = new ProcessBuilder("cmd.exe", "/c", "start cmd.exe");
                term.directory(new File(profile.getProjectPath()));
                term.start();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private List<WorkspaceProfile> generateDefaultProfiles() {
        List<WorkspaceProfile> defaultProfiles = new ArrayList<>();
        defaultProfiles.add(new WorkspaceProfile(
            UUID.randomUUID().toString(),
            "Nexus Development",
            System.getProperty("user.dir"), // Current dir
            "idea64", // Defaulting to IntelliJ IDEA as per user's preference
            List.of("https://github.com", "http://localhost:8080")
        ));
        saveProfiles(defaultProfiles);
        return defaultProfiles;
    }
}
