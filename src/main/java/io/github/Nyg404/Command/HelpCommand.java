package io.github.Nyg404.Command;

import io.github.Nyg404.KeyBoard.Cringe.PermissionType;
import io.github.Nyg404.Server.ServerProfile;

public class HelpCommand implements CommandExecutor{
    @Override
    public void execute(CommandContext context) {
        ServerProfile.selectPerm(context.getChat().getId(), PermissionType.SLAP)
            .thenAccept(slapLevel -> {
                String message = String.format("""
                    Добро пожаловать в моего бота! Bii

                    Открытый исходный код бота : [Клик-клик](https://github.com/Nyg404/Bii)
                    Телеграмм канал бота : t.me/BiitelegramBot

                    Текущий уровень SLAP: %d
                    """, slapLevel);
                context.sendMessage(message);
            })
            .exceptionally(ex -> {
                // Обработка исключений
                ex.printStackTrace();
                return null;
            });
    }
}
