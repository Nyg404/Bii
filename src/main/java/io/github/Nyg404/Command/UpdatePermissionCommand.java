package io.github.Nyg404.Command;

import io.github.Nyg404.KeyBoard.Cringe.KeyboardManager;

public class UpdatePermissionCommand implements CommandExecutor {
    @Override
    public void execute(CommandContext context) {
        // Получаем KeyboardManager и вызываем генерацию клавиатуры
        KeyboardManager keyboardManager = KeyboardManager.getInstance();
        keyboardManager.generateKeyboard(context);
    }
}