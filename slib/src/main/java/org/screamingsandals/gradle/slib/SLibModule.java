package org.screamingsandals.gradle.slib;

import lombok.Data;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
public class SLibModule implements AdditionalContent {
    /**
     * Name of the optional internal module
     */
    @NotNull
    private final String name;

    @Override
    @ApiStatus.Internal
    @ApiStatus.OverrideOnly
    public void apply(String configuration, DependencyHandler dependencies, String slibVersion, List<String> platforms) {
        dependencies.add(configuration, Constants.SCREAMING_LIB_GROUP_ID + ":" + name + "-common:" + slibVersion);
        platforms.forEach(s ->
                dependencies.add(configuration, Constants.SCREAMING_LIB_GROUP_ID + ":" + name + "-" + s + ":" + slibVersion)
        );
    }

    @Override
    public void applyMultiModule(String configuration, DependencyHandler dependencies, String slibVersion, String platformName) {
        dependencies.add(configuration, Constants.SCREAMING_LIB_GROUP_ID + ":" + name + "-" + platformName + ":" + slibVersion);
    }
}
