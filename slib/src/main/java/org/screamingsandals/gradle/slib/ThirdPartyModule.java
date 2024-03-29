package org.screamingsandals.gradle.slib;

import lombok.Data;
import lombok.Setter;
import lombok.experimental.Tolerate;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.provider.Provider;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

@Data
public class ThirdPartyModule implements AdditionalContent {
    /**
     * Group id of the third party module.
     */
    @Setter(onParam_ = @NotNull) // don't allow users to set it to null
    private String groupId;
    /**
     * Prefix of the artifact id of the third party module.
     *
     * <p>
     *
     * The final artifact id looks like `module-platform` or `module-common`
     */
    @Setter(onParam_ = @NotNull)
    private String module;
    /**
     * Version of the third party module.
     */
    @Setter(onParam_ = @NotNull)
    private String version;

    @Tolerate
    public void setVersion(@NotNull Provider<@NotNull String> version) {
        this.version = version.get();
    }

    /**
     * Group id of the third party module.
     *
     * @param groupId new group id
     */
    public void groupId(@NotNull String groupId) {
        this.groupId = groupId;
    }

    /**
     * Prefix of the artifact id of the third party module.
     *
     * <p>
     *
     * The final artifact id looks like `module-platform` or `module-common`
     *
     * @param module new artifact id prefix
     */
    public void module(@NotNull String module) {
        this.module = module;
    }

    /**
     * Version of the third party module.
     *
     * @param version new version
     */
    public void version(@NotNull String version) {
        this.version = version;
    }

    /**
     * Version of the third party module.
     *
     * @param version new version
     */
    public void version(@NotNull Provider<@NotNull String> version) {
        this.version = version.get();
    }

    @Override
    @ApiStatus.Internal
    @ApiStatus.OverrideOnly
    public void apply(String configuration, DependencyHandler dependencies, String slibVersion, List<String> platforms) {
        {
            var dependency = dependencies.add(configuration, groupId + ":" + module + "-common:" + version);
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

    @Override
    public void applyMultiModule(String configuration, DependencyHandler dependencies, String slibVersion, String platformName) {
        var dependency = dependencies.add(configuration, groupId + ":" + module + "-" + platformName + ":" + version);
        if (dependency instanceof ModuleDependency) {
            ((ModuleDependency) dependency).exclude(Map.of("group", Constants.SCREAMING_LIB_GROUP_ID));
        }
    }
}
