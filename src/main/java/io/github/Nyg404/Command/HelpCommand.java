package io.github.Nyg404.Command;



public class HelpCommand implements CommandExecutor{
    @Override
    public void execute(CommandContext context) {
        if(context.getChat().isUserChat()){
            context.sendMessageReply("""
                Вы использовали команду help\
                
                Доступные команды: \
                
                А их нет ххохоох""");
        } else if (context.getChat().isGroupChat()) {
            context.sendMessageReply("Куда смотришь?");

        }

    }
}
