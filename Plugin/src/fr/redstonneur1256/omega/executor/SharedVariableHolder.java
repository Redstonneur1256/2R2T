package fr.redstonneur1256.omega.executor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rhino.ScriptRuntime;
import rhino.Scriptable;
import rhino.ScriptableObject;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class SharedVariableHolder implements Map<Object, Object>, Scriptable {

    private Map<Object, Object> delegate;
    private Scriptable prototype;
    private Scriptable parentScope;

    public SharedVariableHolder(Map<Object, Object> delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getClassName() {
        return "SharedObject";
    }

    @Override
    public Object get(String s, Scriptable scriptable) {
        return delegate.get(s);
    }

    @Override
    public Object get(int i, Scriptable scriptable) {
        return delegate.get(i);
    }

    @Override
    public boolean has(String s, Scriptable scriptable) {
        return delegate.containsKey(s);
    }

    @Override
    public boolean has(int i, Scriptable scriptable) {
        return delegate.containsKey(i);
    }

    @Override
    public void put(String s, Scriptable scriptable, Object o) {
        delegate.put(s, o);
    }

    @Override
    public void put(int i, Scriptable scriptable, Object o) {
        delegate.put(i, o);
    }

    @Override
    public void delete(String s) {
        delegate.remove(s);
    }

    @Override
    public void delete(int i) {
        delegate.remove(i);
    }

    @Override
    public Scriptable getPrototype() {
        return prototype;
    }

    @Override
    public void setPrototype(Scriptable prototype) {
        this.prototype = prototype;
    }

    @Override
    public Scriptable getParentScope() {
        return parentScope;
    }

    @Override
    public void setParentScope(Scriptable parentScope) {
        this.parentScope = parentScope;
    }

    @Override
    public Object[] getIds() {
        return delegate.keySet().toArray();
    }

    @Override
    public Object getDefaultValue(Class<?> aClass) {
        return ScriptableObject.getDefaultValue(this, aClass);
    }

    @Override
    public boolean hasInstance(Scriptable scriptable) {
        return ScriptRuntime.jsDelegatesTo(scriptable, this);
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return delegate.get(key);
    }

    @Nullable
    @Override
    public Object put(Object key, Object value) {
        return delegate.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return delegate.remove(key);
    }

    @Override
    public void putAll(@NotNull Map<?, ?> m) {
        delegate.putAll(m);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @NotNull
    @Override
    public Set<Object> keySet() {
        return delegate.keySet();
    }

    @NotNull
    @Override
    public Collection<Object> values() {
        return delegate.values();
    }

    @NotNull
    @Override
    public Set<Entry<Object, Object>> entrySet() {
        return delegate.entrySet();
    }

    @Override
    public String toString() {
        return getClassName();
    }

}
