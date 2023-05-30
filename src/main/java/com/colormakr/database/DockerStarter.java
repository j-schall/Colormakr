package com.colormakr.database;

public class DockerStarter {
    public static void start() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("docker", "start", "colormakr-postgres-1");
            Process process = processBuilder.start();

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Der Docker-Container wurde erfolgreich gestartet");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
