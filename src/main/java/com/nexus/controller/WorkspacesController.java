package com.nexus.controller;

import com.nexus.model.WorkspaceProfile;
import com.nexus.service.WorkspaceManagerService;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WorkspacesController {

    private final WorkspaceManagerService workspaceManager;

    @FXML public FlowPane profilesFlowPane;

    public WorkspacesController(WorkspaceManagerService workspaceManager) {
        this.workspaceManager = workspaceManager;
    }

    @FXML
    public void initialize() {
        loadProfiles();
    }

    private void loadProfiles() {
        profilesFlowPane.getChildren().clear();
        List<WorkspaceProfile> profiles = workspaceManager.getProfiles();

        for (WorkspaceProfile profile : profiles) {
            VBox card = new VBox(15);
            card.getStyleClass().add("result-card");
            card.setPrefWidth(280);
            card.setAlignment(Pos.CENTER_LEFT);

            Label title = new Label(profile.getName());
            title.getStyleClass().add("card-title");

            Label path = new Label("Path: " + profile.getProjectPath());
            path.getStyleClass().add("card-subtitle");
            path.setWrapText(true);

            Button launchBtn = new Button("Launch Workspace");
            launchBtn.getStyleClass().add("primary-button");
            launchBtn.setMaxWidth(Double.MAX_VALUE);
            
            launchBtn.setOnAction(e -> {
                launchBtn.setText("Launching...");
                launchBtn.setDisable(true);
                workspaceManager.launchWorkspace(profile).thenRun(() -> {
                    javafx.application.Platform.runLater(() -> {
                        launchBtn.setText("Launch Workspace");
                        launchBtn.setDisable(false);
                    });
                });
            });

            card.getChildren().addAll(title, path, launchBtn);
            profilesFlowPane.getChildren().add(card);
        }
    }
}
