package org.screamingsandals.gradle.slib;

import groovy.lang.Closure;
import lombok.Data;

import java.util.List;
import java.util.function.Consumer;

@Data
public class AdditionalContentBuilder {
    private final List<AdditionalContent> additionalContent;

    public void module(String... modules) {
        for (var module : modules) {
            additionalContent.add(new SLibModule(module));
        }
    }

    public void thirdParty(Consumer<ThirdPartyModule> thirdPartyModuleConsumer) {
        var module = new ThirdPartyModule();
        thirdPartyModuleConsumer.accept(module);
        additionalContent.add(module);
    }

    public void thirdParty(Closure<ThirdPartyModule> thirdPartyModuleConsumer) {
        var module = new ThirdPartyModule();
        thirdPartyModuleConsumer.setResolveStrategy(Closure.DELEGATE_FIRST);
        thirdPartyModuleConsumer.setDelegate(module);
        thirdPartyModuleConsumer.call(module);
        additionalContent.add(module);
    }

    public void simpleInventories(Consumer<ThirdPartyModule> thirdPartyModuleConsumer) {
        var module = new ThirdPartyModule();
        module.setGroupId(Constants.SIMPLE_INVENTORIES_GROUP_ID);
        module.setModule(Constants.SIMPLE_INVENTORIES_MODULE_NAME);
        thirdPartyModuleConsumer.accept(module);
        additionalContent.add(module);
    }

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
