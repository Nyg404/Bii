package io.github.Nyg404.DataBase;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
public class DBTables {
    private static final String createTablesUser = "CREATE TABLE IF NOT EXISTS users ("
            + "userid BIGINT,"
            + "serverid BIGINT,"
            + "perms_level BIGINT,"
            + "PRIMARY KEY (userid, serverid),"
            + "FOREIGN KEY (serverid) REFERENCES servers(serverid));";
    private static final String createTablesServer = "CREATE TABLE IF NOT EXISTS servers("
            + "serverid BIGINT PRIMARY KEY,"
            + "prefix VARCHAR(1) NOT NULL);";
    public static void createTables(){
        try (Connection connection = DBConnection.getConnection()){
            try (Statement prst = connection.createStatement()){
                prst.executeUpdate(createTablesServer);
                prst.executeUpdate(createTablesUser);
                log.info("Таблицы созданны");
            }
        } catch (SQLException e){
            log.error("Ошибка создание таблиц", e);
        }
    }
}
