package fr.redstonneur1256.omega.bot.api.socket;

import co.aikar.commands.BaseCommand;
import fr.redstonneur1256.omega.bot.api.Server;
import fr.redstonneur1256.omega.bot.api.ws.WsConnection;
import fr.redstonneur1256.omega.bot.commands.CodeExecuteCommand;
import fr.redstonneur1256.omega.bot.config.ServerInfo;
import fr.redstonneur1256.omega.messages.auth.AuthenticationRequest;
import fr.redstonneur1256.omega.messages.code.ExecutionRequest;
import fr.redstonneur1256.omega.messages.code.ExecutionResult;
import org.eclipse.collections.api.factory.primitive.IntObjectMaps;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

public class ServerSlaveConnection {

    public static final Logger LOGGER = LoggerFactory.getLogger(ServerSlaveConnection.class);

    private Server server;
    private WsConnection connection;
    private List<BaseCommand> registeredCommands;
    private MutableIntObjectMap<BiConsumer<Boolean, String>> pendingExecutions;
    private int lastExecutionId;
    private ServerInfo serverInfo;
    private List<String> environments;

    public ServerSlaveConnection(Server server, WsConnection connection) {
        this.server = server;
        this.connection = connection;
        this.registeredCommands = new CopyOnWriteArrayList<>();
        this.pendingExecutions = IntObjectMaps.mutable.empty();

        connection.onClose(this::onClose);
        connection.on(AuthenticationRequest.class, this::onAuthentication);
        connection.on(ExecutionResult.class, this::onExecutionResult);
    }

    public void executeCode(String env, String code, BiConsumer<Boolean, String> callback) {
        if(env.contains("groovy")) {
            code = server.getBot().groovyCodeBase + code;
        }

        int id = lastExecutionId++;
        pendingExecutions.put(id, callback);
        connection.send(new ExecutionRequest(id, env, code));
    }

    private void onClose() {
        pendingExecutions.forEachValue(callback -> callback.accept(true, "Connection to server was closed before result was returned"));
        pendingExecutions.clear();

        for(BaseCommand command : registeredCommands) {
            server.getBot().getCommandManager().unregisterCommand(command);
        }

        server.deleteConnection(this);
    }

    private void onAuthentication(AuthenticationRequest request) {
        if(serverInfo != null) {
            return;
        }
        serverInfo = server.getBot().getConfig().getServerByKey(request.key);
        if(serverInfo == null) {
            connection.getContext().closeSession(1003, null);
            return;
        }
        environments = request.environments;

        // Register commands:
        var bot = server.getBot();
        var manager = bot.getCommandManager();
        var aliases = bot.getConfig().commandAliases;
        var suffix = serverInfo.suffix;
        for(String environment : getEnvironments()) {
            var names = new ArrayList<>(List.of(environment + suffix));
            names.addAll(aliases.getOrDefault(environment, Collections.emptyList()).stream().map(alias -> alias + suffix).toList());
            for(String name : names) {
                var command = new CodeExecuteCommand(name, bot, environment, this);
                manager.registerCommand(command);
                registeredCommands.add(command);
            }
        }

        server.registerConnection(serverInfo.getName(), this);
    }

    private void onExecutionResult(ExecutionResult result) {
        var callback = pendingExecutions.remove(result.executionId);
        if(callback != null) {
            callback.accept(result.failure, result.text);
        }
    }

    public Server getServer() {
        return server;
    }

    public WsConnection getConnection() {
        return connection;
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public List<String> getEnvironments() {
        return environments;
    }

    public boolean supportsEnvironment(String env) {
        return environments.contains(env);
    }

}
