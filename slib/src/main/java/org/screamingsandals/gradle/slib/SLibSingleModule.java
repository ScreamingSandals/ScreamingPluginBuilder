package org.screamingsandals.gradle.slib;

import lombok.Data;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
public class SLibSingleModule implements AdditionalContent {
    /**
     * Artifact id of the optional internal module
     */
    @NotNull
    private final String artifactId;

    @Override
    @ApiStatus.Internal
    @ApiStatus.OverrideOnly
    public void apply(DependencyHandler dependencies, String slibVersion, List<String> platforms) {
        dependencies.add(Constants.IMPLEMENTATION_CONFIGURATION, Constants.SCREAMING_LIB_GROUP_ID + ":" + artifactId + ":" + slibVersion);
    }

    @Override
    public void applyMultiModule(DependencyHandler dependencies, String slibVersion, String platformName) {
        if ("common".equals(platformName)) {
            dependencies.add(Constants.IMPLEMENTATION_CONFIGURATION, Constants.SCREAMING_LIB_GROUP_ID + ":" + artifactId + ":" + slibVersion);
        }
    }
}
