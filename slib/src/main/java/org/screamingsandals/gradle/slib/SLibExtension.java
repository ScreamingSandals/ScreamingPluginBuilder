package org.screamingsandals.gradle.slib;

import groovy.lang.Closure;
import lombok.Data;
import lombok.Setter;
import org.gradle.api.Action;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class SLibExtension {
    /**
     * ScreamingLib version
     */
    @Setter(onParam_ = @NotNull) // don't allow users to set it to null
    private String version;
    /**
     * Custom Relocate Path (default to the group id)
     */
    @Nullable
    private String customRelocatePath;
    /**
     * Supported platforms
     */
    private final List<String> platforms = new ArrayList<>();
    private final List<AdditionalContent> additionalContent = new ArrayList<>();

    /**
     * Disables automatic relocation.
     *
     * <p>
     *
     * Note: This can break some functionality.
     *
     */
    @ApiStatus.Experimental
    private boolean disableRelocate;
    /**
     * Disables compiler tricks.
     *
     * <p>
     *
     * Note: This can break some functionality.
     *
     */
    @ApiStatus.Experimental
    private boolean disableCompilerTricks;
    /**
     * Disables annotation processor.
     *
     * <p>
     *
     * Note: If this is disabled, your plugin won't work.
     *
     */
    @ApiStatus.Experimental
    private boolean disableAnnotationProcessor;
    /**
     * Disables kapt.
     *
     * <p>
     *
     * Note: If this is disabled, and the project use Kotlin,
     * you should apply the kapt plugin by yourself.
     * The plugin shouldn't be applied before this one.
     */
    @ApiStatus.Experimental
    private boolean disableAutoKaptApplicationForKotlin;
    /**
     * Disables kotlin-sam-with-receiver configuration.
     *
     * <p>
     *
     * Note: This can break some functionality.
     */
    @ApiStatus.Experimental
    private boolean disableAutoSAMWithReceiverConfigurationForKotlin;

    /**
     * Sets the ScreamingLib version and version for all internal modules
     *
     * @param version ScreamingLib version
     */
    public void version(@NotNull String version) {
        this.version = version;
    }

    /**
     * Sets the Custom relocate path. If the parameter is null, it defaults to your group id.
     *
     * @param customRelocatePath new custom relocate path or null
     */
    public void customRelocatePath(@Nullable String customRelocatePath) {
        this.customRelocatePath = customRelocatePath;
    }

    /**
     * Sets the supported platforms. Note that you can't mix proxy platforms with non-proxy platforms.
     *
     * @param platforms array of supported platforms
     */
    public void platforms(@NotNull String @NotNull... platforms) {
        this.platforms.addAll(Arrays.asList(platforms));
    }

    /**
     * Allows you to add additional content, like optional module, simple inventories, or 3rd party library.
     *
     * @param consumer additional content builder
     */
    public void additionalContent(@NotNull Action<@NotNull AdditionalContentBuilder> consumer) {
        var builder = new AdditionalContentBuilder(additionalContent);
        consumer.execute(builder);
    }

    /**
     * Allows you to add additional content, like optional module, simple inventories, or 3rd party library.
     *
     * @param closure additional content builder
     */
    public void additionalContent(Closure<AdditionalContentBuilder> closure) {
        var builder = new AdditionalContentBuilder(additionalContent);
        closure.setDelegate(builder);
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.call(builder);
    }
}
