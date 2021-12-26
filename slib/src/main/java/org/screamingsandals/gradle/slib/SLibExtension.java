package org.screamingsandals.gradle.slib;

import groovy.lang.Closure;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@Data
public class SLibExtension {
    private String version;
    private String customRelocatePath;
    private boolean dontRelocate;
    private boolean dontTryToTrickTheCompiler;
    private final List<String> platforms = new ArrayList<>();
    private final List<AdditionalContent> additionalContent = new ArrayList<>();

    public void version(String version) {
        this.version = version;
    }

    public void customRelocatePath(String customRelocatePath) {
        this.customRelocatePath = customRelocatePath;
    }

    public void platforms(String... platforms) {
        this.platforms.addAll(Arrays.asList(platforms));
    }

    public void additionalContent(Consumer<AdditionalContentBuilder> consumer) {
        var builder = new AdditionalContentBuilder(additionalContent);
        consumer.accept(builder);
    }

    public void additionalContent(Closure<AdditionalContentBuilder> closure) {
        var builder = new AdditionalContentBuilder(additionalContent);
        closure.setDelegate(builder);
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.call(builder);
    }
}
