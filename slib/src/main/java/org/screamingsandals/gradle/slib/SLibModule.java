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
    public void apply(DependencyHandler dependencies, String slibVersion, List<String> platforms) {
        dependencies.add(Constants.IMPLEMENTATION_CONFIGURATION, Constants.SCREAMING_LIB_GROUP_ID + ":" + name + "-common:" + slibVersion);
        platforms.forEach(s ->
                dependencies.add(Constants.IMPLEMENTATION_CONFIGURATION, Constants.SCREAMING_LIB_GROUP_ID + ":" + name + "-" + s + ":" + slibVersion)
        );
    }

    @Override
    public void applyMultiModule(DependencyHandler dependencies, String slibVersion, String platformName) {
        dependencies.add(Constants.IMPLEMENTATION_CONFIGURATION, Constants.SCREAMING_LIB_GROUP_ID + ":" + name + "-" + platformName + ":" + slibVersion);
    }
}
