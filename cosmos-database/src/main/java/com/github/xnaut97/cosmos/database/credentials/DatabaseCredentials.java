package com.github.xnaut97.cosmos.database.credentials;

import lombok.Getter;

@Getter
public class DatabaseCredentials {

    private final String host;
    private final int port;

    private final String database;

    private final String username;
    private final String password;

    private final int poolSize;

    private final long connectionTimeout;
    private final long idleTimeout;
    private final long maxLifetime;

    public DatabaseCredentials(
            String host,
            int port,
            String database,
            String username,
            String password,
            int poolSize,
            long connectionTimeout,
            long idleTimeout,
            long maxLifetime
    ) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        this.poolSize = poolSize;
        this.connectionTimeout = connectionTimeout;
        this.idleTimeout = idleTimeout;
        this.maxLifetime = maxLifetime;
    }
}