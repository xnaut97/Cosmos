package com.github.xnaut97.cosmos.database.credentials;

import lombok.Getter;

@Getter
public class MongoCredentials {

    private final String connectionString;

    private final String host;
    private final int port;

    private final String database;
    private final String authDatabase;

    private final String username;
    private final String password;

    private final int minPoolSize;
    private final int maxPoolSize;

    private final long connectionTimeout;
    private final long readTimeout;
    private final long serverSelectionTimeout;

    public MongoCredentials(
            String host,
            int port,
            String database,
            String username,
            String password,
            int minPoolSize,
            int maxPoolSize,
            long connectionTimeout,
            long readTimeout,
            long serverSelectionTimeout
    ) {
        this(null, host, port, database, database, username, password, minPoolSize, maxPoolSize,
                connectionTimeout, readTimeout, serverSelectionTimeout);
    }

    public MongoCredentials(
            String host,
            int port,
            String database,
            String authDatabase,
            String username,
            String password,
            int minPoolSize,
            int maxPoolSize,
            long connectionTimeout,
            long readTimeout,
            long serverSelectionTimeout
    ) {
        this(null, host, port, database, authDatabase, username, password, minPoolSize, maxPoolSize,
                connectionTimeout, readTimeout, serverSelectionTimeout);
    }

    public MongoCredentials(
            String connectionString,
            String database,
            int minPoolSize,
            int maxPoolSize,
            long connectionTimeout,
            long readTimeout,
            long serverSelectionTimeout
    ) {
        this(connectionString, null, 27017, database, database, null, null, minPoolSize, maxPoolSize,
                connectionTimeout, readTimeout, serverSelectionTimeout);
    }

    private MongoCredentials(
            String connectionString,
            String host,
            int port,
            String database,
            String authDatabase,
            String username,
            String password,
            int minPoolSize,
            int maxPoolSize,
            long connectionTimeout,
            long readTimeout,
            long serverSelectionTimeout
    ) {
        this.connectionString = connectionString;
        this.host = host;
        this.port = port;
        this.database = database;
        this.authDatabase = authDatabase == null || authDatabase.isEmpty() ? database : authDatabase;
        this.username = username;
        this.password = password;
        this.minPoolSize = Math.max(0, minPoolSize);
        this.maxPoolSize = Math.max(1, maxPoolSize);
        this.connectionTimeout = connectionTimeout;
        this.readTimeout = readTimeout;
        this.serverSelectionTimeout = serverSelectionTimeout;
    }

    public boolean usesConnectionString() {
        return connectionString != null && !connectionString.isEmpty();
    }

    public boolean hasCredentials() {
        return username != null && !username.isEmpty() && password != null;
    }
}
