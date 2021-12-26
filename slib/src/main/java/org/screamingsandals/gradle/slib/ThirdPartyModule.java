package org.screamingsandals.gradle.slib;

import lombok.Data;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;

import java.util.List;
import java.util.Map;

@Data
public class ThirdPartyModule implements AdditionalContent {
    private String groupId;
    private String module;
    private String version;

    public void groupId(String groupId) {
        this.groupId = groupId;
    }

    public void module(String module) {
        this.module = module;
    }

    public void version(String version) {
        this.version = version;
    }

    @Override
    public void apply(DependencyHandler dependencies, String slibVersion, List<String> platforms) {
        {
            var dependency = dependencies.add(Constants.IMPLEMENTATION_CONFIGURATION, groupId + ":" + module + "-common:" + version);
            if (dependency instanceof ModuleDependency) {
                ((ModuleDependency) dependency).exclude(Map.of("group", Constants.SCREAMING_LIB_GROUP_ID));
            }
        }
        platforms.forEach(s -> {
            var dependency = dependencies.add(Constants.IMPLEMENTATION_CONFIGURATION, groupId + ":" + module + "-" + s + ":" + version);
            if (dependency instanceof ModuleDependency) {
                ((ModuleDependency) dependency).exclude(Map.of("group", Constants.SCREAMING_LIB_GROUP_ID));
            }
        });
    }
}
