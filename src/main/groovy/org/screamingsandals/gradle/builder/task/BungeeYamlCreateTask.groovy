package org.screamingsandals.gradle.builder.task

import kr.entree.spigradle.mapper.Mapper
import org.gradle.api.DefaultTask
import org.gradle.api.file.CopySpec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Jar
import org.screamingsandals.gradle.builder.asm.CustomByteInspector
import org.screamingsandals.gradle.builder.attributes.BungeePluginAttributes
import kr.entree.spigradle.libs.snakeyaml.DumperOptions
import kr.entree.spigradle.libs.snakeyaml.Yaml

class BungeeYamlCreateTask extends DefaultTask {
    @Input
    BungeePluginAttributes attributes
    @Input
    String encoding = 'UTF-8'
    @Input
    Yaml yaml = createYaml()
    @Input
    Map<CopySpec, Boolean> includeTasks = new HashMap<>()

    @TaskAction
    def createPluginYaml() {
        def file = new File(temporaryDir, 'bungee.yml')
        file.newWriter(encoding).withCloseable {
            yaml.dump(createMap(), it)
        }
        (getJarTasks() + getFlattenIncludeTasks()).each {
            it.from file
        }
    }

    private Collection<CopySpec> getJarTasks() {
        return project.tasks.withType(Jar).findAll {
            includeTasks.getOrDefault(it, true)
        }
    }

    private Collection<CopySpec> getFlattenIncludeTasks() {
        return includeTasks.findAll { it.value }
                .collect { it.key }
    }

    def include(copySpec, Boolean whether = true) {
        if (copySpec instanceof CopySpec) {
            includeTasks.put(copySpec, whether)
        }
    }

    def exclude(copySpec, Boolean whether = true) {
        include(copySpec, !whether)
    }

    def createMap() {
        attributes.with {
            name = name ?: project.name
            version = version ?: project.version
        }
        if (attributes.main == null) {
            def inspected = new CustomByteInspector(project, "net/md_5/bungee/api/plugin/Plugin").doInspect()
            attributes.main = inspected.mainClass
        }
        def yamlMap = Mapper.mapping(attributes, true) as Map<String, Object>
        validateYamlMap(yamlMap)
        return yamlMap
    }

    static def validateYamlMap(Map<String, Object> yamlMap) {
        if (yamlMap.get("main") == null) {
            throw new IllegalArgumentException("""\
                ScreamingBuilderPlugin couldn\'t find a main class automatically.
                Please manually present your main class using @kr.entree.spigradle.Plugin annotation
                or set the 'main' property in bungee {} block in build.gradle
                or just disable the bungeePluginYaml task like below.
                
                "tasks.bungeePluginYaml.enabled = false"\
            """.stripIndent())
        }
    }

    static Yaml createYaml() {
        def options = new DumperOptions()
        options.with {
            defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
            indicatorIndent = indent - 1
        }
        return new Yaml(options)
    }
}
