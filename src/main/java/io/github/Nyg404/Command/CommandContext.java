package io.github.Nyg404.Command;

import io.github.Nyg404.Account.UserAccount;
import io.github.Nyg404.Server.ServerProfile;
import io.github.Nyg404.Main;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.util.List;
import java.util.concurrent.CompletableFuture;



@Getter
@Slf4j
public class CommandContext {
    private final Chat chat;
    private final CompletableFuture<UserAccount> userId;
    private String command;
    private List<String> args;
    private final int messageId;
    
    private CommandContext(Chat chat, int messageId, long userIdValue, long chatIdValue) {
        this.chat = chat;
        this.userId = UserAccount.of(userIdValue, chatIdValue);
        this.messageId = messageId;
        this.command = null;
        this.args = List.of();
    }
    

    public static CompletableFuture<CommandContext> createCommandContext(Message message) {
        CommandContext context = new CommandContext(
            message.getChat(),
            message.getMessageId(),
            message.getFrom().getId(),
            message.getChatId()
        );
        
        return ServerProfile.of(message.getChatId()).thenApply(serverProfile -> {
            // Извлекаем актуальный префикс из БД или используем значение по умолчанию
            String currentPrefix = (serverProfile != null) ? serverProfile.getPrefix() : "/";
            String text = message.getText();
            final String defaultPrefix = "/"; // для команды updateprefix
            
            if (text != null) {
                // Если команда updateprefix вызывается с дефолтным префиксом "/"
                if (text.startsWith(defaultPrefix) && 
                    text.substring(defaultPrefix.length()).toLowerCase().startsWith("updateprefix")) {
                    String[] parts = text.substring(defaultPrefix.length()).split("\\s+", 2);
                    context.command = parts[0];
                    context.args = parts.length > 1 ? List.of(parts[1].split("\\s+")) : List.of();
                }
                // Иначе, используем актуальный префикс из БД для остальных команд
                else if (text.startsWith(currentPrefix)) {
                    String[] parts = text.substring(currentPrefix.length()).split("\\s+", 2);
                    context.command = parts[0];
                    context.args = parts.length > 1 ? List.of(parts[1].split("\\s+")) : List.of();
                } else {
                    context.command = null;
                    context.args = List.of();
                }
            }
            
            return context;
        });
    }
       
    

    
    public void sendMessage(String text) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chat.getId().toString())
                .text(text)
                .build();
        try {
            Main.getBot().getClient().execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения в чат {}: {}", chat.getId(), e.getMessage());
        }
    }

    public void sendMessageReply(String text) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chat.getId().toString())
                .text(text)
                .replyToMessageId(messageId)
                .build();
        try {
            Main.getBot().getClient().execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки ответа в чат {}: {}", chat.getId(), e.getMessage());
        }
    }

    public void sendMessageKeyboard(String text, InlineKeyboardMarkup keyboard) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chat.getId().toString())
                .text(text)
                .replyMarkup(keyboard)
                .build();
        try {
            Main.getBot().getClient().execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения с клавиатурой в чат {}: {}", chat.getId(), e.getMessage());
        }
    }
}