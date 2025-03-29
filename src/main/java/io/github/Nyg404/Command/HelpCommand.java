package io.github.Nyg404.Command;



public class HelpCommand implements CommandExecutor{
    @Override
    public void execute(CommandContext context) {
        context.sendMessage("Привет");
        if (context.getArgs().isEmpty()){
            context.sendMessage("Мало аргументиков");
        }
    }
}
