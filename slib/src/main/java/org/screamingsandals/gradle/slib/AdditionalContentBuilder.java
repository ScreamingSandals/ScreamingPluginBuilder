package org.screamingsandals.gradle.slib;

import groovy.lang.Closure;
import lombok.Data;
import org.gradle.api.Action;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
public class AdditionalContentBuilder {
    @NotNull
    @ApiStatus.Internal
    private final List<AdditionalContent> additionalContent;

    /**
     * Adds optional modules of ScreamingLib.
     *
     * @param modules slib optional modules
     */
    public void module(@NotNull String @NotNull... modules) {
        for (var module : modules) {
            additionalContent.add(new SLibModule(module));
        }
    }

    /**
     * Adds ScreamingLib lang module
     */
    public void lang() {
        additionalContent.add(new SLibSingleModule("lang"));
    }

    /**
     * Adds optional single modules of ScreamingLib.
     * Unlike normal modules, these modules consist of just one artifact.
     *
     * <p>
     *
     * Note: Except `lang`, there are currently no modules that can be added using this method.
     * For adding lang use its own method {@link #lang()}
     *
     * @param modules slib optional single modules
     * @see #module(String...)
     * @see #lang()
     */
    public void singleModule(@NotNull String @NotNull... modules) {
        for (var module : modules) {
            additionalContent.add(new SLibSingleModule(module));
        }
    }

    /**
     * Adds third party module to screaming lib.
     *
     * @param thirdPartyModuleConsumer third party module builder
     */
    public void thirdParty(@NotNull Action<@NotNull ThirdPartyModule> thirdPartyModuleConsumer) {
        var module = new ThirdPartyModule();
        if (thirdPartyModuleConsumer instanceof Closure) {
            System.out.println("this is closure");
            ((Closure<?>) thirdPartyModuleConsumer).setResolveStrategy(Closure.DELEGATE_FIRST);
        }
        thirdPartyModuleConsumer.execute(module);
        additionalContent.add(module);
    }

    /**
     * Adds third party module to screaming lib.
     *
     * @param thirdPartyModuleConsumer third party module builder
     */
    public void thirdParty(Closure<ThirdPartyModule> thirdPartyModuleConsumer) {
        var module = new ThirdPartyModule();
        thirdPartyModuleConsumer.setResolveStrategy(Closure.DELEGATE_FIRST);
        thirdPartyModuleConsumer.setDelegate(module);
        thirdPartyModuleConsumer.call(module);
        additionalContent.add(module);
    }

    /**
     * Adds Simple Inventories to your project.
     * Despite this method requires third party module consumer, group id and module are already set.
     * The only property you should specify is the version.
     *
     * @param thirdPartyModuleConsumer third party module builder
     */
    public void simpleInventories(@NotNull Action<@NotNull ThirdPartyModule> thirdPartyModuleConsumer) {
        var module = new ThirdPartyModule();
        module.setGroupId(Constants.SIMPLE_INVENTORIES_GROUP_ID);
        module.setModule(Constants.SIMPLE_INVENTORIES_MODULE_NAME);
        thirdPartyModuleConsumer.execute(module);
        additionalContent.add(module);
    }

    /**
     * Adds Simple Inventories to your project.
     * Despite this method requires third party module consumer, group id and module are already set.
     * The only property you should specify is the version.
     *
     * @param thirdPartyModuleConsumer third party module builder
     */
    public void simpleInventories(Closure<ThirdPartyModule> thirdPartyModuleConsumer) {
        var module = new ThirdPartyModule();
        module.setGroupId(Constants.SIMPLE_INVENTORIES_GROUP_ID);
        module.setModule(Constants.SIMPLE_INVENTORIES_MODULE_NAME);
        thirdPartyModuleConsumer.setResolveStrategy(Closure.DELEGATE_FIRST);
        thirdPartyModuleConsumer.setDelegate(module);
        thirdPartyModuleConsumer.call(module);
        additionalContent.add(module);
    }
}
