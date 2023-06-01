package com.colormakr.database;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.exception.NotModifiedException;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;

public class DockerStarter {
    public static void start() {
        try {
            DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
            DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                    .dockerHost(config.getDockerHost())
                    .sslConfig(config.getSSLConfig())
                    .build();

            DockerClient dockerClient = DockerClientBuilder.getInstance(config)
                    .withDockerHttpClient(httpClient)
                    .build();

            String containerId = "505e5ace383a"; // This is the container ID of the PostgreSQL Database
            dockerClient.startContainerCmd(containerId).exec(); // Execute the container
        } catch (NotModifiedException e) {
            StackTraceElement[] response = e.getStackTrace();
        }
    }
}
