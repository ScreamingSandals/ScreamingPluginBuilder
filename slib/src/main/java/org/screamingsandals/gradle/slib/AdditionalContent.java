package org.screamingsandals.gradle.slib;

import org.gradle.api.artifacts.dsl.DependencyHandler;

import java.util.List;

public interface AdditionalContent {
    void apply(DependencyHandler dependencies, String slibVersion, List<String> platforms);
}
