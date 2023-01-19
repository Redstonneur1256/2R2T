package fr.redstonneur1256.omega.bot.api.endpoints;

import fr.redstonneur1256.omega.bot.api.Server;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class RunHandler implements Handler {

    private Server server;

    public RunHandler(Server server) {
        this.server = server;
    }

    @Override
    public void handle(@NotNull Context ctx) {
        if(!server.getBot().getConfig().apiKeys.contains(ctx.header("key"))) {
            ctx.status(HttpStatus.NOT_FOUND).result("Not Found");
            return;
        }

        var connection = server.getConnection(ctx.pathParam("server"));
        if(connection == null) {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", "Server isn't available"));
            return;
        }
        var env = ctx.pathParam("env");
        if(!connection.supportsEnvironment(env)) {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", "Server doesn't support environment " + env, "supported", connection.getEnvironments()));
            return;
        }
        var code = ctx.body();

        ctx.async(() -> {
            var future = new CompletableFuture<String>();
            connection.executeCode(env, code, (failed, message) -> future.complete(message));
            ctx.status(HttpStatus.OK).result(future.get());
        });
    }

}
