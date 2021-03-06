/*
 * Copyright 2017 Bram Hagens
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.bramhaag.discordselfbot;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import lombok.Getter;
import lombok.NonNull;
import me.bramhaag.discordselfbot.commands.admin.CommandEvaluate;
import me.bramhaag.discordselfbot.commands.admin.CommandPing;
import me.bramhaag.discordselfbot.commands.admin.CommandPrune;
import me.bramhaag.discordselfbot.commands.admin.CommandReload;
import me.bramhaag.discordselfbot.commands.CommandHandler;
import me.bramhaag.discordselfbot.commands.admin.CommandVersion;
import me.bramhaag.discordselfbot.commands.fun.*;
import me.bramhaag.discordselfbot.commands.util.*;
import me.bramhaag.discordselfbot.listeners.MessageListener;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import org.apache.commons.io.FileUtils;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

public class Bot {

    @Getter
    private JDA jda;

    @Getter
    private CommandHandler commandHandler;

    @Getter
    private static Config config;

    /**
     * Constructor which starts bot
     *
     * @param token Bot's token
     */
    Bot(@NonNull String token) throws FileNotFoundException, LoginException, InterruptedException, RateLimitedException {
        this.jda = new JDABuilder(AccountType.CLIENT).setToken(token).setAutoReconnect(true).buildBlocking();

        Preconditions.checkState(extractLibs(), "Cannot extract files!");
        loadConfig();

        this.jda.addEventListener(new MessageListener(this));
        this.commandHandler = new CommandHandler();

        registerCommands();
    }

    /**
     * Register all commands
     */
    //TODO scan package with commands
    public void registerCommands() {
        this.commandHandler.register(
                new CommandEmbed(),
                new CommandEvaluate(),
                new CommandPing(),
                new CommandPrune(),
                new CommandReload(),
                new CommandTimer(),
                new CommandTriggered(),
                new CommandSpace(),
                new CommandHTML(),
                new CommandSpeedtest(),
                new CommandJavadoc(),
                new CommandLMGTFY(),
                new CommandText(),
                new CommandScreeching(),
                new CommandEmoji(),
                new CommandReact(),
                new CommandVersion(),
                new CommandChannels()
        );
    }

    /**
     * Load config
     * @throws FileNotFoundException when file is not found
     */
    public void loadConfig() throws FileNotFoundException {
        config = new Gson().fromJson(new JsonReader(new FileReader("config.json")), Config.class);
    }

    /**
     * Extract files from resources. It won't copy the file if it already exists.
     *
     * @return {@code true} when all files were copied without issues, {@code false} when something went wrong
     */
    private boolean extractLibs() {
        File libsDir   = new File("libs");
        File assetsDir = new File("assets");

        if(!createDir(libsDir) || !createDir(assetsDir)) {
            return false;
        }

        try {
            extract(getClass().getResource("/config.json"),                      new File("config.json"));
            extract(getClass().getResource("/libs/speedtest.py"),                new File(libsDir,   "speedtest.py"));
            extract(getClass().getResource("/assets/triggered.png"),             new File(assetsDir, "triggered.png"));
            extract(getClass().getResource("/assets/autistic_screeching.png"),   new File(assetsDir, "autistic_screeching.png"));
            extract(getClass().getResource("/assets/mildly_inconvenienced.png"), new File(assetsDir, "mildly_inconvenienced.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    /**
     * Create directory
     *
     * @param dir directory to create
     *
     * @return {@code true} when directory was created without issues, {@code false} when something went wrong
     */
    private boolean createDir(File dir) {
        if(!dir.exists() || !dir.isDirectory()) {
            System.out.println(String.format("%s folder not found. Unpacking...", dir.getName()));
            boolean mkdir = dir.mkdir();

            if(!mkdir) {
                System.err.println(String.format("Cannot create folder %s, shutting down...", dir.getName()));
                return false;
            }
        }

        return true;
    }

    /**
     * Copy file from {@code url} to {@code destination}
     *
     * @param url url to file
     * @param destination destination file
     *
     * @throws IOException if {@code url} cannot be opened
     * @throws IOException if {@code destination} is a directory
     * @throws IOException if {@code destination} cannot be written
     * @throws IOException if {@code destination} needs creating but can't be
     * @throws IOException if an IO error occurs during copying
     */
    private void extract(URL url, File destination) throws IOException {
        if(!destination.exists())
            FileUtils.copyURLToFile(url, destination);
    }
}
