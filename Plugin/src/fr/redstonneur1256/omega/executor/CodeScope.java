package fr.redstonneur1256.omega.executor;

import java.util.concurrent.CompletableFuture;

public interface CodeScope {

    CompletableFuture<Object> execute(String code);

    Object getVariable(String name);

    void setVariable(String name, Object value);

    void deleteVariable(String name);

}
