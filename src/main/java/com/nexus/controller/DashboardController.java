package com.nexus.controller;

import com.nexus.service.SystemHealthService;
import com.nexus.service.SystemOptimizerService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class DashboardController {

    private final ApplicationContext applicationContext;
    private final SystemHealthService healthService;
    private final SystemOptimizerService optimizerService;

    @FXML private BorderPane rootPane;
    @FXML private StackPane contentArea;
    @FXML private Button btnNavScanner;
    @FXML private Button btnNavWorkspaces;
    @FXML private Button btnNavProcesses;
    @FXML private Button btnThemeToggle;
    
    @FXML private VBox healthWidget;
    @FXML private Label cpuLabel;
    @FXML private Label ramLabel;
    @FXML private Label healthIcon;
    @FXML private Button btnOptimize;

    private boolean isDarkMode = false;
    private ScheduledExecutorService healthPoller;

    public DashboardController(ApplicationContext applicationContext, 
                               SystemHealthService healthService, 
                               SystemOptimizerService optimizerService) {
        this.applicationContext = applicationContext;
        this.healthService = healthService;
        this.optimizerService = optimizerService;
    }

    @FXML
    public void initialize() {
        showScanner();
        startHealthMonitor();
    }
    
    private void startHealthMonitor() {
        healthPoller = Executors.newSingleThreadScheduledExecutor();
        healthPoller.scheduleAtFixedRate(() -> {
            double cpu = healthService.getCpuLoad();
            double ram = healthService.getMemoryUsage();
            
            Platform.runLater(() -> {
                cpuLabel.setText(String.format("%.1f%%", cpu));
                ramLabel.setText(String.format("%.1f%%", ram));
                
                if (cpu > 85.0 || ram > 85.0) {
                    if (!healthWidget.getStyleClass().contains("health-widget-warning")) {
                        healthWidget.getStyleClass().add("health-widget-warning");
                    }
                    healthIcon.setText("🔴");
                    btnOptimize.setVisible(true);
                    btnOptimize.setManaged(true);
                } else {
                    healthWidget.getStyleClass().remove("health-widget-warning");
                    healthIcon.setText("🟢");
                    btnOptimize.setVisible(false);
                    btnOptimize.setManaged(false);
                }
            });
        }, 0, 2, TimeUnit.SECONDS);
    }
    
    @FXML
    protected void optimizeSystem() {
        btnOptimize.setText("Refreshing...");
        btnOptimize.setDisable(true);
        
        optimizerService.optimizeSystem().thenAccept(success -> {
            Platform.runLater(() -> {
                btnOptimize.setText("✨ Refresh");
                btnOptimize.setDisable(false);
                healthIcon.setText("✨"); // Celebration icon
            });
        });
    }

    @FXML
    protected void showScanner() {
        loadView("/views/scanner.fxml");
        setActiveButton(btnNavScanner);
    }

    @FXML
    protected void showWorkspaces() {
        loadView("/views/workspaces.fxml");
        setActiveButton(btnNavWorkspaces);
    }

    @FXML
    protected void showProcesses() {
        loadView("/views/processes.fxml");
        setActiveButton(btnNavProcesses);
    }
    
    @FXML
    protected void toggleTheme() {
        isDarkMode = !isDarkMode;
        if (isDarkMode) {
            rootPane.getScene().getRoot().getStyleClass().add("dark-mode");
            btnThemeToggle.setText("☀️ Light Mode");
        } else {
            rootPane.getScene().getRoot().getStyleClass().remove("dark-mode");
            btnThemeToggle.setText("🌙 Dark Mode");
        }
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(applicationContext::getBean);
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setActiveButton(Button activeButton) {
        btnNavScanner.getStyleClass().remove("nav-button-active");
        btnNavWorkspaces.getStyleClass().remove("nav-button-active");
        btnNavProcesses.getStyleClass().remove("nav-button-active");
        
        activeButton.getStyleClass().add("nav-button-active");
    }
}
