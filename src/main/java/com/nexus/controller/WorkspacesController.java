package com.nexus.controller;

import com.nexus.model.WorkspaceProfile;
import com.nexus.service.ProjectAnalyzerService;
import com.nexus.service.WorkspaceManagerService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class WorkspacesController {

    private final WorkspaceManagerService workspaceManager;
    private final ProjectAnalyzerService projectAnalyzer;

    @FXML public FlowPane profilesFlowPane;

    public WorkspacesController(WorkspaceManagerService workspaceManager, ProjectAnalyzerService projectAnalyzer) {
        this.workspaceManager = workspaceManager;
        this.projectAnalyzer = projectAnalyzer;
    }

    @FXML
    public void initialize() {
        loadProfiles();
    }

    private void loadProfiles() {
        profilesFlowPane.getChildren().clear();
        List<WorkspaceProfile> profiles = workspaceManager.getProfiles();
        
        // Sort by most recently opened
        profiles.sort((a, b) -> Long.compare(b.getLastOpened(), a.getLastOpened()));

        for (WorkspaceProfile profile : profiles) {
            VBox card = new VBox(10); // Reduced spacing to fit more info
            card.getStyleClass().add("result-card");
            card.setPrefWidth(280);
            card.setAlignment(Pos.CENTER_LEFT);

            Label title = new Label(profile.getName());
            title.getStyleClass().add("card-title");

            Label path = new Label("Path: " + profile.getProjectPath());
            path.getStyleClass().add("card-subtitle");
            path.setWrapText(true);
            
            // Labels for dynamic stats
            Label typeLabel = new Label("Type: Loading...");
            Label gitLabel = new Label("Git: Loading...");
            Label depsLabel = new Label("Deps: Loading...");
            Label modLabel = new Label("Modified: Loading...");
            
            typeLabel.getStyleClass().add("card-subtitle");
            gitLabel.getStyleClass().add("card-subtitle");
            depsLabel.getStyleClass().add("card-subtitle");
            modLabel.getStyleClass().add("card-subtitle");

            Button launchBtn = new Button("Launch Workspace");
            launchBtn.getStyleClass().add("primary-button");
            launchBtn.setMaxWidth(Double.MAX_VALUE);
            
            launchBtn.setOnAction(e -> {
                launchBtn.setText("Launching...");
                launchBtn.setDisable(true);
                workspaceManager.launchWorkspace(profile).thenRun(() -> {
                    Platform.runLater(() -> {
                        launchBtn.setText("Launch Workspace");
                        launchBtn.setDisable(false);
                        loadProfiles(); // Refresh to bump to top
                    });
                });
            });

            card.getChildren().addAll(title, path, typeLabel, gitLabel, depsLabel, modLabel, launchBtn);
            profilesFlowPane.getChildren().add(card);
            
            // Asynchronously analyze and update UI
            CompletableFuture.runAsync(() -> {
                projectAnalyzer.analyzeWorkspaceDetails(profile);
                Platform.runLater(() -> {
                    typeLabel.setText("Type: " + profile.getProjectType() + " (" + profile.getPrimaryLanguage() + ")");
                    gitLabel.setText("Git: " + profile.getGitBranch() + " | " + profile.getGitStatus());
                    
                    List<String> deps = profile.getKeyDependencies();
                    depsLabel.setText("Deps: " + (deps != null && !deps.isEmpty() ? String.join(", ", deps) : "None"));
                    
                    if (profile.getLastModified() > 0) {
                        String date = Instant.ofEpochMilli(profile.getLastModified())
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime()
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                        modLabel.setText("Modified: " + date);
                    } else {
                        modLabel.setText("Modified: Unknown");
                    }
                });
            });
        }
    }
}
