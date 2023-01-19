package fr.redstonneur1256.omega.config;

import java.util.List;

public class Configuration {

    private List<RemoteServerInfo> servers;
    private List<String> repositories;
    private List<String> dependencies;
    private int watchdogCrashTime;

    public List<RemoteServerInfo> getServers() {
        return servers;
    }

    public List<String> getRepositories() {
        return repositories;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public int getWatchdogCrashTime() {
        return watchdogCrashTime;
    }

    public static Configuration createDefault() {
        var config = new Configuration();

        config.servers = List.of(RemoteServerInfo.createDefault());
        config.repositories = List.of("https://repo.maven.apache.org/maven2");
        config.dependencies = List.of("org.codehaus.groovy:groovy-all:3.0.12", "org.apache.ivy:ivy:2.5.0");
        config.watchdogCrashTime = 60_000; // 60 seconds

        return config;
    }

}
