package fr.redstonneur1256.omega.bot.util;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@SuppressWarnings("unchecked")
public class EventManager implements EventListener {

    private Map<Class<?>, List<RegisteredListener<?>>> listeners;

    public EventManager() {
        this.listeners = new HashMap<>();
    }

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        callEvent(event);
    }

    public <T extends GenericEvent> RegisteredListener<T> on(Class<T> type, Listener<T> listener) {
        return on(type, listener, Priority.NORMAL);
    }

    public <T extends GenericEvent> RegisteredListener<T> on(Class<T> type, Listener<T> listener, Priority priority) {
        RegisteredListener<T> registeredListener = new RegisteredListener<>(this, type, listener, priority);

        List<RegisteredListener<?>> listeners = this.listeners.computeIfAbsent(type, t -> new CopyOnWriteArrayList<>());
        listeners.add(registeredListener);
        listeners.sort(Comparator.comparing(RegisteredListener::priority));

        return registeredListener;
    }

    public void callEvent(Object event) {
        if(event == null) {
            return;
        }
        List<RegisteredListener<?>> listeners = this.listeners.get(event.getClass());
        if(listeners == null) {
            return;
        }

        for(RegisteredListener<?> listener : listeners) {
            ((Listener<Object>) listener.listener).accept(event);
        }
    }

    public enum Priority {

        HIGHEST,
        HIGH,
        NORMAL,
        LOW,
        LOWEST

    }

    public record RegisteredListener<T>(EventManager manager, Class<T> type, Listener<T> listener, Priority priority) {

        public void unregister() {
            manager.listeners.get(type).remove(this);
        }

    }

    public interface Listener<T> {

        void accept(T event);

    }

}
