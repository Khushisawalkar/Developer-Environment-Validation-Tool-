package com.nexus;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NexusApplication {
    public static void main(String[] args) {
        try {
            Application.launch(NexusFxApplication.class, args);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
