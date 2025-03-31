package io.github.Nyg404.KeyBoard.Cringe;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import io.github.Nyg404.Command.CommandContext;
import io.github.Nyg404.Server.ServerProfile;

public class KeyboardManager {
    private static KeyboardManager instance;
    private Map<String, KeyBoardInterface> registeredKeyboards = new HashMap<>();

    private KeyboardManager() {}

    public static KeyboardManager getInstance() {
        if (instance == null) {
            synchronized (KeyboardManager.class) {
                if (instance == null) {
                    instance = new KeyboardManager();
                }
            }
        }
        return instance;
    }

    public KeyBoardInterface getKeyboardForCallback(String callbackData) {
        for (Map.Entry<String, KeyBoardInterface> entry : registeredKeyboards.entrySet()) {
            if (callbackData.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    public void generateKeyboard(CommandContext context) {
        String command = context.getCommand().toLowerCase();
        if (command.equals("perms")) {
            if (context.getArgs().size() < 1) {
                context.sendMessage("Пожалуйста, укажите тип разрешения.");
                return;
            }
            String permTypeName = context.getArgs().get(0).toLowerCase();
            try {
                PermissionType permissionType = PermissionType.fromString(permTypeName);
                CompletableFuture<Integer> currentLevel = ServerProfile.selectPerm(context.getChat().getId(), permissionType);
                KeyBoardInterface keyboard = new PermissionLevelKeyboard(context.getChat().getId(), permissionType, currentLevel);
                keyboard.generateKeyboard().thenAccept(markup -> {
                    context.sendMessageKeyboard("Выберите уровень для " + permissionType.getPermissionName(), markup);
                    String callbackPrefix = permissionType.getPermissionName() + "_level_";
                    registerKeyboard(callbackPrefix, keyboard);
                    System.out.println("Полученная команда: " + command);
                    System.out.println("Полученный аргумент: " + permTypeName);

                }).exceptionally(throwable -> {
                    context.sendMessage("Ошибка при генерации клавиатуры: " + throwable.getMessage());
                    return null;
                });
            } catch (IllegalArgumentException e) {
                System.out.println("Полученная команда: " + command);
                System.out.println("Полученный аргумент: " + permTypeName);

                context.sendMessage("Неверный тип разрешения: " + permTypeName);
            }
        }
        ;

    }

    public void registerKeyboard(String callbackPrefix, KeyBoardInterface keyboard) {
        registeredKeyboards.put(callbackPrefix, keyboard);
    }
}