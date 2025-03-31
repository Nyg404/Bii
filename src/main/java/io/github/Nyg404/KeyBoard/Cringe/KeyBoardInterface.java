package io.github.Nyg404.KeyBoard.Cringe;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import io.github.Nyg404.Command.CommandContext;

import java.util.concurrent.CompletableFuture;

public interface KeyBoardInterface {
    CompletableFuture<InlineKeyboardMarkup> generateKeyboard();
    void handleCallback(CommandContext context);
}