package fr.redstonneur1256.omega.bot;

import co.aikar.commands.CommandConfig;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class SimpleCommandConfig implements CommandConfig {

    private List<String> prefixes;

    public SimpleCommandConfig(String... prefixes) {
        this.prefixes = Arrays.asList(prefixes);
    }

    @NotNull
    @Override
    public List<String> getCommandPrefixes() {
        return prefixes;
    }

}
