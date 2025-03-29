package io.github.Nyg404.Command.ServerCommand;

import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import io.github.Nyg404.Main;
import io.github.Nyg404.Command.CommandContext;
import io.github.Nyg404.Command.CommandExecutor;
import io.github.Nyg404.Server.ServerProfile;
import lombok.extern.slf4j.Slf4j;
@Slf4j
public class UpdatePrefix implements CommandExecutor {

    @Override
    public void execute(CommandContext context) {
        context.getUserId().thenAccept(userAccount -> {
            try {
                GetChatMember getChatMember = GetChatMember.builder()
                        .chatId(context.getChat().getId().toString())
                        .userId(userAccount.getTelegramUserId())
                        .build();

                ChatMember chatMember = Main.getBot().getClient().execute(getChatMember);
                String status = chatMember.getStatus();
                log.info("Статус пользователя: {}", status); // Следите за логами!

                if (!status.equals("administrator") && !status.equals("creator")) {
                    context.sendMessage("❌ Требуются права администратора!");
                    return;
                }

                // Проверяем наличие хотя бы одного аргумента
                if (context.getArgs().isEmpty()) {
                    context.sendMessage("⚠ Используйте: /updateprefix <новый_префикс>");
                    return;
                }
                
                String newPrefix = context.getArgs().get(0);
                log.info("Запрос на изменение префикса: {}", newPrefix);
                ServerProfile.updatePrefix(context.getChat().getId(), newPrefix)
                    .thenRun(() -> {
                        log.info("Префикс изменён на {} для сервера {}", newPrefix, context.getChat().getId());
                        context.sendMessage("✅ Префикс изменён на: " + newPrefix);
                    })
                    .exceptionally(ex -> {
                        log.error("Ошибка: {}", ex.getMessage());
                        context.sendMessage("🚫 Ошибка при изменении префикса!");
                        return null;
                    });
                
            } catch (TelegramApiException e) {
                context.sendMessage("🚫 Ошибка при проверке прав!");
                log.error("Ошибка API: {}", e.getMessage());
            }
        }).exceptionally(e -> {
            log.error("Асинхронная ошибка: {}", e.getMessage());
            return null;
        });
    }
}
