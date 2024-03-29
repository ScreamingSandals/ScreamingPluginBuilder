package org.screamingsandals.gradle.slib;

import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

public interface AdditionalContent {
    @ApiStatus.Internal
    @ApiStatus.OverrideOnly
    void apply(String configuration, DependencyHandler dependencies, String slibVersion, List<String> platforms);

    @ApiStatus.Internal
    @ApiStatus.OverrideOnly
    void applyMultiModule(String configuration, DependencyHandler dependencies, String slibVersion, String platformName);
}
