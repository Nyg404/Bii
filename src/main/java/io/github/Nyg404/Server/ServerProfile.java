package io.github.Nyg404.Server;

import io.github.Nyg404.DataBase.DBConnection;
import io.github.Nyg404.KeyBoard.Cringe.PermissionType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Getter
@Slf4j
public class ServerProfile {
    private final Long serverId;
    private final String prefix;
    private final Map<PermissionType, Integer> permLevels;

    private static final String selectServer = "SELECT * FROM servers WHERE serverid = ?";
    private static final String selectPrefix = "SELECT prefix FROM servers WHERE serverid = ?";
    private static final String updatePrefix = "UPDATE servers SET prefix = ? WHERE serverid = ?";
    private static final String insertServer = "INSERT INTO servers (serverid, prefix, slap_level, ban_level, kick_level) VALUES (?, ?, ?, ?, ?)";
    private static final ConcurrentHashMap<Long, ServerProfile> cacheServer = new ConcurrentHashMap<>();

    public ServerProfile(Long serverId, String prefix, Map<PermissionType, Integer> permLevels) {
        this.serverId = serverId;
        this.prefix = prefix;
        this.permLevels = permLevels;
    }

    public ServerProfile(Long serverId, String prefix) {
        this.serverId = serverId;
        this.prefix = prefix;
        this.permLevels = new HashMap<>();
        for (PermissionType type : PermissionType.values()) {
            permLevels.put(type, 0);
        }
    }

    public int getLevel(PermissionType type) {
        return permLevels.getOrDefault(type, 0);
    }

    public static CompletableFuture<ServerProfile> of(long serverId) {
        List<PermissionType> permTypes = Arrays.asList(PermissionType.values());
        List<String> columnNames = permTypes.stream().map(PermissionType::getColumnName).collect(Collectors.toList());
        String selectClause = "SELECT prefix, " + String.join(", ", columnNames) + " FROM servers WHERE serverid = ?";

        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement prst = connection.prepareStatement(selectClause)) {
                prst.setLong(1, serverId);
                try (ResultSet rs = prst.executeQuery()) {
                    if (rs.next()) {
                        String prefix = rs.getString(1);
                        Map<PermissionType, Integer> permLevels = new HashMap<>();
                        for (int i = 0; i < permTypes.size(); i++) {
                            permLevels.put(permTypes.get(i), rs.getInt(i + 2));
                        }
                        ServerProfile profile = new ServerProfile(serverId, prefix, permLevels);
                        cacheServer.put(serverId, profile);
                        return profile;
                    }
                }
            } catch (SQLException e) {
                log.error("Ошибка при получении сервера: ", e);
            }
            return null;
        });
    }

    public static CompletableFuture<ServerProfile> createIfNotExists(long serverId) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(insertServer)) {
                ps.setLong(1, serverId);
                ps.setString(2, "/");
                ps.setInt(3, 0); // slap_level
                ps.setInt(4, 0); // ban_level
                ps.setInt(5, 0); // kick_level
                ps.executeUpdate();
    
                ServerProfile profile = new ServerProfile(serverId, "/");
                cacheServer.put(serverId, profile);
                return profile;
            } catch (SQLException e) {
                log.error("Ошибка при создании сервера", e);
                // Дополнительная проверка для конкретной ошибки отсутствия столбца
                if (e.getMessage().contains("ban_level")) {
                    log.error("Требуется обновление структуры БД: добавьте столбцы slap_level, ban_level, kick_level в таблицу servers");
                }
                throw new RuntimeException(e);
            }
        });
    }

    public static CompletableFuture<String> getPrefix(long serverId) {
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
                    cacheServer.put(serverId, new ServerProfile(serverId, prefix));
                    log.info("Префикс загружен из БД: {} для сервера {}", prefix, serverId);
                    return prefix;
                }
            } catch (SQLException e) {
                log.error("Ошибка при загрузке префикса", e);
            }
            return "/";
        });
    }

    public static CompletableFuture<Void> updatePrefix(long serverId, String prefix) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(updatePrefix)) {
                ps.setString(1, prefix);
                ps.setLong(2, serverId);
                if (ps.executeUpdate() > 0) {
                    ServerProfile cached = cacheServer.get(serverId);
                    if (cached != null) {
                        ServerProfile updated = new ServerProfile(serverId, prefix, new HashMap<>(cached.getPermLevels()));
                        cacheServer.put(serverId, updated);
                        log.info("Кэш обновлён. Новый префикс: {} для сервера {}", prefix, serverId);
                    }
                }
            } catch (SQLException e) {
                log.error("Ошибка при обновлении префикса", e);
                throw new RuntimeException(e);
            }
        });
    }

    public static CompletableFuture<Integer> selectPerm(Long serverId, PermissionType permissionType) {
        ServerProfile cached = cacheServer.get(serverId);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached.getLevel(permissionType));
        }

        return of(serverId).thenCompose(profile -> {
            if (profile != null) {
                return CompletableFuture.completedFuture(profile.getLevel(permissionType));
            } else {
                log.warn("Сервер с id {} не найден, создаем новый", serverId);
                return createIfNotExists(serverId).thenApply(newProfile -> newProfile.getLevel(permissionType));
            }
        });
    }

    public static CompletableFuture<Void> updatePerm(Long serverId, PermissionType permissionType, int level) {
        String updateQuery = "UPDATE servers SET " + permissionType.getColumnName() + " = ? WHERE serverid = ?";
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(updateQuery)) {
                ps.setInt(1, level);
                ps.setLong(2, serverId);
                int rowsAffected = ps.executeUpdate();
                if (rowsAffected > 0) {
                    ServerProfile cached = cacheServer.get(serverId);
                    if (cached != null) {
                        Map<PermissionType, Integer> updatedLevels = new HashMap<>(cached.getPermLevels());
                        updatedLevels.put(permissionType, level);
                        cacheServer.put(serverId, new ServerProfile(serverId, cached.getPrefix(), updatedLevels));
                        log.info("Уровень прав {} для сервера {} обновлен до {}", permissionType.getPermissionName(), serverId, level);
                    }
                }
            } catch (SQLException e) {
                log.error("Ошибка при обновлении уровня прав для сервера {}: ", serverId, e);
                throw new RuntimeException(e);
            }
        });
    }
}