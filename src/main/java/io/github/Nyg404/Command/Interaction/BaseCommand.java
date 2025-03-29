package io.github.Nyg404.Command.Interaction;

import io.github.Nyg404.Command.CommandContext;
import io.github.Nyg404.Command.CommandExecutor;

public class BaseCommand implements CommandExecutor {

    @Override
    public void execute(CommandContext context) {
        if (context.getArgs().size() > 0) {
            String action = context.getArgs().get(0).toLowerCase(); // Первый аргумент
            
            switch (action) {
                case "slap":
                    if (context.isReplyToAnotherUser()) {
                        String repliedUserName = context.getRepliedUserName();
                        context.sendMessageReply("Вы дали по жопе @" + repliedUserName + "!");
                    } else {
                        context.sendMessageReply("Вы должны ответить на сообщение пользователя!");
                    }
                    break;
                
                case "kick":
                    if (context.isReplyToAnotherUser()) {
                        String repliedUserName = context.getRepliedUserName();
                        context.sendMessageReply("Вы пнули @" + repliedUserName + "!");
                    } else {
                        context.sendMessageReply("Вы должны ответить на сообщение пользователя!");
                    }
                    break;
                
                default:
                    context.sendMessageReply("Неизвестное действие! Используйте: `slap`, `kick` и т. д.");
                    break;
            }
        } else {
            context.sendMessageReply("Вы должны указать действие! Например: `/отн slap`");
        }

    }
}
