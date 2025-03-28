package io.github.Nyg404;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
public class Main {
    private static Bot botInstance;
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();
        try {
            String token = dotenv.get("TOKEN");
            TelegramBotsLongPollingApplication bot = new TelegramBotsLongPollingApplication();
            botInstance = new Bot(token);
            bot.registerBot(token, new Bot(token));
        } catch (TelegramApiException e) {
            log.error("Ошибка запуска бота: ", e);
        }

    }

    public static Bot getBot(){
        return botInstance;
    }
}