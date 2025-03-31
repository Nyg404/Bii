package io.github.Nyg404;

import io.github.Nyg404.Account.UserAccount;
import io.github.Nyg404.Command.CommandContext;
import io.github.Nyg404.Command.CommandManager;
import io.github.Nyg404.Command.HelpCommand;
import io.github.Nyg404.Command.UpdatePermissionCommand;
import io.github.Nyg404.Command.Interaction.BaseCommand;
import io.github.Nyg404.Command.ServerCommand.UpdatePrefix;
import io.github.Nyg404.DataBase.DBTables;
import io.github.Nyg404.KeyBoard.Cringe.KeyBoardInterface;
import io.github.Nyg404.KeyBoard.Cringe.KeyboardManager;
import io.github.Nyg404.Server.ServerProfile;
import lombok.extern.slf4j.Slf4j;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
@Slf4j
public class Bot implements LongPollingSingleThreadUpdateConsumer {
    private final TelegramClient telegramClient;
    private final OkHttpTelegramClient okHttpTelegramClient;
    private final KeyboardManager keyboardManager;

    public Bot(String token) {
        this.okHttpTelegramClient = new OkHttpTelegramClient(token);
        this.telegramClient = this.okHttpTelegramClient;
        this.keyboardManager = KeyboardManager.getInstance();
        DBTables.createTables();

        // Получаем единственный экземпляр CommandManager
        CommandManager commandManager = CommandManager.getInstance();
        commandManager.registerCommand("help", new HelpCommand());
        commandManager.registerCommand("updateprefix", new UpdatePrefix());
        commandManager.registerCommand("отн", new BaseCommand());
        commandManager.registerCommand("perms", new UpdatePermissionCommand());
    }

    @Override
    public void consume(Update update) {
        if(update.hasMessage() && update.getMessage().hasText()){
            ServerProfile.createIfNotExists(update.getMessage().getChatId());
            UserAccount.addUserAsync(update.getMessage().getFrom().getId(), update.getMessage().getChatId(), 0);
        }

        if (update.hasCallbackQuery()) {
            handleCallback(update);
        } else if (update.hasMessage() && update.getMessage().hasText()) {
            handleMessage(update);
        }
    }
    private void handleCallback(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        Message message = (Message) callbackQuery.getMessage();
        String callbackData = callbackQuery.getData();
    
        try {
            // Отвечаем на callback (убираем "часики")
            telegramClient.execute(
                AnswerCallbackQuery.builder()
                    .callbackQueryId(callbackQuery.getId())
                    .build()
            );
        } catch (TelegramApiException e) {
            log.error("Ошибка ответа на callback: {}", e.getMessage());
        }
    
        // Обработка клавиатуры
        CommandContext.createForCallback(message, callbackData)
            .thenAccept(context -> {
                KeyBoardInterface keyboard = keyboardManager.getKeyboardForCallback(callbackData);
                if (keyboard != null) {
                    keyboard.handleCallback(context);
                }
            });
    }
    private void handleMessage(Update update) {
        Message message = update.getMessage();
        if (message != null && message.getFrom() != null) {
            ServerProfile.createIfNotExists(message.getChatId());
            CommandContext.createCommandContext(message, null)
                .thenAccept(context -> {
                    if (context != null) {
                        CommandManager.getInstance().executeCommand(context);
                    }
                });
        }
    }


    public OkHttpTelegramClient getClient() {
        return okHttpTelegramClient;
    }
}