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
    private static final String selectServer = "SELECT * FROM servers WHERE serverid + ?";
    private static final ConcurrentHashMap<Long, ServerProfile> cacheServer = new ConcurrentHashMap<>();

    public ServerProfile(Long serverId, String prefix) {
        this.serverId = serverId;
        this.prefix = prefix;
    }

    public static CompletableFuture<ServerProfile> of(long serverId){
        ServerProfile serverProfil = cacheServer.get(serverId);
        if(serverProfil != null){
            return CompletableFuture.completedFuture(serverProfil);
        }

        return CompletableFuture.supplyAsync(() -> {
            ServerProfile serverProfil1 = null;
            try(Connection connection = DBConnection.getConnection()) {
                try (PreparedStatement prst = connection.prepareStatement(selectServer)){
                    prst.setLong(1, serverId);
                    try (ResultSet rs = prst.executeQuery()){
                        if(rs.next()){
                            serverProfil1 = new ServerProfile(serverId, "/");
                        }
                    }
                }
            } catch (SQLException e){
                log.error("Ошибка при получении сервера: ", e);
            }
            return serverProfil1;
        });
    }

}
