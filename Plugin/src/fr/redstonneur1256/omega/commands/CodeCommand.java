package fr.redstonneur1256.omega.commands;

import arc.util.CommandHandler;
import arc.util.Strings;
import fr.redstonneur1256.omega.Core2R2T;
import fr.redstonneur1256.omega.executor.CodeScope;
import mindustry.gen.Player;

import java.util.concurrent.CompletionException;

public class CodeCommand implements CommandHandler.CommandRunner<Player> {

    private Core2R2T plugin;
    private String environment;
    private boolean admin;

    public CodeCommand(Core2R2T plugin, String environment) {
        this(plugin, environment, false);
    }

    public CodeCommand(Core2R2T plugin, String environment, boolean admin) {
        this.plugin = plugin;
        this.environment = environment;
        this.admin = admin;
    }

    @Override
    public void accept(String[] args, Player player) {
        if(admin && !player.admin()) {
            return;
        }
        var env = plugin.getEnvironment(environment);
        if(env == null) {
            player.sendMessage("[red]Currently unavailable");
            return;
        }

        CodeScope scope = env.getScope(player);
        scope.setVariable("player", player);

        scope.execute(args[0])
                .thenAccept(result -> sendResult(player, String.valueOf(result), true))
                .exceptionally(throwable -> {
                    if(throwable instanceof CompletionException exception) {
                        throwable = exception.getCause();
                    }
                    return sendResult(player, Strings.getStackTrace(throwable), false);
                });
    }

    private Void sendResult(Player player, String message, boolean success) {
        String[] lines = message.split("\n");
        for(int i = 0; i < (success ? lines.length : 3); i++) {
            player.sendMessage((success ? "[green]" : "[red]") + lines[i]);
        }
        return null;
    }

}
