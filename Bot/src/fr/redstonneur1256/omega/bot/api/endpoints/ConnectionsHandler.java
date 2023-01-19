package fr.redstonneur1256.omega.bot.api.endpoints;

import fr.redstonneur1256.omega.bot.api.Server;
import fr.redstonneur1256.omega.bot.api.socket.ServerSlaveConnection;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ConnectionsHandler implements Handler {

    private Server server;

    public ConnectionsHandler(Server server) {
        this.server = server;
    }

    @Override
    public void handle(@NotNull Context ctx) {
        ctx.json(server.getConnections().values().stream().map(connection -> Map.of(
                "name", connection.getServerInfo().getName(),
                "environments", connection.getEnvironments()
        )).toList());
    }

}
