package io.github.Nyg404.KeyBoard.Cringe;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import io.github.Nyg404.Command.CommandContext;
import io.github.Nyg404.Server.ServerProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PermissionLevelKeyboard implements KeyBoardInterface {
    private final Long chatId;
    private final PermissionType permissionType;
    private final CompletableFuture<Integer> currentLevel;

    public PermissionLevelKeyboard(Long chatId, PermissionType permissionType, CompletableFuture<Integer> currentLevel) {
        this.chatId = chatId;
        this.permissionType = permissionType;
        this.currentLevel = currentLevel;
    }

    @Override
    public CompletableFuture<InlineKeyboardMarkup> generateKeyboard() {
        return currentLevel.handle((level, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
                return InlineKeyboardMarkup.builder().keyboard(new ArrayList<>()).build();
            }

            List<InlineKeyboardRow> rows = new ArrayList<>();

            for (int i = 0; i <= 4; i += 2) {
                InlineKeyboardRow row = new InlineKeyboardRow();

                String callbackData1 = permissionType.getPermissionName() + "_level_" + i;
                InlineKeyboardButton button1 = InlineKeyboardButton.builder()
                        .text((i == level ? "✅ " : "") + permissionType.getPermissionName() + " Level " + i)
                        .callbackData(callbackData1)
                        .build();
                row.add(button1);

                if (i + 1 <= 4) {
                    String callbackData2 = permissionType.getPermissionName() + "_level_" + (i + 1);
                    InlineKeyboardButton button2 = InlineKeyboardButton.builder()
                            .text((i + 1 == level ? "✅ " : "") + permissionType.getPermissionName() + " Level " + (i + 1))
                            .callbackData(callbackData2)
                            .build();
                    row.add(button2);
                }

                rows.add(row);
            }

            if (rows.size() < 3) {
                InlineKeyboardRow lastRow = new InlineKeyboardRow();
                String callbackData5 = permissionType.getPermissionName() + "_level_5";
                InlineKeyboardButton button5 = InlineKeyboardButton.builder()
                        .text((5 == level ? "✅ " : "") + permissionType.getPermissionName() + " Level 5")
                        .callbackData(callbackData5)
                        .build();
                lastRow.add(button5);
                rows.add(lastRow);
            }

            return InlineKeyboardMarkup.builder().keyboard(rows).build();
        });
    }

    @Override
    public void handleCallback(CommandContext context) {
        String callbackData = context.getCallbackData();

        if (callbackData.startsWith(permissionType.getPermissionName() + "_level_")) {
            int newLevel = Integer.parseInt(callbackData.split("_")[2]);
            ServerProfile.updatePerm(chatId, permissionType, newLevel)
                    .thenRun(() -> context.sendMessage("Установлен уровень " + permissionType.getPermissionName() + ": Level " + newLevel));
        }
    }
}