package org.bukkit.plugin;

import java.util.logging.Logger;

public interface Plugin {

    public Logger getLogger();

    public String getName();

    default org.slf4j.Logger getSLF4JLogger() {
        return null;
    }
}