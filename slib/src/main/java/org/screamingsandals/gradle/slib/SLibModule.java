package org.screamingsandals.gradle.slib;

import lombok.Data;
import org.gradle.api.artifacts.dsl.DependencyHandler;

import java.util.List;

@Data
public class SLibModule implements AdditionalContent {
    private final String name;

    @Override
    public void apply(DependencyHandler dependencies, String slibVersion, List<String> platforms) {
        dependencies.add(Constants.IMPLEMENTATION_CONFIGURATION, Constants.SCREAMING_LIB_GROUP_ID + ":" + name + "-common:" + slibVersion);
        platforms.forEach(s ->
                dependencies.add(Constants.IMPLEMENTATION_CONFIGURATION, Constants.SCREAMING_LIB_GROUP_ID + ":" + name + "-" + s + ":" + slibVersion)
        );
    }
}
