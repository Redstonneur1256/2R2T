package fr.redstonneur1256.omega.executor.groovy;

import arc.Core;
import fr.redstonneur1256.omega.executor.CodeScope;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.util.concurrent.CompletableFuture;

public class GroovyScope implements CodeScope {

    private GroovyScopeManager manager;
    private String identifier;
    private String scriptBaseName;
    private GroovyShell shell;
    private Binding binding;

    public GroovyScope(GroovyScopeManager manager, String identifier, String scriptBaseName) {
        this.manager = manager;
        this.identifier = identifier;
        this.scriptBaseName = scriptBaseName;
        this.shell = new GroovyShell();
        this.binding = shell.getContext();
    }

    @Override
    public CompletableFuture<Object> execute(String code) {
        return CompletableFuture.supplyAsync(() -> shell.parse(code, scriptBaseName + ".groovy"))
                .thenCompose(this::runScriptSync);
    }

    @Override
    public Object getVariable(String name) {
        return binding.getVariable(name);
    }

    @Override
    public void setVariable(String name, Object value) {
        binding.setVariable(name, value);
    }

    @Override
    public void deleteVariable(String name) {
        binding.removeVariable(name);
    }

    private CompletableFuture<Object> runScriptSync(Script script) {

        CompletableFuture<Object> future = new CompletableFuture<>();
        Core.app.post(() -> {
            try {
                manager.getConstants().each(binding::setVariable);

                future.complete(script.run());
            } catch(Throwable throwable) {
                future.completeExceptionally(throwable);
            }
        });
        return future;
    }

    public GroovyScopeManager getManager() {
        return manager;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getScriptBaseName() {
        return scriptBaseName;
    }

    public GroovyShell getShell() {
        return shell;
    }

    public Binding getBinding() {
        return binding;
    }

}
