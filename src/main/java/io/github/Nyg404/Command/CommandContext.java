package io.github.Nyg404.Command;

import io.github.Nyg404.Account.UserAccount;
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

import static io.github.Nyg404.Command.CommandManager.PREFIX;


@Getter
@Slf4j
public class CommandContext {
    private final Chat chat;
    private final CompletableFuture<UserAccount> userId;
    private final String command;
    private final List<String> args;
    private final int messageId;

    public CommandContext(Message message) {
        this.chat = message.getChat();
        this.userId = UserAccount.of(message.getFrom().getId(), message.getChatId());
        this.messageId = message.getMessageId();

        String text = message.getText();

        // Проверка на наличие префикса
        if (text.startsWith(PREFIX)) {
            // Убираем префикс
            String[] parts = text.substring(PREFIX.length()).split("\\s+", 2);
            this.command = parts[0];
            this.args = parts.length > 1 ? List.of(parts[1].split("\\s+")) : List.of();
        } else {
            this.command = null;  // Если нет префикса, то команда считается невалидной
            this.args = List.of();
        }
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
