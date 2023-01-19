package fr.redstonneur1256.omega;

import arc.Core;
import arc.Events;
import arc.util.CommandHandler;
import arc.util.Log;
import arc.util.Time;
import com.owlike.genson.Genson;
import com.owlike.genson.GensonBuilder;
import com.owlike.genson.reflect.VisibilityFilter;
import dev.jeka.core.api.depmanagement.*;
import dev.jeka.core.api.depmanagement.resolution.JkDependencyResolver;
import dev.jeka.core.api.depmanagement.resolution.JkResolutionParameters;
import dev.jeka.core.api.depmanagement.resolution.JkResolveResult;

import fr.redstonneur1256.omega.commands.CodeCommand;
import fr.redstonneur1256.omega.config.Configuration;
import fr.redstonneur1256.omega.executor.CodeScopeManager;
import fr.redstonneur1256.omega.executor.SharedVariableHolder;
import fr.redstonneur1256.omega.executor.groovy.GroovyScopeManager;
import fr.redstonneur1256.omega.executor.javascript.JavascriptScopeManager;
import fr.redstonneur1256.omega.remote.Connection;
import fr.redstonneur1256.omega.util.WatchDogThread;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import mindustry.mod.Plugin;
import mindustry.net.Packets;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Core2R2T extends Plugin {

    private static final Genson GENSON = new GensonBuilder().setFieldFilter(VisibilityFilter.ALL).useIndentation(true).create();

    private Configuration config;
    private ScheduledExecutorService scheduler;
    private WatchDogThread watchdog;
    private Map<String, CodeScopeManager> executors;
    private Map<Object, Object> sharedVariables;
    private HttpClient client;
    private List<Connection> connections;

    @Override
    public void init() {
        try {
            reloadConfiguration();

            Log.info("Loading libraries, this might take a while");
            var start = Time.millis();

            List<JkCoordinateDependency> dependencies = config.getDependencies()
                    .stream()
                    .map(JkCoordinateDependency::of)
                    .map(dependency -> dependency.withTransitivity(JkTransitivity.RUNTIME))
                    .toList();
            JkDependencyResolver<Void> resolver = JkDependencyResolver.of();
            resolver.addRepos(JkRepoSet.of(config.getRepositories().toArray(String[]::new)));
            JkResolveResult result = resolver.resolve(JkDependencySet.of(dependencies), JkResolutionParameters.of().setFailOnDependencyResolutionError(false));

            var method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            var loader = getClass().getClassLoader();
            for(Path entry : result.getFiles().getEntries()) {
                method.invoke(loader, entry.toUri().toURL());
            }

            Log.info("Loaded libraries in @ ms", Time.timeSinceMillis(start));

            scheduler = Executors.newSingleThreadScheduledExecutor();

            watchdog = new WatchDogThread(this);
            watchdog.start();

            executors = new HashMap<>();
            executors.put("javascript", new JavascriptScopeManager());
            executors.put("groovy", new GroovyScopeManager());

            sharedVariables = new ConcurrentHashMap<>();
            executors.get("javascript").defineConstant("shared", new SharedVariableHolder(sharedVariables));
            executors.get("groovy").defineConstant("shared", sharedVariables);

            client = HttpClient.newHttpClient();

            connections = config.getServers().stream().map(server -> new Connection(this, server)).toList();
            connections.forEach(Connection::connect);

            Events.run(EventType.DisposeEvent.class, this::disable);
        } catch(Exception exception) {
            Log.err("Failed to load 2R2T-Core", exception);
            throw new RuntimeException(exception);
        }
    }

    public void disable() {
        watchdog.interrupt();

        connections.forEach(Connection::disconnect);

        scheduler.shutdownNow();
    }

    @Override
    public void registerClientCommands(CommandHandler handler) {
        // groovy is disabled from in game because imports are missing
        //handler.register("groovy", "<code...>", "", new CodeCommand(this, "groovy", true))
        //handler.register("g", "<code...>", "", new CodeCommand(this, "groovy", true))

        handler.register("javascript", "<code...>", "", new CodeCommand(this, "javascript"));
        handler.register("js", "<code...>", "", new CodeCommand(this, "javascript"));
    }

    public void reloadConfiguration() throws Exception {
        var configFile = getConfig();

        if(!configFile.exists()) {
            config = Configuration.createDefault();
            saveConfiguration();
            return;
        }

        try(var reader = configFile.reader()) {
            config = GENSON.deserialize(reader, Configuration.class);
        }
    }

    public void saveConfiguration() throws IOException {
        try(var writer = getConfig().writer(false)) {
            GENSON.serialize(config, writer);
        }
    }

    public Configuration getConfiguration() {
        return config;
    }

    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    public Map<String, CodeScopeManager> getExecutors() {
        return executors;
    }

    public Map<Object, Object> getSharedVariables() {
        return sharedVariables;
    }

    public HttpClient getClient() {
        return client;
    }

    public CodeScopeManager getEnvironment(String environment) {
        return executors.get(environment);
    }

}
