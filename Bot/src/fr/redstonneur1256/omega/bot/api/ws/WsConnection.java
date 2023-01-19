package fr.redstonneur1256.omega.bot.api.ws;

import fr.redstonneur1256.omega.messages.Serialization;
import io.javalin.websocket.WsContext;
import io.javalin.websocket.WsMessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class WsConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(WsConnection.class);

    private WsContext context;
    private List<Runnable> closingListeners;
    private Map<Class<?>, List<Consumer<Object>>> typedListeners;

    public WsConnection(WsContext context) {
        this.context = context;
        this.closingListeners = new CopyOnWriteArrayList<>();
        this.typedListeners = new ConcurrentHashMap<>();
    }

    public void onClose(Runnable listener) {
        this.closingListeners.add(listener);
    }

    void onClosed() {
        for(Runnable listener : closingListeners) {
            listener.run(); // TODO: Exception handling ?
        }
    }

    @SuppressWarnings("unchecked")
    public <T> Runnable on(Class<T> type, Consumer<T> listener) {
        List<Consumer<Object>> listeners = typedListeners.computeIfAbsent(type, t -> new ArrayList<>());
        listeners.add((Consumer<Object>) listener);
        return () -> listeners.remove(listener);
    }

    public void send(Object message) {
        context.send(Serialization.GENSON.serialize(message));
    }

    void handleMessage(WsMessageContext context) {
        Object message = Serialization.GENSON.deserialize(context.message(), Object.class);
        var listeners = typedListeners.get(message == null ? null : message.getClass());
        if(listeners == null) {
            LOGGER.warn("Received message {} with no associated listeners", message);
            return;
        }
        listeners.forEach(consumer -> {
            try {
                consumer.accept(message);
            } catch(Throwable throwable) {
                LOGGER.error("Failed to dispatch event {}", message, throwable);
            }
        });
    }

    public WsContext getContext() {
        return context;
    }

    public Map<Class<?>, List<Consumer<Object>>> getTypedListeners() {
        return typedListeners;
    }

}
