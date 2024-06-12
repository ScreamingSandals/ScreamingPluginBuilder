/*
 * Copyright 2024 ScreamingSandals
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.screamingsandals.gradle.builder.tasks;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public abstract class JavadocUploadTask extends SftpUploadTask {
    @Input
    public abstract @NotNull Property<String> getJavaDocCustomDirectoryPath();

    public JavadocUploadTask() {
        dependsOn("javadoc");
    }

    @TaskAction
    public void run() {
        try {
            List<String> projectJavadocDirectories;
            var custom = getJavaDocCustomDirectoryPath().getOrNull();
            if (custom != null && !custom.isEmpty()) {
                if (this.getProject().getRootProject() == this.getProject()) {
                    projectJavadocDirectories = List.of(custom);
                } else {
                    projectJavadocDirectories = List.of(custom, this.getProject().getName());
                }
            } else {
                if (this.getProject().getRootProject() == this.getProject()) {
                    projectJavadocDirectories = List.of(this.getProject().getName());
                } else {
                    projectJavadocDirectories = List.of(this.getProject().getRootProject().getName(), this.getProject().getName());
                }
            }

            uploadFolder(Objects.requireNonNull(((Javadoc) this.getProject().getTasks().getByName("javadoc")).getDestinationDir()), projectJavadocDirectories);
        } catch (SftpException | JSchException exception) {
            throw new RuntimeException("Unable to upload JavaDoc to SFTP", exception);
        }
    }
}
