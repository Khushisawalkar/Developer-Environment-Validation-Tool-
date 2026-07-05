package com.nexus.controller;

import com.nexus.model.SystemProcess;
import com.nexus.service.ProcessManagerService;
import javafx.application.Platform;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.springframework.stereotype.Component;

@Component
public class ProcessesController {

    private final ProcessManagerService processManagerService;

    @FXML private TableView<SystemProcess> processTable;
    @FXML private TableColumn<SystemProcess, Number> pidCol;
    @FXML private TableColumn<SystemProcess, String> nameCol;
    @FXML private TableColumn<SystemProcess, String> userCol;
    @FXML private TableColumn<SystemProcess, Void> actionCol;

    public ProcessesController(ProcessManagerService processManagerService) {
        this.processManagerService = processManagerService;
    }

    @FXML
    public void initialize() {
        pidCol.setCellValueFactory(cellData -> new SimpleLongProperty(cellData.getValue().pid()));
        nameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().name()));
        userCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().user()));

        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button killBtn = new Button("Kill");

            {
                killBtn.getStyleClass().add("primary-button");
                killBtn.setStyle("-fx-background-color: #f44336; -fx-padding: 5 15; -fx-font-size: 12px;");
                killBtn.setOnAction(event -> {
                    SystemProcess process = getTableView().getItems().get(getIndex());
                    if (processManagerService.killProcess(process.pid())) {
                        loadProcesses(); 
                    } else {
                        killBtn.setText("Failed");
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(killBtn);
                }
            }
        });

        loadProcesses();
    }

    @FXML
    protected void loadProcesses() {
        processManagerService.getActiveProcesses().thenAccept(processes -> {
            Platform.runLater(() -> {
                processTable.setItems(FXCollections.observableArrayList(processes));
            });
        });
    }
}
