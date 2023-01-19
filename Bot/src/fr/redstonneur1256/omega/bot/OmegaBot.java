package fr.redstonneur1256.omega.bot;

import co.aikar.commands.JDACommandManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import fr.redstonneur1256.omega.bot.config.Config;
import fr.redstonneur1256.omega.bot.api.Server;
import fr.redstonneur1256.omega.bot.util.EventManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class OmegaBot {

    public static void main(String[] args) throws Throwable {
        OmegaBot bot = new OmegaBot();
        bot.start();
    }

    private Config config;
    public String groovyCodeBase;
    private EventManager eventManager;
    private JDA jda;
    private JDACommandManager commandManager;
    private Server server;

    public void start() throws Throwable {
        config = new ObjectMapper(new YAMLFactory()).readValue(new File("config.yml"), Config.class);

        groovyCodeBase = Files.readString(Path.of("groovy.groovy"));

        eventManager = new EventManager();
        eventManager.on(ReadyEvent.class, event -> System.out.printf("Bot connected as %s (%s)%n", event.getJDA().getSelfUser().getAsTag(), event.getJDA().getSelfUser().getId()));

        jda = JDABuilder.createLight(config.token)
                .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(eventManager)
                .build();

        commandManager = new JDACommandManager(jda);
        commandManager.setDefaultConfig(new SimpleCommandConfig("!"));

        server = new Server(this);
        server.bind(config.serverHost, config.serverPort);

        var scanner = new Scanner(System.in);
        String line;
        while(scanner.hasNext() && (line = scanner.nextLine()) != null) {
            if(line.equalsIgnoreCase("stop")) {
                break;
            }
        }
        scanner.close();

        shutdown();
    }

    private void shutdown() {
        jda.shutdownNow();

        server.stop();

        System.exit(0);
    }

    public Config getConfig() {
        return config;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public JDA getJDA() {
        return jda;
    }

    public JDACommandManager getCommandManager() {
        return commandManager;
    }

    public Server getServer() {
        return server;
    }

}
