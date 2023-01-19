package fr.redstonneur1256.omega.remote;

import arc.Core;
import arc.util.Log;
import arc.util.Strings;
import arc.util.Timer;
import fr.redstonneur1256.omega.Core2R2T;
import fr.redstonneur1256.omega.config.RemoteServerInfo;
import fr.redstonneur1256.omega.messages.auth.AuthenticationRequest;
import fr.redstonneur1256.omega.messages.code.ExecutionRequest;
import fr.redstonneur1256.omega.messages.Serialization;
import fr.redstonneur1256.omega.messages.code.ExecutionResult;
import org.codehaus.groovy.util.CharSequenceReader;

import java.net.URI;
import java.net.http.WebSocket;
import java.util.*;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

public class Connection implements WebSocket.Listener {

    private Core2R2T plugin;
    private RemoteServerInfo info;
    private StringBuilder builder;
    private Map<Class<?>, List<Consumer<Object>>> typedListeners;
    private boolean connectingOrConnected;
    private WebSocket socket;

    public Connection(Core2R2T plugin, RemoteServerInfo info) {
        this.plugin = plugin;
        this.info = info;
        this.builder = new StringBuilder();
        this.typedListeners = new HashMap<>();

        on(ExecutionRequest.class, this::onExecutionRequest);
    }

    public void connect() {
        if(connectingOrConnected) {
            return;
        }
        connectingOrConnected = true;
        connect(0);
    }

    public void disconnect() {
        if(!connectingOrConnected) {
            return;
        }
        connectingOrConnected = false;
        socket.sendClose(WebSocket.NORMAL_CLOSURE, "shutting down");
    }

    protected void connect(int attempt) {
        if(!connectingOrConnected) {
            return;
        }
        try {
            socket = plugin.getClient()
                    .newWebSocketBuilder()
                    .buildAsync(URI.create(info.uri), this)
                    .join();
            send(new AuthenticationRequest(info.key, plugin.getExecutors().keySet().stream().toList()));
        } catch(CompletionException exception) {
            int delay = (int) Math.min(Math.pow(2, attempt + 1), 1024);
            Log.err("Failed to connect to remote, retrying in " + delay + " seconds", exception);
            Timer.schedule(() -> connect(attempt + 1), delay);
        }
    }

    public void send(Object object) {
        socket.sendText(Serialization.GENSON.serialize(object), true);
    }

    @SuppressWarnings("unchecked")
    public <T> Runnable on(Class<T> type, Consumer<T> listener) {
        List<Consumer<Object>> listeners = typedListeners.computeIfAbsent(type, t -> new ArrayList<>());
        listeners.add((Consumer<Object>) listener);
        return () -> listeners.remove(listener);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        builder.append(data);
        if(last) {
            Object message = Serialization.GENSON.deserialize(new CharSequenceReader(builder), Object.class);
            builder.setLength(0);

            var listeners = typedListeners.get(message == null ? null : message.getClass());
            if(listeners != null) {
                listeners.forEach(consumer -> {
                    try {
                        consumer.accept(message);
                    } catch(Throwable throwable) {
                        Log.err("Failed to dispatch event @", message);
                        Log.err(throwable);
                    }
                });
            }
        }
        return WebSocket.Listener.super.onText(webSocket, data, last);
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        if(statusCode != WebSocket.NORMAL_CLOSURE) {
            Log.info("WebSocket disconnected, @: @", statusCode, reason);
            connect(1);
        }
        return null;
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        Log.err("Error in websocket", error);
    }

    private void onExecutionRequest(ExecutionRequest request) {
        var executor = plugin.getExecutors().get(request.environment);
        if(executor == null) {
            sendResponse(request, true, "No such environment %s found".formatted(request.environment));
            return;
        }
        Core.app.post(() -> executor.getScope("remote", null)
                .execute(request.code)
                .thenAccept(object -> sendResponse(request, false, String.valueOf(object)))
                .exceptionally(throwable -> {
                    if(throwable instanceof CompletionException exception) {
                        throwable = exception.getCause();
                    }
                    return sendResponse(request, true, Strings.getStackTrace(throwable));
                }));
    }

    private Void sendResponse(ExecutionRequest request, boolean failure, String code) {
        send(new ExecutionResult(request.executionId, failure, code));
        return null; // used for CompletableFuture exceptionally
    }

    public Core2R2T getPlugin() {
        return plugin;
    }

    public RemoteServerInfo getInfo() {
        return info;
    }

}
