package fr.redstonneur1256.omega.executor;

import arc.struct.ObjectMap;
import mindustry.gen.Player;
import org.jetbrains.annotations.NotNull;

public interface CodeScopeManager {

    @NotNull
    default CodeScope getScope(Player player) {
        return getScope(player.uuid(), player.name());
    }

    @NotNull
    CodeScope getScope(String identifier, String friendlyName);

    default void deleteScope(Player player) {
        deleteScope(player.uuid());
    }

    void deleteScope(String identifier);

    ObjectMap<String, Object> getConstants();

    void defineConstant(String name, Object value);

    void deleteConstant(String name);

}
