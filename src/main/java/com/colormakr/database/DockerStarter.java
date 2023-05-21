package com.colormakr.database;

import com.colormakr.Main;
import javafx.application.Platform;

public class DockerStarter {
    public static void main(String[] args) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("docker", "start", "colormakr-postgres-1");
            Process process = processBuilder.start();

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Der Docker-Container wurde erfolgreich gestartet");
            } else {
                System.out.println("Es gab ein Fehler beim Starten des Docker-Containers " + exitCode);
            }
        } catch(Exception e) {
           e.printStackTrace();
        }

    }
}
