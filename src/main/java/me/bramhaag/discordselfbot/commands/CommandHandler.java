package me.bramhaag.discordselfbot.commands;

import lombok.NonNull;
import me.bramhaag.discordselfbot.Bot;
import me.bramhaag.discordselfbot.util.BreakException;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class CommandHandler {

    private Map<String, CommandData> commands = new HashMap<>();

    /**
     * Register commands
     * @param objects Command classes to register
     */
    public void register(@NonNull Object... objects) {
        Arrays.stream(objects).forEach(this::register);
    }

    /**
     * Register a single command
     * @param object Command to register
     */
    private void register(@NonNull Object object) {
        Arrays.stream(object.getClass().getMethods()).forEach(method -> {
            if(!method.isAnnotationPresent(Command.class)) return;

            Command info = method.getAnnotation(Command.class);
            CommandData commandData = new CommandData(method, info, object);

            commands.put(info.name(), commandData);
            Arrays.stream(info.aliases()).forEach(alias -> commands.put(alias, commandData));
        });
    }

    public void unregister() {
        commands.clear();
    }

    /**
     * Execute command from a {@link MessageReceivedEvent}
     * @param event Event which contains all data for the command
     *
     * @throws BreakException thrown when command is found
     */
    public void executeCommand(@NonNull MessageReceivedEvent event) {
        Message message = event.getMessage();

        String content = message.getRawContent();
        String[] parts = content.split(" ");

        String name = parts[0].substring(Bot.PREFIX.length()).trim();
        String[] args = new String[parts.length - 1];

        for(int i = 1; i < parts.length; i++) {
            args[i - 1] = parts[i].trim();
        }

        commands.forEach((command, data) -> {
            if (!name.equalsIgnoreCase(command)) return;

            Command info = data.getAnnotation();
            if (info.minArgs() != -1 && info.minArgs() > args.length) {
                message.editMessage(new MessageBuilder().appendCodeBlock("An error occurred while executing command! Not enough arguments!", "javascript").build()).queue();
                return;
            }

            try {
                data.getMethod().invoke(data.getExecutor(), message, message.getTextChannel(), args);
                throw new BreakException();

            } catch (IllegalAccessException | InvocationTargetException e) {
                //TODO handle exception
                e.printStackTrace();
            }

            /*try {
                Object output = data.getMethod().invoke(data.getExecutor(), message, args);

                if(output == null) {
                    return;
                }

                if(output instanceof Message) {
                    message.editMessage((Message) output).queue();
                    return;
                }

                if(output instanceof MessageEmbed) {
                    MessageEmbed messageEmbed = (MessageEmbed) output;

                    if(message.getGuild().getMember(message.getAuthor()).hasPermission((Channel)message.getChannel(), Permission.MESSAGE_EMBED_LINKS))
                        message.editMessage(messageEmbed).queue();
                    else
                        message.editMessage(messageEmbed.getDescription()).queue();
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                //TODO handle exception
                e.printStackTrace();
            }*/
        });
    }
}
