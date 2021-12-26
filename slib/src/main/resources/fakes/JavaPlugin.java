package org.bukkit.plugin.java;

import java.util.logging.Logger;
import org.bukkit.plugin.PluginBase;

public abstract class JavaPlugin extends PluginBase {
    public JavaPlugin() {}

    public String getName() {
        return "name";
    }

    public Logger getLogger() {
        return null;
    }
}