package io.github.inbox.loyalty.support;

import org.testcontainers.DockerClientFactory;

public final class Docker {

    private Docker() {
    }

    public static boolean availableOrCi() {
        return System.getenv("CI") != null || DockerClientFactory.instance().isDockerAvailable();
    }
}
