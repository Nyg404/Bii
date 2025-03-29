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
    private static final String INSERT_USER_SQL = "INSERT INTO users(userid, serverid, perms_level) VALUES (?,?,?)";


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

    public static CompletableFuture<Void> addUserAsync(long userId, long serverId, int perms_level) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(INSERT_USER_SQL)) {
   
                ps.setLong(1, userId);
                ps.setLong(2, serverId);
                ps.setInt(3, perms_level);  // добавлен параметр perms_level
   
                if (ps.executeUpdate() > 0) {
                    UserAccount userAccount = new UserAccount(userId, serverId, perms_level);
                    cacheUser.put(cacheKey(userId, serverId), userAccount); // исправлено
                }
            } catch (SQLException e) {
                log.error("User insert error [{}@{}]: {}", userId, serverId, e.getMessage());
                throw new RuntimeException(e);
            }
        }).exceptionally(ex -> {
            log.error("Async user add failed: {}", ex.getMessage());
            return null;
        });
    }
   
}
