package fr.redstonneur1256.omega.executor.groovy;

import arc.struct.ObjectMap;
import fr.redstonneur1256.omega.executor.CodeScopeManager;
import org.jetbrains.annotations.NotNull;

public class GroovyScopeManager implements CodeScopeManager {

    private ObjectMap<String, GroovyScope> scopes;
    private ObjectMap<String, Object> constants;

    public GroovyScopeManager() {
        scopes = new ObjectMap<>();
        constants = new ObjectMap<>();
    }

    @NotNull
    @Override
    public GroovyScope getScope(String identifier, String friendlyName) {
        return scopes.get(identifier, () -> new GroovyScope(this, identifier, friendlyName));
    }

    @Override
    public void deleteScope(String identifier) {
        scopes.remove(identifier);
    }

    @Override
    public ObjectMap<String, Object> getConstants() {
        return constants;
    }

    @Override
    public void defineConstant(String name, Object value) {
        constants.put(name, value);
    }

    @Override
    public void deleteConstant(String name) {
        constants.remove(name);
        scopes.values().forEach(scope -> scope.deleteVariable(name));
    }

}
