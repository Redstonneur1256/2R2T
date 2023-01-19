package fr.redstonneur1256.omega.bot.config;

import java.util.List;
import java.util.Map;

public class Config {

    public String token;
    public String serverHost;
    public int serverPort;
    public String publicUrl;
    public List<ServerInfo> servers;
    public List<String> apiKeys;
    public Map<String, List<String>> commandAliases;

    public ServerInfo getServerByKey(String key) {
        return servers.stream().filter(info -> info.secret.equalsIgnoreCase(key)).findFirst().orElse(null);
    }

}
