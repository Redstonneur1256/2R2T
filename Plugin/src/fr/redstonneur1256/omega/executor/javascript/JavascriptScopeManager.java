package fr.redstonneur1256.omega.executor.javascript;

import arc.Core;
import arc.struct.ObjectMap;
import fr.redstonneur1256.omega.executor.CodeScopeManager;
import mindustry.Vars;
import org.jetbrains.annotations.NotNull;
import rhino.Context;
import rhino.ImporterTopLevel;
import rhino.Scriptable;

public class JavascriptScopeManager implements CodeScopeManager {

    private String globalCode;
    private ObjectMap<String, JavascriptScope> contexts;
    private ObjectMap<String, Object> constants;
    private Context context;

    public JavascriptScopeManager() {
        globalCode = Core.files.internal("scripts/global.js").readString();
        contexts = new ObjectMap<>();
        constants = new ObjectMap<>();
        context = Vars.platform.getScriptContext();

        context.getWrapFactory().setJavaPrimitiveWrap(false);
        context.setLanguageVersion(Context.VERSION_ES6);
        context.setApplicationClassLoader(getClass().getClassLoader());
        context.setClassShutter(s -> !s.startsWith("fr.redstonneur1256.omega") && !s.startsWith("rhino"));
    }

    @NotNull
    @Override
    public JavascriptScope getScope(String identifier, String friendlyName) {
        return contexts.get(identifier, () -> new JavascriptScope(this, createScriptable(), friendlyName == null ? identifier : friendlyName));
    }

    @Override
    public void deleteScope(String identifier) {
        contexts.remove(identifier);
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
        contexts.values().forEach(context -> context.deleteVariable(name));
    }

    private Scriptable createScriptable() {
        Scriptable scriptable = new ImporterTopLevel(context);
        context.evaluateString(scriptable, globalCode, "global.js", 0);
        return scriptable;
    }

    Context getContext() {
        return context;
    }

}
