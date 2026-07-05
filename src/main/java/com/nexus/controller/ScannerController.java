package com.nexus.controller;

import com.nexus.model.SoftwareStatus;
import com.nexus.service.EnvironmentScannerService;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ScannerController {

    private final EnvironmentScannerService scannerService;
    
    @FXML public Button scanButton;
    @FXML public VBox scanResultsBox;
    @FXML public Label projectPathLabel;

    public ScannerController(EnvironmentScannerService scannerService) {
        this.scannerService = scannerService;
    }

    @FXML
    public void initialize() {
        String currentPath = System.getProperty("user.dir");
        projectPathLabel.setText("🔍 Analyzing context: " + currentPath);
    }

    @FXML
    protected void onScanButtonClick() {
        scanButton.setDisable(true);
        scanButton.setText("Scanning Context... 🐾");
        scanResultsBox.getChildren().clear();

        String currentPath = System.getProperty("user.dir");

        scannerService.scanEnvironment(currentPath).thenAccept(results -> {
            Platform.runLater(() -> {
                displayResults(results);
                scanButton.setDisable(false);
                scanButton.setText("🌸 Run Context Scan 🔍");
            });
        });
    }

    private void displayResults(List<SoftwareStatus> results) {
        int delayOffset = 0;
        for (SoftwareStatus status : results) {
            HBox card = new HBox();
            card.getStyleClass().add("result-card");
            card.setAlignment(Pos.CENTER_LEFT);
            card.setSpacing(15);
            card.setOpacity(0.0); 

            StackPane iconBox = new StackPane();
            iconBox.getStyleClass().add(status.isInstalled() ? "icon-box-success" : "icon-box-error");
            Label iconLabel = new Label(getIconForSoftware(status.name()));
            iconLabel.getStyleClass().add("card-icon");
            iconBox.getChildren().add(iconLabel);

            VBox textContainer = new VBox();
            textContainer.setSpacing(5);
            textContainer.setAlignment(Pos.CENTER_LEFT);
            
            Label titleLabel = new Label(status.name());
            titleLabel.getStyleClass().add("card-title");
            
            Label versionLabel = new Label(status.versionDetails());
            versionLabel.getStyleClass().add(status.isInstalled() ? "card-subtitle" : "card-subtitle-error");
            textContainer.getChildren().addAll(titleLabel, versionLabel);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label statusPill = new Label(status.isInstalled() ? "✔ Installed" : "✖ Not Found");
            statusPill.getStyleClass().add(status.isInstalled() ? "status-pill-success" : "status-pill-error");

            card.getChildren().addAll(iconBox, textContainer, spacer, statusPill);
            
            if (!status.isInstalled()) {
                Button downloadBtn = new Button("Get it! 💖");
                downloadBtn.getStyleClass().add("primary-button");
                downloadBtn.setStyle("-fx-font-size: 14px; -fx-padding: 8 15; -fx-background-radius: 20; -fx-border-radius: 20;");
                downloadBtn.setOnAction(e -> {
                    try {
                        new ProcessBuilder("cmd.exe", "/c", "start " + status.downloadUrl()).start();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
                card.getChildren().add(downloadBtn);
            }

            scanResultsBox.getChildren().add(card);
            
            FadeTransition ft = new FadeTransition(Duration.millis(500), card);
            ft.setToValue(1.0);
            ft.setDelay(Duration.millis(delayOffset));
            ft.play();
            
            delayOffset += 100;
        }
    }

    private String getIconForSoftware(String name) {
        return switch (name.toLowerCase()) {
            case "java" -> "☕";
            case "maven" -> "📦";
            case "git" -> "🌿";
            case "docker" -> "🐳";
            case "node.js" -> "🟢";
            case "postgresql", "mysql", "redis" -> "💾";
            default -> "✨";
        };
    }
}
