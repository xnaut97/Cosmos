package com.github.xnaut97.cosmos.database;

public interface DatabaseConnection {

    boolean isConnected();

    void connect();

    void disconnect();

}
