package org.screamingsandals.gradle.run.config;

import org.jetbrains.annotations.NotNull;

public interface ServerProperties {
    void property(@NotNull String key, @NotNull String value);

    void port(int port);

    void onlineMode(boolean onlineMode);
}
