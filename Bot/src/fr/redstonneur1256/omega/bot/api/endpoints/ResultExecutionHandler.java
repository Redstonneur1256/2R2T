package fr.redstonneur1256.omega.bot.api.endpoints;

import fr.redstonneur1256.omega.bot.api.Server;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ResultExecutionHandler implements Handler {

    private Server server;

    public ResultExecutionHandler(Server server) {
        this.server = server;
    }

    @Override
    public void handle(@NotNull Context ctx) {
        var id = ctx.pathParam("id");
        var content = server.getLogs().getIfPresent(id);
        if(content == null) {
            ctx.result("404");
            return;
        }
        ctx.render("result.jte", Map.of("result", content));
    }

}
