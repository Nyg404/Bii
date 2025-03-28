package io.github.Nyg404.DataBase;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;
@Slf4j
public class DBConnection {
    private static final HikariDataSource hikariDataSource;
    private static final Dotenv dotenv = Dotenv.load();
    private static final String jdbc = dotenv.get("URL");
    private static final String name = dotenv.get("USER");
    private static final String password = dotenv.get("PASSWORD");

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbc);
        config.setUsername(name);
        config.setPassword(password);
        config.setMaximumPoolSize(8);
        config.setIdleTimeout(9000);
        config.setConnectionTimeout(30000);
        hikariDataSource = new HikariDataSource(config);
    }
    public static Connection getConnection() throws SQLException{
        try {
            return hikariDataSource.getConnection();
        } catch (SQLException e) {
            log.error("Ошибка подключенния: ", e);
            throw e;
        }

    }
    public static void close(){
        if(hikariDataSource != null){
            hikariDataSource.close();
        }
    }
}
