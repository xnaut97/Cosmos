package com.github.xnaut97.cosmos.database.dialect;


import com.github.xnaut97.cosmos.database.ColumnType;
import com.github.xnaut97.cosmos.database.credentials.DatabaseCredentials;

public class MySQLDialect implements DatabaseDialect {

    @Override
    public String buildJdbcUrl(DatabaseCredentials credentials) {

        return "jdbc:mysql://" +
                credentials.getHost() +
                ":" +
                credentials.getPort() +
                "/" +
                credentials.getDatabase() +
                "?useSSL=false&serverTimezone=UTC";
    }

    @Override
    public String getType(ColumnType type) {
        return type.getSql();
    }
}