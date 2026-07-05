package com.nexus;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NexusApplication {
    public static void main(String[] args) {
        Application.launch(NexusFxApplication.class, args);
    }
}
