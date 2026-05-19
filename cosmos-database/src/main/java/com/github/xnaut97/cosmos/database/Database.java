package com.github.xnaut97.cosmos.database;

import com.github.xnaut97.cosmos.database.credentials.DatabaseCredentials;
import com.github.xnaut97.cosmos.database.dialect.DatabaseDialect;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import org.bukkit.plugin.Plugin;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class Database implements DatabaseConnection {

    protected final Plugin plugin;
    protected final DatabaseCredentials credentials;
    protected final DatabaseDialect dialect;

    protected HikariDataSource dataSource;

    protected Database(
            Plugin plugin,
            DatabaseCredentials credentials,
            DatabaseDialect dialect
    ) {
        this.plugin = plugin;
        this.credentials = credentials;
        this.dialect = dialect;
    }

    public void connect() {

        try {

            HikariConfig config = new HikariConfig();

            config.setJdbcUrl(dialect.buildJdbcUrl(credentials));
            config.setUsername(credentials.getUsername());
            config.setPassword(credentials.getPassword());

            config.setMaximumPoolSize(credentials.getPoolSize());
            config.setConnectionTimeout(credentials.getConnectionTimeout());
            config.setIdleTimeout(credentials.getIdleTimeout());
            config.setMaxLifetime(credentials.getMaxLifetime());

            this.dataSource = new HikariDataSource(config);

            plugin.getLogger().info("Connected to database.");

        } catch (Exception ex) {
            plugin.getLogger().severe("Failed to connect database.");
            throw new RuntimeException(ex);
        }
    }

    public void disconnect() {

        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public boolean tableExists(String table) {

        try (Connection connection = getConnection()) {

            ResultSet rs = connection.getMetaData().getTables(
                    null,
                    null,
                    table,
                    new String[]{"TABLE"}
            );

            return rs.next();

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean columnExists(String table, String column) {

        try (Connection connection = getConnection()) {

            ResultSet rs = connection.getMetaData().getColumns(
                    null,
                    null,
                    table,
                    column
            );

            return rs.next();

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public void createTable(String table, DatabaseColumn... columns) {

        StringBuilder builder = new StringBuilder();

        builder.append("CREATE TABLE IF NOT EXISTS `")
                .append(table)
                .append("` (");

        for (DatabaseColumn column : columns) {

            builder.append("`")
                    .append(column.getName())
                    .append("` ")
                    .append(dialect.getType(column.getType()));

            if (!column.isNullable()) {
                builder.append(" NOT NULL");
            }

            if (column.isPrimaryKey()) {
                builder.append(" PRIMARY KEY");
            }

            builder.append(", ");
        }

        builder.setLength(builder.length() - 2);

        builder.append(")");

        execute(builder.toString());
    }

    public void execute(String sql) {

        try (
                Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void update(
            String sql,
            StatementConsumer consumer
    ) {

        try (
                Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            consumer.accept(statement);

            statement.executeUpdate();

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public <T> List<T> query(
            String sql,
            StatementConsumer consumer,
            ResultMapper<T> mapper
    ) {

        List<T> results = new ArrayList<T>();

        try (
                Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            consumer.accept(statement);

            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                results.add(mapper.map(rs));
            }

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        return results;
    }
}
