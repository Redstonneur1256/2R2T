package fr.redstonneur1256.omega.bot.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import fr.redstonneur1256.omega.bot.OmegaBot;
import fr.redstonneur1256.omega.bot.api.socket.ServerSlaveConnection;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.Nullable;

public class CodeExecuteCommand extends BaseCommand {

    private OmegaBot bot;
    private String environment;
    private ServerSlaveConnection client;

    @SuppressWarnings("deprecation")
    public CodeExecuteCommand(@Nullable String cmd, OmegaBot bot, String environment, ServerSlaveConnection client) {
        super(cmd);
        this.bot = bot;
        this.environment = environment;
        this.client = client;
    }

    @Default
    @CommandPermission("administrator")
    @Description("Executes some code")
    public void runCode(MessageReceivedEvent event, @Name("code") String code) {
        event.getMessage().addReaction(Emoji.fromUnicode("\u23F3")).queue();

        client.executeCode(environment, code, (failed, message) -> {
            boolean isTooLong = message.length() > 1900;
            String reply = isTooLong ?
                    bot.getServer().createPaste(message, failed) :
                    "```\n%s\n```".formatted(message);

            event.getMessage()
                    .reply(reply)
                    .mentionRepliedUser(false)
                    .failOnInvalidReply(true)
                    .onErrorFlatMap(throwable -> event.getChannel().sendMessage(reply))
                    .queue();
        });

    }

}
