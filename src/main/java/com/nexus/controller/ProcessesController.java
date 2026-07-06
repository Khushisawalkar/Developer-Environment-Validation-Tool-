package com.nexus.controller;

import com.nexus.model.SystemProcess;
import com.nexus.service.ProcessManagerService;
import javafx.application.Platform;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
public class ProcessesController {

    private final ProcessManagerService processManagerService;

    @FXML public TableView<SystemProcess> processTable;
    @FXML public TableColumn<SystemProcess, Long> pidCol;
    @FXML public TableColumn<SystemProcess, String> nameCol;
    @FXML public TableColumn<SystemProcess, String> categoryCol;
    @FXML public TableColumn<SystemProcess, String> memCol;
    @FXML public TableColumn<SystemProcess, String> uptimeCol;
    @FXML public TableColumn<SystemProcess, String> safenessCol;
    @FXML public TableColumn<SystemProcess, SystemProcess> actionCol;
    
    @FXML public Button refreshButton;
    @FXML public ComboBox<String> filterComboBox;

    private List<SystemProcess> allProcesses = new ArrayList<>();

    public ProcessesController(ProcessManagerService processManagerService) {
        this.processManagerService = processManagerService;
    }

    @FXML
    public void initialize() {
        pidCol.setCellValueFactory(cellData -> new SimpleLongProperty(cellData.getValue().pid()).asObject());
        
        nameCol.setCellValueFactory(cellData -> {
            SystemProcess sp = cellData.getValue();
            String displayName = sp.name();
            if (sp.isHung()) {
                displayName += " ⚠️ (Hung)";
            }
            return new SimpleStringProperty(displayName);
        });
        
        categoryCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().category()));
        memCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().memoryStr()));
        uptimeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().uptimeStr()));
        safenessCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().safeness()));
        
        // Safeness Cell Factory (Color Coding)
        safenessCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label label = new Label(item);
                    label.setStyle("-fx-font-weight: bold; -fx-padding: 2 5; -fx-background-radius: 5;");
                    switch (item) {
                        case "Safe" -> {
                            label.setStyle(label.getStyle() + "-fx-background-color: -success-bg; -fx-text-fill: -success;");
                        }
                        case "Warning" -> {
                            label.setStyle(label.getStyle() + "-fx-background-color: #fff3cd; -fx-text-fill: #856404;");
                        }
                        case "Unsafe" -> {
                            label.setStyle(label.getStyle() + "-fx-background-color: -error-bg; -fx-text-fill: -error;");
                        }
                    }
                    setGraphic(label);
                }
            }
        });

        // Action Cell Factory (Kill Button with Protection)
        actionCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue()));
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button killBtn = new Button("Kill");

            {
                killBtn.getStyleClass().add("primary-button");
                killBtn.setStyle("-fx-background-color: -error; -fx-text-fill: white; -fx-padding: 5 10; -fx-font-size: 12px;");
                killBtn.setOnAction(e -> {
                    SystemProcess process = getTableView().getItems().get(getIndex());
                    killBtn.setText("Killing...");
                    killBtn.setDisable(true);
                    
                    CompletableFuture.runAsync(() -> {
                        boolean success = processManagerService.killProcess(process.pid());
                        Platform.runLater(() -> {
                            if (success) {
                                getTableView().getItems().remove(process);
                            } else {
                                killBtn.setText("Failed");
                                killBtn.setStyle("-fx-background-color: gray; -fx-text-fill: white; -fx-padding: 5 10; -fx-font-size: 12px;");
                            }
                        });
                    });
                });
            }

            @Override
            protected void updateItem(SystemProcess process, boolean empty) {
                super.updateItem(process, empty);
                if (empty || process == null) {
                    setGraphic(null);
                } else {
                    // Disable kill button for Unsafe critical system processes!
                    if ("Unsafe".equals(process.safeness())) {
                        killBtn.setDisable(true);
                        killBtn.setText("Protected");
                        killBtn.setStyle("-fx-background-color: #d6d8db; -fx-text-fill: #383d41; -fx-padding: 5 10; -fx-font-size: 12px;");
                    } else {
                        killBtn.setDisable(false);
                        killBtn.setText("Kill");
                        killBtn.setStyle("-fx-background-color: -error; -fx-text-fill: white; -fx-padding: 5 10; -fx-font-size: 12px;");
                    }
                    setGraphic(killBtn);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        refreshProcesses();
    }

    @FXML
    protected void refreshProcesses() {
        refreshButton.setDisable(true);
        refreshButton.setText("🔄 Loading...");
        
        processManagerService.getActiveProcesses().thenAccept(processes -> {
            Platform.runLater(() -> {
                this.allProcesses = processes;
                applyFilter();
                refreshButton.setDisable(false);
                refreshButton.setText("🔄 Refresh");
            });
        });
    }

    @FXML
    protected void onFilterChange() {
        applyFilter();
    }

    private void applyFilter() {
        String filter = filterComboBox.getValue();
        List<SystemProcess> filtered = allProcesses;

        if ("High Memory (> 500MB)".equals(filter)) {
            filtered = allProcesses.stream()
                .filter(p -> p.memoryBytes() > 500 * 1024 * 1024L)
                .collect(Collectors.toList());
        } else if ("Hung / Not Responding".equals(filter)) {
            filtered = allProcesses.stream()
                .filter(SystemProcess::isHung)
                .collect(Collectors.toList());
        } else if ("Safe to Kill".equals(filter)) {
            filtered = allProcesses.stream()
                .filter(p -> "Safe".equals(p.safeness()))
                .collect(Collectors.toList());
        }

        ObservableList<SystemProcess> observableList = FXCollections.observableArrayList(filtered);
        processTable.setItems(observableList);
    }
}
