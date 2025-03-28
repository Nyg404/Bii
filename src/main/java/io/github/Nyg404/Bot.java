package io.github.Nyg404;


import io.github.Nyg404.Command.CommandContext;
import io.github.Nyg404.Command.CommandManager;
import io.github.Nyg404.Command.HelpCommand;
import io.github.Nyg404.DataBase.DBTables;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class Bot implements LongPollingSingleThreadUpdateConsumer {
    private final TelegramClient telegramClient;
    private final OkHttpTelegramClient okHttpTelegramClient;

    public Bot(String token) {
        this.okHttpTelegramClient = new OkHttpTelegramClient(token);
        this.telegramClient = this.okHttpTelegramClient;
        DBTables.createTables();

        // Получаем единственный экземпляр CommandManager
        CommandManager commandManager = CommandManager.getInstance();
        commandManager.registerCommand("help", new HelpCommand());
    }

    @Override
    public void consume(Update update) {
        // Получаем контекст и выполняем команду
        CommandContext context = new CommandContext(update.getMessage());
        CommandManager.getInstance().executeCommand(context);
    }

    public OkHttpTelegramClient getClient() {
        return okHttpTelegramClient;
    }
}


