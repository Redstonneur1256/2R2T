package fr.redstonneur1256.omega.executor.javascript;

import arc.Core;
import fr.redstonneur1256.omega.executor.CodeScope;
import rhino.NativeJavaObject;
import rhino.Scriptable;
import rhino.Undefined;

import java.util.concurrent.CompletableFuture;

public class JavascriptScope implements CodeScope {

    private JavascriptScopeManager manager;
    private Scriptable scope;
    private String scriptName;

    public JavascriptScope(JavascriptScopeManager manager, Scriptable scope, String scriptName) {
        this.manager = manager;
        this.scope = scope;
        this.scriptName = scriptName + ".js";
    }

    @Override
    public CompletableFuture<Object> execute(String code) {
        CompletableFuture<Object> future = new CompletableFuture<>();

        Core.app.post(() -> {
            try {
                manager.getConstants().each((name, value) -> scope.put(name, scope, value));

                var object = manager.getContext().evaluateString(scope, code, scriptName, 0);

                if(object instanceof NativeJavaObject nativeObject) {
                    object = nativeObject.unwrap();
                } else if(object instanceof Undefined) {
                    object = "undefined";
                }

                future.complete(object);
            } catch(Throwable throwable) {
                future.completeExceptionally(throwable);
            }
        });

        return future;
    }

    @Override
    public Object getVariable(String name) {
        return scope.get(name, scope);
    }

    @Override
    public void setVariable(String name, Object value) {
        scope.put(name, scope, value);
    }

    @Override
    public void deleteVariable(String name) {
        scope.delete(name);
    }

    public JavascriptScopeManager getManager() {
        return manager;
    }

    public Scriptable getScope() {
        return scope;
    }

    public String getScriptName() {
        return scriptName;
    }

}
