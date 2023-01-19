package fr.redstonneur1256.omega.bot.api;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import fr.redstonneur1256.omega.bot.OmegaBot;
import fr.redstonneur1256.omega.bot.api.endpoints.ConnectionsHandler;
import fr.redstonneur1256.omega.bot.api.endpoints.ResultExecutionHandler;
import fr.redstonneur1256.omega.bot.api.endpoints.RunHandler;
import fr.redstonneur1256.omega.bot.api.socket.ServerSlaveConnection;
import fr.redstonneur1256.omega.bot.api.ws.WsHandler;
import fr.redstonneur1256.omega.bot.util.StringUtil;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinJte;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class Server {

    private OmegaBot bot;
    private Map<String, ServerSlaveConnection> connections;
    private Javalin javalin;
    private Cache<String, String> logs;

    public Server(OmegaBot bot) {
        this.bot = bot;
        this.connections = new ConcurrentHashMap<>();
        this.javalin = Javalin.create(config -> {
            config.jetty.wsFactoryConfig(wsConfig -> {
                wsConfig.setMaxTextMessageSize(Long.MAX_VALUE);
                wsConfig.setMaxBinaryMessageSize(Long.MAX_VALUE);
                wsConfig.setMaxFrameSize(Long.MAX_VALUE);
            });
            config.showJavalinBanner = false;
        });
        this.logs = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                //.<String, String>weigher((k, v) -> v.length())
                //.maximumWeight(10 * 1024 * 1024) // 10 MB
                .build();

        JavalinJte.init(TemplateEngine.create(new DirectoryCodeResolver(Path.of("web/")), ContentType.Html));

        WsHandler.create(javalin, "/api/ws", connection -> new ServerSlaveConnection(this, connection));
        javalin.get("/api/clients", new ConnectionsHandler(this));
        javalin.post("/api/run/{server}/{env}", new RunHandler(this));
        javalin.get("/paste/{id}", new ResultExecutionHandler(this));
    }

    public void bind(String host, int port) {
        javalin.start(host, port);
    }

    public void stop() {
        connections.values().forEach(connection -> connection.getConnection().getContext().closeSession(1012, "Restarting server"));

        javalin.stop();
    }

    public String createPaste(String message, boolean failed) {
        String id = StringUtil.generateString(20, StringUtil.NUMBERS, StringUtil.LOWERCASE_LETTERS);
        logs.put(id, message);
        return bot.getConfig().publicUrl + "/paste/" + id;
    }

    public void registerConnection(String name, ServerSlaveConnection client) {
        connections.put(name.toLowerCase(), client);
    }

    public void deleteConnection(ServerSlaveConnection connection) {
        connections.remove(connection.getServerInfo().getName());
    }

    public ServerSlaveConnection getConnection(String name) {
        return connections.get(name.toLowerCase());
    }

    public OmegaBot getBot() {
        return bot;
    }

    public Map<String, ServerSlaveConnection> getConnections() {
        return connections;
    }

    public Cache<String, String> getLogs() {
        return logs;
    }

}
