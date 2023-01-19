package fr.redstonneur1256.omega.bot.api.ws;

import io.javalin.Javalin;
import io.javalin.websocket.*;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class WsHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler, WsErrorHandler {

    public static WsHandler create(Javalin javalin, String route, Consumer<WsConnection> connectionListener) {
        var handler = new WsHandler(connectionListener);
        javalin.ws(route, config -> {
            config.onConnect(handler);
            config.onMessage(handler);
            config.onClose(handler);
            config.onError(handler);
        });
        return handler;
    }

    private Map<String, WsConnection> connections;
    private Consumer<WsConnection> connectionListener;

    public WsHandler(Consumer<WsConnection> connectionListener) {
        this.connections = new ConcurrentHashMap<>();
        this.connectionListener = connectionListener;
    }

    @Override
    public void handleConnect(@NotNull WsConnectContext context) {
        context.enableAutomaticPings(15, TimeUnit.SECONDS);

        var connection = new WsConnection(context);
        connections.put(context.getSessionId(), connection);
        connectionListener.accept(connection);
    }

    @Override
    public void handleClose(@NotNull WsCloseContext context) {
        var connection = connections.remove(context.getSessionId());

        if(connection != null) {
            connection.onClosed();
        }
    }

    @Override
    public void handleMessage(@NotNull WsMessageContext context) {
        var connection = connections.get(context.getSessionId());

        if(connection != null) {
            connection.handleMessage(context);
        }
    }

    @Override
    public void handleError(@NotNull WsErrorContext context) {

    }

}
