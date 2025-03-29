package io.github.Nyg404.Server;

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
public class ServerProfile {
    private final Long serverId;
    private final String prefix;
    private static final String selectServer = "SELECT * FROM servers WHERE serverid = ?";
    private static final String selectPrefix = "SELECT prefix FROM servers WHERE serverid = ?";
    private static final String updatePrefix = "UPDATE servers SET prefix = ? WHERE serverid = ?";
    private static final ConcurrentHashMap<Long, ServerProfile> cacheServer = new ConcurrentHashMap<>();
    public ServerProfile(Long serverId, String prefix) {
        this.serverId = serverId;
        this.prefix = prefix;
    }

    public static CompletableFuture<ServerProfile> of(long serverId) {
        // Удаляем проверку кэша здесь, чтобы всегда запрашивать актуальные данные
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement prst = connection.prepareStatement(selectServer)) {
                prst.setLong(1, serverId);
                try (ResultSet rs = prst.executeQuery()) {
                    if (rs.next()) {
                        String prefix = rs.getString("prefix");
                        ServerProfile profile = new ServerProfile(serverId, prefix);
                        cacheServer.put(serverId, profile); // Обновляем кэш
                        return profile;
                    }
                }
            } catch (SQLException e) {
                log.error("Ошибка при получении сервера: ", e);
            }
            return null;
        });
    }   

    public static CompletableFuture<String> getPrefix(long serverId) {
        // Всегда проверяем кэш, но перепроверяем при необходимости
        ServerProfile cached = cacheServer.get(serverId);
        if (cached != null) {
            log.info("Используется кэшированный префикс: {} для сервера {}", cached.getPrefix(), serverId);
            return CompletableFuture.completedFuture(cached.getPrefix());
        }
    
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(selectPrefix)) {
                ps.setLong(1, serverId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String prefix = rs.getString("prefix");
                    // Обновляем кэш
                    cacheServer.put(serverId, new ServerProfile(serverId, prefix));
                    log.info("Префикс загружен из БД: {} для сервера {}", prefix, serverId);
                    return prefix;
                }
            } catch (SQLException e) {
                log.error("Ошибка при загрузке префикса", e);
            }
            return "/"; // Дефолтное значение
        });
    }
    

    public static CompletableFuture<Void> updatePrefix(long serverId, String prefix) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(updatePrefix)) {
                ps.setString(1, prefix);
                ps.setLong(2, serverId);
                if (ps.executeUpdate() > 0) {
                    // Принудительно обновляем кэш
                    cacheServer.put(serverId, new ServerProfile(serverId, prefix));
                    log.info("Кэш обновлён. Новый префикс: {} для сервера {}", prefix, serverId);
                }
            } catch (SQLException e) {
                log.error("Ошибка при обновлении префикса", e);
                throw new RuntimeException(e);
            }
        });
    }
    

    public static CompletableFuture<ServerProfile> createIfNotExists(long serverId) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO servers (serverid, prefix) VALUES (?, ?) ON CONFLICT DO NOTHING")) {
                ps.setLong(1, serverId);
                ps.setString(2, "/");
                ps.executeUpdate();
                
                ServerProfile profile = new ServerProfile(serverId, "/");
                cacheServer.put(serverId, profile); 
                return profile;
            } catch (SQLException e) {
                log.error("Ошибка при создании сервера", e);
                return null;
            }
        });
    }
    

}
