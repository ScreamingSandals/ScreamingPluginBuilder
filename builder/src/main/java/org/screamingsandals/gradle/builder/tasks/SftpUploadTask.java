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

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

public abstract class SftpUploadTask extends DefaultTask {
    @Input
    public abstract @NotNull Property<String> getSftpUser();
    @Input
    public abstract @NotNull Property<String> getSftpPassword();
    @Input
    public abstract @NotNull Property<String> getSftpHost();
    @Input
    public abstract @NotNull Property<Integer> getSftpPort();
    @Input
    public abstract @NotNull Property<String> getSftpPath();

    public SftpUploadTask() {
        getSftpPort().convention(22);
        getSftpPath().set("www");
    }

    protected void uploadFolder(@NotNull File folder, @NotNull List<@NotNull String> remotePath) throws SftpException, JSchException {
        var jsch = new JSch();
        var jschSession = jsch.getSession(getSftpUser().get(), getSftpHost().get(), getSftpPort().get());
        jschSession.setConfig("StrictHostKeyChecking", "no");
        jschSession.setPassword(getSftpPassword().get());
        jschSession.connect();
        var sftpChannel = (ChannelSftp) jschSession.openChannel("sftp");
        sftpChannel.connect();

        sftpChannel.cd(getSftpPath().get());

        for (var it : remotePath) {
            try {
                sftpChannel.cd(it);
            } catch (SftpException ignored) {
                sftpChannel.mkdir(it);
                sftpChannel.cd(it);
            }
        }

        recursiveClear(sftpChannel);

        recursiveFolderUpload(sftpChannel, folder);

        sftpChannel.disconnect();

        jschSession.disconnect();
    }

    protected static void recursiveClear(@NotNull ChannelSftp sftpChannel) throws SftpException {
        //noinspection unchecked
        for (var entry : ((Vector<ChannelSftp.LsEntry>) sftpChannel.ls("."))) {
            var filename = entry.getFilename();
            if (".".equals(filename) || "..".equals(filename))
                continue;
            if (entry.getAttrs().isDir()) {
                sftpChannel.cd(filename);
                recursiveClear(sftpChannel);
                sftpChannel.cd("..");
                sftpChannel.rmdir(filename);
            } else {
                sftpChannel.rm(filename);
            }
        }
    }

    protected static void recursiveFolderUpload(@NotNull ChannelSftp channelSftp, @NotNull File sourceFolder) throws SftpException {
        for (var entry : Objects.requireNonNull(sourceFolder.listFiles())) {
            if (entry.isDirectory()) {
                channelSftp.mkdir(entry.getName());
                channelSftp.cd(entry.getName());
                recursiveFolderUpload(channelSftp, entry);
                channelSftp.cd("..");
            } else {
                channelSftp.put(entry.getAbsolutePath(), entry.getName());
            }
        }
    }
}
