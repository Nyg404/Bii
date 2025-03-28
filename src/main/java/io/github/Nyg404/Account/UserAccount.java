package io.github.Nyg404.Account;

import io.github.Nyg404.DataBase.DBConnection;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
@Getter
@Slf4j
public class UserAccount {

    private final long telegramUserId;
    private final long telegramUserIdOnServer;
    private final int permsLevel;

    private static final ConcurrentHashMap<String, UserAccount> cacheUser = new ConcurrentHashMap<>();

    private static final String selectUser = "SELECT * FROM users WHERE userid = ? AND serverid = ?";


    public UserAccount(long telegramUserId, long telegramUserIdOnServer, int permsLevel) {
        this.telegramUserId = telegramUserId;
        this.telegramUserIdOnServer = telegramUserIdOnServer;
        this.permsLevel = permsLevel;
    }
    private static String cacheKey(Long telegramUserId, long telegramUserIdOnServer){
        return telegramUserId + ":" + telegramUserIdOnServer;
    }

    public static CompletableFuture<UserAccount> of(long telegramUserId, long telegramUserIdOnServer) {
        String cacheKey = cacheKey(telegramUserId, telegramUserIdOnServer);
        UserAccount userAccount = cacheUser.get(cacheKey);

        if (userAccount != null) {
            return CompletableFuture.completedFuture(userAccount);
        }

        return CompletableFuture.supplyAsync(() -> {
            UserAccount account = null;
            try (Connection connection = DBConnection.getConnection()) {
                try (PreparedStatement prst = connection.prepareStatement(selectUser)) {
                    prst.setLong(1, telegramUserId);
                    prst.setLong(2, telegramUserIdOnServer);
                    try (ResultSet rs = prst.executeQuery()) {
                        if (rs.next()) {
                            account = new UserAccount(rs.getLong("userid"), rs.getLong("serverid"), rs.getInt("perms_level"));
                            cacheUser.put(cacheKey, account);
                        }
                    }
                }
            } catch (SQLException e) {
                log.error("Ошибка при получении пользователя: ", e);
            }
            return account;
        });
    }
}
