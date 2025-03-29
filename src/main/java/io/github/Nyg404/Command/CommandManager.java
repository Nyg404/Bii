package io.github.Nyg404.Command;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
@Slf4j
public class CommandManager {
    private final Map<String, CommandExecutor> commands = new HashMap<>();
    protected static final String PREFIX = "/";
    private static CommandManager instance;


    private CommandManager() {
    }


    public static CommandManager getInstance() {
        if (instance == null) {
            synchronized (CommandManager.class) {
                if (instance == null) {
                    instance = new CommandManager();
                }
            }
        }
        return instance;
    }
    public void registerCommand(String name, CommandExecutor command){
        commands.put(name.toLowerCase(), command);
        log.info("Зарегистрирована команда: {}", name);
    }

    public void executeCommand(CommandContext context) {
        if (context.getCommand() == null) {
            return;
        }

        CommandExecutor executor = commands.get(context.getCommand().toLowerCase());
        if (executor != null) {
            executor.execute(context);
            log.info("Выполнение команды: {}", context.getCommand());
        } else {
            context.sendMessage("Неизвестная команда: " + context.getCommand());
            log.warn("Попытка выполнить неизвестную команду: {}", context.getCommand());
        }
    }


}
