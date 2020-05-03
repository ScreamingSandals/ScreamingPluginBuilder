package org.screamingsandals.gradle.builder.asm

import org.gradle.api.Project
import kr.entree.spigradle.libs.asm.ClassReader;
import kr.entree.spigradle.libs.asm.Opcodes

import java.nio.file.Files
import java.nio.file.Path
import kr.entree.spigradle.asm.ByteInspector
import kr.entree.spigradle.asm.InspectorContext
import kr.entree.spigradle.asm.visitor.ClassInspector

import static kr.entree.spigradle.libs.asm.ClassReader.*;

class CustomByteInspector extends ByteInspector {

    public final def pluginClass;

    CustomByteInspector() {
        this(SPIGOT_PLUGIN_NAME)
    }

    CustomByteInspector(Project project, String pluginClass) {
        super(project);
        this.pluginClass = pluginClass;
    }

    /* from ByteInspector */

    @Override
    InspectorContext doInspect() {
        def context = new InspectorContext()
        def targets = new HashSet([this.pluginClass])
        directories.find { directory ->
            Files.walk(directory.toPath()).withCloseable { classPaths ->
                classPaths.findAll {
                    it.toString().endsWith('.class')
                }.<Path> find { classPath ->
                    classPath.withInputStream { input ->
                        def classBytes = input.bytes
                        def reader = new ClassReader(classBytes)
                        reader.accept(
                                new ClassInspector(Opcodes.ASM7, context, targets),
                                SKIP_CODE | SKIP_DEBUG | SKIP_FRAMES
                        )
                        if (context.pluginsAnnotationFound) {
                            redefineCompileThings(classBytes, classPath)
                            context.pluginsAnnotationFound = false
                        }
                    }
                    context.isDone()
                }
            }
            context.isDone()
        }
        return context
    }
}
